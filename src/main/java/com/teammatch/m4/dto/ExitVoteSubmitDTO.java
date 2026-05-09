package com.teammatch.m4.dto;

import lombok.Data;

/**
 * T-139: 提交投票请求体
 */
@Data
public class ExitVoteSubmitDTO {
    /** 投票人 userId */
    private Long voterId;
    /** agree / disagree */
    private String choice;

    public Long getVoterId() { return voterId; }
    public void setVoterId(Long voterId) { this.voterId = voterId; }
    public String getChoice() { return choice; }
    public void setChoice(String choice) { this.choice = choice; }
}
