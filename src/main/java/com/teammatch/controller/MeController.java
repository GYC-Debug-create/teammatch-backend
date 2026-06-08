package com.teammatch.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teammatch.common.Result;
import com.teammatch.dto.UserBadgesVO;
import com.teammatch.entity.Evaluation;
import com.teammatch.mapper.EvaluationMapper;
import com.teammatch.mapper.TeamMemberMapper;
import com.teammatch.m4.entity.ExitVote;
import com.teammatch.m4.entity.ExitVoteRecord;
import com.teammatch.entity.TeamMember;
import com.teammatch.m4.entity.TeamRequest;
import com.teammatch.mapper.UserMapper;
import com.teammatch.m4.mapper.M4ExitVoteMapper;
import com.teammatch.m4.mapper.M4ExitVoteRecordMapper;
import com.teammatch.m4.mapper.M4TeamRequestMapper;
import com.teammatch.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * M3 用户中心控制器
 * 聚合展示当前用户维度的待处理事项（角标/队列）
 */
@RestController
public class MeController {

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private M4TeamRequestMapper teamRequestMapper;

    @Autowired
    private M4ExitVoteMapper exitVoteMapper;

    @Autowired
    private M4ExitVoteRecordMapper exitVoteRecordMapper;

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Autowired
    private TeamMemberMapper teamMemberMapper;

    /**
     * 获取当前用户的角标聚合
     * GET /api/me/badges
     *
     * 统计待处理的：组队请求、退出投票、互评
     */
    @GetMapping("/me/badges")
    public Result<UserBadgesVO> getBadges(@RequestHeader("Authorization") String token) {
        Long userId = authUtil.requireUserId(token);

        // 1. 统计待处理的组队请求（邀请/申请）
        LambdaQueryWrapper<TeamRequest> inviteWrapper = new LambdaQueryWrapper<>();
        inviteWrapper.eq(TeamRequest::getToUserId, userId)
                .eq(TeamRequest::getStatus, "pending");
        long pendingInvites = teamRequestMapper.selectCount(inviteWrapper);

        // 2. 统计待处理的退出投票
        // 先查出用户活跃的项目
        LambdaQueryWrapper<TeamMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getStatus, "active");
        List<TeamMember> activeMemberships = teamMemberMapper.selectList(memberWrapper);
        List<Long> projectIds = activeMemberships.stream()
                .map(TeamMember::getProjectId)
                .collect(Collectors.toList());

        int pendingVotes = 0;
        if (!projectIds.isEmpty()) {
            LambdaQueryWrapper<ExitVote> voteWrapper = new LambdaQueryWrapper<>();
            voteWrapper.in(ExitVote::getProjectId, projectIds)
                    .eq(ExitVote::getStatus, "voting");
            List<ExitVote> activeVotes = exitVoteMapper.selectList(voteWrapper);

            for (ExitVote vote : activeVotes) {
                // 检查当前用户是否已经投过票
                LambdaQueryWrapper<ExitVoteRecord> recordWrapper = new LambdaQueryWrapper<>();
                recordWrapper.eq(ExitVoteRecord::getVoteId, vote.getId())
                        .eq(ExitVoteRecord::getVoterId, userId);
                Long votedCount = exitVoteRecordMapper.selectCount(recordWrapper);
                if (votedCount == 0) {
                    pendingVotes++;
                }
            }
        }

        // 3. 统计待完成的互评（简化版：统计用户活跃项目中其他成员数，
        // 减去用户已提交的 evaluation 记录数）
        int pendingEvaluations = 0;
        if (!projectIds.isEmpty()) {
            // 获取所有 active 项目中的其他成员总数
            long totalOtherMembers = 0;
            for (Long pid : projectIds) {
                LambdaQueryWrapper<TeamMember> otherMembers = new LambdaQueryWrapper<>();
                otherMembers.eq(TeamMember::getProjectId, pid)
                        .eq(TeamMember::getStatus, "active")
                        .ne(TeamMember::getUserId, userId);
                totalOtherMembers += teamMemberMapper.selectCount(otherMembers);
            }

            // 用户已提交的评价数
            LambdaQueryWrapper<Evaluation> evalGiven = new LambdaQueryWrapper<>();
            evalGiven.eq(Evaluation::getEvaluatorId, userId);
            long totalGiven = evaluationMapper.selectCount(evalGiven);

            pendingEvaluations = Math.max(0, (int) (totalOtherMembers - totalGiven));
        }

        return Result.success(new UserBadgesVO((int) pendingInvites, pendingVotes, pendingEvaluations));
    }
}
