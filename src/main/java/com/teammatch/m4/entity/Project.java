package com.teammatch.m4.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("project")
public class Project {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long creatorId;
    private Long boardId;
    private String title;
    private String description;
    private Integer maxMembers;
    private String status; // recruiting/in_progress/ended/eval_closed
    private LocalDateTime deadline;
    private LocalDateTime evalDeadline;
    private LocalDateTime endedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
