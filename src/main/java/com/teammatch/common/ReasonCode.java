package com.teammatch.common;

import java.util.HashMap;
import java.util.Map;

/**
 * M5 模块错误码枚举
 * 用于统一管理互评业务的错误码和错误信息
 */
public enum ReasonCode {
    SUCCESS("00000", "成功"),

    // 通用错误（M10xx）
    PARAM_ERROR("M1001", "参数错误"),
    NOT_FOUND("M1002", "资源不存在"),
    STATUS_CONFLICT("M1003", "状态冲突"),

    // 项目级错误（M50xx）
    NOT_PROJECT_MEMBER("M5001", "用户不是该项目成员"),
    PROJECT_NOT_ENDED("M5002", "项目尚未结束"),
    EVAL_WINDOW_CLOSED("M5003", "互评窗口已关闭"),
    PROJECT_NOT_FOUND("M5004", "项目不存在"),

    // 成员级错误（M51xx）
    SELF_EVALUATION_NOT_ALLOWED("M5101", "不允许自评"),
    DUPLICATE_EVALUATION("M5102", "重复评价"),
    TARGET_NOT_MEMBER("M5103", "被评价人不是项目成员"),
    EVALUATOR_NOT_MEMBER("M5104", "评价人不是项目成员"),

    // 内容校验级错误（M52xx）
    SCORE_FIELD_MISSING("M5201", "评分字段缺失"),
    SCORE_OUT_OF_RANGE("M5202", "评分超出范围"),
    COMMENT_CONTAINS_VIOLATION("M5203", "评论包含违规词"),
    HIGH_SCORE_MISSING_POSITIVE_TAG("M5204", "高分缺少正向标签"),
    LOW_SCORE_MISSING_NEGATIVE_TAG("M5205", "低分缺少负向标签"),
    LOW_SCORE_COMMENT_EMPTY("M5206", "低分评论为空"),

    // 通用错误
    UNKNOWN_ERROR("M9999", "未知错误");

    private final String code;
    private final String message;

    /** 错误码到枚举的映射缓存，提升查找性能 */
    private static final Map<String, ReasonCode> CODE_MAP = new HashMap<>();

    static {
        // 类加载时初始化映射表
        for (ReasonCode reasonCode : ReasonCode.values()) {
            CODE_MAP.put(reasonCode.code, reasonCode);
        }
    }

    ReasonCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /** 获取错误码 */
    public String getCode() {
        return code;
    }

    /** 获取错误信息 */
    public String getMessage() {
        return message;
    }

    /**
     * 根据错误码字符串查找对应的枚举
     * 使用 Map 缓存，时间复杂度 O(1)
     *
     * @param code 错误码字符串（如 "M5001"）
     * @return 对应的 ReasonCode 枚举，找不到返回 UNKNOWN_ERROR
     */
    public static ReasonCode fromCode(String code) {
        return CODE_MAP.getOrDefault(code, UNKNOWN_ERROR);
    }
}
