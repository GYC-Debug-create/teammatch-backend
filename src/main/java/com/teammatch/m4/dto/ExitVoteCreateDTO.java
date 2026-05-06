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
}
