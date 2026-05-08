package com.teammatch.service;

import com.teammatch.dto.CreditCalculationResult;
import com.teammatch.entity.Evaluation;
import com.teammatch.m4.service.CreditRuleService;
import com.teammatch.service.impl.CreditCalculationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class CreditCalculationServiceTest {

    @Mock
    private CreditRuleService creditRuleService;

    @InjectMocks
    private CreditCalculationServiceImpl creditCalculationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void calculate_full_flow_no_cap() {
        Evaluation eval = createEvaluation(1L, 1L, 5, 5, 5, 5);

        when(creditRuleService.mapAverageScoreToDelta(5.0)).thenReturn(5);
        when(creditRuleService.getExistingProjectDelta(1L, 1L)).thenReturn(0);
        when(creditRuleService.calculateCappedDelta(1L, 1L, 5)).thenReturn(5);

        CreditCalculationResult result = creditCalculationService.calculate(eval);

        assertNotNull(result);
        assertEquals(new BigDecimal("5.00"), result.getAverageScore());
        assertEquals(5, result.getRawDelta());
        assertEquals(0, result.getExistingProjectDelta());
        assertEquals(5, result.getCappedDelta());
        assertEquals(5, result.getProjectTotalAfterApplied());
        assertFalse(result.getCapped());
    }

    @Test
    void calculate_full_flow_with_cap() {
        Evaluation eval = createEvaluation(1L, 1L, 5, 5, 5, 5);

        when(creditRuleService.mapAverageScoreToDelta(5.0)).thenReturn(5);
        when(creditRuleService.getExistingProjectDelta(1L, 1L)).thenReturn(8);
        when(creditRuleService.calculateCappedDelta(1L, 1L, 5)).thenReturn(2);

        CreditCalculationResult result = creditCalculationService.calculate(eval);

        assertNotNull(result);
        assertEquals(new BigDecimal("5.00"), result.getAverageScore());
        assertEquals(5, result.getRawDelta());
        assertEquals(8, result.getExistingProjectDelta());
        assertEquals(2, result.getCappedDelta());
        assertEquals(10, result.getProjectTotalAfterApplied());
        assertTrue(result.getCapped());
    }

    @Test
    void calculate_negative_cap() {
        Evaluation eval = createEvaluation(1L, 1L, 1, 1, 1, 1);

        when(creditRuleService.mapAverageScoreToDelta(1.0)).thenReturn(-5);
        when(creditRuleService.getExistingProjectDelta(1L, 1L)).thenReturn(-8);
        when(creditRuleService.calculateCappedDelta(1L, 1L, -5)).thenReturn(-2);

        CreditCalculationResult result = creditCalculationService.calculate(eval);

        assertNotNull(result);
        assertEquals(new BigDecimal("1.00"), result.getAverageScore());
        assertEquals(-5, result.getRawDelta());
        assertEquals(-8, result.getExistingProjectDelta());
        assertEquals(-2, result.getCappedDelta());
        assertEquals(-10, result.getProjectTotalAfterApplied());
        assertTrue(result.getCapped());
    }

    @Test
    void calculate_zero_existing() {
        Evaluation eval = createEvaluation(1L, 1L, 4, 4, 4, 4);

        when(creditRuleService.mapAverageScoreToDelta(4.0)).thenReturn(3);
        when(creditRuleService.getExistingProjectDelta(1L, 1L)).thenReturn(0);
        when(creditRuleService.calculateCappedDelta(1L, 1L, 3)).thenReturn(3);

        CreditCalculationResult result = creditCalculationService.calculate(eval);

        assertNotNull(result);
        assertEquals(new BigDecimal("4.00"), result.getAverageScore());
        assertEquals(3, result.getRawDelta());
        assertEquals(0, result.getExistingProjectDelta());
        assertEquals(3, result.getCappedDelta());
        assertEquals(3, result.getProjectTotalAfterApplied());
        assertFalse(result.getCapped());
    }

    @Test
    void calculate_already_at_cap() {
        Evaluation eval = createEvaluation(1L, 1L, 5, 5, 5, 5);

        when(creditRuleService.mapAverageScoreToDelta(5.0)).thenReturn(5);
        when(creditRuleService.getExistingProjectDelta(1L, 1L)).thenReturn(10);
        when(creditRuleService.calculateCappedDelta(1L, 1L, 5)).thenReturn(0);

        CreditCalculationResult result = creditCalculationService.calculate(eval);

        assertNotNull(result);
        assertEquals(new BigDecimal("5.00"), result.getAverageScore());
        assertEquals(5, result.getRawDelta());
        assertEquals(10, result.getExistingProjectDelta());
        assertEquals(0, result.getCappedDelta());
        assertEquals(10, result.getProjectTotalAfterApplied());
        assertTrue(result.getCapped());
    }

    @Test
    void calculate_boundary_4_49() {
        Evaluation eval = createEvaluation(1L, 1L, 4, 5, 4, 4);

        when(creditRuleService.mapAverageScoreToDelta(4.25)).thenReturn(3);
        when(creditRuleService.getExistingProjectDelta(1L, 1L)).thenReturn(0);
        when(creditRuleService.calculateCappedDelta(1L, 1L, 3)).thenReturn(3);

        CreditCalculationResult result = creditCalculationService.calculate(eval);

        assertNotNull(result);
        assertEquals(new BigDecimal("4.25"), result.getAverageScore());
        assertEquals(3, result.getRawDelta());
        assertEquals(0, result.getExistingProjectDelta());
        assertEquals(3, result.getCappedDelta());
        assertEquals(3, result.getProjectTotalAfterApplied());
        assertFalse(result.getCapped());
    }

    @Test
    void calculate_neutral_score() {
        Evaluation eval = createEvaluation(1L, 1L, 3, 3, 3, 3);

        when(creditRuleService.mapAverageScoreToDelta(3.0)).thenReturn(0);
        when(creditRuleService.getExistingProjectDelta(1L, 1L)).thenReturn(5);
        when(creditRuleService.calculateCappedDelta(1L, 1L, 0)).thenReturn(0);

        CreditCalculationResult result = creditCalculationService.calculate(eval);

        assertNotNull(result);
        assertEquals(new BigDecimal("3.00"), result.getAverageScore());
        assertEquals(0, result.getRawDelta());
        assertEquals(5, result.getExistingProjectDelta());
        assertEquals(0, result.getCappedDelta());
        assertEquals(5, result.getProjectTotalAfterApplied());
        assertFalse(result.getCapped());
    }

    @Test
    void calculate_positive_negative_offset() {
        Evaluation eval = createEvaluation(1L, 1L, 1, 2, 1, 2);

        when(creditRuleService.mapAverageScoreToDelta(1.5)).thenReturn(-3);
        when(creditRuleService.getExistingProjectDelta(1L, 1L)).thenReturn(7);
        when(creditRuleService.calculateCappedDelta(1L, 1L, -3)).thenReturn(-3);

        CreditCalculationResult result = creditCalculationService.calculate(eval);

        assertNotNull(result);
        assertEquals(new BigDecimal("1.50"), result.getAverageScore());
        assertEquals(-3, result.getRawDelta());
        assertEquals(7, result.getExistingProjectDelta());
        assertEquals(-3, result.getCappedDelta());
        assertEquals(4, result.getProjectTotalAfterApplied());
        assertFalse(result.getCapped());
    }

    private Evaluation createEvaluation(Long targetId, Long projectId,
                                       int comm, int task, int skill, int resp) {
        Evaluation eval = new Evaluation();
        eval.setTargetId(targetId);
        eval.setProjectId(projectId);
        eval.setCommunicationScore(comm);
        eval.setTaskScore(task);
        eval.setSkillScore(skill);
        eval.setResponsibilityScore(resp);
        return eval;
    }
}
