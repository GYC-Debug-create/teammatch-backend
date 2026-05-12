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
}
