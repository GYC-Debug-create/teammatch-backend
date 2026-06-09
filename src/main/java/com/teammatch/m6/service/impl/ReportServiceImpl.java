package com.teammatch.m6.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teammatch.m6.constants.PenaltyType;
import com.teammatch.m6.dto.PenaltyCreateDTO;
import com.teammatch.m6.dto.ReportCreateDTO;
import com.teammatch.m6.dto.ReportHandleDTO;
import com.teammatch.m6.entity.Report;
import com.teammatch.m6.mapper.ReportMapper;
import com.teammatch.exception.BusinessException;
import com.teammatch.m6.service.PenaltyService;
import com.teammatch.m6.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 举报 Service 实现类
 *
 * 根据详细设计文档 7.2 节、7.4 节定义实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    private final PenaltyService penaltyService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Report createReport(Long reporterId, ReportCreateDTO dto) {
        // 创建举报实体
        Report report = new Report();
        report.setReporterId(reporterId);
        report.setTargetType(dto.getTargetType());
        report.setTargetId(dto.getTargetId());
        report.setReason(dto.getReason());
        report.setStatus("pending");
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        // 保存到数据库
        baseMapper.insert(report);
        return report;
    }

    @Override
    public List<Report> getMyReports(Long reporterId) {
        return baseMapper.selectByReporterId(reporterId);
    }

    @Override
    public List<Report> getReportList(String status) {
        if (status == null || status.isEmpty()) {
            // 查询全部，按状态和时间排序
            return baseMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Report>()
                            .orderByDesc(Report::getCreatedAt)
            );
        }
        return baseMapper.selectByStatus(status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Report handleReport(Long reportId, Long handlerId, ReportHandleDTO dto) {
        // 检查举报是否存在
        Report report = baseMapper.selectById(reportId);
        if (report == null) {
            throw new IllegalArgumentException("举报不存在: " + reportId);
        }

        // 检查状态是否为待处理
        if (!"pending".equals(report.getStatus())) {
            throw new IllegalStateException("举报已处理，无法重复处理");
        }

        if ("resolved".equals(dto.getStatus()) && Boolean.TRUE.equals(dto.getCreatePenalty())) {
            validatePenaltyLinkage(dto, report);
        }

        report.setStatus(dto.getStatus());
        report.setHandlerId(handlerId);
        report.setHandleResult(dto.getHandleResult());
        report.setHandledAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        baseMapper.updateById(report);

        if ("resolved".equals(dto.getStatus()) && Boolean.TRUE.equals(dto.getCreatePenalty())) {
            createPenaltyFromReport(report, handlerId, dto);
        }

        return report;
    }

    private void validatePenaltyLinkage(ReportHandleDTO dto, Report report) {
        if (!"user".equals(report.getTargetType())) {
            throw new IllegalArgumentException("仅 user 类型举报可联动创建处罚");
        }
        if (dto.getPenaltyType() == null || dto.getPenaltyType().isBlank()) {
            throw new IllegalArgumentException("createPenalty=true 时必须指定 penaltyType");
        }
        if (!PenaltyType.CREDIT_DEDUCT.equals(dto.getPenaltyType())
                && !PenaltyType.FUNCTION_LIMIT.equals(dto.getPenaltyType())) {
            throw new IllegalArgumentException("penaltyType 必须是 credit_deduct 或 function_limit");
        }
        if (PenaltyType.CREDIT_DEDUCT.equals(dto.getPenaltyType())
                && (dto.getCreditDeductValue() == null || dto.getCreditDeductValue() <= 0)) {
            throw new IllegalArgumentException("credit_deduct 处罚必须指定大于 0 的 creditDeductValue");
        }
    }

    /**
     * 根据举报创建处罚（调用前须已通过 {@link #validatePenaltyLinkage}）
     */
    private void createPenaltyFromReport(Report report, Long handlerId, ReportHandleDTO dto) {
        PenaltyCreateDTO penaltyDTO = new PenaltyCreateDTO();
        penaltyDTO.setUserId(report.getTargetId());
        penaltyDTO.setType(dto.getPenaltyType());
        penaltyDTO.setCreditDeductValue(dto.getCreditDeductValue());
        penaltyDTO.setReason(dto.getPenaltyReason() != null ? dto.getPenaltyReason() : report.getReason());
        penaltyDTO.setRelatedReportId(report.getId());

        try {
            penaltyService.createPenalty(handlerId, penaltyDTO);
            log.info("根据举报创建处罚成功: reportId={}, userId={}", report.getId(), report.getTargetId());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("根据举报创建处罚失败: reportId={}", report.getId(), e);
            throw new RuntimeException("创建处罚失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Report getReportById(Long reportId) {
        return baseMapper.selectById(reportId);
    }
}
