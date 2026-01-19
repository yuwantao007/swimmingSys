package com.swimmingsys.controller;

import com.swimmingsys.common.Result;
import com.swimmingsys.common.RoleConstant;
import com.swimmingsys.common.annotation.AuthCheck;
import com.swimmingsys.model.vo.*;
import com.swimmingsys.service.StatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 统计分析控制器
 */
@Api(tags = "统计分析")
@RestController
@RequestMapping("/api/v1/statistics")
@Validated
public class StatisticsController {

    @Resource
    private StatisticsService statisticsService;

    /**
     * 获取会员总览统计
     * 权限：仅管理员
     * 
     * @return 会员总览统计数据
     */
    @ApiOperation("获取会员总览统计")
    @GetMapping("/member/overview")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<MemberOverviewVO> getMemberOverview() {
        try {
            MemberOverviewVO vo = statisticsService.getMemberOverview();
            return Result.success("查询成功", vo);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取会员分布统计
     * 权限：仅管理员
     * 
     * @return 会员分布统计数据
     */
    @ApiOperation("获取会员分布统计")
    @GetMapping("/member/distribution")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<MemberDistributionVO> getMemberDistribution() {
        try {
            MemberDistributionVO vo = statisticsService.getMemberDistribution();
            return Result.success("查询成功", vo);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取会员增长趋势
     * 权限：仅管理员
     * 
     * @param period 统计周期（当前仅支持month）
     * @param limit 返回条数（默认6）
     * @return 趋势数据列表
     */
    @ApiOperation("获取会员增长趋势")
    @GetMapping("/member/trend")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<List<TrendDataVO>> getMemberTrend(
            @ApiParam("统计周期") @RequestParam(defaultValue = "month") String period,
            @ApiParam("返回条数") @RequestParam(defaultValue = "6") Integer limit) {
        try {
            List<TrendDataVO> list = statisticsService.getMemberTrend(period, limit);
            return Result.success("查询成功", list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取预约总览统计
     * 权限：仅管理员
     * 
     * @return 预约总览统计数据
     */
    @ApiOperation("获取预约总览统计")
    @GetMapping("/booking/overview")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<BookingOverviewVO> getBookingOverview() {
        try {
            BookingOverviewVO vo = statisticsService.getBookingOverview();
            return Result.success("查询成功", vo);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取热门课程排行
     * 权限：仅管理员
     * 
     * @param limit 返回条数（默认10）
     * @return 热门课程列表
     */
    @ApiOperation("获取热门课程排行")
    @GetMapping("/booking/hot-courses")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<List<HotCourseVO>> getHotCourses(
            @ApiParam("返回条数") @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<HotCourseVO> list = statisticsService.getHotCourses(limit);
            return Result.success("查询成功", list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取预约趋势统计
     * 权限：仅管理员
     * 
     * @param days 统计天数（默认7天）
     * @return 趋势数据列表
     */
    @ApiOperation("获取预约趋势统计")
    @GetMapping("/booking/trend")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<List<TrendDataVO>> getBookingTrend(
            @ApiParam("统计天数") @RequestParam(defaultValue = "7") Integer days) {
        try {
            List<TrendDataVO> list = statisticsService.getBookingTrend(days);
            return Result.success("查询成功", list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取入场总览统计
     * 权限：仅管理员
     * 
     * @return 入场总览统计数据
     */
    @ApiOperation("获取入场总览统计")
    @GetMapping("/entrance/overview")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<EntranceOverviewVO> getEntranceOverview() {
        try {
            EntranceOverviewVO vo = statisticsService.getEntranceOverview();
            return Result.success("查询成功", vo);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取入场趋势统计
     * 权限：仅管理员
     * 
     * @param days 统计天数（默认30天）
     * @return 趋势数据列表
     */
    @ApiOperation("获取入场趋势统计")
    @GetMapping("/entrance/trend")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<List<TrendDataVO>> getEntranceTrend(
            @ApiParam("统计天数") @RequestParam(defaultValue = "30") Integer days) {
        try {
            List<TrendDataVO> list = statisticsService.getEntranceTrend(days);
            return Result.success("查询成功", list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取入场时段分布统计
     * 权限：仅管理员
     * 
     * @return 时段分布列表
     */
    @ApiOperation("获取入场时段分布统计")
    @GetMapping("/entrance/hourly")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<List<HourlyDistributionVO>> getHourlyDistribution() {
        try {
            List<HourlyDistributionVO> list = statisticsService.getHourlyDistribution();
            return Result.success("查询成功", list);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取运营情况概览
     * 权限：仅管理员
     * 
     * @return 运营情况概览数据
     */
    @ApiOperation("获取运营情况概览")
    @GetMapping("/dashboard")
    @AuthCheck(mustRole = RoleConstant.ADMIN)
    public Result<DashboardVO> getDashboard() {
        try {
            DashboardVO vo = statisticsService.getDashboard();
            return Result.success("查询成功", vo);
        } catch (Exception e) {
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
