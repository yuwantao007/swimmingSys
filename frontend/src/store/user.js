import { defineStore } from 'pinia'

// 角色常量
export const ROLE = {
  ADMIN: 0,      // 管理员
  MEMBER: 1,     // 会员
  NON_MEMBER: 2  // 非会员
}

// 角色名称映射
export const ROLE_NAME = {
  [ROLE.ADMIN]: '管理员',
  [ROLE.MEMBER]: '会员',
  [ROLE.NON_MEMBER]: '非会员'
}

// 状态常量
export const STATUS = {
  DISABLED: 0,  // 禁用
  ENABLED: 1    // 启用
}

// 性别常量
export const GENDER = {
  UNKNOWN: 0,  // 未知
  MALE: 1,     // 男
  FEMALE: 2    // 女
}

export const GENDER_NAME = {
  [GENDER.UNKNOWN]: '未知',
  [GENDER.MALE]: '男',
  [GENDER.FEMALE]: '女'
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),
  
  getters: {
    // 是否已登录
    isLogin: (state) => !!state.token,
    
    // 是否是管理员
    isAdmin: (state) => state.userInfo?.role === ROLE.ADMIN,
    
    // 是否是会员
    isMember: (state) => state.userInfo?.role === ROLE.MEMBER,
    
    // 是否是非会员
    isNonMember: (state) => state.userInfo?.role === ROLE.NON_MEMBER,
    
    // 获取角色名称
    roleName: (state) => ROLE_NAME[state.userInfo?.role] || '未知',
    
    // 获取用户ID
    userId: (state) => state.userInfo?.id
  },
  
  actions: {
    // 设置token
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },
    
    // 设置用户信息
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },
    
    // 登录
    login(token, userInfo) {
      this.setToken(token)
      this.setUserInfo(userInfo)
    },
    
    // 登出
    logout() {
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },
    
    // 更新用户信息
    updateUserInfo(userInfo) {
      this.userInfo = { ...this.userInfo, ...userInfo }
      localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
    }
  }
})
