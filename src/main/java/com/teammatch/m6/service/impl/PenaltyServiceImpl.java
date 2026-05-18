package com.teammatch.m6.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teammatch.m6.dto.PenaltyCreateDTO;
import com.teammatch.m6.dto.PenaltyRevokeDTO;
import com.teammatch.m6.entity.Penalty;
import com.teammatch.m6.mapper.PenaltyMapper;
import com.teammatch.m6.service.PenaltyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 处罚记录 Service 实现类
 *
 * 根据详细设计文档 7.2 节、7.4 节定义实现
 */
@Service
public class PenaltyServiceImpl extends ServiceImpl<PenaltyMapper, Penalty> implements PenaltyService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Penalty createPenalty(Long adminId, PenaltyCreateDTO dto) {
        // 校验credit_deduct类型必须有扣分值
        if ("credit_deduct".equals(dto.getType()) && dto.getCreditDeductValue() == null) {
            throw new IllegalArgumentException("credit_deduct类型的处罚必须指定扣分值");
        }

        // 创建处罚实体
        Penalty penalty = new Penalty();
        penalty.setUserId(dto.getUserId());
        penalty.setType(dto.getType());
        penalty.setCreditDeductValue(dto.getCreditDeductValue());
        penalty.setReason(dto.getReason());
        penalty.setAdminId(adminId);
        penalty.setRelatedReportId(dto.getRelatedReportId());
        penalty.setStatus("active");
        penalty.setCreatedAt(LocalDateTime.now());
        penalty.setUpdatedAt(LocalDateTime.now());

        // 保存到数据库
        baseMapper.insert(penalty);

        // TODO: 执行处罚副作用
        // - credit_deduct类型：向credit_change写扣分流水，更新user.credit_score
        // - function_limit类型：设置user.status='banned'

        return penalty;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Penalty revokePenalty(Long penaltyId, Long adminId, PenaltyRevokeDTO dto) {
        // 检查处罚是否存在
        Penalty penalty = baseMapper.selectById(penaltyId);
        if (penalty == null) {
            throw new IllegalArgumentException("处罚记录不存在: " + penaltyId);
        }

        // 检查状态是否为已撤销
        if ("revoked".equals(penalty.getStatus())) {
            throw new IllegalStateException("处罚已撤销，无法重复撤销");
        }

        // 更新处罚状态
        penalty.setStatus("revoked");
        penalty.setRevokedAt(LocalDateTime.now());
        penalty.setUpdatedAt(LocalDateTime.now());

        baseMapper.updateById(penalty);

        // TODO: 执行撤销副作用
        // - credit_deduct类型：恢复用户信誉分
        // - function_limit类型：恢复用户状态

        return penalty;
    }

    @Override
    public List<Penalty> getUserPenalties(Long userId) {
        return baseMapper.selectByUserId(userId);
    }

    @Override
    public List<Penalty> getUserActivePenalties(Long userId) {
        return baseMapper.selectActiveByUserId(userId);
    }

    @Override
    public List<Penalty> getPenaltyList(String status, String type) {
        // 根据条件查询
        if (status != null && !status.isEmpty() && type != null && !type.isEmpty()) {
            // 同时按状态和类型筛选
            return baseMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Penalty>()
                            .eq(Penalty::getStatus, status)
                            .eq(Penalty::getType, type)
                            .orderByDesc(Penalty::getCreatedAt)
            );
        } else if (status != null && !status.isEmpty()) {
            return baseMapper.selectByStatus(status);
        } else if (type != null && !type.isEmpty()) {
            return baseMapper.selectByType(type);
        }

        // 查询全部
        return baseMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Penalty>()
                        .orderByDesc(Penalty::getCreatedAt)
        );
    }

    @Override
    public Penalty getPenaltyById(Long penaltyId) {
        return baseMapper.selectById(penaltyId);
    }
}
