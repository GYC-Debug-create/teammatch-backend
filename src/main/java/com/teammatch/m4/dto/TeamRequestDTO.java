package com.teammatch.m4.dto;

import lombok.Data;

@Data
public class TeamRequestDTO {
    private Long projectId; // 项目ID
    private Long fromUserId; // 发起人（队长）ID
    private Long toUserId; // 被邀请人ID
    private String message; // 邀请/申请留言

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getFromUserId() { return fromUserId; }
    public void setFromUserId(Long fromUserId) { this.fromUserId = fromUserId; }
    public Long getToUserId() { return toUserId; }
    public void setToUserId(Long toUserId) { this.toUserId = toUserId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
