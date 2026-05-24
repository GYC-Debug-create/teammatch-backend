package com.teammatch.m6.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teammatch.common.Result;
import com.teammatch.dto.EvaluationReviewCommand;
import com.teammatch.dto.EvaluationReviewResult;
import com.teammatch.entity.Evaluation;
import com.teammatch.m6.dto.EvaluationReviewRequest;
import com.teammatch.m6.dto.PendingEvaluationVO;
import com.teammatch.mapper.EvaluationMapper;
import com.teammatch.service.EvaluationReviewService;
import com.teammatch.util.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评价复核 Controller（管理端）。
 *
 * 提供待复核评价列表查询和执行评价复核两个管理端 HTTP 入口。
 * 业务逻辑全部委托给 M5-6 的 {@link EvaluationReviewService}。
 */
@RestController
@RequestMapping("/admin/evaluations")
@RequiredArgsConstructor
public class EvaluationAdminController {

    private final AuthUtil authUtil;
    private final EvaluationReviewService evaluationReviewService;
    private final EvaluationMapper evaluationMapper;

    /**
     * 查询待复核评价列表。
     *
     * GET /api/admin/evaluations/pending?projectId={optional}
     */
    @GetMapping("/pending")
    public Result<List<PendingEvaluationVO>> getPendingEvaluations(
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) Long projectId) {
        authUtil.requireAdmin(token);

        LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Evaluation::getStatus, Evaluation.STATUS_PENDING_REVIEW);
        if (projectId != null) {
            wrapper.eq(Evaluation::getProjectId, projectId);
        }
        wrapper.orderByDesc(Evaluation::getCreatedAt);

        List<Evaluation> evaluations = evaluationMapper.selectList(wrapper);
        List<PendingEvaluationVO> voList = evaluations.stream()
                .map(this::toPendingEvaluationVO)
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    /**
     * 执行评价复核。
     *
     * POST /api/admin/evaluations/{id}/review
     */
    @PostMapping("/{id}/review")
    public Result<EvaluationReviewResult> reviewEvaluation(
            @RequestHeader("Authorization") String token,
            @PathVariable("id") Long id,
            @RequestBody EvaluationReviewRequest request) {
        authUtil.requireAdmin(token);
        Long adminId = authUtil.requireUserId(token);

        EvaluationReviewCommand command = new EvaluationReviewCommand();
        command.setEvaluationId(id);
        command.setReviewerId(adminId);
        command.setAction(request.getAction());
        command.setReviewNote(request.getReviewNote());

        return evaluationReviewService.review(command);
    }

    private PendingEvaluationVO toPendingEvaluationVO(Evaluation evaluation) {
        PendingEvaluationVO vo = new PendingEvaluationVO();
        vo.setEvaluationId(evaluation.getId());
        vo.setProjectId(evaluation.getProjectId());
        vo.setEvaluatorId(evaluation.getEvaluatorId());
        vo.setTargetId(evaluation.getTargetId());
        vo.setCommunicationScore(evaluation.getCommunicationScore());
        vo.setTaskScore(evaluation.getTaskScore());
        vo.setSkillScore(evaluation.getSkillScore());
        vo.setResponsibilityScore(evaluation.getResponsibilityScore());
        vo.setAverageScore(evaluation.getAverageScore());
        vo.setComment(evaluation.getComment());
        vo.setStatus(evaluation.getStatus());
        vo.setCreatedAt(evaluation.getCreatedAt());
        return vo;
    }
}
