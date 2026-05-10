package com.teammatch.m4.controller;

import com.teammatch.m4.dto.ExitVoteCreateDTO;
import com.teammatch.m4.dto.ExitVoteSubmitDTO;
import com.teammatch.m4.entity.ExitVote;
import com.teammatch.m4.service.ExitVoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * T-136~T-141: 退出流程 REST API
 * 路径前缀: /m4/projects/{projectId}/exit
 */
@RestController
@RequestMapping("/m4/projects/{projectId}/exit")
@RequiredArgsConstructor
public class ExitVoteController {

    private final ExitVoteService exitVoteService;

    /**
     * T-136: 成员主动退出
     * POST /m4/projects/{projectId}/exit/self?userId=xxx
     */
    @PostMapping("/self")
    public ResponseEntity<?> selfExit(
            @PathVariable Long projectId,
            @RequestParam Long userId) {
        exitVoteService.selfExit(projectId, userId);
        return ResponseEntity.ok("已成功退出项目");
    }

    /**
     * T-137: 队长发起退出投票
     * POST /m4/projects/{projectId}/exit/votes
     */
    @PostMapping("/votes")
    public ResponseEntity<?> initiateVote(
            @PathVariable Long projectId,
            @RequestBody ExitVoteCreateDTO dto) {
        ExitVote vote = exitVoteService.initiateVote(projectId, dto);
        return ResponseEntity.ok(vote);
    }

    /**
     * T-138: 投票详情
     * GET /m4/projects/{projectId}/exit/votes/{voteId}
     */
    @GetMapping("/votes/{voteId}")
    public ResponseEntity<?> getVoteDetail(
            @PathVariable Long projectId,
            @PathVariable Long voteId) {
        ExitVote vote = exitVoteService.getVoteDetail(voteId);
        return ResponseEntity.ok(vote);
    }

    /**
     * T-139: 提交投票
     * POST /m4/projects/{projectId}/exit/votes/{voteId}/submit
     */
    @PostMapping("/votes/{voteId}/submit")
    public ResponseEntity<?> submitVote(
            @PathVariable Long projectId,
            @PathVariable Long voteId,
            @RequestBody ExitVoteSubmitDTO dto) {
        exitVoteService.submitVote(voteId, dto);
        return ResponseEntity.ok("投票成功");
    }

    /**
     * T-140: 关闭投票并执行结果（T-141 事务+幂等在 Service 层保证）
     * POST /m4/projects/{projectId}/exit/votes/{voteId}/close?operatorId=xxx
     */
    @PostMapping("/votes/{voteId}/close")
    public ResponseEntity<?> closeVote(
            @PathVariable Long projectId,
            @PathVariable Long voteId,
            @RequestParam Long operatorId) {
        ExitVote result = exitVoteService.closeVote(voteId, operatorId);
        return ResponseEntity.ok(result);
    }
}
