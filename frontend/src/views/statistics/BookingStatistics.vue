<template>
  <div class="statistics-container">
    <div class="page-header">
      <h2>预约统计</h2>
      <div class="header-actions">
        <el-radio-group v-model="trendDays" @change="fetchTrendData" size="small" style="margin-right: 12px;">
          <el-radio-button :label="7">近7天</el-radio-button>
          <el-radio-button :label="30">近30天</el-radio-button>
        </el-radio-group>
        <el-button :icon="Refresh" @click="fetchAllData" :loading="loading">刷新</el-button>
      </div>
    </div>

    <el-skeleton :rows="8" animated v-if="loading && !bookingOverview" />

    <div v-else class="statistics-content">
      <!-- 预约总览 -->
      <el-card shadow="never" class="overview-card">
        <template #header>
          <div class="card-header">
            <span>预约总览</span>
          </div>
        </template>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="4">
            <div class="overview-item">
              <div class="overview-label">总预约次数</div>
              <div class="overview-value primary">{{ bookingOverview?.totalBookings || 0 }}</div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="4">
            <div class="overview-item">
              <div class="overview-label">已预约</div>
              <div class="overview-value info">{{ bookingOverview?.activeBookings || 0 }}</div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="4">
            <div class="overview-item">
              <div class="overview-label">已完成</div>
              <div class="overview-value success">{{ bookingOverview?.completedBookings || 0 }}</div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="4">
            <div class="overview-item">
              <div class="overview-label">已取消</div>
              <div class="overview-value danger">{{ bookingOverview?.cancelledBookings || 0 }}</div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="4">
            <div class="overview-item">
              <div class="overview-label">完成率</div>
              <div class="overview-value success">{{ formatPercent(bookingOverview?.completionRate) }}</div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="4">
            <div class="overview-item">
              <div class="overview-label">取消率</div>
              <div class="overview-value danger">{{ formatPercent(bookingOverview?.cancellationRate) }}</div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <el-row :gutter="20" style="margin-top: 20px;">
        <!-- 热门课程排行 -->
        <el-col :xs="24" :lg="14">
          <el-card shadow="never">
            <template #header>
              <div class="card-header">
                <span>热门课程排行 TOP 10</span>
              </div>
            </template>
            <div v-if="hotCourses && hotCourses.length > 0" class="hot-course-list">
              <div 
                v-for="(course, index) in hotCourses" 
                :key="course.courseId"
                class="course-item"
              >
                <div class="rank-badge" :class="getRankClass(index)">
                  {{ index + 1 }}
                </div>
                <div class="course-info">
                  <div class="course-name">{{ course.courseName }}</div>
                  <div class="course-meta">
                    <el-tag size="small">{{ course.courseType }}</el-tag>
                    <span class="booking-count">预约 {{ course.bookingCount }} 次</span>
                  </div>
                </div>
                <div class="course-stats">
                  <el-progress 
                    :percentage="Math.round(course.occupancyRate || 0)" 
                    :color="getProgressColor(course.occupancyRate)"
                    :stroke-width="8"
                  />
                  <div class="occupancy-text">
                    上座率 {{ formatPercent(course.occupancyRate) }}
                  </div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无数据" :image-size="100" />
          </el-card>
        </el-col>

        <!-- 预约趋势 -->
        <el-col :xs="24" :lg="10">
          <el-card shadow="never">
            <template #header>
              <div class="card-header">
                <span>预约趋势</span>
              </div>
            </template>
            <div v-if="bookingTrend && bookingTrend.length > 0" class="trend-mini-chart">
              <div class="mini-chart-content">
                <div v-for="item in bookingTrend" :key="item.period" class="mini-bar-item">
                  <div class="mini-bar-wrapper">
                    <div class="mini-bar" :style="{ height: getMiniBarHeight(item.count) + '%' }">
                      <span class="mini-bar-value">{{ item.count }}</span>
                    </div>
                  </div>
                  <div class="mini-bar-label">{{ formatDate(item.period) }}</div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无数据" :image-size="100" />
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { 
  getBookingOverview, 
  getHotCourses, 
  getBookingTrend 
} from '@/api/statistics'

const loading = ref(false)
const trendDays = ref(7)
const bookingOverview = ref(null)
const hotCourses = ref([])
const bookingTrend = ref([])

// 获取所有数据
const fetchAllData = async () => {
  loading.value = true
  try {
    const [overviewRes, hotCoursesRes, trendRes] = await Promise.all([
      getBookingOverview(),
      getHotCourses(10),
      getBookingTrend(trendDays.value)
    ])

    if (overviewRes.code === 200) {
      bookingOverview.value = overviewRes.data
    }
    if (hotCoursesRes.code === 200) {
      hotCourses.value = hotCoursesRes.data
    }
    if (trendRes.code === 200) {
      bookingTrend.value = trendRes.data
    }
  } catch (error) {
    console.error('获取预约统计数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 获取趋势数据
const fetchTrendData = async () => {
  try {
    const res = await getBookingTrend(trendDays.value)
    if (res.code === 200) {
      bookingTrend.value = res.data
    }
  } catch (error) {
    console.error('获取趋势数据失败:', error)
  }
}

// 格式化百分比
const formatPercent = (value) => {
  if (value === null || value === undefined) return '0%'
  return value.toFixed(2) + '%'
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const parts = dateStr.split('-')
  return parts.length === 3 ? `${parts[1]}/${parts[2]}` : dateStr
}

// 获取排名样式类
const getRankClass = (index) => {
  if (index === 0) return 'rank-1'
  if (index === 1) return 'rank-2'
  if (index === 2) return 'rank-3'
  return ''
}

// 获取进度条颜色
const getProgressColor = (rate) => {
  if (rate >= 90) return '#67C23A'
  if (rate >= 70) return '#E6A23C'
  return '#F56C6C'
}

// 计算迷你柱状图高度
const getMiniBarHeight = (count) => {
  if (!bookingTrend.value || bookingTrend.value.length === 0) return 0
  const max = Math.max(...bookingTrend.value.map(item => item.count))
  if (max === 0) return 0
  return (count / max) * 100
}

onMounted(() => {
  fetchAllData()
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

.header-actions {
  display: flex;
  align-items: center;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
}

/* 总览卡片 */
.overview-card {
  border-radius: 12px;
}

.overview-item {
  text-align: center;
  padding: 16px 0;
}

.overview-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.overview-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.overview-value.primary {
  color: #409EFF;
}

.overview-value.success {
  color: #67C23A;
}

.overview-value.danger {
  color: #F56C6C;
}

.overview-value.info {
  color: #909399;
}

/* 热门课程列表 */
.hot-course-list {
  padding: 8px 0;
}

.course-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background 0.3s;
}

.course-item:last-child {
  border-bottom: none;
}

.course-item:hover {
  background: #f5f7fa;
}

.rank-badge {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
  margin-right: 16px;
  flex-shrink: 0;
  background: #f0f0f0;
  color: #909399;
}

.rank-badge.rank-1 {
  background: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.4);
}

.rank-badge.rank-2 {
  background: linear-gradient(135deg, #C0C0C0 0%, #808080 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(192, 192, 192, 0.4);
}

.rank-badge.rank-3 {
  background: linear-gradient(135deg, #CD7F32 0%, #8B4513 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(205, 127, 50, 0.4);
}

.course-info {
  flex: 1;
  min-width: 0;
}

.course-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.course-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.booking-count {
  font-size: 13px;
  color: #909399;
}

.course-stats {
  width: 180px;
  margin-left: 16px;
  flex-shrink: 0;
}

.occupancy-text {
  font-size: 12px;
  color: #606266;
  text-align: right;
  margin-top: 4px;
}

/* 迷你趋势图 */
.trend-mini-chart {
  padding: 16px 0;
}

.mini-chart-content {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 200px;
  padding: 20px 0;
  border-bottom: 2px solid #e4e7ed;
}

.mini-bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
}

.mini-bar-wrapper {
  flex: 1;
  width: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding: 0 4px;
}

.mini-bar {
  width: 100%;
  max-width: 40px;
  background: linear-gradient(180deg, #409EFF 0%, #3a8ee6 100%);
  border-radius: 6px 6px 0 0;
  position: relative;
  transition: all 0.3s;
  min-height: 15px;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 6px;
}

.mini-bar:hover {
  opacity: 0.8;
}

.mini-bar-value {
  font-size: 11px;
  font-weight: 600;
  color: white;
}

.mini-bar-label {
  margin-top: 6px;
  font-size: 11px;
  color: #909399;
  text-align: center;
  white-space: nowrap;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .header-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .overview-value {
    font-size: 24px;
  }

  .course-stats {
    width: 120px;
  }

  .mini-chart-content {
    height: 150px;
  }
}
</style>
