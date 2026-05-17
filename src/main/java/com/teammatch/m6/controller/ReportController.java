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
        Long reporterId = authService.getUserIdFromToken(token);
        if (reporterId == null) {
            return Result.fail(ReasonCode.UNAUTHORIZED);
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
        Long reporterId = authService.getUserIdFromToken(token);
        if (reporterId == null) {
            return Result.fail(ReasonCode.UNAUTHORIZED);
        }

        List<Report> reports = reportService.getMyReports(reporterId);
        return Result.success(reports);
    }
}
