package com.swimmingsys.service.impl;

import com.swimmingsys.mapper.StatisticsMapper;
import com.swimmingsys.model.vo.*;
import com.swimmingsys.service.StatisticsService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 统计分析服务实现类
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Resource
    private StatisticsMapper statisticsMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // Redis缓存Key前缀
    private static final String CACHE_PREFIX = "statistics:";
    private static final String MEMBER_OVERVIEW_KEY = CACHE_PREFIX + "member:overview";
    private static final String MEMBER_DISTRIBUTION_KEY = CACHE_PREFIX + "member:distribution";
    private static final String MEMBER_TREND_KEY_PREFIX = CACHE_PREFIX + "member:trend:";
    private static final String BOOKING_OVERVIEW_KEY = CACHE_PREFIX + "booking:overview";
    private static final String HOT_COURSES_KEY_PREFIX = CACHE_PREFIX + "booking:hot:top";
    private static final String BOOKING_TREND_KEY_PREFIX = CACHE_PREFIX + "booking:trend:day:";
    private static final String ENTRANCE_OVERVIEW_KEY = CACHE_PREFIX + "entrance:overview";
    private static final String ENTRANCE_TREND_KEY_PREFIX = CACHE_PREFIX + "entrance:trend:day:";
    private static final String HOURLY_DISTRIBUTION_KEY = CACHE_PREFIX + "entrance:hourly:distribution";
    private static final String DASHBOARD_KEY = CACHE_PREFIX + "dashboard:overview";

    @Override
    public MemberOverviewVO getMemberOverview() {
        // 尝试从缓存获取
        String cacheKey = MEMBER_OVERVIEW_KEY;
        MemberOverviewVO cached = (MemberOverviewVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库
        MemberOverviewVO vo = new MemberOverviewVO();

        // 获取会员总数统计
        Map<String, Object> countStats = statisticsMapper.getMemberCountStats();
        vo.setTotalMembers(getLongValue(countStats.get("totalMembers")));
        vo.setActiveMemberCount(getLongValue(countStats.get("activeMemberCount")));
        vo.setInactiveMemberCount(getLongValue(countStats.get("inactiveMemberCount")));

        // 计算启用率
        if (vo.getTotalMembers() > 0) {
            double rate = vo.getActiveMemberCount() * 100.0 / vo.getTotalMembers();
            vo.setActiveRate(roundToTwo(rate));
        } else {
            vo.setActiveRate(0.0);
        }

        // 获取今日新增会员
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN);
        vo.setTodayNewMembers(statisticsMapper.getNewMemberCount(todayStart, todayEnd));

        // 获取本周新增会员
        LocalDateTime weekStart = LocalDateTime.of(LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1), LocalTime.MIN);
        vo.setWeekNewMembers(statisticsMapper.getNewMemberCount(weekStart, todayEnd));

        // 获取本月新增会员
        LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        vo.setMonthNewMembers(statisticsMapper.getNewMemberCount(monthStart, todayEnd));

        // 存入缓存，过期时间10分钟
        redisTemplate.opsForValue().set(cacheKey, vo, 10, TimeUnit.MINUTES);

        return vo;
    }

    @Override
    public MemberDistributionVO getMemberDistribution() {
        // 尝试从缓存获取
        String cacheKey = MEMBER_DISTRIBUTION_KEY;
        MemberDistributionVO cached = (MemberDistributionVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库
        MemberDistributionVO vo = new MemberDistributionVO();

        // 角色分布
        Map<String, Object> roleStats = statisticsMapper.getRoleDistribution();
        MemberDistributionVO.RoleDistribution roleDistribution = new MemberDistributionVO.RoleDistribution();
        roleDistribution.setAdminCount(getLongValue(roleStats.get("adminCount")));
        roleDistribution.setMemberCount(getLongValue(roleStats.get("memberCount")));
        roleDistribution.setNonMemberCount(getLongValue(roleStats.get("nonMemberCount")));
        vo.setRoleDistribution(roleDistribution);

        // 性别分布（排除未知性别）
        Map<String, Object> genderStats = statisticsMapper.getGenderDistribution();
        MemberDistributionVO.GenderDistribution genderDistribution = new MemberDistributionVO.GenderDistribution();
        Long maleCount = getLongValue(genderStats.get("maleCount"));
        Long femaleCount = getLongValue(genderStats.get("femaleCount"));
        genderDistribution.setMaleCount(maleCount);
        genderDistribution.setFemaleCount(femaleCount);

        // 计算性别占比（基数为男性+女性总数）
        Long totalGenderCount = maleCount + femaleCount;
        if (totalGenderCount > 0) {
            double maleRate = maleCount * 100.0 / totalGenderCount;
            double femaleRate = femaleCount * 100.0 / totalGenderCount;
            genderDistribution.setMaleRate(roundToTwo(maleRate));
            genderDistribution.setFemaleRate(roundToTwo(femaleRate));
        } else {
            genderDistribution.setMaleRate(0.0);
            genderDistribution.setFemaleRate(0.0);
        }
        vo.setGenderDistribution(genderDistribution);

        // 存入缓存，过期时间10分钟
        redisTemplate.opsForValue().set(cacheKey, vo, 10, TimeUnit.MINUTES);

        return vo;
    }

    @Override
    public List<TrendDataVO> getMemberTrend(String period, Integer limit) {
        // 默认按月统计，默认返回6条
        if (limit == null || limit <= 0) {
            limit = 6;
        }

        // 尝试从缓存获取
        String cacheKey = MEMBER_TREND_KEY_PREFIX + limit;
        @SuppressWarnings("unchecked")
        List<TrendDataVO> cached = (List<TrendDataVO>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库
        List<TrendDataVO> list = statisticsMapper.getMemberTrendByMonth(limit);

        // 存入缓存，过期时间1小时
        redisTemplate.opsForValue().set(cacheKey, list, 1, TimeUnit.HOURS);

        return list;
    }

    @Override
    public BookingOverviewVO getBookingOverview() {
        // 尝试从缓存获取
        String cacheKey = BOOKING_OVERVIEW_KEY;
        BookingOverviewVO cached = (BookingOverviewVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库
        BookingOverviewVO vo = new BookingOverviewVO();
        Map<String, Object> stats = statisticsMapper.getBookingStats();

        vo.setTotalBookings(getLongValue(stats.get("totalBookings")));
        vo.setActiveBookings(getLongValue(stats.get("activeBookings")));
        vo.setCompletedBookings(getLongValue(stats.get("completedBookings")));
        vo.setCancelledBookings(getLongValue(stats.get("cancelledBookings")));

        // 计算完成率和取消率
        if (vo.getTotalBookings() > 0) {
            double completionRate = vo.getCompletedBookings() * 100.0 / vo.getTotalBookings();
            double cancellationRate = vo.getCancelledBookings() * 100.0 / vo.getTotalBookings();
            vo.setCompletionRate(roundToTwo(completionRate));
            vo.setCancellationRate(roundToTwo(cancellationRate));
        } else {
            vo.setCompletionRate(0.0);
            vo.setCancellationRate(0.0);
        }

        // 存入缓存，过期时间10分钟
        redisTemplate.opsForValue().set(cacheKey, vo, 10, TimeUnit.MINUTES);

        return vo;
    }

    @Override
    public List<HotCourseVO> getHotCourses(Integer limit) {
        // 默认返回10条
        if (limit == null || limit <= 0) {
            limit = 10;
        }

        // 尝试从缓存获取
        String cacheKey = HOT_COURSES_KEY_PREFIX + limit;
        @SuppressWarnings("unchecked")
        List<HotCourseVO> cached = (List<HotCourseVO>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库
        List<HotCourseVO> list = statisticsMapper.getHotCourses(limit);

        // 计算上座率
        for (HotCourseVO vo : list) {
            if (vo.getCapacity() != null && vo.getCapacity() > 0) {
                double rate = vo.getCurrentCount() * 100.0 / vo.getCapacity();
                vo.setOccupancyRate(roundToTwo(rate));
            } else {
                vo.setOccupancyRate(0.0);
            }
        }

        // 存入缓存，过期时间30分钟
        redisTemplate.opsForValue().set(cacheKey, list, 30, TimeUnit.MINUTES);

        return list;
    }

    @Override
    public List<TrendDataVO> getBookingTrend(Integer days) {
        // 默认统计7天
        if (days == null || days <= 0) {
            days = 7;
        }

        // 尝试从缓存获取
        String cacheKey = BOOKING_TREND_KEY_PREFIX + days;
        @SuppressWarnings("unchecked")
        List<TrendDataVO> cached = (List<TrendDataVO>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库
        List<TrendDataVO> list = statisticsMapper.getBookingTrendByDay(days);

        // 存入缓存，过期时间1小时
        redisTemplate.opsForValue().set(cacheKey, list, 1, TimeUnit.HOURS);

        return list;
    }

    @Override
    public EntranceOverviewVO getEntranceOverview() {
        // 尝试从缓存获取
        String cacheKey = ENTRANCE_OVERVIEW_KEY;
        EntranceOverviewVO cached = (EntranceOverviewVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库
        EntranceOverviewVO vo = new EntranceOverviewVO();

        // 总入场次数
        vo.setTotalEntrances(statisticsMapper.getTotalEntranceCount());

        // 今日入场次数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN);
        vo.setTodayEntrances(statisticsMapper.getEntranceCount(todayStart, todayEnd));

        // 本周入场次数
        LocalDateTime weekStart = LocalDateTime.of(LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1), LocalTime.MIN);
        vo.setWeekEntrances(statisticsMapper.getEntranceCount(weekStart, todayEnd));

        // 本月入场次数
        LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        vo.setMonthEntrances(statisticsMapper.getEntranceCount(monthStart, todayEnd));

        // 存入缓存，过期时间10分钟（周月数据）
        // 今日数据每次都会重新计算，但整体对象会缓存
        redisTemplate.opsForValue().set(cacheKey, vo, 10, TimeUnit.MINUTES);

        return vo;
    }

    @Override
    public List<TrendDataVO> getEntranceTrend(Integer days) {
        // 默认统计30天
        if (days == null || days <= 0) {
            days = 30;
        }

        // 尝试从缓存获取
        String cacheKey = ENTRANCE_TREND_KEY_PREFIX + days;
        @SuppressWarnings("unchecked")
        List<TrendDataVO> cached = (List<TrendDataVO>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库
        List<TrendDataVO> list = statisticsMapper.getEntranceTrendByDay(days);

        // 存入缓存，过期时间1小时
        redisTemplate.opsForValue().set(cacheKey, list, 1, TimeUnit.HOURS);

        return list;
    }

    @Override
    public List<HourlyDistributionVO> getHourlyDistribution() {
        // 尝试从缓存获取
        String cacheKey = HOURLY_DISTRIBUTION_KEY;
        @SuppressWarnings("unchecked")
        List<HourlyDistributionVO> cached = (List<HourlyDistributionVO>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库（统计近30天）
        List<HourlyDistributionVO> list = statisticsMapper.getHourlyDistribution(30);

        // 存入缓存，过期时间1小时
        redisTemplate.opsForValue().set(cacheKey, list, 1, TimeUnit.HOURS);

        return list;
    }

    @Override
    public DashboardVO getDashboard() {
        // 尝试从缓存获取
        String cacheKey = DASHBOARD_KEY;
        DashboardVO cached = (DashboardVO) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库
        DashboardVO vo = new DashboardVO();

        // 会员总数
        Map<String, Object> countStats = statisticsMapper.getMemberCountStats();
        vo.setMemberCount(getLongValue(countStats.get("totalMembers")));

        // 今日新增会员
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.MIN);
        Long todayNewMembers = statisticsMapper.getNewMemberCount(todayStart, todayEnd);
        vo.setTodayNewMembers(todayNewMembers);

        // 计算今日新增会员增长率（对比昨日）
        Long yesterdayNewMembers = statisticsMapper.getYesterdayNewMemberCount();
        vo.setTodayNewMembersGrowth(calculateGrowthRate(todayNewMembers, yesterdayNewMembers));

        // 本月预约数
        Long monthBookings = statisticsMapper.getMonthBookingCount();
        vo.setMonthBookings(monthBookings);

        // 计算本月预约数增长率（对比上月）
        Long lastMonthBookings = statisticsMapper.getLastMonthBookingCount();
        vo.setMonthBookingsGrowth(calculateGrowthRate(monthBookings, lastMonthBookings));

        // 今日入场人次
        Long todayEntrances = statisticsMapper.getEntranceCount(todayStart, todayEnd);
        vo.setTodayEntrances(todayEntrances);

        // 计算今日入场人次增长率（对比昨日）
        Long yesterdayEntrances = statisticsMapper.getYesterdayEntranceCount();
        vo.setTodayEntrancesGrowth(calculateGrowthRate(todayEntrances, yesterdayEntrances));

        // 课程总数
        vo.setCourseCount(statisticsMapper.getCourseCount());

        // 教练总数
        vo.setCoachCount(statisticsMapper.getCoachCount());

        // 存入缓存，过期时间5分钟（高频访问页面）
        redisTemplate.opsForValue().set(cacheKey, vo, 5, TimeUnit.MINUTES);

        return vo;
    }

    @Override
    public void clearMemberCache() {
        redisTemplate.delete(MEMBER_OVERVIEW_KEY);
        redisTemplate.delete(MEMBER_DISTRIBUTION_KEY);
        // 清除会员趋势缓存（支持多个limit参数）
        redisTemplate.delete(MEMBER_TREND_KEY_PREFIX + "6");
        redisTemplate.delete(MEMBER_TREND_KEY_PREFIX + "12");
    }

    @Override
    public void clearBookingCache() {
        redisTemplate.delete(BOOKING_OVERVIEW_KEY);
        // 清除热门课程缓存
        redisTemplate.delete(HOT_COURSES_KEY_PREFIX + "10");
        redisTemplate.delete(HOT_COURSES_KEY_PREFIX + "20");
        // 清除预约趋势缓存
        redisTemplate.delete(BOOKING_TREND_KEY_PREFIX + "7");
        redisTemplate.delete(BOOKING_TREND_KEY_PREFIX + "30");
    }

    @Override
    public void clearEntranceCache() {
        redisTemplate.delete(ENTRANCE_OVERVIEW_KEY);
        // 清除入场趋势缓存
        redisTemplate.delete(ENTRANCE_TREND_KEY_PREFIX + "7");
        redisTemplate.delete(ENTRANCE_TREND_KEY_PREFIX + "30");
        redisTemplate.delete(HOURLY_DISTRIBUTION_KEY);
    }

    @Override
    public void clearDashboardCache() {
        redisTemplate.delete(DASHBOARD_KEY);
    }

    /**
     * 将Object转换为Long类型
     * 
     * @param value 值
     * @return Long值
     */
    private Long getLongValue(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).longValue();
        }
        return Long.parseLong(value.toString());
    }

    /**
     * 四舍五入保留两位小数
     * 
     * @param value 值
     * @return 保留两位小数的值
     */
    private Double roundToTwo(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * 计算增长率（带符号）
     * 
     * @param current 当前值
     * @param previous 对比值
     * @return 增长率字符串，如 "+25.5%" 或 "-10.0%"
     */
    private String calculateGrowthRate(Long current, Long previous) {
        if (previous == null || previous == 0) {
            if (current != null && current > 0) {
                return "+100.0%";
            }
            return "0.0%";
        }

        if (current == null) {
            current = 0L;
        }

        double rate = (current - previous) * 100.0 / previous;
        double roundedRate = roundToTwo(rate);

        if (roundedRate > 0) {
            return "+" + roundedRate + "%";
        } else if (roundedRate < 0) {
            return roundedRate + "%";
        } else {
            return "0.0%";
        }
    }
}
