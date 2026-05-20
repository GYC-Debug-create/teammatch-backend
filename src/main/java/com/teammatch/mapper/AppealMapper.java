package com.teammatch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teammatch.entity.Appeal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 申诉数据访问层
 * M5-7 只需 selectById，继承 BaseMapper 自动提供
 * 完整 Mapper 方法由 M6 扩展
 */
@Mapper
public interface AppealMapper extends BaseMapper<Appeal> {

    /**
     * 查询用户的申诉列表
     */
    @Select("SELECT * FROM appeal WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Appeal> selectByUserId(@Param("userId") Long userId);

    /**
     * 按状态查询申诉列表
     */
    @Select("SELECT * FROM appeal WHERE status = #{status} ORDER BY created_at DESC")
    List<Appeal> selectByStatus(@Param("status") String status);

    /**
     * 按目标类型查询申诉列表
     */
    @Select("SELECT * FROM appeal WHERE target_type = #{targetType} ORDER BY created_at DESC")
    List<Appeal> selectByTargetType(@Param("targetType") String targetType);

    /**
     * 查询指定目标的待处理申诉
     */
    @Select("SELECT * FROM appeal WHERE target_type = #{targetType} AND target_id = #{targetId} AND status = 'pending' LIMIT 1")
    Appeal selectPendingByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);
}
