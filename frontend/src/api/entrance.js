import request from '@/utils/request'

/**
 * 生成入场二维码
 * 会员调用，生成一次性入场二维码
 */
export function generateEntranceQrcode() {
  return request({
    url: '/api/v1/entrance/generate',
    method: 'post'
  })
}

/**
 * 验证入场二维码
 * 管理员扫码验证会员入场
 * @param {Object} data - { qrcodeToken: string }
 */
export function verifyEntrance(data) {
  return request({
    url: '/api/v1/entrance/verify',
    method: 'post',
    data
  })
}

/**
 * 获取我的入场记录
 * 会员查看个人入场历史
 * @param {Object} params - { pageNum, pageSize, startTime, endTime }
 */
export function getMyEntranceRecords(params) {
  return request({
    url: '/api/v1/entrance/my',
    method: 'get',
    params
  })
}

/**
 * 获取所有入场记录（管理员）
 * @param {Object} params - { pageNum, pageSize, userId, startTime, endTime }
 */
export function getAllEntranceRecords(params) {
  return request({
    url: '/api/v1/entrance',
    method: 'get',
    params
  })
}

/**
 * 获取指定入场记录详情
 * @param {Number} id - 记录ID
 */
export function getEntranceRecordById(id) {
  return request({
    url: `/api/v1/entrance/${id}`,
    method: 'get'
  })
}

/**
 * 删除入场记录（管理员）
 * @param {Number} id - 记录ID
 */
export function deleteEntranceRecord(id) {
  return request({
    url: `/api/v1/entrance/${id}`,
    method: 'delete'
  })
}
