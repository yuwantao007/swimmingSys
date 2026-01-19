package com.swimmingsys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.swimmingsys.common.RoleConstant;
import com.swimmingsys.mapper.BookingMapper;
import com.swimmingsys.mapper.EntranceQrcodeMapper;
import com.swimmingsys.mapper.EntranceRecordMapper;
import com.swimmingsys.model.dto.EntranceRecordQueryDTO;
import com.swimmingsys.model.dto.EntranceVerifyDTO;
import com.swimmingsys.model.entity.Booking;
import com.swimmingsys.model.entity.EntranceQrcode;
import com.swimmingsys.model.entity.EntranceRecord;
import com.swimmingsys.model.entity.User;
import com.swimmingsys.model.vo.EntranceQrcodeVO;
import com.swimmingsys.model.vo.EntranceRecordVO;
import com.swimmingsys.model.vo.EntranceVerifyResultVO;
import com.swimmingsys.service.EntranceService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 入场服务实现类
 */
@Service
public class EntranceServiceImpl implements EntranceService {

    @Resource
    private EntranceQrcodeMapper entranceQrcodeMapper;

    @Resource
    private EntranceRecordMapper entranceRecordMapper;

    @Resource
    private BookingMapper bookingMapper;

    @Resource
    private com.swimmingsys.mapper.CourseMapper courseMapper;

    @Resource
    private com.swimmingsys.mapper.CoachMapper coachMapper;

    @Resource
    private com.swimmingsys.service.StatisticsService statisticsService;

    /**
     * 生成入场二维码
     *
     * @param loginUser 当前登录用户（会员）
     * @return 二维码信息
     */
    @Override
    public EntranceQrcodeVO generateEntranceQrcode(User loginUser) {
        // 1. 校验用户角色
        if (loginUser.getRole() != RoleConstant.MEMBER) {
            throw new RuntimeException("仅会员可生成入场码");
        }

        // 2. 校验用户状态
        if (loginUser.getStatus() != 1) {
            throw new RuntimeException("会员已停用，无法生成入场码");
        }

        // 3. 生成唯一令牌
        String qrcodeToken = generateUniqueToken(loginUser.getId());

        // 4. 查询是否有即将开始的课程预约（2小时内的预约）
        // 由于booking表没有课程时间冗余字段，需要联合查询course表
        QueryWrapper<Booking> bookingWrapper = new QueryWrapper<>();
        bookingWrapper.eq("user_id", loginUser.getId());
        bookingWrapper.eq("status", 1); // 已预约状态
        bookingWrapper.eq("is_delete", 0);
        java.util.List<Booking> userBookings = bookingMapper.selectList(bookingWrapper);
        
        Booking upcomingBooking = null;
        com.swimmingsys.model.entity.Course upcomingCourse = null;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoHoursLater = now.plusHours(2);
        
        // 遍历预约，找到最近即将开始的课程
        for (Booking booking : userBookings) {
            com.swimmingsys.model.entity.Course course = courseMapper.selectById(booking.getCourseId());
            if (course != null && course.getIsDelete() == 0) {
                LocalDateTime courseStartTime = course.getStartTime();
                // 课程在2小时内开始
                if (courseStartTime.isAfter(now) && courseStartTime.isBefore(twoHoursLater)) {
                    if (upcomingBooking == null || courseStartTime.isBefore(upcomingCourse.getStartTime())) {
                        upcomingBooking = booking;
                        upcomingCourse = course;
                    }
                }
            }
        }

        // 5. 创建二维码记录
        EntranceQrcode qrcode = new EntranceQrcode();
        qrcode.setUserId(loginUser.getId());
        qrcode.setUserName(loginUser.getUserName());
        qrcode.setQrcodeToken(qrcodeToken);
        qrcode.setGeneratedTime(LocalDateTime.now());
        qrcode.setIsUsed(0);

        // 6. 如果有课程预约，附加预约信息
        if (upcomingBooking != null && upcomingCourse != null) {
            qrcode.setBookingId(upcomingBooking.getId());
            qrcode.setCourseName(upcomingCourse.getCourseName());
            qrcode.setCourseStartTime(upcomingCourse.getStartTime());
            
            // 查询教练信息
            if (upcomingCourse.getCoachId() != null) {
                com.swimmingsys.model.entity.Coach coach = coachMapper.selectById(upcomingCourse.getCoachId());
                if (coach != null) {
                    qrcode.setCoachName(coach.getName());
                }
            }
        }

        // 7. 保存到数据库
        int result = entranceQrcodeMapper.insert(qrcode);
        if (result <= 0) {
            throw new RuntimeException("生成入场码失败");
        }

        // 8. 构建返回结果
        EntranceQrcodeVO qrcodeVO = new EntranceQrcodeVO();
        qrcodeVO.setQrcodeToken(qrcodeToken);
        qrcodeVO.setUserId(loginUser.getId());
        qrcodeVO.setUserName(loginUser.getUserName());
        qrcodeVO.setGeneratedTime(qrcode.getGeneratedTime());

        // 9. 附加课程预约信息
        if (upcomingBooking != null && qrcode.getCourseName() != null) {
            EntranceQrcodeVO.CourseBookingInfo bookingInfo = new EntranceQrcodeVO.CourseBookingInfo();
            bookingInfo.setBookingId(upcomingBooking.getId());
            bookingInfo.setCourseName(qrcode.getCourseName());
            bookingInfo.setStartTime(qrcode.getCourseStartTime());
            bookingInfo.setCoachName(qrcode.getCoachName());
            qrcodeVO.setCourseBooking(bookingInfo);
        }

        return qrcodeVO;
    }

    /**
     * 验证入场二维码
     *
     * @param verifyDTO 验证请求
     * @param verifier  验证人（管理员）
     * @return 验证结果
     */
    @Override
    @Transactional
    public EntranceVerifyResultVO verifyEntrance(EntranceVerifyDTO verifyDTO, User verifier) {
        // 1. 查询二维码记录
        QueryWrapper<EntranceQrcode> qrcodeWrapper = new QueryWrapper<>();
        qrcodeWrapper.eq("qrcode_token", verifyDTO.getQrcodeToken());
        qrcodeWrapper.eq("is_delete", 0);
        EntranceQrcode qrcode = entranceQrcodeMapper.selectOne(qrcodeWrapper);

        if (qrcode == null) {
            throw new RuntimeException("无效的入场码");
        }

        // 2. 检查是否已使用
        if (qrcode.getIsUsed() == 1) {
            throw new RuntimeException("该入场码已使用，请重新生成");
        }

        // 3. 生成入场记录
        EntranceRecord record = new EntranceRecord();
        record.setUserId(qrcode.getUserId());
        record.setUserName(qrcode.getUserName());
        record.setQrcodeToken(qrcode.getQrcodeToken());
        record.setEntranceTime(LocalDateTime.now());
        record.setVerifierId(verifier.getId());
        record.setVerifierName(verifier.getUserName());

        if (qrcode.getBookingId() != null) {
            record.setBookingId(qrcode.getBookingId());
            record.setCourseName(qrcode.getCourseName());
        }

        int recordResult = entranceRecordMapper.insert(record);
        if (recordResult <= 0) {
            throw new RuntimeException("生成入场记录失败");
        }

        // 4. 标记二维码为已使用
        qrcode.setIsUsed(1);
        qrcode.setUsedTime(LocalDateTime.now());
        int updateResult = entranceQrcodeMapper.updateById(qrcode);
        if (updateResult <= 0) {
            throw new RuntimeException("更新二维码状态失败");
        }

        // 清除入场统计缓存和运营概览缓存
        try {
            statisticsService.clearEntranceCache();
            statisticsService.clearDashboardCache();
        } catch (Exception e) {
            // 缓存清除失败不影响主业务
        }

        // 5. 构建返回结果
        EntranceVerifyResultVO resultVO = new EntranceVerifyResultVO();
        resultVO.setUserName(qrcode.getUserName());
        resultVO.setEntranceTime(record.getEntranceTime());

        // 6. 如果有课程预约信息，附加提示
        if (qrcode.getBookingId() != null) {
            EntranceVerifyResultVO.CourseInfo courseInfo = new EntranceVerifyResultVO.CourseInfo();
            courseInfo.setCourseName(qrcode.getCourseName());
            courseInfo.setStartTime(qrcode.getCourseStartTime());
            courseInfo.setCoachName(qrcode.getCoachName());
            courseInfo.setTip("该会员有课程预约，请提醒上课时间");
            resultVO.setCourseInfo(courseInfo);
        }

        return resultVO;
    }

    /**
     * 获取我的入场记录（会员）
     *
     * @param queryDTO  查询条件
     * @param loginUser 当前登录用户
     * @return 入场记录分页列表
     */
    @Override
    public IPage<EntranceRecordVO> getMyEntranceRecords(EntranceRecordQueryDTO queryDTO, User loginUser) {
        // 1. 构建查询条件
        QueryWrapper<EntranceRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", loginUser.getId());

        // 时间范围查询
        if (queryDTO.getStartTime() != null) {
            queryWrapper.ge("entrance_time", queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            queryWrapper.le("entrance_time", queryDTO.getEndTime());
        }

        // 排除已删除
        queryWrapper.eq("is_delete", 0);
        // 按入场时间倒序
        queryWrapper.orderByDesc("entrance_time");

        // 2. 执行分页查询
        Page<EntranceRecord> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<EntranceRecord> recordPage = entranceRecordMapper.selectPage(page, queryWrapper);

        // 3. 转换为VO分页
        return recordPage.convert(this::convertToEntranceRecordVO);
    }

    /**
     * 获取所有入场记录（管理员）
     *
     * @param queryDTO 查询条件
     * @return 入场记录分页列表
     */
    @Override
    public IPage<EntranceRecordVO> getAllEntranceRecords(EntranceRecordQueryDTO queryDTO) {
        // 1. 构建查询条件
        QueryWrapper<EntranceRecord> queryWrapper = new QueryWrapper<>();

        // 用户ID筛选
        if (queryDTO.getUserId() != null) {
            queryWrapper.eq("user_id", queryDTO.getUserId());
        }

        // 时间范围查询
        if (queryDTO.getStartTime() != null) {
            queryWrapper.ge("entrance_time", queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            queryWrapper.le("entrance_time", queryDTO.getEndTime());
        }

        // 排除已删除
        queryWrapper.eq("is_delete", 0);
        // 按入场时间倒序
        queryWrapper.orderByDesc("entrance_time");

        // 2. 执行分页查询
        Page<EntranceRecord> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        IPage<EntranceRecord> recordPage = entranceRecordMapper.selectPage(page, queryWrapper);

        // 3. 转换为VO分页
        return recordPage.convert(this::convertToEntranceRecordVO);
    }

    /**
     * 根据ID获取入场记录详情
     *
     * @param id 记录ID
     * @return 入场记录信息
     */
    @Override
    public EntranceRecordVO getEntranceRecordById(Long id) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new RuntimeException("记录ID无效");
        }

        // 2. 查询记录
        EntranceRecord record = entranceRecordMapper.selectById(id);
        if (record == null || record.getIsDelete() == 1) {
            throw new RuntimeException("入场记录不存在");
        }

        // 3. 转换并返回
        return convertToEntranceRecordVO(record);
    }

    /**
     * 删除入场记录（管理员）
     * 逻辑删除
     *
     * @param id 记录ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteEntranceRecord(Long id) {
        // 1. 参数校验
        if (id == null || id <= 0) {
            throw new RuntimeException("记录ID无效");
        }

        // 2. 查询记录
        EntranceRecord record = entranceRecordMapper.selectById(id);
        if (record == null || record.getIsDelete() == 1) {
            throw new RuntimeException("入场记录不存在");
        }

        // 3. 逻辑删除
        record.setIsDelete(1);
        int result = entranceRecordMapper.updateById(record);
        return result > 0;
    }

    /**
     * 生成唯一令牌
     *
     * @param userId 用户ID
     * @return 令牌字符串
     */
    private String generateUniqueToken(Long userId) {
        return UUID.randomUUID().toString().replace("-", "")
                + "_" + System.currentTimeMillis()
                + "_" + userId;
    }

    /**
     * 将EntranceRecord实体转换为VO
     *
     * @param record 入场记录实体
     * @return 入场记录VO
     */
    private EntranceRecordVO convertToEntranceRecordVO(EntranceRecord record) {
        if (record == null) {
            return null;
        }
        EntranceRecordVO vo = new EntranceRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }
}
