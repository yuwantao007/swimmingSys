<template>
  <div class="statistics-container">
    <div class="page-header">
      <h2>运营情况概览</h2>
      <el-button :icon="Refresh" @click="fetchData" :loading="loading">刷新数据</el-button>
    </div>

    <el-skeleton :rows="6" animated v-if="loading && !dashboardData" />
    
    <div v-else class="statistics-content">
      <!-- 核心指标卡片 -->
      <el-row :gutter="20" class="stat-cards">
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-card shadow="hover" class="stat-card primary">
            <div class="stat-icon">
              <el-icon :size="32"><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">会员总数</div>
              <div class="stat-value">{{ dashboardData?.memberCount || 0 }}</div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-card shadow="hover" class="stat-card success">
            <div class="stat-icon">
              <el-icon :size="32"><TrendCharts /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">今日新增会员</div>
              <div class="stat-value">
                {{ dashboardData?.todayNewMembers || 0 }}
                <span class="stat-trend" :class="getTrendClass(dashboardData?.todayNewMembersGrowth)">
                  {{ dashboardData?.todayNewMembersGrowth || '0%' }}
                </span>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-card shadow="hover" class="stat-card warning">
            <div class="stat-icon">
              <el-icon :size="32"><Calendar /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">本月预约数</div>
              <div class="stat-value">
                {{ dashboardData?.monthBookings || 0 }}
                <span class="stat-trend" :class="getTrendClass(dashboardData?.monthBookingsGrowth)">
                  {{ dashboardData?.monthBookingsGrowth || '0%' }}
                </span>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-card shadow="hover" class="stat-card info">
            <div class="stat-icon">
              <el-icon :size="32"><Tickets /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">今日入场人次</div>
              <div class="stat-value">
                {{ dashboardData?.todayEntrances || 0 }}
                <span class="stat-trend" :class="getTrendClass(dashboardData?.todayEntrancesGrowth)">
                  {{ dashboardData?.todayEntrancesGrowth || '0%' }}
                </span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="stat-cards">
        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon">
              <el-icon :size="32"><Reading /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">课程总数</div>
              <div class="stat-value">{{ dashboardData?.courseCount || 0 }}</div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="8" :lg="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon">
              <el-icon :size="32"><Avatar /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">教练总数</div>
              <div class="stat-value">{{ dashboardData?.coachCount || 0 }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 快速入口 -->
      <div class="quick-links">
        <h3>详细统计</h3>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="8">
            <el-card shadow="hover" class="link-card" @click="goTo('/statistics/member')">
              <div class="link-icon">
                <el-icon :size="40"><User /></el-icon>
              </div>
              <h4>会员统计</h4>
              <p>查看会员数量、分布和增长趋势</p>
            </el-card>
          </el-col>

          <el-col :xs="24" :sm="8">
            <el-card shadow="hover" class="link-card" @click="goTo('/statistics/booking')">
              <div class="link-icon">
                <el-icon :size="40"><Calendar /></el-icon>
              </div>
              <h4>预约统计</h4>
              <p>查看预约情况和热门课程</p>
            </el-card>
          </el-col>

          <el-col :xs="24" :sm="8">
            <el-card shadow="hover" class="link-card" @click="goTo('/statistics/entrance')">
              <div class="link-icon">
                <el-icon :size="40"><Tickets /></el-icon>
              </div>
              <h4>入场统计</h4>
              <p>查看入场次数和时段分布</p>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Refresh, 
  User, 
  TrendCharts, 
  Calendar, 
  Tickets, 
  Reading, 
  Avatar 
} from '@element-plus/icons-vue'
import { getDashboard } from '@/api/statistics'

const router = useRouter()
const loading = ref(false)
const dashboardData = ref(null)

// 获取运营概览数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getDashboard()
    if (res.code === 200) {
      dashboardData.value = res.data
    }
  } catch (error) {
    console.error('获取运营概览失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 判断趋势类别
const getTrendClass = (trend) => {
  if (!trend) return ''
  if (trend.startsWith('+')) return 'trend-up'
  if (trend.startsWith('-')) return 'trend-down'
  return ''
}

// 跳转到详情页
const goTo = (path) => {
  router.push(path)
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.statistics-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.statistics-content {
  animation: fadeIn 0.3s ease-in;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 统计卡片 */
.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  cursor: default;
  transition: all 0.3s;
  border-radius: 12px;
  margin-bottom: 20px;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  padding: 24px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-card.primary .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card.success .stat-icon {
  background: linear-gradient(135deg, #67C23A 0%, #529b2e 100%);
}

.stat-card.warning .stat-icon {
  background: linear-gradient(135deg, #E6A23C 0%, #cf9236 100%);
}

.stat-card.info .stat-icon {
  background: linear-gradient(135deg, #409EFF 0%, #3a8ee6 100%);
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.stat-trend {
  font-size: 14px;
  font-weight: normal;
}

.stat-trend.trend-up {
  color: #67C23A;
}

.stat-trend.trend-down {
  color: #F56C6C;
}

/* 快速入口 */
.quick-links {
  margin-top: 32px;
}

.quick-links h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  color: #303133;
}

.link-card {
  cursor: pointer;
  text-align: center;
  transition: all 0.3s;
  border-radius: 12px;
  margin-bottom: 20px;
}

.link-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
  border-color: #409EFF;
}

.link-card :deep(.el-card__body) {
  padding: 32px 24px;
}

.link-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.link-card h4 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #303133;
}

.link-card p {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .stat-value {
    font-size: 24px;
  }
}
</style>
