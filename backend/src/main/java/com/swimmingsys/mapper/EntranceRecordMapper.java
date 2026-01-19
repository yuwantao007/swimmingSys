package com.swimmingsys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swimmingsys.model.entity.EntranceRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入场记录Mapper接口
 */
@Mapper
public interface EntranceRecordMapper extends BaseMapper<EntranceRecord> {
}
