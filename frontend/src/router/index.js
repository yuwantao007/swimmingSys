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
  }

  // 如果已登录访问访客页面（登录/注册）
  if (to.meta.guest && token) {
    next({ path: '/home' })
    return
  }

  next()
})

export default router