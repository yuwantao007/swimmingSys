import request from '../utils/request'

/**
 * 用户注册
 * @param {Object} data - 注册信息
 * @returns {Promise}
 */
export function register(data) {
  return request({
    url: '/api/v1/users/register',
    method: 'post',
    data
  })
}

/**
 * 用户登录
 * @param {Object} data - 登录信息
 * @returns {Promise}
 */
export function login(data) {
  return request({
    url: '/api/v1/users/login',
    method: 'post',
    data
  })
}

/**
 * 获取当前登录用户信息
 * @returns {Promise}
 */
export function getCurrentUser() {
  return request({
    url: '/api/v1/users/current',
    method: 'get'
  })
}

/**
 * 获取用户列表（分页/条件查询）
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getUserList(params) {
  return request({
    url: '/api/v1/users',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取用户信息
 * @param {Number} id - 用户ID
 * @returns {Promise}
 */
export function getUserById(id) {
  return request({
    url: `/api/v1/users/${id}`,
    method: 'get'
  })
}

/**
 * 新增用户
 * @param {Object} data - 用户信息
 * @returns {Promise}
 */
export function addUser(data) {
  return request({
    url: '/api/v1/users',
    method: 'post',
    data
  })
}

/**
 * 更新用户信息
 * @param {Number} id - 用户ID
 * @param {Object} data - 更新信息
 * @returns {Promise}
 */
export function updateUser(id, data) {
  return request({
    url: `/api/v1/users/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除用户（逻辑删除）
 * @param {Number} id - 用户ID
 * @returns {Promise}
 */
export function deleteUser(id) {
  return request({
    url: `/api/v1/users/${id}`,
    method: 'delete'
  })
}

// ==================== 过期管理API ====================

/**
 * 获取即将过期的用户列表
 * @param {Number} days - 预警天数，默认7天
 * @returns {Promise}
 */
export function getExpiringUsers(days = 7) {
  return request({
    url: '/api/v1/users/expiring',
    method: 'get',
    params: { days }
  })
}

/**
 * 手动检查并处理过期用户
 * @returns {Promise}
 */
export function checkExpiredUsers() {
  return request({
    url: '/api/v1/users/check-expired',
    method: 'post'
  })
}

/**
 * 批量设置用户过期时间
 * @param {Object} data - 批量操作数据
 * @param {Array} data.userIds - 用户ID列表
 * @param {String} data.operationType - 操作类型：set/extend/shorten/clear
 * @param {String} data.expirationTime - 过期时间（set操作使用）
 * @param {Number} data.days - 天数（extend/shorten操作使用）
 * @returns {Promise}
 */
export function batchSetExpiration(data) {
  return request({
    url: '/api/v1/users/batch-expiration',
    method: 'post',
    data
  })
}
