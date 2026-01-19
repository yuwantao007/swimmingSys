package com.swimmingsys.mapper;

import com.swimmingsys.model.vo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 统计分析Mapper接口
 * 用于执行复杂的统计SQL查询
 */
@Mapper
public interface StatisticsMapper {

    /**
     * 统计会员总数（按角色和状态）
     * 
     * @return 包含总数、启用数、停用数的Map
     */
    @Select("SELECT " +
            "COUNT(*) as totalMembers, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as activeMemberCount, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as inactiveMemberCount " +
            "FROM user " +
            "WHERE role = 1 AND is_delete = 0")
    Map<String, Object> getMemberCountStats();

    /**
     * 统计新增会员数（按时间范围）
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 新增会员数
     */
    @Select("SELECT COUNT(*) FROM user " +
            "WHERE role = 1 AND is_delete = 0 " +
            "AND created_time >= #{startTime} AND created_time < #{endTime}")
    Long getNewMemberCount(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计性别分布（排除未知性别）
     * 
     * @return 包含男性、女性数量的Map
     */
    @Select("SELECT " +
            "SUM(CASE WHEN gender = 1 THEN 1 ELSE 0 END) as maleCount, " +
            "SUM(CASE WHEN gender = 2 THEN 1 ELSE 0 END) as femaleCount " +
            "FROM user " +
            "WHERE role = 1 AND is_delete = 0 AND gender IN (1, 2)")
    Map<String, Object> getGenderDistribution();

    /**
     * 统计角色分布
     * 
     * @return 包含各角色数量的Map
     */
    @Select("SELECT " +
            "SUM(CASE WHEN role = 0 THEN 1 ELSE 0 END) as adminCount, " +
            "SUM(CASE WHEN role = 1 THEN 1 ELSE 0 END) as memberCount, " +
            "SUM(CASE WHEN role = 2 THEN 1 ELSE 0 END) as nonMemberCount " +
            "FROM user " +
            "WHERE is_delete = 0")
    Map<String, Object> getRoleDistribution();

    /**
     * 获取会员增长趋势（按月）
     * 
     * @param months 月数
     * @return 趋势数据列表
     */
    @Select("SELECT " +
            "DATE_FORMAT(created_time, '%Y-%m') as period, " +
            "COUNT(*) as count " +
            "FROM user " +
            "WHERE role = 1 AND is_delete = 0 " +
            "AND created_time >= DATE_SUB(NOW(), INTERVAL #{months} MONTH) " +
            "GROUP BY DATE_FORMAT(created_time, '%Y-%m') " +
            "ORDER BY period ASC")
    List<TrendDataVO> getMemberTrendByMonth(@Param("months") Integer months);

    /**
     * 获取预约统计（按状态）
     * 
     * @return 包含各状态预约数的Map
     */
    @Select("SELECT " +
            "COUNT(*) as totalBookings, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as activeBookings, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as completedBookings, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as cancelledBookings " +
            "FROM booking " +
            "WHERE is_delete = 0")
    Map<String, Object> getBookingStats();

    /**
     * 获取热门课程排行
     * 
     * @param limit 返回条数
     * @return 热门课程列表
     */
    @Select("SELECT " +
            "c.id as courseId, " +
            "c.course_name as courseName, " +
            "c.course_type as courseType, " +
            "COUNT(b.id) as bookingCount, " +
            "c.capacity, " +
            "c.current_count as currentCount " +
            "FROM course c " +
            "LEFT JOIN booking b ON c.id = b.course_id AND b.is_delete = 0 " +
            "WHERE c.is_delete = 0 " +
            "GROUP BY c.id, c.course_name, c.course_type, c.capacity, c.current_count " +
            "ORDER BY bookingCount DESC " +
            "LIMIT #{limit}")
    List<HotCourseVO> getHotCourses(@Param("limit") Integer limit);

    /**
     * 获取预约趋势（按天）
     * 
     * @param days 天数
     * @return 趋势数据列表
     */
    @Select("SELECT " +
            "DATE(booking_time) as period, " +
            "COUNT(*) as count " +
            "FROM booking " +
            "WHERE is_delete = 0 AND status IN (1, 2) " +
            "AND booking_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(booking_time) " +
            "ORDER BY period ASC")
    List<TrendDataVO> getBookingTrendByDay(@Param("days") Integer days);

    /**
     * 获取入场统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 入场次数
     */
    @Select("SELECT COUNT(*) FROM entrance_record " +
            "WHERE is_delete = 0 " +
            "AND entrance_time >= #{startTime} AND entrance_time < #{endTime}")
    Long getEntranceCount(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 获取总入场次数
     * 
     * @return 总入场次数
     */
    @Select("SELECT COUNT(*) FROM entrance_record WHERE is_delete = 0")
    Long getTotalEntranceCount();

    /**
     * 获取入场趋势（按天）
     * 
     * @param days 天数
     * @return 趋势数据列表
     */
    @Select("SELECT " +
            "DATE(entrance_time) as period, " +
            "COUNT(*) as count " +
            "FROM entrance_record " +
            "WHERE is_delete = 0 " +
            "AND entrance_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY DATE(entrance_time) " +
            "ORDER BY period ASC")
    List<TrendDataVO> getEntranceTrendByDay(@Param("days") Integer days);

    /**
     * 获取入场时段分布（按小时）
     * 
     * @param days 统计天数
     * @return 时段分布列表
     */
    @Select("SELECT " +
            "HOUR(entrance_time) as hour, " +
            "COUNT(*) as entranceCount " +
            "FROM entrance_record " +
            "WHERE is_delete = 0 " +
            "AND entrance_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "GROUP BY HOUR(entrance_time) " +
            "ORDER BY hour ASC")
    List<HourlyDistributionVO> getHourlyDistribution(@Param("days") Integer days);

    /**
     * 获取课程总数
     * 
     * @return 课程总数
     */
    @Select("SELECT COUNT(*) FROM course WHERE is_delete = 0")
    Long getCourseCount();

    /**
     * 获取教练总数
     * 
     * @return 教练总数
     */
    @Select("SELECT COUNT(*) FROM coach WHERE is_delete = 0")
    Long getCoachCount();

    /**
     * 获取本月预约数
     * 
     * @return 本月预约数
     */
    @Select("SELECT COUNT(*) FROM booking " +
            "WHERE is_delete = 0 AND status IN (1, 2) " +
            "AND booking_time >= DATE_FORMAT(NOW(), '%Y-%m-01 00:00:00')")
    Long getMonthBookingCount();

    /**
     * 获取上月预约数
     * 
     * @return 上月预约数
     */
    @Select("SELECT COUNT(*) FROM booking " +
            "WHERE is_delete = 0 AND status IN (1, 2) " +
            "AND booking_time >= DATE_FORMAT(DATE_SUB(NOW(), INTERVAL 1 MONTH), '%Y-%m-01 00:00:00') " +
            "AND booking_time < DATE_FORMAT(NOW(), '%Y-%m-01 00:00:00')")
    Long getLastMonthBookingCount();

    /**
     * 获取昨日入场数
     * 
     * @return 昨日入场数
     */
    @Select("SELECT COUNT(*) FROM entrance_record " +
            "WHERE is_delete = 0 " +
            "AND DATE(entrance_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    Long getYesterdayEntranceCount();

    /**
     * 获取昨日新增会员数
     * 
     * @return 昨日新增会员数
     */
    @Select("SELECT COUNT(*) FROM user " +
            "WHERE role = 1 AND is_delete = 0 " +
            "AND DATE(created_time) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)")
    Long getYesterdayNewMemberCount();
}
