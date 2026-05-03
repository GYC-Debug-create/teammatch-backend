package com.teammatch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teammatch.entity.Evaluation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 互评数据访问层
 * 用于 M5-1B/C 互评状态查询和重复评价检查
 */
@Mapper
public interface EvaluationMapper extends BaseMapper<Evaluation> {

    // 继承 BaseMapper 提供的基础 CRUD 方法

    /**
     * 检查是否已经评价过
     * 用于 M5-1C 最终提交前资格兜底校验（防止重复评价）
     *
     * @param evaluatorId 评价人 ID
     * @param targetId 被评价人 ID
     * @param projectId 项目 ID
     * @return 如果已评价返回评价记录，否则返回 null
     */
    @Select("SELECT * FROM evaluation WHERE evaluator_id = #{evaluatorId} AND target_id = #{targetId} AND project_id = #{projectId} LIMIT 1")
    Evaluation findEvaluation(@Param("evaluatorId") Long evaluatorId,
                              @Param("targetId") Long targetId,
                              @Param("projectId") Long projectId);

    /**
     * 查询评价人在某个项目中已评价的所有目标用户 ID
     * 用于 M5-1B 待评价成员列表判断（标记哪些成员已评价）
     *
     * @param evaluatorId 评价人 ID
     * @param projectId 项目 ID
     * @return 已评价的目标用户 ID 列表
     */
    @Select("SELECT target_id FROM evaluation WHERE evaluator_id = #{evaluatorId} AND project_id = #{projectId}")
    List<Long> findEvaluatedTargetIds(@Param("evaluatorId") Long evaluatorId,
                                      @Param("projectId") Long projectId);
}
