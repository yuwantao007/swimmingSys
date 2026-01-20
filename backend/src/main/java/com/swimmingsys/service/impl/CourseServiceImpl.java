package com.swimmingsys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swimmingsys.mapper.BookingMapper;
import com.swimmingsys.mapper.CoachMapper;
import com.swimmingsys.mapper.CourseMapper;
import com.swimmingsys.model.dto.CourseAddDTO;
import com.swimmingsys.model.dto.CourseQueryDTO;
import com.swimmingsys.model.dto.CourseUpdateDTO;
import com.swimmingsys.model.entity.Booking;
import com.swimmingsys.model.entity.Coach;
import com.swimmingsys.model.entity.Course;
import com.swimmingsys.model.vo.CourseVO;
import com.swimmingsys.service.CourseService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 课程服务实现类
 */
@Service
public class CourseServiceImpl implements CourseService {

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private CoachMapper coachMapper;

    @Resource
    private BookingMapper bookingMapper;

    /**
     * 获取课程列表（分页/条件查询）
     *
     * @param queryDTO 查询条件
     * @return 课程分页列表
     */
    @Override
    public IPage<CourseVO> getCourseList(CourseQueryDTO queryDTO) {
        // 1. 构建查询条件
        QueryWrapper<Course> queryWrapper = new QueryWrapper<>();

        // 课程名称模糊查询
        if (StringUtils.hasText(queryDTO.getCourseName())) {
            queryWrapper.like("course_name", queryDTO.getCourseName());
        }
        // 课程类型精确查询
        if (StringUtils.hasText(queryDTO.getCourseType())) {
            queryWrapper.eq("course_type", queryDTO.getCourseType());
        }
        // 教练ID精确查询
        if (queryDTO.getCoachId() != null) {
            queryWrapper.eq("coach_id", queryDTO.getCoachId());
        }
        // 状态精确查询
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq("status", queryDTO.getStatus());
        }
        // 开始时间范围查询
        if (queryDTO.getStartTimeBegin() != null) {
            queryWrapper.ge("start_time", queryDTO.getStartTimeBegin());
        }
        if (queryDTO.getStartTimeEnd() != null) {
            queryWrapper.le("start_time", queryDTO.getStartTimeEnd());
        }
        // 只查询可预约的课程
        if (Boolean.TRUE.equals(queryDTO.getBookableOnly())) {
            queryWrapper.eq("status", 1);
            queryWrapper.gt("start_time", LocalDateTime.now());
            queryWrapper.apply("current_count < capacity");
        }
        // 排除已删除
        queryWrapper.eq("is_delete", 0);
        // 按开始时间排序
        queryWrapper.orderByAsc("start_time");

        // 2. 执行分页查询
        Page<Course> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<Course> coursePage = courseMapper.selectPage(page, queryWrapper);

        // 3. 转换为VO分页
        return coursePage.convert(this::convertToCourseVO);
    }

    /**
     * 根据ID获取课程信息
     *
     * @param id 课程ID
     * @return 课程信息
     */
    @Override
    public CourseVO getCourseById(Long id) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new RuntimeException("课程ID无效");
        }

        // 2. 查询课程
        Course course = courseMapper.selectById(id);
        if (course == null || course.getIsDelete() == 1) {
            throw new RuntimeException("课程不存在");
        }

        // 3. 转换并返回
        return convertToCourseVO(course);
    }

    /**
     * 新增课程
     *
     * @param addDTO 新增课程信息
     * @return 新增的课程信息
     */
    @Override
    public CourseVO addCourse(CourseAddDTO addDTO) {
        // 1. 参数校验
        if (addDTO == null) {
            throw new RuntimeException("课程信息不能为空");
        }

        // 2. 校验教练是否存在且在职
        Coach coach = coachMapper.selectById(addDTO.getCoachId());
        if (coach == null || coach.getIsDelete() == 1) {
            throw new RuntimeException("教练不存在");
        }
        if (coach.getStatus() != 1) {
            throw new RuntimeException("教练已停用");
        }

        // 3. 校验时间
        if (addDTO.getStartTime().isAfter(addDTO.getEndTime())) {
            throw new RuntimeException("课程开始时间不能晚于结束时间");
        }
        if (addDTO.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("课程开始时间不能早于当前时间");
        }

        // 4. 创建课程对象
        Course course = new Course();
        course.setCourseName(addDTO.getCourseName().trim());
        course.setCourseType(addDTO.getCourseType().trim());
        course.setCoachId(addDTO.getCoachId());
        course.setStartTime(addDTO.getStartTime());
        course.setEndTime(addDTO.getEndTime());
        course.setCapacity(addDTO.getCapacity());
        course.setCurrentCount(0);
        course.setDescription(addDTO.getDescription());
        course.setStatus(addDTO.getStatus());

        // 5. 插入数据库
        int result = courseMapper.insert(course);
        if (result <= 0) {
            throw new RuntimeException("新增课程失败");
        }

        // 6. 返回课程信息
        return convertToCourseVO(course);
    }

    /**
     * 更新课程信息
     *
     * @param id        课程ID
     * @param updateDTO 更新信息
     * @return 更新后的课程信息
     */
    @Override
    public CourseVO updateCourse(Long id, CourseUpdateDTO updateDTO) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new RuntimeException("课程ID无效");
        }
        if (updateDTO == null) {
            throw new RuntimeException("更新信息不能为空");
        }

        // 2. 查询课程是否存在
        Course course = courseMapper.selectById(id);
        if (course == null || course.getIsDelete() == 1) {
            throw new RuntimeException("课程不存在");
        }

        // 3. 更新字段
        if (StringUtils.hasText(updateDTO.getCourseName())) {
            course.setCourseName(updateDTO.getCourseName().trim());
        }
        if (StringUtils.hasText(updateDTO.getCourseType())) {
            course.setCourseType(updateDTO.getCourseType().trim());
        }
        if (updateDTO.getCoachId() != null) {
            // 校验教练是否存在且在职
            Coach coach = coachMapper.selectById(updateDTO.getCoachId());
            if (coach == null || coach.getIsDelete() == 1) {
                throw new RuntimeException("教练不存在");
            }
            if (coach.getStatus() != 1) {
                throw new RuntimeException("教练已停用");
            }
            course.setCoachId(updateDTO.getCoachId());
        }
        if (updateDTO.getStartTime() != null) {
            course.setStartTime(updateDTO.getStartTime());
        }
        if (updateDTO.getEndTime() != null) {
            course.setEndTime(updateDTO.getEndTime());
        }
        // 校验时间逻辑
        LocalDateTime startTime = updateDTO.getStartTime() != null ? updateDTO.getStartTime() : course.getStartTime();
        LocalDateTime endTime = updateDTO.getEndTime() != null ? updateDTO.getEndTime() : course.getEndTime();
        if (startTime.isAfter(endTime)) {
            throw new RuntimeException("课程开始时间不能晚于结束时间");
        }
        if (updateDTO.getCapacity() != null) {
            // 校验容量不能小于已预约人数
            if (updateDTO.getCapacity() < course.getCurrentCount()) {
                throw new RuntimeException("课程容量不能小于已预约人数");
            }
            course.setCapacity(updateDTO.getCapacity());
        }
        if (updateDTO.getDescription() != null) {
            course.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getStatus() != null) {
            course.setStatus(updateDTO.getStatus());
        }

        // 4. 更新数据库
        int result = courseMapper.updateById(course);
        if (result <= 0) {
            throw new RuntimeException("更新课程信息失败");
        }

        // 5. 返回更新后的课程信息
        return convertToCourseVO(course);
    }

    /**
     * 删除课程（逻辑删除）
     *
     * @param id 课程ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteCourse(Long id) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new RuntimeException("课程ID无效");
        }

        // 2. 查询课程是否存在
        Course course = courseMapper.selectById(id);
        if (course == null || course.getIsDelete() == 1) {
            throw new RuntimeException("课程不存在");
        }

        // 3. 检查是否有未完成的预约
        QueryWrapper<Booking> bookingWrapper = new QueryWrapper<>();
        bookingWrapper.eq("course_id", id);
        bookingWrapper.eq("status", 1); // 已预约状态
        bookingWrapper.eq("is_delete", 0);
        Long bookingCount = bookingMapper.selectCount(bookingWrapper);
        if (bookingCount > 0) {
            throw new RuntimeException("该课程还有未完成的预约，无法删除");
        }

        // 4. 逻辑删除
        int result = courseMapper.deleteById(id);
        return result > 0;
    }

    /**
     * 修改课程状态（发布/下架）
     *
     * @param id     课程ID
     * @param status 状态
     * @return 更新后的课程信息
     */
    @Override
    public CourseVO updateCourseStatus(Long id, Integer status) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new RuntimeException("课程ID无效");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new RuntimeException("状态值无效");
        }

        // 2. 查询课程是否存在
        Course course = courseMapper.selectById(id);
        if (course == null || course.getIsDelete() == 1) {
            throw new RuntimeException("课程不存在");
        }

        // 3. 更新状态
        course.setStatus(status);
        int result = courseMapper.updateById(course);
        if (result <= 0) {
            throw new RuntimeException("更新课程状态失败");
        }

        // 4. 返回更新后的课程信息
        return convertToCourseVO(course);
    }

    /**
     * 将Course实体类转换为CourseVO
     *
     * @param course 课程实体
     * @return 课程VO
     */
    private CourseVO convertToCourseVO(Course course) {
        if (course == null) {
            return null;
        }
        CourseVO courseVO = new CourseVO();
        BeanUtils.copyProperties(course, courseVO);

        // 计算剩余名额
        courseVO.setRemainingCount(course.getCapacity() - course.getCurrentCount());

        // 判断是否可预约
        boolean bookable = course.getStatus() == 1
                && course.getCurrentCount() < course.getCapacity()
                && course.getStartTime().isAfter(LocalDateTime.now());
        courseVO.setBookable(bookable);

        // 获取教练姓名
        if (course.getCoachId() != null) {
            Coach coach = coachMapper.selectById(course.getCoachId());
            if (coach != null) {
                courseVO.setCoachName(coach.getName());
            }
        }

        return courseVO;
    }
}
