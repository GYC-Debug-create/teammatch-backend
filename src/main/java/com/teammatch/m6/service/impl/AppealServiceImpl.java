package com.teammatch.m6.service.impl;

import com.teammatch.entity.Appeal;
import com.teammatch.entity.Evaluation;
import com.teammatch.mapper.AppealMapper;
import com.teammatch.mapper.EvaluationMapper;
import com.teammatch.m6.dto.AppealCreateDTO;
import com.teammatch.m6.dto.AppealHandleDTO;
import com.teammatch.m6.dto.PenaltyRevokeDTO;
import com.teammatch.m6.entity.Penalty;
import com.teammatch.m6.mapper.PenaltyMapper;
import com.teammatch.m6.service.AppealService;
import com.teammatch.m6.service.PenaltyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 申诉 Service 实现类
 *
 * 根据详细设计文档 7.5 节定义实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppealServiceImpl implements AppealService {

    private final AppealMapper appealMapper;
    private final EvaluationMapper evaluationMapper;
    private final PenaltyMapper penaltyMapper;
    private final PenaltyService penaltyService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Appeal createAppeal(Long userId, AppealCreateDTO dto) {
        // 校验目标类型
        if (!"evaluation".equals(dto.getTargetType()) && !"penalty".equals(dto.getTargetType())) {
            throw new IllegalArgumentException("无效的申诉目标类型，必须是 evaluation 或 penalty");
        }

        // 校验目标是否存在且属于当前用户
        validateTargetExistsAndBelongsToUser(dto.getTargetType(), dto.getTargetId(), userId);

        // 检查是否已存在待处理的申诉
        Appeal existingPending = appealMapper.selectPendingByTarget(dto.getTargetType(), dto.getTargetId());
        if (existingPending != null) {
            throw new IllegalStateException("该目标已存在待处理的申诉，请勿重复提交");
        }

        // 创建申诉实体
        Appeal appeal = new Appeal();
        appeal.setUserId(userId);
        appeal.setTargetType(dto.getTargetType());
        appeal.setTargetId(dto.getTargetId());
        appeal.setReason(dto.getReason());
        appeal.setStatus("pending");
        appeal.setCreatedAt(LocalDateTime.now());
        appeal.setUpdatedAt(LocalDateTime.now());

        // 保存到数据库
        appealMapper.insert(appeal);
        log.info("申诉创建成功: appealId={}, userId={}, targetType={}, targetId={}",
                appeal.getId(), userId, dto.getTargetType(), dto.getTargetId());

        return appeal;
    }

    /**
     * 校验目标是否存在且属于当前用户
     */
    private void validateTargetExistsAndBelongsToUser(String targetType, Long targetId, Long userId) {
        if ("evaluation".equals(targetType)) {
            Evaluation evaluation = evaluationMapper.selectById(targetId);
            if (evaluation == null) {
                throw new IllegalArgumentException("评价记录不存在: " + targetId);
            }
            // 申诉用户必须是评价的被评价人
            if (!evaluation.getTargetId().equals(userId)) {
                throw new IllegalArgumentException("只能对自己的评价提交申诉");
            }
        } else if ("penalty".equals(targetType)) {
            com.teammatch.m6.entity.Penalty penalty = penaltyMapper.selectById(targetId);
            if (penalty == null) {
                throw new IllegalArgumentException("处罚记录不存在: " + targetId);
            }
            // 申诉用户必须是被处罚人
            if (!penalty.getUserId().equals(userId)) {
                throw new IllegalArgumentException("只能对自己的处罚提交申诉");
            }
            // 已撤销的处罚不能申诉
            if ("revoked".equals(penalty.getStatus())) {
                throw new IllegalStateException("该处罚已撤销，无需申诉");
            }
        }
    }

    @Override
    public List<Appeal> getMyAppeals(Long userId) {
        return appealMapper.selectByUserId(userId);
    }

    @Override
    public List<Appeal> getAppealList(String status, String targetType) {
        // 根据条件查询
        if (status != null && !status.isEmpty() && targetType != null && !targetType.isEmpty()) {
            // 同时按状态和目标类型筛选
            return appealMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Appeal>()
                            .eq(Appeal::getStatus, status)
                            .eq(Appeal::getTargetType, targetType)
                            .orderByDesc(Appeal::getCreatedAt)
            );
        } else if (status != null && !status.isEmpty()) {
            return appealMapper.selectByStatus(status);
        } else if (targetType != null && !targetType.isEmpty()) {
            return appealMapper.selectByTargetType(targetType);
        }

        // 查询全部
        return appealMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Appeal>()
                        .orderByDesc(Appeal::getCreatedAt)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Appeal handleAppeal(Long appealId, Long handlerId, AppealHandleDTO dto) {
        // 检查申诉是否存在
        Appeal appeal = appealMapper.selectById(appealId);
        if (appeal == null) {
            throw new IllegalArgumentException("申诉记录不存在: " + appealId);
        }

        // 检查状态是否为待处理
        if (!"pending".equals(appeal.getStatus())) {
            throw new IllegalStateException("申诉已处理，无法重复处理");
        }

        // 校验处理结果
        if (!"approved".equals(dto.getStatus()) && !"rejected".equals(dto.getStatus())) {
            throw new IllegalArgumentException("无效的处理结果，必须是 approved 或 rejected");
        }

        // 更新申诉状态
        appeal.setStatus(dto.getStatus());
        appeal.setHandlerId(handlerId);
        appeal.setHandleResult(dto.getHandleResult());
        appeal.setHandledAt(LocalDateTime.now());
        appeal.setUpdatedAt(LocalDateTime.now());

        appealMapper.updateById(appeal);

        // 如果批准申诉，执行相应的恢复操作
        if ("approved".equals(dto.getStatus())) {
            executeApprovedSideEffects(appeal, dto);
        }

        log.info("申诉处理完成: appealId={}, status={}, handlerId={}",
                appealId, dto.getStatus(), handlerId);

        return appeal;
    }

    /**
     * 执行申诉批准的副作用
     * - evaluation 类型：由 M5-7 的 AppealRestoreService 处理信誉恢复
     * - penalty 类型：调用 PenaltyService.revokePenalty 撤销处罚
     */
    private void executeApprovedSideEffects(Appeal appeal, AppealHandleDTO dto) {
        if ("penalty".equals(appeal.getTargetType())) {
            // 撤销处罚
            PenaltyRevokeDTO revokeDTO = new PenaltyRevokeDTO();
            revokeDTO.setReason("申诉通过: " + (dto.getHandleResult() != null ? dto.getHandleResult() : "经审核，撤销处罚"));

            try {
                penaltyService.revokePenalty(appeal.getTargetId(), appeal.getHandlerId(), revokeDTO);
                log.info("处罚撤销成功: appealId={}, penaltyId={}", appeal.getId(), appeal.getTargetId());
            } catch (Exception e) {
                log.error("处罚撤销失败: appealId={}, penaltyId={}", appeal.getId(), appeal.getTargetId(), e);
                throw new RuntimeException("处罚撤销失败: " + e.getMessage(), e);
            }
        }
        // evaluation 类型的信誉恢复由 M5-7 的 AppealRestoreService 处理
    }

    @Override
    public Appeal getAppealById(Long appealId) {
        return appealMapper.selectById(appealId);
    }

    @Override
    public Appeal getPendingAppeal(String targetType, Long targetId) {
        return appealMapper.selectPendingByTarget(targetType, targetId);
    }
}
