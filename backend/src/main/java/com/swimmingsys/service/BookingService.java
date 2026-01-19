package com.swimmingsys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.swimmingsys.model.dto.BookingAddDTO;
import com.swimmingsys.model.dto.BookingConfirmDTO;
import com.swimmingsys.model.dto.BookingQueryDTO;
import com.swimmingsys.model.entity.User;
import com.swimmingsys.model.vo.BookingConflictCheckVO;
import com.swimmingsys.model.vo.BookingVO;

/**
 * 预约服务接口
 */
public interface BookingService {

    /**
     * 预约请求（含冲突检测）
     * 检测是否有时间冲突的预约
     *
     * @param dto       预约信息
     * @param loginUser 登录用户
     * @return 冲突检测结果
     */
    BookingConflictCheckVO requestBooking(BookingAddDTO dto, User loginUser);

    /**
     * 确认预约（支持强制替换）
     * 使用乐观锁处理并发问题
     *
     * @param dto       确认预约信息
     * @param loginUser 登录用户
     * @return 预约信息
     */
    BookingVO confirmBooking(BookingConfirmDTO dto, User loginUser);

    /**
     * 取消预约
     *
     * @param id        预约ID
     * @param loginUser 登录用户
     * @return 是否取消成功
     */
    boolean cancelBooking(Long id, User loginUser);

    /**
     * 获取我的预约记录
     *
     * @param queryDTO  查询条件
     * @param loginUser 登录用户
     * @return 预约分页列表
     */
    IPage<BookingVO> getMyBookings(BookingQueryDTO queryDTO, User loginUser);

    /**
     * 获取所有预约记录（管理员）
     *
     * @param queryDTO 查询条件
     * @return 预约分页列表
     */
    IPage<BookingVO> getAllBookings(BookingQueryDTO queryDTO);

    /**
     * 获取指定课程的预约列表
     *
     * @param courseId 课程ID
     * @param queryDTO 查询条件
     * @return 预约分页列表
     */
    IPage<BookingVO> getBookingsByCourse(Long courseId, BookingQueryDTO queryDTO);

    /**
     * 根据ID获取预约详情
     *
     * @param id 预约ID
     * @return 预约信息
     */
    BookingVO getBookingById(Long id);
}
