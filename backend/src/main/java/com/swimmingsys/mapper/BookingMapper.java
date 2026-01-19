package com.swimmingsys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swimmingsys.model.entity.Booking;
import org.apache.ibatis.annotations.Mapper;

/**
 * 预约数据访问层
 */
@Mapper
public interface BookingMapper extends BaseMapper<Booking> {
}
