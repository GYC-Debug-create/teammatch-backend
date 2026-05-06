package com.teammatch.m4.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teammatch.entity.CreditChange;
import com.teammatch.entity.Evaluation;
import com.teammatch.m4.service.CreditRuleService;
import com.teammatch.mapper.CreditChangeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CreditRuleServiceImpl implements CreditRuleService {

    private final CreditChangeMapper creditChangeMapper;

    /**
     * 评分到 delta 映射算法 (简单平均分映射):
     * 四个维度的满分通常是5分，计算平均分 avg
     * avg >= 4.5 -> +5 分
     * avg >= 4.0 -> +3 分
     * avg >= 3.0 -> 0 分
     * avg <  3.0 -> -3 分
     * avg <  2.0 -> -5 分
     * 注：若后续需要修改映射表，可以直接调整此处阈值
     */
    @Override
    public int calculateDeltaFromEvaluation(Evaluation evaluation) {
        int totalScore = evaluation.getCommunicationScore() + 
                         evaluation.getTaskScore() + 
                         evaluation.getSkillScore() + 
                         evaluation.getResponsibilityScore();
        double avg = totalScore / 4.0;
        
        if (avg >= 4.5) return 5;
        if (avg >= 4.0) return 3;
        if (avg >= 3.0) return 0;
        if (avg >= 2.0) return -3;
        return -5;
    }

    @Override
    public int calculateCappedDelta(Long userId, Long projectId, int proposedDelta) {
        if (proposedDelta == 0) return 0;

        // 获取用户在该项目下已生效所有的信誉变动和
        List<CreditChange> changes = creditChangeMapper.selectList(new LambdaQueryWrapper<CreditChange>()
                .eq(CreditChange::getUserId, userId)
                .eq(CreditChange::getSourceType, "evaluation")
                .eq(CreditChange::getProjectId, projectId) 
                .eq(CreditChange::getEffective, true));
                
        int currentTotalDelta = 0;
        for (CreditChange cc : changes) {
            currentTotalDelta += cc.getChangeValue();
        }

        // 判断截断
        int maxAllowed = 10;
        int targetTotal = currentTotalDelta + proposedDelta;

        if (targetTotal > maxAllowed) {
            // 正向截断：如果当前已经积了 8 分，新来了 5 分 -> 只能再加 2 分
            return Math.max(0, maxAllowed - currentTotalDelta);
        } else if (targetTotal < -maxAllowed) {
            // 负向截断：如果当前扣了 -8 分，新来了 -5 分 -> 只能再扣 -2 分
            return Math.min(0, -maxAllowed - currentTotalDelta);
        }

        return proposedDelta;
    }
}
