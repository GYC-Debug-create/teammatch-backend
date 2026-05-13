package com.teammatch.m6.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报实体类
 * 对应数据库表：report
 *
 * 根据详细设计文档 7.5 节定义
 * 支持对用户(user)或项目(project)的举报
 */
@Data
@TableName("report")
public class Report {

    /**
     * 举报ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 举报人ID
     */
    private Long reporterId;

    /**
     * 举报对象类型
     * 可选值：user（用户）、project（项目）
     */
    private String targetType;

    /**
     * 举报对象ID
     */
    private Long targetId;

    /**
     * 举报原因
     */
    private String reason;

    /**
     * 举报状态
     * 可选值：pending（待处理）、resolved（已处理）、dismissed（已驳回）
     */
    private String status;

    /**
     * 处理人ID（管理员）
     */
    private Long handlerId;

    /**
     * 处理结果说明
     */
    private String handleResult;

    /**
     * 处理时间
     */
    private LocalDateTime handledAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
