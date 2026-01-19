package com.swimmingsys.service;

import com.swimmingsys.model.vo.*;

import java.util.List;

/**
 * 统计分析服务接口
 */
public interface StatisticsService {

    /**
     * 获取会员总览统计
     * 
     * @return 会员总览统计数据
     */
    MemberOverviewVO getMemberOverview();

    /**
     * 获取会员分布统计（角色、性别）
     * 
     * @return 会员分布统计数据
     */
    MemberDistributionVO getMemberDistribution();

    /**
     * 获取会员增长趋势
     * 
     * @param period 统计周期：month
     * @param limit 返回条数
     * @return 趋势数据列表
     */
    List<TrendDataVO> getMemberTrend(String period, Integer limit);

    /**
     * 获取预约总览统计
     * 
     * @return 预约总览统计数据
     */
    BookingOverviewVO getBookingOverview();

    /**
     * 获取热门课程排行
     * 
     * @param limit TOP N
     * @return 热门课程列表
     */
    List<HotCourseVO> getHotCourses(Integer limit);

    /**
     * 获取预约趋势
     * 
     * @param days 天数
     * @return 趋势数据列表
     */
    List<TrendDataVO> getBookingTrend(Integer days);

    /**
     * 获取入场总览统计
     * 
     * @return 入场总览统计数据
     */
    EntranceOverviewVO getEntranceOverview();

    /**
     * 获取入场趋势
     * 
     * @param days 天数
     * @return 趋势数据列表
     */
    List<TrendDataVO> getEntranceTrend(Integer days);

    /**
     * 获取入场时段分布
     * 
     * @return 时段分布列表
     */
    List<HourlyDistributionVO> getHourlyDistribution();

    /**
     * 获取运营情况概览
     * 
     * @return 运营情况概览数据
     */
    DashboardVO getDashboard();

    /**
     * 清除会员统计缓存
     */
    void clearMemberCache();

    /**
     * 清除预约统计缓存
     */
    void clearBookingCache();

    /**
     * 清除入场统计缓存
     */
    void clearEntranceCache();

    /**
     * 清除运营概览缓存
     */
    void clearDashboardCache();
}
