package com.teammatch.m6.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teammatch.m6.dto.ReportCreateDTO;
import com.teammatch.m6.dto.ReportHandleDTO;
import com.teammatch.m6.entity.Report;
import com.teammatch.m6.mapper.ReportMapper;
import com.teammatch.m6.service.ReportService;
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
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

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

        // 更新处理信息
        report.setStatus(dto.getStatus());
        report.setHandlerId(handlerId);
        report.setHandleResult(dto.getHandleResult());
        report.setHandledAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        baseMapper.updateById(report);
        return report;
    }

    @Override
    public Report getReportById(Long reportId) {
        return baseMapper.selectById(reportId);
    }
}
