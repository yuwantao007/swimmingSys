<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="logo">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <circle cx="15" cy="4" r="2" />
            <path d="M2 14c1.5 0 2.5 1 4 1s2.5-1 4-1 2.5 1 4 1 2.5-1 4-1 2.5 1 4 1v-2c-1.5 0-2.5-1-4-1s-2.5 1-4 1-2.5-1-4-1-2.5 1-4 1-2.5-1-4-1v2z"/>
          </svg>
        </div>
        <span v-show="!isCollapsed" class="logo-text">游泳馆管理</span>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        router
        background-color="#1A6B9F"
        text-color="#fff"
        active-text-color="#2AA9E0"
      >
        <el-menu-item index="/home">
          <el-icon><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>
        
        <el-menu-item v-if="userStore.isAdmin" index="/user/list">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
        
        <el-menu-item v-if="userStore.isAdmin" index="/coach">
          <el-icon><Avatar /></el-icon>
          <template #title>教练管理</template>
        </el-menu-item>
        
        <!-- 课程管理菜单（管理员） -->
        <el-menu-item v-if="userStore.isAdmin" index="/course/manage">
          <el-icon><Calendar /></el-icon>
          <template #title>课程管理</template>
        </el-menu-item>
        
        <!-- 课程浏览菜单（所有用户） -->
        <el-menu-item index="/course/list">
          <el-icon><Reading /></el-icon>
          <template #title>课程浏览</template>
        </el-menu-item>
        
        <!-- 我的预约菜单（会员） -->
        <el-menu-item v-if="userStore.isMember" index="/booking/my">
          <el-icon><Tickets /></el-icon>
          <template #title>我的预约</template>
        </el-menu-item>
        
        <el-menu-item index="/user/profile">
          <el-icon><UserFilled /></el-icon>
          <template #title>个人中心</template>
        </el-menu-item>
      </el-menu>
    </aside>

    <!-- 主内容区域 -->
    <div class="main-container">
      <!-- 顶部导航 -->
      <header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Expand v-if="isCollapsed" />
            <Fold v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRouteName">{{ currentRouteName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="36" :src="userStore.userInfo.avatar || ''">
                {{ userStore.userInfo.userName?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ userStore.userInfo.userName || '用户' }}</span>
              <span class="user-role">{{ userStore.roleName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><UserFilled /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 内容区域 -->
      <main class="main-content">
        <router-view />
      </main>

      <!-- 底部 -->
      <footer class="footer">
        <p>游泳馆会员管理系统 ©2026</p>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { 
  HomeFilled, 
  User, 
  UserFilled, 
  Avatar,
  Calendar,
  Reading,
  Tickets,
  Expand, 
  Fold, 
  ArrowDown,
  SwitchButton 
} from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 侧边栏折叠状态
const isCollapsed = ref(false)

// 当前激活菜单
const activeMenu = computed(() => route.path)

// 当前路由名称
const currentRouteName = computed(() => {
  const routeMap = {
    '/home': '',
    '/user/list': '用户管理',
    '/user/profile': '个人中心',
    '/coach': '教练管理',
    '/course/manage': '课程管理',
    '/course/list': '课程浏览',
    '/booking/my': '我的预约'
  }
  return routeMap[route.path] || ''
})

// 切换侧边栏折叠
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/user/profile')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      ElMessage.success('已退出登录')
      router.push('/login')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.layout-container {
  display: flex;
  min-height: 100vh;
}

/* 侧边栏样式 */
.sidebar {
  width: 220px;
  background: linear-gradient(180deg, #1A6B9F 0%, #145A85 100%);
  transition: width 0.3s;
  display: flex;
  flex-direction: column;
}

.sidebar.collapsed {
  width: 64px;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  gap: 10px;
}

.logo-icon {
  width: 36px;
  height: 36px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.logo-icon svg {
  width: 24px;
  height: 24px;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  color: white;
  white-space: nowrap;
}

.sidebar-menu {
  border-right: none;
  flex: 1;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}

/* 主内容区域 */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #f5f7fa;
}

/* 顶部导航 */
.header {
  height: 60px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
  transition: color 0.3s;
}

.collapse-btn:hover {
  color: #1A6B9F;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.3s;
}

.user-info:hover {
  background: #f5f7fa;
}

.user-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.user-role {
  font-size: 12px;
  color: #909399;
  background: #f0f2f5;
  padding: 2px 8px;
  border-radius: 4px;
}

/* 内容区域 */
.main-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

/* 底部 */
.footer {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-top: 1px solid #e4e7ed;
}

.footer p {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

/* 响应式 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    z-index: 1000;
    height: 100vh;
  }
  
  .sidebar.collapsed {
    width: 0;
    overflow: hidden;
  }
  
  .user-name,
  .user-role {
    display: none;
  }
}
</style>
