package com.teammatch.m4.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectCreateDTO {
    private Long creatorId; // 发起人ID（实际场景建议从Token获取）
    private Long boardId; // 所属板块
    private String title; // 项目标题
    private String description; // 项目描述
    private Integer maxMembers; // 最大人数
    private LocalDateTime deadline; // 招募截止时间
    private List<Long> skillTagIds; // 关联技能标签列表

    public Long getCreatorId() { return creatorId; }
    public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
    public Long getBoardId() { return boardId; }
    public void setBoardId(Long boardId) { this.boardId = boardId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getMaxMembers() { return maxMembers; }
    public void setMaxMembers(Integer maxMembers) { this.maxMembers = maxMembers; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public List<Long> getSkillTagIds() { return skillTagIds; }
    public void setSkillTagIds(List<Long> skillTagIds) { this.skillTagIds = skillTagIds; }
}
