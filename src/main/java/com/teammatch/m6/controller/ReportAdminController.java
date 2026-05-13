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
        Long handlerId = getUserIdFromToken(token);
        if (handlerId == null) {
            return Result.fail(ReasonCode.UNKNOWN_ERROR);
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
            // FIXME【P0】: 硬编码返回1L，所有管理员共用同一ID，需M3提供真实解析方法
            return 1L;
        } catch (Exception e) {
            return null;
        }
    }
}
