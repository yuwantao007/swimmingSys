package com.swimmingsys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swimmingsys.model.entity.Course;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课程数据访问层
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {
}
