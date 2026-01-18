/**
 * 教练管理API接口
 */
import request from '@/utils/request'

/**
 * 获取教练列表（分页查询）
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页大小
 * @param {string} params.name - 姓名（模糊查询）
 * @param {string} params.phone - 电话（模糊查询）
 * @param {string} params.specialty - 擅长项目（模糊查询）
 * @param {number} params.status - 状态（0-停用，1-在职）
 * @returns {Promise} 教练分页列表
 */
export function getCoachList(params) {
  return request({
    url: '/api/v1/coaches',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取教练详情
 * @param {number} id - 教练ID
 * @returns {Promise} 教练信息
 */
export function getCoachById(id) {
  return request({
    url: `/api/v1/coaches/${id}`,
    method: 'get'
  })
}

/**
 * 新增教练
 * @param {Object} data - 教练信息
 * @param {string} data.name - 教练姓名
 * @param {string} data.phone - 联系电话
 * @param {number} data.gender - 性别
 * @param {string} data.avatar - 头像
 * @param {string} data.specialty - 擅长项目
 * @param {string} data.description - 简介
 * @param {number} data.status - 状态
 * @returns {Promise} 新增的教练信息
 */
export function addCoach(data) {
  return request({
    url: '/api/v1/coaches',
    method: 'post',
    data
  })
}

/**
 * 更新教练信息
 * @param {number} id - 教练ID
 * @param {Object} data - 更新的教练信息
 * @returns {Promise} 更新后的教练信息
 */
export function updateCoach(id, data) {
  return request({
    url: `/api/v1/coaches/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除教练（逻辑删除）
 * @param {number} id - 教练ID
 * @returns {Promise} 删除结果
 */
export function deleteCoach(id) {
  return request({
    url: `/api/v1/coaches/${id}`,
    method: 'delete'
  })
}

/**
 * 获取在职教练列表（用于下拉选择）
 * @returns {Promise} 在职教练列表
 */
export function getActiveCoachList() {
  return request({
    url: '/api/v1/coaches/active',
    method: 'get'
  })
}
