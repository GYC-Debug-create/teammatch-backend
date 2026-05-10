package com.teammatch.m4.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teammatch.m4.dto.ExitVoteCreateDTO;
import com.teammatch.m4.dto.ExitVoteSubmitDTO;
import com.teammatch.m4.entity.ExitVote;
import com.teammatch.m4.entity.ExitVoteRecord;
import com.teammatch.m4.entity.TeamMember;
import com.teammatch.m4.mapper.M4ExitVoteMapper;
import com.teammatch.m4.mapper.M4ExitVoteRecordMapper;
import com.teammatch.m4.entity.Project;
import com.teammatch.m4.service.ExitVoteService;
import com.teammatch.m4.service.ProjectService;
import com.teammatch.m4.service.TeamMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * T-136~T-141: 退出流程实现
 */
@Service
@RequiredArgsConstructor
public class ExitVoteServiceImpl extends ServiceImpl<M4ExitVoteMapper, ExitVote> implements ExitVoteService {

    private final M4ExitVoteRecordMapper exitVoteRecordMapper;
    private final TeamMemberService teamMemberService;
    private final ProjectService projectService;

    // ------------------------------------------------------------------ T-136
    /**
     * 成员主动退出：
     * 1. 校验成员是 active
     * 2. 将 team_member status 改为 exited，记录 exitMode/leftAt
     * （调用 M5 写 credit_change -10 的接口，待 T-194/T-098 就绪后对接，此处预留注释）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void selfExit(Long projectId, Long userId) {
        TeamMember member = getActiveMember(projectId, userId);
        if (member == null) {
            throw new RuntimeException("该用户不是项目的活跃成员");
        }
        if ("leader".equals(member.getRole())) {
            throw new RuntimeException("队长不能主动退出，请先转让队长身份");
        }
        member.setStatus("exited");
        member.setExitMode("self_exit");
        member.setLeftAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());
        teamMemberService.updateById(member);
        // TODO T-194: 调用 M5 CreditChangeService 写 -10 流水
    }

    // ------------------------------------------------------------------ T-137
    /**
     * 队长发起退出投票：
     * 1. 项目必须是 in_progress
     * 2. 操作人必须是队长
     * 3. 目标成员必须是 active 且不是队长
     * 4. 同一项目同一目标不能存在 voting 状态的投票（防重复）
     * 5. 快照当前参与投票人数（所有 active 成员，不含目标成员自身）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExitVote initiateVote(Long projectId, ExitVoteCreateDTO dto) {
        // 校验项目必须是 in_progress
        Project project = projectService.getById(projectId);
        if (project == null || !"in_progress".equals(project.getStatus())) {
            throw new RuntimeException("只有进行中的项目才能发起退出投票");
        }
        // 校验发起人是队长
        TeamMember initiator = getActiveMember(projectId, dto.getInitiatorId());
        if (initiator == null || !"leader".equals(initiator.getRole())) {
            throw new RuntimeException("只有队长可以发起退出投票");
        }
        // 校验目标成员
        TeamMember target = getActiveMember(projectId, dto.getTargetUserId());
        if (target == null) {
            throw new RuntimeException("目标成员不是项目活跃成员");
        }
        if ("leader".equals(target.getRole())) {
            throw new RuntimeException("不能对队长发起退出投票");
        }
        // 防重复投票（唯一活跃投票由 active_vote_key 约束，此处做业务校验）
        long votingCount = this.count(new LambdaQueryWrapper<ExitVote>()
                .eq(ExitVote::getProjectId, projectId)
                .eq(ExitVote::getTargetUserId, dto.getTargetUserId())
                .eq(ExitVote::getStatus, "voting"));
        if (votingCount > 0) {
            throw new RuntimeException("该成员已有进行中的退出投票");
        }
        // 快照可投票人数（不含目标成员）
        long voterCount = teamMemberService.count(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getProjectId, projectId)
                .eq(TeamMember::getStatus, "active")
                .ne(TeamMember::getUserId, dto.getTargetUserId()));

        ExitVote vote = new ExitVote();
        vote.setProjectId(projectId);
        vote.setInitiatorId(dto.getInitiatorId());
        vote.setTargetUserId(dto.getTargetUserId());
        vote.setReason(dto.getReason());
        vote.setPenaltyLevel(dto.getPenaltyLevel() != null ? dto.getPenaltyLevel() : "negotiated");
        vote.setStatus("voting");
        vote.setAgreeCount(0);
        vote.setDisagreeCount(0);
        vote.setTotalVoters((int) voterCount);
        vote.setDeadlineAt(LocalDateTime.now().plusHours(24));
        vote.setCreatedAt(LocalDateTime.now());
        vote.setUpdatedAt(LocalDateTime.now());
        this.save(vote);
        return vote;
    }

    // ------------------------------------------------------------------ T-138
    /** 返回投票详情（含统计），懒检查 deadline */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExitVote getVoteDetail(Long voteId) {
        ExitVote vote = this.getById(voteId);
        if (vote == null) {
            throw new RuntimeException("投票不存在");
        }
        lazyCloseIfDeadlinePassed(vote);
        return vote;
    }

    // ------------------------------------------------------------------ T-139
    /**
     * 提交投票（agree/disagree）：
     * 1. 投票必须是 open
     * 2. 投票人必须是活跃成员且不是目标成员
     * 3. 不能重复投票（幂等）
     * 4. 更新计数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitVote(Long voteId, ExitVoteSubmitDTO dto) {
        ExitVote vote = this.getById(voteId);
        if (vote == null) {
            throw new RuntimeException("投票不存在");
        }
        lazyCloseIfDeadlinePassed(vote);
        if (!"voting".equals(vote.getStatus())) {
            throw new RuntimeException("投票已关闭");
        }
        if (vote.getTargetUserId().equals(dto.getVoterId())) {
            throw new RuntimeException("目标成员不能参与自己的退出投票");
        }
        // 校验投票人必须是项目 active 成员
        TeamMember voter = getActiveMember(vote.getProjectId(), dto.getVoterId());
        if (voter == null) {
            throw new RuntimeException("投票人不是项目活跃成员");
        }
        // 防重复投票（T-141 幂等）
        long alreadyVoted = exitVoteRecordMapper.selectCount(new LambdaQueryWrapper<ExitVoteRecord>()
                .eq(ExitVoteRecord::getVoteId, voteId)
                .eq(ExitVoteRecord::getVoterId, dto.getVoterId()));
        if (alreadyVoted > 0) {
            throw new RuntimeException("DUPLICATE_VOTE");
        }
        // 写投票记录
        ExitVoteRecord record = new ExitVoteRecord();
        record.setVoteId(voteId);
        record.setVoterId(dto.getVoterId());
        record.setChoice(dto.getChoice());
        record.setCreatedAt(LocalDateTime.now());
        exitVoteRecordMapper.insert(record);

        // 更新计数
        if ("agree".equals(dto.getChoice())) {
            vote.setAgreeCount(vote.getAgreeCount() + 1);
        } else {
            vote.setDisagreeCount(vote.getDisagreeCount() + 1);
        }
        vote.setUpdatedAt(LocalDateTime.now());
        this.updateById(vote);
    }

    // ------------------------------------------------------------------ T-140 / T-141
    /**
     * 关闭投票并执行结果（T-141：事务 + 幂等）：
     * 1. 幂等：非 open 直接返回
     * 2. 简单多数（agree > disagree）→ approved，目标成员 status=exited，exitMode=exit_vote
     * 3. 否则 → rejected
     * 4. TODO T-194: approved 时调用 M5 写 credit_change -10
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExitVote closeVote(Long voteId, Long operatorId) {
        // T-141 行级锁：同一条投票并发关闭时，后到的线程会排队，避免双重执行
        ExitVote vote = baseMapper.selectByIdForUpdate(voteId);
        if (vote == null) {
            throw new RuntimeException("投票不存在");
        }
        if (!"voting".equals(vote.getStatus())) {
            return vote; // T-141 幂等：已关闭直接返回当前结果，行锁已释放
        }
        // 只有发起人才能主动关闭（超时则任何人触发懒关闭）
        if (!vote.getInitiatorId().equals(operatorId)) {
            throw new RuntimeException("只有发起人可以关闭投票");
        }

        boolean pass = vote.getAgreeCount() > vote.getDisagreeCount();
        vote.setStatus("closed");
        vote.setResult(pass ? "pass" : "reject");
        vote.setClosedAt(LocalDateTime.now());
        vote.setUpdatedAt(LocalDateTime.now());
        this.updateById(vote);

        if (pass) {
            // 将目标成员标记为退出（exit_mode=exit_vote，与 schema 一致）
            List<TeamMember> members = teamMemberService.list(new LambdaQueryWrapper<TeamMember>()
                    .eq(TeamMember::getProjectId, vote.getProjectId())
                    .eq(TeamMember::getUserId, vote.getTargetUserId())
                    .eq(TeamMember::getStatus, "active"));
            if (!members.isEmpty()) {
                TeamMember m = members.get(0);
                m.setStatus("exited");
                m.setExitMode("exit_vote");
                m.setLeftAt(LocalDateTime.now());
                m.setUpdatedAt(LocalDateTime.now());
                teamMemberService.updateById(m);
            }
            // TODO T-194: 调用 M5 CreditChangeService 写 -10 流水
        }
        return vote;
    }

    // ------------------------------------------------------------------ helper

    /**
     * deadline_at 懒检查：若 voting 且已超时，按当前票数自动关闭。
     * 调用方应在行锁内或安全上下文中调用。
     */
    private void lazyCloseIfDeadlinePassed(ExitVote vote) {
        if (!"voting".equals(vote.getStatus())) return;
        if (vote.getDeadlineAt() == null || !LocalDateTime.now().isAfter(vote.getDeadlineAt())) return;
        boolean pass = vote.getAgreeCount() > vote.getDisagreeCount();
        vote.setStatus("closed");
        vote.setResult(pass ? "pass" : "reject");
        vote.setClosedAt(LocalDateTime.now());
        vote.setUpdatedAt(LocalDateTime.now());
        this.updateById(vote);
        if (pass) {
            List<TeamMember> members = teamMemberService.list(new LambdaQueryWrapper<TeamMember>()
                    .eq(TeamMember::getProjectId, vote.getProjectId())
                    .eq(TeamMember::getUserId, vote.getTargetUserId())
                    .eq(TeamMember::getStatus, "active"));
            if (!members.isEmpty()) {
                TeamMember m = members.get(0);
                m.setStatus("exited");
                m.setExitMode("exit_vote");
                m.setLeftAt(LocalDateTime.now());
                m.setUpdatedAt(LocalDateTime.now());
                teamMemberService.updateById(m);
            }
        }
    }

    private TeamMember getActiveMember(Long projectId, Long userId) {
        return teamMemberService.getOne(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getProjectId, projectId)
                .eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getStatus, "active"));
    }
}
