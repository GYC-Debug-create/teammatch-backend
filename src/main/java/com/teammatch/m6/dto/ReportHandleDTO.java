package com.teammatch.m6.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 处理举报请求 DTO
 *
 * 根据详细设计文档 7.2 节定义
 * 供管理员处理举报使用
 */
@Data
public class ReportHandleDTO {

    /**
     * 处理结果
     * 约束：必填，resolved（已处理）或 dismissed（已驳回）
     */
    @NotBlank(message = "处理结果不能为空")
    @Pattern(regexp = "resolved|dismissed", message = "处理结果必须是 resolved 或 dismissed")
    private String status;

    /**
     * 处理结果说明
     * 约束：可选，长度 0-500
     */
    @Size(max = 500, message = "处理结果说明长度不能超过500")
    private String handleResult;
}
