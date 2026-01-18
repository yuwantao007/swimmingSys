package com.swimmingsys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swimmingsys.model.entity.Coach;
import org.apache.ibatis.annotations.Mapper;

/**
 * 教练数据访问层
 */
@Mapper
public interface CoachMapper extends BaseMapper<Coach> {
}
