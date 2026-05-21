package com.teammatch.m6.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 申诉创建请求 DTO
 */
@Data
public class AppealCreateDTO {

    /**
     * 申诉目标类型: evaluation / penalty
     */
    @NotBlank(message = "申诉目标类型不能为空")
    private String targetType;

    /**
     * 申诉目标ID
     */
    @NotNull(message = "申诉目标ID不能为空")
    private Long targetId;

    /**
     * 申诉原因
     */
    @NotBlank(message = "申诉原因不能为空")
    private String reason;
}
