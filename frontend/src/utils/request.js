import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 创建axios实例
const service = axios.create({
  baseURL: '', // vite.config.js 中已配置代理
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 如果返回的状态码不是200，则判断为错误
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    return res
  },
  error => {
    console.error('响应错误:', error)
    
    // 处理401未授权错误
    if (error.response && error.response.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      // 清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      // 跳转登录页
      router.push('/login')
      return Promise.reject(error)
    }
    
    // 处理403权限不足错误
    if (error.response && error.response.status === 403) {
      ElMessage.error('权限不足')
      return Promise.reject(error)
    }
    
    ElMessage.error(error.message || '网络请求失败')
    return Promise.reject(error)
  }
)

export default service
