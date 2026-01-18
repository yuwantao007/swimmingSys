<template>
  <div class="home-container">
    <!-- 欢迎卡片 -->
    <div class="welcome-card">
      <div class="welcome-content">
        <div class="welcome-avatar">
          <el-avatar :size="64" :src="userStore.userInfo.avatar || ''">
            {{ userStore.userInfo.userName?.charAt(0) || 'U' }}
          </el-avatar>
        </div>
        <div class="welcome-info">
          <h2>{{ greeting }}，{{ userStore.userInfo.userName || '用户' }}！</h2>
          <p>欢迎使用游泳馆会员管理系统</p>
        </div>
      </div>
      <div class="welcome-decoration">
        <svg viewBox="0 0 200 100" fill="none">
          <path d="M0 50 Q50 20 100 50 T200 50" stroke="rgba(255,255,255,0.3)" stroke-width="2" fill="none"/>
          <path d="M0 60 Q50 30 100 60 T200 60" stroke="rgba(255,255,255,0.2)" stroke-width="2" fill="none"/>
          <path d="M0 70 Q50 40 100 70 T200 70" stroke="rgba(255,255,255,0.1)" stroke-width="2" fill="none"/>
        </svg>
      </div>
    </div>

    <!-- 统计卡片区域（仅管理员显示） -->
    <div v-if="userStore.isAdmin" class="stats-section">
      <div class="section-title">
        <h3>系统概览</h3>
      </div>
      <el-row :gutter="20">
        <el-col :xs="12" :sm="6">
          <div class="stat-card stat-card-blue">
            <div class="stat-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">--</span>
              <span class="stat-label">用户总数</span>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card stat-card-green">
            <div class="stat-icon">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">--</span>
              <span class="stat-label">会员数量</span>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card stat-card-orange">
            <div class="stat-icon">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">--</span>
              <span class="stat-label">今日预约</span>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="6">
          <div class="stat-card stat-card-purple">
            <div class="stat-icon">
              <el-icon><Ticket /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">--</span>
              <span class="stat-label">今日入场</span>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 快捷入口 -->
    <div class="quick-entry-section">
      <div class="section-title">
        <h3>快捷入口</h3>
      </div>
      <el-row :gutter="20">
        <el-col v-if="userStore.isAdmin" :xs="12" :sm="8" :md="6">
          <div class="quick-card" @click="router.push('/user/list')">
            <el-icon class="quick-icon"><User /></el-icon>
            <span class="quick-label">用户管理</span>
          </div>
        </el-col>
        <el-col :xs="12" :sm="8" :md="6">
          <div class="quick-card" @click="router.push('/user/profile')">
            <el-icon class="quick-icon"><UserFilled /></el-icon>
            <span class="quick-label">个人中心</span>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 个人信息卡片（非管理员显示） -->
    <div v-if="!userStore.isAdmin" class="user-info-section">
      <div class="section-title">
        <h3>我的信息</h3>
      </div>
      <div class="user-info-card">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户账号">
            {{ userStore.userInfo.userAccount || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="用户名">
            {{ userStore.userInfo.userName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            {{ userStore.userInfo.phone || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ userStore.userInfo.email || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag :type="getRoleTagType(userStore.userInfo.role)">
              {{ userStore.roleName }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="userStore.userInfo.status === 1 ? 'success' : 'danger'">
              {{ userStore.userInfo.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { User, UserFilled, Calendar, Ticket } from '@element-plus/icons-vue'
import { useUserStore, ROLE } from '../store/user'

const router = useRouter()
const userStore = useUserStore()

// 根据时间段显示问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

// 获取角色标签类型
const getRoleTagType = (role) => {
  if (role === ROLE.ADMIN) return 'danger'
  if (role === ROLE.MEMBER) return 'success'
  return 'info'
}
</script>

<style scoped>
.home-container {
  padding: 0;
}

/* 欢迎卡片 */
.welcome-card {
  background: linear-gradient(135deg, #1A6B9F 0%, #2AA9E0 100%);
  border-radius: 12px;
  padding: 32px;
  color: white;
  position: relative;
  overflow: hidden;
  margin-bottom: 24px;
}

.welcome-content {
  display: flex;
  align-items: center;
  gap: 20px;
  position: relative;
  z-index: 1;
}

.welcome-avatar :deep(.el-avatar) {
  background: rgba(255, 255, 255, 0.2);
  font-size: 24px;
  font-weight: 600;
}

.welcome-info h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.welcome-info p {
  margin: 0;
  font-size: 14px;
  opacity: 0.9;
}

.welcome-decoration {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 200px;
  height: 100px;
  opacity: 0.5;
}

/* 统计卡片 */
.stats-section {
  margin-bottom: 24px;
}

.section-title {
  margin-bottom: 16px;
}

.section-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: transform 0.3s, box-shadow 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
}

.stat-card-blue .stat-icon {
  background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%);
}

.stat-card-green .stat-icon {
  background: linear-gradient(135deg, #67C23A 0%, #95D475 100%);
}

.stat-card-orange .stat-icon {
  background: linear-gradient(135deg, #E6A23C 0%, #F5C161 100%);
}

.stat-card-purple .stat-icon {
  background: linear-gradient(135deg, #9C27B0 0%, #BA68C8 100%);
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

/* 快捷入口 */
.quick-entry-section {
  margin-bottom: 24px;
}

.quick-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.quick-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
  background: linear-gradient(135deg, #1A6B9F 0%, #2AA9E0 100%);
  color: white;
}

.quick-icon {
  font-size: 32px;
  color: #1A6B9F;
  transition: color 0.3s;
}

.quick-card:hover .quick-icon {
  color: white;
}

.quick-label {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  transition: color 0.3s;
}

.quick-card:hover .quick-label {
  color: white;
}

/* 用户信息区域 */
.user-info-section {
  margin-bottom: 24px;
}

.user-info-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

/* 响应式 */
@media (max-width: 768px) {
  .welcome-card {
    padding: 20px;
  }
  
  .welcome-info h2 {
    font-size: 18px;
  }
  
  .stat-card {
    padding: 16px;
  }
  
  .stat-value {
    font-size: 20px;
  }
}
</style>
