package com.teammatch.m6.controller;

import com.teammatch.common.ReasonCode;
import com.teammatch.common.Result;
import com.teammatch.m6.dto.ReportHandleDTO;
import com.teammatch.m6.entity.Report;
import com.teammatch.m6.service.ReportService;
import com.teammatch.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 举报管理 Controller（管理端）
 *
 * 根据详细设计文档 7.2 节定义
 * 管理端接口前缀：/api/admin/reports
 * 需要管理员权限
 */
@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class ReportAdminController {

    private final ReportService reportService;
    private final AuthService authService;

    /**
     * 获取举报列表
     * GET /api/admin/reports
     * 需要管理员权限
     *
     * @param status 状态筛选（可选：pending/resolved/dismissed）
     * @param token  认证token
     * @return 举报列表
     */
    @GetMapping
    public Result<List<Report>> getReportList(
            @RequestParam(required = false) String status,
            @RequestHeader("Authorization") String token) {
        // 校验管理员权限
        if (!authService.isAdmin(token)) {
            return Result.fail(ReasonCode.ADMIN_REQUIRED);
        }

        List<Report> reports = reportService.getReportList(status);
        return Result.success(reports);
    }

    /**
     * 获取举报详情
     * GET /api/admin/reports/{id}
     * 需要管理员权限
     *
     * @param id    举报ID
     * @param token 认证token
     * @return 举报详情
     */
    @GetMapping("/{id}")
    public Result<Report> getReport(@PathVariable Long id,
                                     @RequestHeader("Authorization") String token) {
        // 校验管理员权限
        if (!authService.isAdmin(token)) {
            return Result.fail(ReasonCode.ADMIN_REQUIRED);
        }

        Report report = reportService.getReportById(id);
        if (report == null) {
            return Result.fail(ReasonCode.NOT_FOUND);
        }
        return Result.success(report);
    }

    /**
     * 处理举报
     * PUT /api/admin/reports/{id}/handle
     * 需要管理员权限
     *
     * @param id    举报ID
     * @param dto   处理请求
     * @param token 认证token
     * @return 处理后的举报记录
     */
    @PutMapping("/{id}/handle")
    public Result<Report> handleReport(@PathVariable Long id,
                                       @Valid @RequestBody ReportHandleDTO dto,
                                       @RequestHeader("Authorization") String token) {
        // 校验管理员权限
        if (!authService.isAdmin(token)) {
            return Result.fail(ReasonCode.ADMIN_REQUIRED);
        }

        // 获取处理人ID（管理员ID）
        Long handlerId = authService.getUserIdFromToken(token);
        if (handlerId == null) {
            return Result.fail(ReasonCode.UNAUTHORIZED);
        }

        try {
            Report report = reportService.handleReport(id, handlerId, dto);
            return Result.success(report);
        } catch (IllegalArgumentException e) {
            return Result.fail(ReasonCode.NOT_FOUND);
        } catch (IllegalStateException e) {
            return Result.fail(ReasonCode.STATUS_CONFLICT);
        }
    }
}