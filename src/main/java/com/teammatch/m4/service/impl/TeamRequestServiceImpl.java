package com.teammatch.m4.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teammatch.m4.dto.TeamRequestDTO;
import com.teammatch.m4.entity.TeamMember;
import com.teammatch.m4.entity.TeamRequest;
import com.teammatch.m4.entity.Project;
import com.teammatch.m4.mapper.M4TeamRequestMapper;
import com.teammatch.m4.service.ProjectService;
import com.teammatch.m4.service.TeamMemberService;
import com.teammatch.m4.service.TeamRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeamRequestServiceImpl extends ServiceImpl<M4TeamRequestMapper, TeamRequest> implements TeamRequestService {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TeamMemberService teamMemberService;

    /**
     * T-122/T-123: 发送邀请/申请，含 T-133 拦截：
     * - 项目必须是 recruiting 状态
     * - 项目不能已满员
     * - 不能有同类型 pending 重复请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendRequest(TeamRequestDTO dto, String requestType) {
        Project project = projectService.getById(dto.getProjectId());
        if (project == null) {
            throw new RuntimeException("该项目不存在");
        }
        // T-133: 项目状态拦截
        if (!"recruiting".equals(project.getStatus())) {
            throw new RuntimeException("PROJECT_NOT_RECRUITING");
        }
        // T-133: 满员拦截
        long activeCount = teamMemberService.count(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getProjectId, dto.getProjectId())
                .eq(TeamMember::getStatus, "active"));
        if (project.getMaxMembers() != null && activeCount >= project.getMaxMembers()) {
            throw new RuntimeException("PROJECT_FULL");
        }
        // T-133: invite 类型必须由队长发起
        if ("invite".equals(requestType)) {
            long leaderCount = teamMemberService.count(new LambdaQueryWrapper<TeamMember>()
                    .eq(TeamMember::getProjectId, dto.getProjectId())
                    .eq(TeamMember::getUserId, dto.getFromUserId())
                    .eq(TeamMember::getRole, "leader")
                    .eq(TeamMember::getStatus, "active"));
            if (leaderCount == 0) {
                throw new RuntimeException("只有队长可以发送邀请");
            }
        }
        // T-133: 重复 pending 请求拦截
        long duplicateCount = this.count(new LambdaQueryWrapper<TeamRequest>()
                .eq(TeamRequest::getProjectId, dto.getProjectId())
                .eq(TeamRequest::getFromUserId, dto.getFromUserId())
                .eq(TeamRequest::getToUserId, dto.getToUserId())
                .eq(TeamRequest::getRequestType, requestType)
                .eq(TeamRequest::getStatus, "pending"));
        if (duplicateCount > 0) {
            throw new RuntimeException("DUPLICATE_PENDING_REQUEST");
        }

        TeamRequest request = new TeamRequest();
        request.setProjectId(dto.getProjectId());
        request.setFromUserId(dto.getFromUserId());
        request.setToUserId(dto.getToUserId());
        request.setMessage(dto.getMessage());
        request.setRequestType(requestType);
        request.setStatus("pending");
        this.save(request);
    }

    /**
     * T-124: 接受请求，写入 team_member，请求状态改 accepted
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acceptRequest(Long requestId, Long operatorId) {
        TeamRequest request = this.getById(requestId);
        if (request == null) {
            throw new RuntimeException("请求不存在");
        }
        if (!"pending".equals(request.getStatus())) {
            throw new RuntimeException("该请求已处理");
        }
        // 权限校验：
        // invite: toUserId=被邀请人，接受者必须是被邀请人本人
        // apply:  toUserId=队长，接受者必须是队长（需再次校验其确实是 leader）
        if ("invite".equals(request.getRequestType())) {
            if (!operatorId.equals(request.getToUserId())) {
                throw new RuntimeException("无权操作此请求");
            }
        } else {
            // apply：operatorId 必须是项目队长
            long leaderCount = teamMemberService.count(new LambdaQueryWrapper<TeamMember>()
                    .eq(TeamMember::getProjectId, request.getProjectId())
                    .eq(TeamMember::getUserId, operatorId)
                    .eq(TeamMember::getRole, "leader")
                    .eq(TeamMember::getStatus, "active"));
            if (leaderCount == 0) {
                throw new RuntimeException("只有队长可以接受申请");
            }
        }
        // T-133 二次校验：项目仍在招募中
        Project project = projectService.getById(request.getProjectId());
        if (project == null || !"recruiting".equals(project.getStatus())) {
            throw new RuntimeException("PROJECT_NOT_RECRUITING");
        }
        // T-133 二次校验：项目未满员
        long activeCount = teamMemberService.count(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getProjectId, request.getProjectId())
                .eq(TeamMember::getStatus, "active"));
        if (project.getMaxMembers() != null && activeCount >= project.getMaxMembers()) {
            throw new RuntimeException("PROJECT_FULL");
        }

        // 写入 team_member
        TeamMember member = new TeamMember();
        member.setProjectId(request.getProjectId());
        // invite: fromUserId=队长, toUserId=被邀请人 -> 被邀请人加入
        // apply: fromUserId=申请人, toUserId=队长   -> 申请人加入
        Long newMemberId = "invite".equals(request.getRequestType())
                ? request.getToUserId()
                : request.getFromUserId();
        member.setUserId(newMemberId);
        member.setRole("member");
        member.setStatus("active");
        member.setJoinedAt(LocalDateTime.now());
        teamMemberService.save(member);

        // 更新请求状态
        request.setStatus("accepted");
        request.setHandledAt(LocalDateTime.now());
        this.updateById(request);
    }

    /**
     * T-129: 拒绝请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectRequest(Long requestId, Long operatorId) {
        TeamRequest request = this.getById(requestId);
        if (request == null) {
            throw new RuntimeException("请求不存在");
        }
        if (!"pending".equals(request.getStatus())) {
            throw new RuntimeException("该请求已处理");
        }
        // 权限校验：invite 时被邀请人可拒绝；apply 时仅队长可拒绝
        if ("invite".equals(request.getRequestType())) {
            if (!operatorId.equals(request.getToUserId())) {
                throw new RuntimeException("无权操作此请求");
            }
        } else {
            long leaderCount = teamMemberService.count(new LambdaQueryWrapper<TeamMember>()
                    .eq(TeamMember::getProjectId, request.getProjectId())
                    .eq(TeamMember::getUserId, operatorId)
                    .eq(TeamMember::getRole, "leader")
                    .eq(TeamMember::getStatus, "active"));
            if (leaderCount == 0) {
                throw new RuntimeException("只有队长可以拒绝申请");
            }
        }
        request.setStatus("rejected");
        request.setHandledAt(LocalDateTime.now());
        this.updateById(request);
    }

    /**
     * T-130: 发送方取消 pending 请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelRequest(Long requestId, Long operatorId) {
        TeamRequest request = this.getById(requestId);
        if (request == null) {
            throw new RuntimeException("请求不存在");
        }
        if (!"pending".equals(request.getStatus())) {
            throw new RuntimeException("该请求已处理，无法取消");
        }
        if (!operatorId.equals(request.getFromUserId())) {
            throw new RuntimeException("只有发送方才能取消请求");
        }
        request.setStatus("cancelled");
        request.setHandledAt(LocalDateTime.now());
        this.updateById(request);
    }

    /**
     * T-131: 查询收到或发出的请求列表
     * direction: received -> toUserId=userId; sent -> fromUserId=userId
     */
    @Override
    public List<TeamRequest> getRequestList(Long userId, String direction) {
        LambdaQueryWrapper<TeamRequest> wrapper = new LambdaQueryWrapper<>();
        if ("received".equals(direction)) {
            wrapper.eq(TeamRequest::getToUserId, userId);
        } else {
            wrapper.eq(TeamRequest::getFromUserId, userId);
        }
        wrapper.orderByDesc(TeamRequest::getCreatedAt);
        return this.list(wrapper);
    }
}


