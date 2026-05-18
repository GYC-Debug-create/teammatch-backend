package com.teammatch.m6.controller;

import com.teammatch.common.ReasonCode;
import com.teammatch.common.Result;
import com.teammatch.m6.dto.PenaltyCreateDTO;
import com.teammatch.m6.dto.PenaltyRevokeDTO;
import com.teammatch.m6.entity.Penalty;
import com.teammatch.m6.service.PenaltyService;
import com.teammatch.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 处罚管理 Controller（管理端）
 *
 * 根据详细设计文档 7.2 节定义
 * 管理端接口前缀：/api/admin/penalties
 * 需要管理员权限
 */
@RestController
@RequestMapping("/admin/penalties")
@RequiredArgsConstructor
public class PenaltyAdminController {

    private final PenaltyService penaltyService;
    private final AuthService authService;

    /**
     * 创建处罚
     * POST /api/admin/penalties
     * 需要管理员权限
     *
     * @param dto   处罚请求
     * @param token 认证token
     * @return 创建的处罚记录
     */
    @PostMapping
    public Result<Penalty> createPenalty(@Valid @RequestBody PenaltyCreateDTO dto,
                                         @RequestHeader("Authorization") String token) {
        // 校验管理员权限
        if (!authService.isAdmin(token)) {
            return Result.fail(ReasonCode.ADMIN_REQUIRED);
        }

        // 获取管理员ID
        Long adminId = authService.getUserIdFromToken(token);
        if (adminId == null) {
            return Result.fail(ReasonCode.UNAUTHORIZED);
        }

        try {
            Penalty penalty = penaltyService.createPenalty(adminId, dto);
            return Result.success(penalty);
        } catch (IllegalArgumentException e) {
            return Result.fail(ReasonCode.PARAM_ERROR);
        }
    }

    /**
     * 获取处罚列表
     * GET /api/admin/penalties
     * 需要管理员权限
     *
     * @param status 状态筛选（可选：active/revoked）
     * @param type   类型筛选（可选：credit_deduct/function_limit）
     * @param token  认证token
     * @return 处罚列表
     */
    @GetMapping
    public Result<List<Penalty>> getPenaltyList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestHeader("Authorization") String token) {
        // 校验管理员权限
        if (!authService.isAdmin(token)) {
            return Result.fail(ReasonCode.ADMIN_REQUIRED);
        }

        List<Penalty> penalties = penaltyService.getPenaltyList(status, type);
        return Result.success(penalties);
    }

    /**
     * 获取处罚详情
     * GET /api/admin/penalties/{id}
     * 需要管理员权限
     *
     * @param id    处罚ID
     * @param token 认证token
     * @return 处罚详情
     */
    @GetMapping("/{id}")
    public Result<Penalty> getPenalty(@PathVariable Long id,
                                      @RequestHeader("Authorization") String token) {
        // 校验管理员权限
        if (!authService.isAdmin(token)) {
            return Result.fail(ReasonCode.ADMIN_REQUIRED);
        }

        Penalty penalty = penaltyService.getPenaltyById(id);
        if (penalty == null) {
            return Result.fail(ReasonCode.NOT_FOUND);
        }
        return Result.success(penalty);
    }

    /**
     * 撤销处罚
     * PUT /api/admin/penalties/{id}/revoke
     * 需要管理员权限，通常在申诉通过时使用
     *
     * @param id    处罚ID
     * @param dto   撤销请求
     * @param token 认证token
     * @return 撤销后的处罚记录
     */
    @PutMapping("/{id}/revoke")
    public Result<Penalty> revokePenalty(@PathVariable Long id,
                                         @Valid @RequestBody PenaltyRevokeDTO dto,
                                         @RequestHeader("Authorization") String token) {
        // 校验管理员权限
        if (!authService.isAdmin(token)) {
            return Result.fail(ReasonCode.ADMIN_REQUIRED);
        }

        // 获取管理员ID
        Long adminId = authService.getUserIdFromToken(token);
        if (adminId == null) {
            return Result.fail(ReasonCode.UNAUTHORIZED);
        }

        try {
            Penalty penalty = penaltyService.revokePenalty(id, adminId, dto);
            return Result.success(penalty);
        } catch (IllegalArgumentException e) {
            return Result.fail(ReasonCode.NOT_FOUND);
        } catch (IllegalStateException e) {
            return Result.fail(ReasonCode.STATUS_CONFLICT);
        }
    }
}
