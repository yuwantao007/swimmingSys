package com.swimmingsys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.swimmingsys.model.dto.CourseAddDTO;
import com.swimmingsys.model.dto.CourseQueryDTO;
import com.swimmingsys.model.dto.CourseUpdateDTO;
import com.swimmingsys.model.vo.CourseVO;

/**
 * 课程服务接口
 */
public interface CourseService {

    /**
     * 获取课程列表（分页/条件查询）
     *
     * @param queryDTO 查询条件
     * @return 课程分页列表
     */
    IPage<CourseVO> getCourseList(CourseQueryDTO queryDTO);

    /**
     * 根据ID获取课程信息
     *
     * @param id 课程ID
     * @return 课程信息
     */
    CourseVO getCourseById(Long id);

    /**
     * 新增课程
     *
     * @param addDTO 新增课程信息
     * @return 新增的课程信息
     */
    CourseVO addCourse(CourseAddDTO addDTO);

    /**
     * 更新课程信息
     *
     * @param id        课程ID
     * @param updateDTO 更新信息
     * @return 更新后的课程信息
     */
    CourseVO updateCourse(Long id, CourseUpdateDTO updateDTO);

    /**
     * 删除课程（逻辑删除）
     *
     * @param id 课程ID
     * @return 是否删除成功
     */
    boolean deleteCourse(Long id);

    /**
     * 修改课程状态（发布/下架）
     *
     * @param id     课程ID
     * @param status 状态：0-已下架，1-已发布
     * @return 更新后的课程信息
     */
    CourseVO updateCourseStatus(Long id, Integer status);
}
