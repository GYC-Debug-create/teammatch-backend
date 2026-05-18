package com.teammatch.m6.controller;

import com.teammatch.common.ReasonCode;
import com.teammatch.common.Result;
import com.teammatch.m6.entity.Penalty;
import com.teammatch.m6.service.PenaltyService;
import com.teammatch.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 处罚 Controller（用户端）
 *
 * 根据详细设计文档 7.2 节定义
 * 用户端接口前缀：/api/penalties
 * 仅提供查询功能，创建和撤销由管理端处理
 */
@RestController
@RequestMapping("/penalties")
@RequiredArgsConstructor
public class PenaltyController {

    private final PenaltyService penaltyService;
    private final AuthService authService;

    /**
     * 获取我的处罚记录列表
     * GET /api/penalties/my
     *
     * @param token 认证token
     * @return 处罚记录列表
     */
    @GetMapping("/my")
    public Result<List<Penalty>> getMyPenalties(@RequestHeader("Authorization") String token) {
        // 从token获取当前用户ID
        Long userId = authService.getUserIdFromToken(token);
        if (userId == null) {
            return Result.fail(ReasonCode.UNAUTHORIZED);
        }

        List<Penalty> penalties = penaltyService.getUserPenalties(userId);
        return Result.success(penalties);
    }

    /**
     * 获取我生效中的处罚记录
     * GET /api/penalties/my/active
     *
     * @param token 认证token
     * @return 生效中的处罚记录列表
     */
    @GetMapping("/my/active")
    public Result<List<Penalty>> getMyActivePenalties(@RequestHeader("Authorization") String token) {
        // 从token获取当前用户ID
        Long userId = authService.getUserIdFromToken(token);
        if (userId == null) {
            return Result.fail(ReasonCode.UNAUTHORIZED);
        }

        List<Penalty> penalties = penaltyService.getUserActivePenalties(userId);
        return Result.success(penalties);
    }
}
