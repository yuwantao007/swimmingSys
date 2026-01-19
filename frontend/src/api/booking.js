/**
 * 课程预约API接口
 */
import request from '@/utils/request'

/**
 * 预约请求（含冲突检测）
 * @param {Object} data - 预约信息
 * @param {number} data.courseId - 课程ID
 * @returns {Promise} 冲突检测结果
 */
export function requestBooking(data) {
  return request({
    url: '/api/v1/bookings/request',
    method: 'post',
    data
  })
}

/**
 * 确认预约（支持强制替换）
 * @param {Object} data - 确认预约信息
 * @param {number} data.courseId - 课程ID
 * @param {boolean} data.forceReplace - 是否强制替换
 * @param {number} data.replaceBookingId - 要替换的预约ID
 * @returns {Promise} 预约信息
 */
export function confirmBooking(data) {
  return request({
    url: '/api/v1/bookings/confirm',
    method: 'post',
    data
  })
}

/**
 * 取消预约
 * @param {number} id - 预约ID
 * @returns {Promise} 取消结果
 */
export function cancelBooking(id) {
  return request({
    url: `/api/v1/bookings/${id}`,
    method: 'delete'
  })
}

/**
 * 获取我的预约记录
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页大小
 * @param {number} params.status - 预约状态
 * @returns {Promise} 预约分页列表
 */
export function getMyBookings(params) {
  return request({
    url: '/api/v1/bookings/my',
    method: 'get',
    params
  })
}

/**
 * 获取所有预约记录（管理员）
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页大小
 * @param {number} params.userId - 用户ID
 * @param {number} params.courseId - 课程ID
 * @param {number} params.status - 预约状态
 * @returns {Promise} 预约分页列表
 */
export function getAllBookings(params) {
  return request({
    url: '/api/v1/bookings',
    method: 'get',
    params
  })
}

/**
 * 获取指定课程的预约列表（管理员）
 * @param {number} courseId - 课程ID
 * @param {Object} params - 查询参数
 * @returns {Promise} 预约分页列表
 */
export function getBookingsByCourse(courseId, params) {
  return request({
    url: `/api/v1/bookings/course/${courseId}`,
    method: 'get',
    params
  })
}

/**
 * 根据ID获取预约详情
 * @param {number} id - 预约ID
 * @returns {Promise} 预约信息
 */
export function getBookingById(id) {
  return request({
    url: `/api/v1/bookings/${id}`,
    method: 'get'
  })
}
