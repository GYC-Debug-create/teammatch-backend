package com.teammatch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teammatch.entity.CreditChange;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CreditChangeMapper extends BaseMapper<CreditChange> {

    @Select("<script>" +
            "SELECT * FROM credit_change WHERE source_type = #{sourceType} AND source_id IN " +
            "<foreach collection='sourceIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND effective = 1" +
            "</script>")
    List<CreditChange> findBySourceTypeAndSourceIds(@Param("sourceType") String sourceType,
                                                     @Param("sourceIds") List<Long> sourceIds);

    @Update("<script>" +
            "UPDATE credit_change SET effective = #{effective}, updated_at = NOW() WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateEffective(@Param("ids") List<Long> ids, @Param("effective") Boolean effective);

    /**
     * 查询单条 suspended credit_change（限定完整条件）
     * 用于 M5-6 评价复核：确认 evaluation 对应的 credit_change 存在且可操作
     */
    @Select("SELECT * FROM credit_change WHERE source_type = #{sourceType} AND source_id = #{sourceId} " +
            "AND change_type = #{changeType} AND user_id = #{userId} AND effective = 0 LIMIT 1")
    CreditChange findSuspendedOne(@Param("sourceType") String sourceType,
                                  @Param("sourceId") Long sourceId,
                                  @Param("changeType") String changeType,
                                  @Param("userId") Long userId);

    /**
     * 更新单条 credit_change 的 description（审计用途）
     * 用于 M5-6 评价复核 keep_no_credit：标记流水终态原因
     */
    @Update("UPDATE credit_change SET description = #{description}, updated_at = NOW() WHERE id = #{id}")
    int updateDescription(@Param("id") Long id, @Param("description") String description);
}
