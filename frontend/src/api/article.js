import request from '@/utils/request'

/**
 * 文章资料相关API
 */

// 获取资料列表（管理员）
export function getArticleList(params) {
  return request({
    url: '/api/v1/articles',
    method: 'get',
    params
  })
}

// 获取资料详情
export function getArticleDetail(id) {
  return request({
    url: `/api/v1/articles/${id}`,
    method: 'get'
  })
}

// 创建资料
export function createArticle(data) {
  return request({
    url: '/api/v1/articles/upload',
    method: 'post',
    data
  })
}

// 更新资料
export function updateArticle(id, data) {
  return request({
    url: `/api/v1/articles/${id}`,
    method: 'put',
    data
  })
}

// 删除资料
export function deleteArticle(id) {
  return request({
    url: `/api/v1/articles/${id}`,
    method: 'delete'
  })
}

// 发布资料
export function publishArticle(id) {
  return request({
    url: `/api/v1/articles/${id}/publish`,
    method: 'put'
  })
}

// 下架资料
export function unpublishArticle(id) {
  return request({
    url: `/api/v1/articles/${id}/unpublish`,
    method: 'put'
  })
}

// 获取公开资料列表
export function getPublicArticleList(params) {
  return request({
    url: '/api/v1/articles/public',
    method: 'get',
    params
  })
}

// 增加浏览次数
export function increaseViewCount(id) {
  return request({
    url: `/api/v1/articles/${id}/view`,
    method: 'put'
  })
}

// 点赞/取消点赞
export function toggleLike(id) {
  return request({
    url: `/api/v1/articles/${id}/like`,
    method: 'put'
  })
}

// 上传文件
export function uploadFile(data) {
  return request({
    url: '/api/v1/articles/files/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}