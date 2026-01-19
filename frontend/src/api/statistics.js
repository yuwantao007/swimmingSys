import request from '@/utils/request'

/**
 * 统计分析API
 */

// ==================== 运营概览 ====================

/**
 * 获取运营情况概览
 */
export function getDashboard() {
  return request({
    url: '/api/v1/statistics/dashboard',
    method: 'get'
  })
}

// ==================== 会员统计 ====================

/**
 * 获取会员总览统计
 */
export function getMemberOverview() {
  return request({
    url: '/api/v1/statistics/member/overview',
    method: 'get'
  })
}

/**
 * 获取会员分布统计
 */
export function getMemberDistribution() {
  return request({
    url: '/api/v1/statistics/member/distribution',
    method: 'get'
  })
}

/**
 * 获取会员增长趋势
 * @param {string} period - 统计周期（默认month）
 * @param {number} limit - 返回条数（默认6）
 */
export function getMemberTrend(period = 'month', limit = 6) {
  return request({
    url: '/api/v1/statistics/member/trend',
    method: 'get',
    params: { period, limit }
  })
}

// ==================== 预约统计 ====================

/**
 * 获取预约总览统计
 */
export function getBookingOverview() {
  return request({
    url: '/api/v1/statistics/booking/overview',
    method: 'get'
  })
}

/**
 * 获取热门课程排行
 * @param {number} limit - 返回条数（默认10）
 */
export function getHotCourses(limit = 10) {
  return request({
    url: '/api/v1/statistics/booking/hot-courses',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取预约趋势统计
 * @param {number} days - 统计天数（默认7）
 */
export function getBookingTrend(days = 7) {
  return request({
    url: '/api/v1/statistics/booking/trend',
    method: 'get',
    params: { days }
  })
}

// ==================== 入场统计 ====================

/**
 * 获取入场总览统计
 */
export function getEntranceOverview() {
  return request({
    url: '/api/v1/statistics/entrance/overview',
    method: 'get'
  })
}

/**
 * 获取入场趋势统计
 * @param {number} days - 统计天数（默认30）
 */
export function getEntranceTrend(days = 30) {
  return request({
    url: '/api/v1/statistics/entrance/trend',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取入场时段分布统计
 */
export function getHourlyDistribution() {
  return request({
    url: '/api/v1/statistics/entrance/hourly',
    method: 'get'
  })
}
