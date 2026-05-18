package com.teammatch.m6.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teammatch.m6.entity.Penalty;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 处罚记录 Mapper 接口
 *
 * 根据详细设计文档 7.5 节定义
 */
@Mapper
public interface PenaltyMapper extends BaseMapper<Penalty> {

    /**
     * 查询用户的处罚记录列表
     *
     * @param userId 用户ID
     * @return 处罚记录列表
     */
    @Select("SELECT * FROM penalty WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Penalty> selectByUserId(Long userId);

    /**
     * 查询用户生效中的处罚记录
     *
     * @param userId 用户ID
     * @return 生效中的处罚记录列表
     */
    @Select("SELECT * FROM penalty WHERE user_id = #{userId} AND status = 'active' ORDER BY created_at DESC")
    List<Penalty> selectActiveByUserId(Long userId);

    /**
     * 查询指定状态的处罚记录列表
     *
     * @param status 状态
     * @return 处罚记录列表
     */
    @Select("SELECT * FROM penalty WHERE status = #{status} ORDER BY created_at DESC")
    List<Penalty> selectByStatus(String status);

    /**
     * 查询指定类型的处罚记录列表
     *
     * @param type 处罚类型
     * @return 处罚记录列表
     */
    @Select("SELECT * FROM penalty WHERE type = #{type} ORDER BY created_at DESC")
    List<Penalty> selectByType(String type);
}
