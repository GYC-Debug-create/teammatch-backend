package com.teammatch.m4.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teammatch.entity.CreditChange;
import com.teammatch.entity.Evaluation;
import com.teammatch.m4.service.impl.CreditRuleServiceImpl;
import com.teammatch.mapper.CreditChangeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CreditRuleServiceImplTest {

    @Mock
    private CreditChangeMapper creditChangeMapper;

    @InjectMocks
    private CreditRuleServiceImpl creditRuleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void mapAverageScoreToDelta_boundary_1_00() {
        assertEquals(-5, creditRuleService.mapAverageScoreToDelta(1.00));
    }

    @Test
    void mapAverageScoreToDelta_boundary_1_50() {
        assertEquals(-3, creditRuleService.mapAverageScoreToDelta(1.50));
    }

    @Test
    void mapAverageScoreToDelta_boundary_2_00() {
        assertEquals(-2, creditRuleService.mapAverageScoreToDelta(2.00));
    }

    @Test
    void mapAverageScoreToDelta_boundary_2_50() {
        assertEquals(-1, creditRuleService.mapAverageScoreToDelta(2.50));
    }

    @Test
    void mapAverageScoreToDelta_boundary_3_00() {
        assertEquals(0, creditRuleService.mapAverageScoreToDelta(3.00));
    }

    @Test
    void mapAverageScoreToDelta_boundary_3_50() {
        assertEquals(2, creditRuleService.mapAverageScoreToDelta(3.50));
    }

    @Test
    void mapAverageScoreToDelta_boundary_4_00() {
        assertEquals(3, creditRuleService.mapAverageScoreToDelta(4.00));
    }

    @Test
    void mapAverageScoreToDelta_boundary_4_50() {
        assertEquals(5, creditRuleService.mapAverageScoreToDelta(4.50));
    }

    @Test
    void mapAverageScoreToDelta_boundary_5_00() {
        assertEquals(5, creditRuleService.mapAverageScoreToDelta(5.00));
    }

    @Test
    void mapAverageScoreToDelta_upper_bound_4_49() {
        assertEquals(3, creditRuleService.mapAverageScoreToDelta(4.25));
    }

    @Test
    void mapAverageScoreToDelta_lower_bound_2_99() {
        assertEquals(-1, creditRuleService.mapAverageScoreToDelta(2.75));
    }

    @Test
    void calculateDeltaFromEvaluation_all_5() {
        Evaluation eval = new Evaluation();
        eval.setCommunicationScore(5);
        eval.setTaskScore(5);
        eval.setSkillScore(5);
        eval.setResponsibilityScore(5);
        assertEquals(5, creditRuleService.calculateDeltaFromEvaluation(eval));
    }

    @Test
    void calculateDeltaFromEvaluation_avg_4_50() {
        Evaluation eval = new Evaluation();
        eval.setCommunicationScore(5);
        eval.setTaskScore(5);
        eval.setSkillScore(4);
        eval.setResponsibilityScore(4);
        assertEquals(5, creditRuleService.calculateDeltaFromEvaluation(eval));
    }

    @Test
    void calculateDeltaFromEvaluation_all_4() {
        Evaluation eval = new Evaluation();
        eval.setCommunicationScore(4);
        eval.setTaskScore(4);
        eval.setSkillScore(4);
        eval.setResponsibilityScore(4);
        assertEquals(3, creditRuleService.calculateDeltaFromEvaluation(eval));
    }

    @Test
    void calculateDeltaFromEvaluation_all_3() {
        Evaluation eval = new Evaluation();
        eval.setCommunicationScore(3);
        eval.setTaskScore(3);
        eval.setSkillScore(3);
        eval.setResponsibilityScore(3);
        assertEquals(0, creditRuleService.calculateDeltaFromEvaluation(eval));
    }

    @Test
    void calculateDeltaFromEvaluation_avg_1_50() {
        Evaluation eval = new Evaluation();
        eval.setCommunicationScore(1);
        eval.setTaskScore(2);
        eval.setSkillScore(1);
        eval.setResponsibilityScore(2);
        assertEquals(-3, creditRuleService.calculateDeltaFromEvaluation(eval));
    }

    @Test
    void calculateCappedDelta_no_cap() {
        when(creditChangeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(createCreditChanges(5));
        int result = creditRuleService.calculateCappedDelta(1L, 1L, 3);
        assertEquals(3, result);
    }

    @Test
    void calculateCappedDelta_positive_cap() {
        when(creditChangeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(createCreditChanges(8));
        int result = creditRuleService.calculateCappedDelta(1L, 1L, 5);
        assertEquals(2, result);
    }

    @Test
    void calculateCappedDelta_already_at_positive_cap() {
        when(creditChangeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(createCreditChanges(10));
        int result = creditRuleService.calculateCappedDelta(1L, 1L, 3);
        assertEquals(0, result);
    }

    @Test
    void calculateCappedDelta_no_negative_cap() {
        when(creditChangeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(createCreditChanges(0));
        int result = creditRuleService.calculateCappedDelta(1L, 1L, -5);
        assertEquals(-5, result);
    }

    @Test
    void calculateCappedDelta_negative_cap() {
        when(creditChangeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(createCreditChanges(-8));
        int result = creditRuleService.calculateCappedDelta(1L, 1L, -5);
        assertEquals(-2, result);
    }

    @Test
    void calculateCappedDelta_already_at_negative_cap() {
        when(creditChangeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(createCreditChanges(-10));
        int result = creditRuleService.calculateCappedDelta(1L, 1L, -3);
        assertEquals(0, result);
    }

    @Test
    void calculateCappedDelta_positive_negative_offset() {
        when(creditChangeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(createCreditChanges(7));
        int result = creditRuleService.calculateCappedDelta(1L, 1L, -3);
        assertEquals(-3, result);
    }

    @Test
    void calculateCappedDelta_negative_positive_offset() {
        when(creditChangeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(createCreditChanges(-3));
        int result = creditRuleService.calculateCappedDelta(1L, 1L, 5);
        assertEquals(5, result);
    }

    @Test
    void calculateCappedDelta_zero_delta() {
        when(creditChangeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(createCreditChanges(5));
        int result = creditRuleService.calculateCappedDelta(1L, 1L, 0);
        assertEquals(0, result);
    }

    private List<CreditChange> createCreditChanges(int totalValue) {
        List<CreditChange> changes = new ArrayList<>();
        if (totalValue != 0) {
            CreditChange change = new CreditChange();
            change.setChangeValue(totalValue);
            changes.add(change);
        }
        return changes;
    }
}
