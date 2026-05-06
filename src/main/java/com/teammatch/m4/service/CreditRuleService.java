package com.teammatch.m4.service;

import java.util.List;
import com.teammatch.entity.Evaluation;

public interface CreditRuleService {
    
    /**
     * T-088 评分到 delta 映射服务
     * 将四维评分映射为最终的信誉分增减 (-5 ~ +5)
     */
    int calculateDeltaFromEvaluation(Evaluation evaluation);

    /**
     * T-089 单项目 ±10 封顶服务
     * 校验本次变动加上当前项目已有的信誉变动，是否超出了 ±10 分的封顶限制，并返回截断后的有效信誉分变动
     */
    int calculateCappedDelta(Long userId, Long projectId, int proposedDelta);
}
