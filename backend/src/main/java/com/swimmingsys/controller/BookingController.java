package com.swimmingsys.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.swimmingsys.common.Result;
import com.swimmingsys.common.RoleConstant;
import com.swimmingsys.common.annotation.AuthCheck;
import com.swimmingsys.model.dto.BookingAddDTO;
import com.swimmingsys.model.dto.BookingConfirmDTO;
import com.swimmingsys.model.dto.BookingQueryDTO;
import com.swimmingsys.model.entity.User;
import com.swimmingsys.model.vo.BookingConflictCheckVO;
import com.swimmingsys.model.vo.BookingVO;
import com.swimmingsys.service.BookingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 预约控制器
 * 提供课程预约相关的REST API接口
 */
@Api(tags = "课程预约")
@RestController
@RequestMapping("/api/v1/bookings")
@Validated
public class BookingController {

    @Resource
    private BookingService bookingService;

    @Resource
    private com.swimmingsys.service.UserService userService;

    /**
     * 预约请求（含冲突检测）
     * 会员可调用，检测是否有时间冲突
     *
     * @param addDTO  预约信息
     * @param request 请求对象（用于获取登录用户）
     * @return 冲突检测结果
     */
    @ApiOperation("预约请求（含冲突检测）")
    @PostMapping("/request")
    @AuthCheck(mustRole = RoleConstant.MEMBER)
    public Result<BookingConflictCheckVO> requestBooking(
            @Valid @RequestBody BookingAddDTO addDTO,
            HttpServletRequest request) {
        // Controller层参数非空判断
        if (addDTO == null) {
            return Result.error("预约信息不能为空");
        }
        try {
            // 通过JWT令牌获取登录用户
            User loginUser = userService.getLoginUser(request);
            BookingConflictCheckVO result = bookingService.requestBooking(addDTO, loginUser);
            return Result.success("检测完成", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 确认预约（支持强制替换）
     * 会员可调用，使用乐观锁处理并发问题
     *
     * @param confirmDTO 确认预约信息
     * @param request    请求对象（用于获取登录用户）
     * @return 预约信息
     */
    @ApiOperation("确认预约（支持强制替换）")
    @PostMapping("/confirm")
    @AuthCheck(mustRole = RoleConstant.MEMBER)
    public Result<BookingVO> confirmBooking(
            @Valid @RequestBody BookingConfirmDTO confirmDTO,
            HttpServletRequest request) {
        // Controller层参数非空判断
        if (confirmDTO == null) {
            return Result.error("确认预约信息不能为空");
        }
        try {
            // 通过JWT令牌获取登录用户
            User loginUser = userService.getLoginUser(request);
            BookingVO bookingVO = bookingService.confirmBooking(confirmDTO, loginUser);
            return Result.success("预约成功", bookingVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消预约
     * 会员可取消自己的预约，管理员可取消任意预约
     *
     * @param id      预约ID
     * @param request 请求对象（用于获取登录用户）
     * @return 是否取消成功
     */
    @ApiOperation("取消预约")
    @DeleteMapping("/{id}")
    @AuthCheck(anyRole = {RoleConstant.ADMIN, RoleConstant.MEMBER})
    public Result<Boolean> cancelBooking(
            @ApiParam(value = "预约ID", required = true) @PathVariable Long id,
            HttpServletRequest request) {
        // Controller层参数非空判断
        if (id == null || id <= 0) {
            return Result.error("预约ID无效");
        }
        try {
            // 通过JWT令牌获取登录用户
            User loginUser = userService.getLoginUser(request);
            boolean result = bookingService.cancelBooking(id, loginUser);
            return Result.success("取消预约成功", result);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取我的预约记录
     * 会员可调用
     *
     * @param queryDTO 查询条件
     * @param request  请求对象（用于获取登录用户）
     * @return 预约分页列表
     */
    @ApiOperation("获取我的预约记录")
    @GetMapping("/my")
    @AuthCheck(mustRole = RoleConstant.MEMBER)
    public Result<IPage<BookingVO>> getMyBookings(
            @Valid BookingQueryDTO queryDTO,
            HttpServletRequest request) {
        // Controller层参数非空判断
        if (queryDTO == null) {
            queryDTO = new BookingQueryDTO();
        }
        try {
            // 通过JWT令牌获取登录用户
            User loginUser = userService.getLoginUser(request);
            IPage<BookingVO> bookingPage = bookingService.getMyBookings(queryDTO, loginUser);
            return Result.success("查询成功", bookingPage);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有预约记录（管理员）
     * 仅管理员可调用
     *
     * @param queryDTO 查询条件
     * @return 预约分页列表
     */
    @ApiOperation("获取所有预约记录（管理员）")
    @GetMapping
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<IPage<BookingVO>> getAllBookings(@Valid BookingQueryDTO queryDTO) {
        // Controller层参数非空判断
        if (queryDTO == null) {
            queryDTO = new BookingQueryDTO();
        }
        try {
            IPage<BookingVO> bookingPage = bookingService.getAllBookings(queryDTO);
            return Result.success("查询成功", bookingPage);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取指定课程的预约列表
     * 仅管理员可调用
     *
     * @param courseId 课程ID
     * @param queryDTO 查询条件
     * @return 预约分页列表
     */
    @ApiOperation("获取指定课程的预约列表")
    @GetMapping("/course/{courseId}")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<IPage<BookingVO>> getBookingsByCourse(
            @ApiParam(value = "课程ID", required = true) @PathVariable Long courseId,
            @Valid BookingQueryDTO queryDTO) {
        // Controller层参数非空判断
        if (courseId == null || courseId <= 0) {
            return Result.error("课程ID无效");
        }
        if (queryDTO == null) {
            queryDTO = new BookingQueryDTO();
        }
        try {
            IPage<BookingVO> bookingPage = bookingService.getBookingsByCourse(courseId, queryDTO);
            return Result.success("查询成功", bookingPage);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取预约详情
     * 管理员和会员可调用
     *
     * @param id 预约ID
     * @return 预约信息
     */
    @ApiOperation("根据ID获取预约详情")
    @GetMapping("/{id}")
    @AuthCheck(anyRole = {RoleConstant.ADMIN, RoleConstant.MEMBER})
    public Result<BookingVO> getBookingById(
            @ApiParam(value = "预约ID", required = true) @PathVariable Long id) {
        // Controller层参数非空判断
        if (id == null || id <= 0) {
            return Result.error("预约ID无效");
        }
        try {
            BookingVO bookingVO = bookingService.getBookingById(id);
            return Result.success("获取成功", bookingVO);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
