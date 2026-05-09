package com.teammatch.m4.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * T-134: exit_vote 退出投票主表
 * status: voting / closed
 * result: pass / reject / null
 * penaltyLevel: negotiated / malicious
 */
@Data
@TableName("exit_vote")
public class ExitVote {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long projectId;
    /** 被踢目标成员 userId */
    private Long targetUserId;
    /** 发起人（队长）*/
    private Long initiatorId;
    /** voting / closed */
    private String status;
    /** 处罚级别：negotiated / malicious */
    private String penaltyLevel;
    /** 投票结果：pass / reject；投票中为 null */
    private String result;
    /** 发起原因 */
    private String reason;
    /** 参与投票总人数（快照，不含目标成员） */
    private Integer totalVoters;
    /** 赞成票数 */
    private Integer agreeCount;
    /** 反对票数 */
    private Integer disagreeCount;
    /** 投票截止时间，创建时 +24h */
    private LocalDateTime deadlineAt;
    private LocalDateTime closedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
    public Long getInitiatorId() { return initiatorId; }
    public void setInitiatorId(Long initiatorId) { this.initiatorId = initiatorId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPenaltyLevel() { return penaltyLevel; }
    public void setPenaltyLevel(String penaltyLevel) { this.penaltyLevel = penaltyLevel; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Integer getTotalVoters() { return totalVoters; }
    public void setTotalVoters(Integer totalVoters) { this.totalVoters = totalVoters; }
    public Integer getAgreeCount() { return agreeCount; }
    public void setAgreeCount(Integer agreeCount) { this.agreeCount = agreeCount; }
    public Integer getDisagreeCount() { return disagreeCount; }
    public void setDisagreeCount(Integer disagreeCount) { this.disagreeCount = disagreeCount; }
    public LocalDateTime getDeadlineAt() { return deadlineAt; }
    public void setDeadlineAt(LocalDateTime deadlineAt) { this.deadlineAt = deadlineAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
