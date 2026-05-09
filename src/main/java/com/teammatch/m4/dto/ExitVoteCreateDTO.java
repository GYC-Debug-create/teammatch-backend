package com.teammatch.m4.dto;

import lombok.Data;

/**
 * T-137: 发起退出投票请求体
 */
@Data
public class ExitVoteCreateDTO {
    /** 操作人（队长）userId */
    private Long initiatorId;
    /** 被踢目标成员 userId */
    private Long targetUserId;
    private String reason;
    /** 处罚级别：negotiated / malicious，P0 默认 negotiated */
    private String penaltyLevel = "negotiated";

    public Long getInitiatorId() { return initiatorId; }
    public void setInitiatorId(Long initiatorId) { this.initiatorId = initiatorId; }
    public Long getTargetUserId() { return targetUserId; }
    public void setTargetUserId(Long targetUserId) { this.targetUserId = targetUserId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getPenaltyLevel() { return penaltyLevel; }
    public void setPenaltyLevel(String penaltyLevel) { this.penaltyLevel = penaltyLevel; }
}
