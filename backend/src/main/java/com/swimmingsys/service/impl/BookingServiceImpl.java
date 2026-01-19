package com.swimmingsys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swimmingsys.mapper.BookingMapper;
import com.swimmingsys.mapper.CoachMapper;
import com.swimmingsys.mapper.CourseMapper;
import com.swimmingsys.mapper.UserMapper;
import com.swimmingsys.model.dto.BookingAddDTO;
import com.swimmingsys.model.dto.BookingConfirmDTO;
import com.swimmingsys.model.dto.BookingQueryDTO;
import com.swimmingsys.model.entity.Booking;
import com.swimmingsys.model.entity.Coach;
import com.swimmingsys.model.entity.Course;
import com.swimmingsys.model.entity.User;
import com.swimmingsys.model.vo.BookingConflictCheckVO;
import com.swimmingsys.model.vo.BookingVO;
import com.swimmingsys.model.vo.CourseVO;
import com.swimmingsys.service.BookingService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 预约服务实现类
 * 使用乐观锁机制处理并发预约问题
 */
@Service
public class BookingServiceImpl implements BookingService {

    @Resource
    private BookingMapper bookingMapper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private CoachMapper coachMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private com.swimmingsys.service.StatisticsService statisticsService;

    /**
     * 乐观锁更新最大重试次数
     */
    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 预约请求（含冲突检测）
     * 检测用户在同一时间段是否有其他预约
     *
     * @param dto       预约信息
     * @param loginUser 登录用户
     * @return 冲突检测结果
     */
    @Override
    public BookingConflictCheckVO requestBooking(BookingAddDTO dto, User loginUser) {
        // 1. 参数校验
        if (dto == null || dto.getCourseId() == null) {
            throw new RuntimeException("课程ID不能为空");
        }

        // 2. 查询课程信息
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null || course.getIsDelete() == 1) {
            throw new RuntimeException("课程不存在");
        }

        // 3. 校验课程是否可预约
        validateCourseBookable(course);

        // 4. 校验用户是否已预约该课程
        checkDuplicateBooking(loginUser.getId(), course.getId());

        // 5. 检测时间冲突
        BookingConflictCheckVO conflictVO = new BookingConflictCheckVO();
        conflictVO.setRequestedCourse(convertToCourseVO(course));

        Booking conflictBooking = findConflictBooking(loginUser.getId(), course.getStartTime(), course.getEndTime());
        if (conflictBooking != null) {
            conflictVO.setHasConflict(true);
            conflictVO.setConflictBooking(convertToBookingVO(conflictBooking));
        } else {
            conflictVO.setHasConflict(false);
        }

        return conflictVO;
    }

    /**
     * 确认预约（支持强制替换）
     * 使用乐观锁处理并发问题，防止超卖
     *
     * @param dto       确认预约信息
     * @param loginUser 登录用户
     * @return 预约信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingVO confirmBooking(BookingConfirmDTO dto, User loginUser) {
        // 1. 参数校验
        if (dto == null || dto.getCourseId() == null) {
            throw new RuntimeException("课程ID不能为空");
        }

        // 2. 查询课程信息
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null || course.getIsDelete() == 1) {
            throw new RuntimeException("课程不存在");
        }

        // 3. 校验课程是否可预约
        validateCourseBookable(course);

        // 4. 校验用户是否已预约该课程
        checkDuplicateBooking(loginUser.getId(), course.getId());

        // 5. 处理时间冲突
        Booking conflictBooking = findConflictBooking(loginUser.getId(), course.getStartTime(), course.getEndTime());
        if (conflictBooking != null) {
            if (Boolean.TRUE.equals(dto.getForceReplace())) {
                // 强制替换：先取消冲突的预约
                if (dto.getReplaceBookingId() == null) {
                    throw new RuntimeException("强制替换时必须提供要替换的预约ID");
                }
                // 校验要替换的预约ID是否匹配
                if (!conflictBooking.getId().equals(dto.getReplaceBookingId())) {
                    throw new RuntimeException("预约ID不匹配，请重新获取冲突信息");
                }
                // 取消冲突的预约
                cancelBookingInternal(conflictBooking);
            } else {
                throw new RuntimeException("该时间段已有其他预约，请选择强制替换或选择其他时间");
            }
        }

        // 6. 使用乐观锁更新课程人数
        int retryCount = 0;
        while (retryCount < MAX_RETRY_COUNT) {
            // 重新获取最新的课程数据
            course = courseMapper.selectById(dto.getCourseId());
            if (course == null || course.getIsDelete() == 1) {
                throw new RuntimeException("课程不存在");
            }

            // 再次校验课程是否可预约
            validateCourseBookable(course);

            // 更新课程人数（使用乐观锁）
            course.setCurrentCount(course.getCurrentCount() + 1);
            int updateResult = courseMapper.updateById(course);

            if (updateResult > 0) {
                // 乐观锁更新成功，创建预约记录
                Booking booking = new Booking();
                booking.setUserId(loginUser.getId());
                booking.setCourseId(course.getId());
                booking.setBookingTime(LocalDateTime.now());
                booking.setStatus(1); // 已预约

                int insertResult = bookingMapper.insert(booking);
                if (insertResult <= 0) {
                    throw new RuntimeException("创建预约记录失败");
                }

                // 清除预约统计缓存和运营概览缓存
                try {
                    statisticsService.clearBookingCache();
                    statisticsService.clearDashboardCache();
                } catch (Exception e) {
                    // 缓存清除失败不影响主业务
                }

                return convertToBookingVO(booking);
            }

            // 乐观锁更新失败，重试
            retryCount++;
        }

        // 超过最大重试次数
        throw new RuntimeException("预约失败，系统繁忙请稍后重试");
    }

    /**
     * 取消预约
     *
     * @param id        预约ID
     * @param loginUser 登录用户
     * @return 是否取消成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelBooking(Long id, User loginUser) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new RuntimeException("预约ID无效");
        }

        // 2. 查询预约记录
        Booking booking = bookingMapper.selectById(id);
        if (booking == null || booking.getIsDelete() == 1) {
            throw new RuntimeException("预约记录不存在");
        }

        // 3. 权限校验：只能取消自己的预约（管理员可以取消任意预约）
        // role为0表示管理员
        if (!booking.getUserId().equals(loginUser.getId()) && loginUser.getRole() != 0) {
            throw new RuntimeException("无权取消他人的预约");
        }

        // 4. 状态校验
        if (booking.getStatus() != 1) {
            throw new RuntimeException("该预约已取消或已完成，无法再次取消");
        }

        // 5. 取消预约并释放课程名额
        return cancelBookingInternal(booking);
    }

    /**
     * 获取我的预约记录
     *
     * @param queryDTO  查询条件
     * @param loginUser 登录用户
     * @return 预约分页列表
     */
    @Override
    public IPage<BookingVO> getMyBookings(BookingQueryDTO queryDTO, User loginUser) {
        // 强制设置用户ID为当前登录用户
        queryDTO.setUserId(loginUser.getId());
        return getBookingsWithCondition(queryDTO);
    }

    /**
     * 获取所有预约记录（管理员）
     *
     * @param queryDTO 查询条件
     * @return 预约分页列表
     */
    @Override
    public IPage<BookingVO> getAllBookings(BookingQueryDTO queryDTO) {
        return getBookingsWithCondition(queryDTO);
    }

    /**
     * 获取指定课程的预约列表
     *
     * @param courseId 课程ID
     * @param queryDTO 查询条件
     * @return 预约分页列表
     */
    @Override
    public IPage<BookingVO> getBookingsByCourse(Long courseId, BookingQueryDTO queryDTO) {
        // 设置课程ID
        queryDTO.setCourseId(courseId);
        return getBookingsWithCondition(queryDTO);
    }

    /**
     * 根据ID获取预约详情
     *
     * @param id 预约ID
     * @return 预约信息
     */
    @Override
    public BookingVO getBookingById(Long id) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new RuntimeException("预约ID无效");
        }

        // 2. 查询预约记录
        Booking booking = bookingMapper.selectById(id);
        if (booking == null || booking.getIsDelete() == 1) {
            throw new RuntimeException("预约记录不存在");
        }

        // 3. 转换并返回
        return convertToBookingVO(booking);
    }

    /**
     * 根据条件查询预约列表
     *
     * @param queryDTO 查询条件
     * @return 预约分页列表
     */
    private IPage<BookingVO> getBookingsWithCondition(BookingQueryDTO queryDTO) {
        // 1. 构建查询条件
        QueryWrapper<Booking> queryWrapper = new QueryWrapper<>();

        // 用户ID精确查询
        if (queryDTO.getUserId() != null) {
            queryWrapper.eq("user_id", queryDTO.getUserId());
        }
        // 课程ID精确查询
        if (queryDTO.getCourseId() != null) {
            queryWrapper.eq("course_id", queryDTO.getCourseId());
        }
        // 状态精确查询
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq("status", queryDTO.getStatus());
        }
        // 预约时间范围查询
        if (queryDTO.getBookingTimeBegin() != null) {
            queryWrapper.ge("booking_time", queryDTO.getBookingTimeBegin());
        }
        if (queryDTO.getBookingTimeEnd() != null) {
            queryWrapper.le("booking_time", queryDTO.getBookingTimeEnd());
        }
        // 排除已删除
        queryWrapper.eq("is_delete", 0);
        // 按预约时间倒序排序
        queryWrapper.orderByDesc("booking_time");

        // 2. 执行分页查询
        Page<Booking> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<Booking> bookingPage = bookingMapper.selectPage(page, queryWrapper);

        // 3. 转换为VO分页
        return bookingPage.convert(this::convertToBookingVO);
    }

    /**
     * 校验课程是否可预约
     *
     * @param course 课程对象
     */
    private void validateCourseBookable(Course course) {
        // 检查课程状态
        if (course.getStatus() != 1) {
            throw new RuntimeException("该课程已下架，无法预约");
        }
        // 检查课程是否已开始
        if (course.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("该课程已开始，无法预约");
        }
        // 检查课程是否已满
        if (course.getCurrentCount() >= course.getCapacity()) {
            throw new RuntimeException("该课程名额已满，无法预约");
        }
    }

    /**
     * 检查用户是否已预约该课程
     *
     * @param userId   用户ID
     * @param courseId 课程ID
     */
    private void checkDuplicateBooking(Long userId, Long courseId) {
        QueryWrapper<Booking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("course_id", courseId);
        queryWrapper.eq("status", 1); // 已预约状态
        queryWrapper.eq("is_delete", 0);

        Long count = bookingMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new RuntimeException("您已预约该课程，请勿重复预约");
        }
    }

    /**
     * 查找时间冲突的预约
     * 检查用户在指定时间段内是否有其他已预约的课程
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 冲突的预约记录，如果没有冲突返回null
     */
    private Booking findConflictBooking(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        // 查询用户所有已预约状态的预约记录
        QueryWrapper<Booking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("status", 1); // 已预约状态
        queryWrapper.eq("is_delete", 0);

        // 获取用户所有已预约的记录
        java.util.List<Booking> bookings = bookingMapper.selectList(queryWrapper);

        // 检查每个预约对应的课程时间是否与目标时间段冲突
        for (Booking booking : bookings) {
            Course course = courseMapper.selectById(booking.getCourseId());
            if (course != null && course.getIsDelete() == 0) {
                // 时间冲突条件：两个时间段有重叠
                // 冲突 = !(结束时间1 <= 开始时间2 || 开始时间1 >= 结束时间2)
                // 等价于：开始时间1 < 结束时间2 && 结束时间1 > 开始时间2
                if (startTime.isBefore(course.getEndTime()) && endTime.isAfter(course.getStartTime())) {
                    return booking;
                }
            }
        }

        return null;
    }

    /**
     * 内部取消预约方法
     * 取消预约并释放课程名额（使用乐观锁）
     *
     * @param booking 预约记录
     * @return 是否取消成功
     */
    private boolean cancelBookingInternal(Booking booking) {
        // 1. 获取课程信息
        Course course = courseMapper.selectById(booking.getCourseId());
        if (course == null || course.getIsDelete() == 1) {
            throw new RuntimeException("课程不存在");
        }

        // 2. 使用乐观锁更新课程人数
        int retryCount = 0;
        while (retryCount < MAX_RETRY_COUNT) {
            // 重新获取最新的课程数据
            course = courseMapper.selectById(booking.getCourseId());
            if (course == null || course.getIsDelete() == 1) {
                throw new RuntimeException("课程不存在");
            }

            // 减少课程人数
            if (course.getCurrentCount() > 0) {
                course.setCurrentCount(course.getCurrentCount() - 1);
            }

            int updateResult = courseMapper.updateById(course);
            if (updateResult > 0) {
                // 乐观锁更新成功，更新预约状态
                booking.setStatus(0); // 已取消
                booking.setCancelTime(LocalDateTime.now());
                int result = bookingMapper.updateById(booking);

                // 清除预约统计缓存和运营概览缓存
                try {
                    statisticsService.clearBookingCache();
                    statisticsService.clearDashboardCache();
                } catch (Exception e) {
                    // 缓存清除失败不影响主业务
                }

                return result > 0;
            }

            // 乐观锁更新失败，重试
            retryCount++;
        }

        throw new RuntimeException("取消预约失败，系统繁忙请稍后重试");
    }

    /**
     * 将Booking实体转换为BookingVO
     *
     * @param booking 预约实体
     * @return 预约VO
     */
    private BookingVO convertToBookingVO(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingVO bookingVO = new BookingVO();
        BeanUtils.copyProperties(booking, bookingVO);

        // 获取用户名称
        if (booking.getUserId() != null) {
            User user = userMapper.selectById(booking.getUserId());
            if (user != null) {
                bookingVO.setUserName(user.getUserName());
            }
        }

        // 获取课程信息
        if (booking.getCourseId() != null) {
            Course course = courseMapper.selectById(booking.getCourseId());
            if (course != null) {
                bookingVO.setCourseName(course.getCourseName());
                bookingVO.setCourseType(course.getCourseType());
                bookingVO.setCourseStartTime(course.getStartTime());
                bookingVO.setCourseEndTime(course.getEndTime());

                // 获取教练姓名
                if (course.getCoachId() != null) {
                    Coach coach = coachMapper.selectById(course.getCoachId());
                    if (coach != null) {
                        bookingVO.setCoachName(coach.getName());
                    }
                }
            }
        }

        return bookingVO;
    }

    /**
     * 将Course实体转换为CourseVO
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
