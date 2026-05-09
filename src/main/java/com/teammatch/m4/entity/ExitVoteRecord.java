package com.teammatch.m4.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * T-135: exit_vote_record 单条投票记录
 */
@Data
@TableName("exit_vote_record")
public class ExitVoteRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 对应 exit_vote.id，列名 vote_id */
    @TableField("vote_id")
    private Long voteId;
    private Long voterId;
    /** agree / disagree */
    private String choice;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVoteId() { return voteId; }
    public void setVoteId(Long voteId) { this.voteId = voteId; }
    public Long getVoterId() { return voterId; }
    public void setVoterId(Long voterId) { this.voterId = voterId; }
    public String getChoice() { return choice; }
    public void setChoice(String choice) { this.choice = choice; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
