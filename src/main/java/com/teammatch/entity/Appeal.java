package com.teammatch.entity;

import java.time.LocalDateTime;

/**
 * 申诉记录实体
 * M5-7 只使用查询相关字段，完整申诉系统由 M6 构建
 */
public class Appeal {

    /** 申诉记录ID */
    private Long id;

    /** 申诉用户ID */
    private Long userId;

    /** 申诉目标类型: evaluation / penalty */
    private String targetType;

    /** 申诉目标ID */
    private Long targetId;

    /** 申诉原因 */
    private String reason;

    /** 申诉状态: pending / approved / rejected */
    private String status;

    /** 处理人ID */
    private Long handlerId;

    /** 处理结果 */
    private String handleResult;

    /** 处理时间 */
    private LocalDateTime handledAt;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getHandlerId() {
        return handlerId;
    }

    public void setHandlerId(Long handlerId) {
        this.handlerId = handlerId;
    }

    public String getHandleResult() {
        return handleResult;
    }

    public void setHandleResult(String handleResult) {
        this.handleResult = handleResult;
    }

    public LocalDateTime getHandledAt() {
        return handledAt;
    }

    public void setHandledAt(LocalDateTime handledAt) {
        this.handledAt = handledAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
