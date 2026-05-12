package com.teammatch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teammatch.entity.Appeal;
import org.apache.ibatis.annotations.Mapper;

/**
 * 申诉数据访问层
 * M5-7 只需 selectById，继承 BaseMapper 自动提供
 * 完整 Mapper 方法由 M6 扩展
 */
@Mapper
public interface AppealMapper extends BaseMapper<Appeal> {
}
