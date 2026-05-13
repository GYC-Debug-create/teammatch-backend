package com.teammatch.m6.controller;

import com.teammatch.common.ReasonCode;
import com.teammatch.common.Result;
import com.teammatch.m6.dto.ReportCreateDTO;
import com.teammatch.m6.entity.Report;
import com.teammatch.m6.service.ReportService;
import com.teammatch.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 举报 Controller（用户端）
 *
 * 根据详细设计文档 7.2 节定义
 * 用户端接口前缀：/api/reports
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final AuthService authService;

    /**
     * 提交举报
     * POST /api/reports
     *
     * @param dto   举报请求
     * @param token 认证token
     * @return 创建的举报记录
     */
    @PostMapping
    public Result<Report> createReport(@Valid @RequestBody ReportCreateDTO dto,
                                       @RequestHeader("Authorization") String token) {
        // 从token获取当前用户ID
        Long reporterId = getUserIdFromToken(token);
        if (reporterId == null) {
            return Result.fail(ReasonCode.UNKNOWN_ERROR);
        }

        try {
            Report report = reportService.createReport(reporterId, dto);
            return Result.success(report);
        } catch (IllegalArgumentException e) {
            return Result.fail(ReasonCode.PARAM_ERROR);
        }
    }

    /**
     * 获取我的举报列表
     * GET /api/reports/my
     *
     * @param token 认证token
     * @return 举报列表
     */
    @GetMapping("/my")
    public Result<List<Report>> getMyReports(@RequestHeader("Authorization") String token) {
        // 从token获取当前用户ID
        Long reporterId = getUserIdFromToken(token);
        if (reporterId == null) {
            return Result.fail(ReasonCode.UNKNOWN_ERROR);
        }

        List<Report> reports = reportService.getMyReports(reporterId);
        return Result.success(reports);
    }

    /**
     * 从 Token 中解析用户 ID
     *
     * TODO【M6-M3依赖】: 当前硬编码返回1L，需等待M3提供公共方法
     * 问题：M3的AuthServiceImpl中有私有方法getUserIdFromToken，但未暴露到接口
     * 建议：请M3在AuthService接口中添加：Long getUserIdFromToken(String token)
     *
     * @param token Authorization header
     * @return 用户ID，解析失败返回null
     */
    private Long getUserIdFromToken(String token) {
        try {
            // 移除 "Bearer " 前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            // FIXME【P0】: 硬编码返回1L，所有用户共用同一ID，需M3提供真实解析方法
            return 1L;
        } catch (Exception e) {
            return null;
        }
    }
}
