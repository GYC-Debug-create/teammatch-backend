package com.teammatch.m6.controller;

import com.teammatch.common.ReasonCode;
import com.teammatch.common.Result;
import com.teammatch.entity.Appeal;
import com.teammatch.m6.dto.AppealCreateDTO;
import com.teammatch.m6.service.AppealService;
import com.teammatch.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 申诉 Controller（用户端）
 *
 * 根据详细设计文档 7.5 节定义
 * 用户端接口前缀：/api/appeals
 */
@RestController
@RequestMapping("/appeals")
@RequiredArgsConstructor
public class AppealController {

    private final AppealService appealService;
    private final AuthService authService;

    /**
     * 提交申诉
     * POST /api/appeals
     *
     * @param token 认证token
     * @param dto 申诉请求
     * @return 创建的申诉记录
     */
    @PostMapping
    public Result<Appeal> createAppeal(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody AppealCreateDTO dto) {
        Long userId = authService.getUserIdFromToken(token);
        if (userId == null) {
            return Result.fail(ReasonCode.UNAUTHORIZED);
        }

        try {
            Appeal appeal = appealService.createAppeal(userId, dto);
            return Result.success(appeal);
        } catch (IllegalArgumentException e) {
            return Result.of(ReasonCode.PARAM_ERROR.getCode(), e.getMessage(), null);
        } catch (IllegalStateException e) {
            return Result.of(ReasonCode.STATUS_CONFLICT.getCode(), e.getMessage(), null);
        }
    }

    /**
     * 获取我的申诉列表
     * GET /api/appeals/my
     *
     * @param token 认证token
     * @return 申诉列表
     */
    @GetMapping("/my")
    public Result<List<Appeal>> getMyAppeals(@RequestHeader("Authorization") String token) {
        Long userId = authService.getUserIdFromToken(token);
        if (userId == null) {
            return Result.fail(ReasonCode.UNAUTHORIZED);
        }

        List<Appeal> appeals = appealService.getMyAppeals(userId);
        return Result.success(appeals);
    }

    /**
     * 获取申诉详情
     * GET /api/appeals/{id}
     *
     * @param token 认证token
     * @param id 申诉ID
     * @return 申诉详情
     */
    @GetMapping("/{id}")
    public Result<Appeal> getAppealById(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long id) {
        Long userId = authService.getUserIdFromToken(token);
        if (userId == null) {
            return Result.fail(ReasonCode.UNAUTHORIZED);
        }

        Appeal appeal = appealService.getAppealById(id);
        if (appeal == null) {
            return Result.fail(ReasonCode.NOT_FOUND);
        }

        // 只能查看自己的申诉
        if (!appeal.getUserId().equals(userId)) {
            return Result.fail(ReasonCode.FORBIDDEN);
        }

        return Result.success(appeal);
    }
}
