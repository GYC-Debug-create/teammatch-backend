package com.teammatch.m4.controller;

import com.teammatch.common.ReasonCode;
import com.teammatch.common.Result;
import com.teammatch.m4.dto.TeamRequestDTO;
import com.teammatch.m4.service.TeamRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/m4/team-requests")
@RequiredArgsConstructor
public class TeamRequestController {

    private final TeamRequestService teamRequestService;

    /** T-122 队长发送邀请 */
    @PostMapping("/invite")
    public Result<Void> sendInvite(@RequestBody TeamRequestDTO dto) {
        try {
            teamRequestService.sendRequest(dto, "invite");
            return Result.success();
        } catch (Exception e) {
            return Result.fail(ReasonCode.STATUS_CONFLICT);
        }
    }

    /** T-123 用户申请加入项目 */
    @PostMapping("/apply")
    public Result<Void> sendApply(@RequestBody TeamRequestDTO dto) {
        try {
            teamRequestService.sendRequest(dto, "apply");
            return Result.success();
        } catch (Exception e) {
            return Result.fail(ReasonCode.STATUS_CONFLICT);
        }
    }

    /** T-124 接受请求 */
    @PostMapping("/{id}/accept")
    public Result<Void> acceptRequest(@PathVariable Long id, @RequestParam Long operatorId) {
        try {
            teamRequestService.acceptRequest(id, operatorId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(ReasonCode.STATUS_CONFLICT);
        }
    }

    /** T-129 拒绝请求 */
    @PostMapping("/{id}/reject")
    public Result<Void> rejectRequest(@PathVariable Long id, @RequestParam Long operatorId) {
        try {
            teamRequestService.rejectRequest(id, operatorId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(ReasonCode.STATUS_CONFLICT);
        }
    }

    /** T-130 取消请求 */
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelRequest(@PathVariable Long id, @RequestParam Long operatorId) {
        try {
            teamRequestService.cancelRequest(id, operatorId);
            return Result.success();
        } catch (Exception e) {
            return Result.fail(ReasonCode.M4_RESOURCE_NOT_FOUND);
        }
    }

    /** T-131 收到/发出的请求列表 */
    @GetMapping
    public Result<?> getRequestList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "received") String direction) {
        return Result.success(teamRequestService.getRequestList(userId, direction));
    }
}

