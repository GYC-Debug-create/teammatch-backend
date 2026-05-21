package com.teammatch.m6.controller;

import com.teammatch.common.ReasonCode;
import com.teammatch.common.Result;
import com.teammatch.m6.dto.PenaltyCreateDTO;
import com.teammatch.m6.dto.PenaltyRevokeDTO;
import com.teammatch.m6.entity.Penalty;
import com.teammatch.m6.service.PenaltyService;
import com.teammatch.util.AuthUtil;
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
    private final AuthUtil authUtil;

    /**
     * 创建处罚
     * POST /api/admin/penalties
     */
    @PostMapping
    public Result<Penalty> createPenalty(@Valid @RequestBody PenaltyCreateDTO dto,
                                         @RequestHeader("Authorization") String token) {
        authUtil.requireAdmin(token);
        Long adminId = authUtil.requireUserId(token);

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
     */
    @GetMapping
    public Result<List<Penalty>> getPenaltyList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestHeader("Authorization") String token) {
        authUtil.requireAdmin(token);
        List<Penalty> penalties = penaltyService.getPenaltyList(status, type);
        return Result.success(penalties);
    }

    /**
     * 获取处罚详情
     * GET /api/admin/penalties/{id}
     */
    @GetMapping("/{id}")
    public Result<Penalty> getPenalty(@PathVariable Long id,
                                      @RequestHeader("Authorization") String token) {
        authUtil.requireAdmin(token);

        Penalty penalty = penaltyService.getPenaltyById(id);
        if (penalty == null) {
            return Result.fail(ReasonCode.NOT_FOUND);
        }
        return Result.success(penalty);
    }

    /**
     * 撤销处罚
     * PUT /api/admin/penalties/{id}/revoke
     */
    @PutMapping("/{id}/revoke")
    public Result<Penalty> revokePenalty(@PathVariable Long id,
                                         @Valid @RequestBody PenaltyRevokeDTO dto,
                                         @RequestHeader("Authorization") String token) {
        authUtil.requireAdmin(token);
        Long adminId = authUtil.requireUserId(token);

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
