/**
 * 课程管理API接口
 */
import request from '@/utils/request'

/**
 * 获取课程列表（分页查询）
 * @param {Object} params - 查询参数
 * @param {number} params.pageNum - 页码
 * @param {number} params.pageSize - 每页大小
 * @param {string} params.courseName - 课程名称（模糊查询）
 * @param {string} params.courseType - 课程类型
 * @param {number} params.coachId - 教练ID
 * @param {number} params.status - 状态（0-已下架，1-已发布）
 * @param {boolean} params.bookableOnly - 只显示可预约
 * @returns {Promise} 课程分页列表
 */
export function getCourseList(params) {
  return request({
    url: '/api/v1/courses',
    method: 'get',
    params
  })
}

/**
 * 根据ID获取课程详情
 * @param {number} id - 课程ID
 * @returns {Promise} 课程信息
 */
export function getCourseById(id) {
  return request({
    url: `/api/v1/courses/${id}`,
    method: 'get'
  })
}

/**
 * 新增课程（管理员）
 * @param {Object} data - 课程信息
 * @param {string} data.courseName - 课程名称
 * @param {string} data.courseType - 课程类型
 * @param {number} data.coachId - 教练ID
 * @param {string} data.startTime - 开始时间
 * @param {string} data.endTime - 结束时间
 * @param {number} data.capacity - 课程容量
 * @param {string} data.description - 课程描述
 * @param {number} data.status - 状态
 * @returns {Promise} 新增的课程信息
 */
export function addCourse(data) {
  return request({
    url: '/api/v1/courses',
    method: 'post',
    data
  })
}

/**
 * 更新课程信息（管理员）
 * @param {number} id - 课程ID
 * @param {Object} data - 更新的课程信息
 * @returns {Promise} 更新后的课程信息
 */
export function updateCourse(id, data) {
  return request({
    url: `/api/v1/courses/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除课程（管理员，逻辑删除）
 * @param {number} id - 课程ID
 * @returns {Promise} 删除结果
 */
export function deleteCourse(id) {
  return request({
    url: `/api/v1/courses/${id}`,
    method: 'delete'
  })
}

/**
 * 修改课程状态（管理员，发布/下架）
 * @param {number} id - 课程ID
 * @param {number} status - 状态（0-下架，1-发布）
 * @returns {Promise} 更新后的课程信息
 */
export function updateCourseStatus(id, status) {
  return request({
    url: `/api/v1/courses/${id}/status`,
    method: 'put',
    params: { status }
  })
}
