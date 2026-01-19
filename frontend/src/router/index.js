import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore, ROLE } from '../store/user'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { title: '用户登录', guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/RegisterView.vue'),
    meta: { title: '用户注册', guest: true }
  },
  {
    path: '/',
    component: () => import('../layout/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('../views/HomeView.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'user/list',
        name: 'UserList',
        component: () => import('../views/UserListView.vue'),
        meta: { title: '用户管理', requiresAdmin: true }
      },
      {
        path: 'user/profile',
        name: 'UserProfile',
        component: () => import('../views/UserProfileView.vue'),
        meta: { title: '个人中心' }
      },
      {
        path: 'coach',
        name: 'CoachList',
        component: () => import('../views/CoachListView.vue'),
        meta: { title: '教练管理', requiresAdmin: true }
      },
      // 课程管理（管理员）
      {
        path: 'course/manage',
        name: 'CourseManage',
        component: () => import('../views/CourseManageView.vue'),
        meta: { title: '课程管理', requiresAdmin: true }
      },
      // 课程列表（所有登录用户可见）
      {
        path: 'course/list',
        name: 'CourseList',
        component: () => import('../views/CourseListView.vue'),
        meta: { title: '课程浏览' }
      },
      // 我的预约（会员）
      {
        path: 'booking/my',
        name: 'MyBookings',
        component: () => import('../views/MyBookingsView.vue'),
        meta: { title: '我的预约', requiresMember: true }
      },
      // 入场管理模块
      // 生成入场码（会员）- 独立入口，支持无预约时也能生成入场码
      {
        path: 'entrance/qrcode',
        name: 'EntranceQrcode',
        component: () => import('../views/entrance/EntranceQrcodeView.vue'),
        meta: { title: '生成入场码', requiresMember: true }
      },
      // 我的入场记录（会员）
      {
        path: 'entrance/my-records',
        name: 'MyEntranceRecords',
        component: () => import('../views/entrance/MyEntranceRecordView.vue'),
        meta: { title: '我的入场记录', requiresMember: true }
      },
      // 扫码验证（管理员）
      {
        path: 'entrance/verify',
        name: 'EntranceVerify',
        component: () => import('../views/entrance/EntranceVerifyView.vue'),
        meta: { title: '扫码验证', requiresAdmin: true }
      },
      // 入场记录管理（管理员）
      {
        path: 'entrance/records',
        name: 'EntranceRecordManage',
        component: () => import('../views/entrance/EntranceRecordManageView.vue'),
        meta: { title: '入场记录管理', requiresAdmin: true }
      },
      // 统计分析模块（管理员）
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('../views/statistics/StatisticsView.vue'),
        meta: { title: '运营概览', requiresAdmin: true }
      },
      {
        path: 'statistics/member',
        name: 'MemberStatistics',
        component: () => import('../views/statistics/MemberStatistics.vue'),
        meta: { title: '会员统计', requiresAdmin: true }
      },
      {
        path: 'statistics/booking',
        name: 'BookingStatistics',
        component: () => import('../views/statistics/BookingStatistics.vue'),
        meta: { title: '预约统计', requiresAdmin: true }
      },
      {
        path: 'statistics/entrance',
        name: 'EntranceStatistics',
        component: () => import('../views/statistics/EntranceStatistics.vue'),
        meta: { title: '入场统计', requiresAdmin: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/home'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  if (to.meta.title) {
    document.title = to.meta.title + ' - 游泳馆管理系统'
  }

  // 获取token
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

  // 如果需要登录验证
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!token) {
      // 未登录，跳转登录页
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
    
    // 如果需要管理员权限
    if (to.meta.requiresAdmin && userInfo.role !== ROLE.ADMIN) {
      // 非管理员，跳转首页
      next({ path: '/home' })
      return
    }
    
    // 如果需要会员权限
    if (to.meta.requiresMember && userInfo.role !== ROLE.MEMBER) {
      // 非会员，跳转首页
      next({ path: '/home' })
      return
    }
  }

  // 如果已登录访问访客页面（登录/注册）
  if (to.meta.guest && token) {
    next({ path: '/home' })
    return
  }

  next()
})

export default router