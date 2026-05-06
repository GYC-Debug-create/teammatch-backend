package com.teammatch.m4.controller;

import com.teammatch.m4.dto.TeamRequestDTO;
import com.teammatch.m4.service.TeamRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/m4/team-requests")
@RequiredArgsConstructor
public class TeamRequestController {

    private final TeamRequestService teamRequestService;

    /** T-122 队长发送邀请 */
    @PostMapping("/invite")
    public ResponseEntity<?> sendInvite(@RequestBody TeamRequestDTO dto) {
        try {
            teamRequestService.sendRequest(dto, "invite");
            return ResponseEntity.ok("邀请发送成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** T-123 用户申请加入项目 */
    @PostMapping("/apply")
    public ResponseEntity<?> sendApply(@RequestBody TeamRequestDTO dto) {
        try {
            teamRequestService.sendRequest(dto, "apply");
            return ResponseEntity.ok("申请发送成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** T-124 接受请求 */
    @PostMapping("/{id}/accept")
    public ResponseEntity<?> acceptRequest(@PathVariable Long id, @RequestParam Long operatorId) {
        try {
            teamRequestService.acceptRequest(id, operatorId);
            return ResponseEntity.ok("请求已接受");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** T-129 拒绝请求 */
    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable Long id, @RequestParam Long operatorId) {
        try {
            teamRequestService.rejectRequest(id, operatorId);
            return ResponseEntity.ok("请求已拒绝");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** T-130 取消请求 */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelRequest(@PathVariable Long id, @RequestParam Long operatorId) {
        try {
            teamRequestService.cancelRequest(id, operatorId);
            return ResponseEntity.ok("请求已取消");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** T-131 收到/发出的请求列表 */
    @GetMapping
    public ResponseEntity<?> getRequestList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "received") String direction) {
        return ResponseEntity.ok(teamRequestService.getRequestList(userId, direction));
    }
}

