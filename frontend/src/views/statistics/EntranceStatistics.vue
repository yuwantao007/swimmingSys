<template>
  <div class="statistics-container">
    <div class="page-header">
      <h2>入场统计</h2>
      <div class="header-actions">
        <el-radio-group v-model="trendDays" @change="fetchTrendData" size="small" style="margin-right: 12px;">
          <el-radio-button :label="7">近7天</el-radio-button>
          <el-radio-button :label="30">近30天</el-radio-button>
        </el-radio-group>
        <el-button :icon="Refresh" @click="fetchAllData" :loading="loading">刷新</el-button>
      </div>
    </div>

    <el-skeleton :rows="8" animated v-if="loading && !entranceOverview" />

    <div v-else class="statistics-content">
      <!-- 入场总览 -->
      <el-card shadow="never" class="overview-card">
        <template #header>
          <div class="card-header">
            <span>入场总览</span>
          </div>
        </template>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="6">
            <div class="overview-item">
              <div class="overview-label">总入场次数</div>
              <div class="overview-value primary">{{ entranceOverview?.totalEntrances || 0 }}</div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <div class="overview-item">
              <div class="overview-label">今日入场</div>
              <div class="overview-value success">{{ entranceOverview?.todayEntrances || 0 }}</div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <div class="overview-item">
              <div class="overview-label">本周入场</div>
              <div class="overview-value info">{{ entranceOverview?.weekEntrances || 0 }}</div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <div class="overview-item">
              <div class="overview-label">本月入场</div>
              <div class="overview-value warning">{{ entranceOverview?.monthEntrances || 0 }}</div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 入场趋势 -->
      <el-card shadow="never" style="margin-top: 20px;">
        <template #header>
          <div class="card-header">
            <span>入场趋势</span>
          </div>
        </template>
        <div v-if="entranceTrend && entranceTrend.length > 0" class="trend-chart">
          <div class="chart-legend">
            <div class="legend-item">
              <span class="legend-dot"></span>
              <span>入场人次</span>
            </div>
          </div>
          <div class="chart-content">
            <div v-for="item in entranceTrend" :key="item.period" class="chart-bar-item">
              <div class="bar-label">{{ formatDate(item.period) }}</div>
              <div class="bar-wrapper">
                <div class="bar" :style="{ height: getBarHeight(item.count) + '%' }">
                  <span class="bar-value">{{ item.count }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无数据" :image-size="100" />
      </el-card>

      <!-- 入场时段分布 -->
      <el-card shadow="never" style="margin-top: 20px;">
        <template #header>
          <div class="card-header">
            <span>入场时段分布（近30天）</span>
            <el-tag v-if="peakHours.length > 0" type="danger" size="small">
              高峰时段: {{ peakHours.join('、') }}
            </el-tag>
          </div>
        </template>
        <div v-if="hourlyDistribution && hourlyDistribution.length > 0" class="hourly-chart">
          <div class="hourly-content">
            <div v-for="item in hourlyDistribution" :key="item.hour" class="hourly-bar-item">
              <div class="hourly-bar-wrapper">
                <div 
                  class="hourly-bar" 
                  :style="{ height: getHourlyBarHeight(item.entranceCount) + '%' }"
                  :class="{ 'is-peak': isPeakHour(item.hour) }"
                >
                  <span class="hourly-bar-value">{{ item.entranceCount }}</span>
                </div>
              </div>
              <div class="hourly-bar-label">{{ item.hour }}:00</div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无数据" :image-size="100" />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { 
  getEntranceOverview, 
  getEntranceTrend, 
  getHourlyDistribution 
} from '@/api/statistics'

const loading = ref(false)
const trendDays = ref(30)
const entranceOverview = ref(null)
const entranceTrend = ref([])
const hourlyDistribution = ref([])

// 高峰时段（入场人数最多的前3个小时）
const peakHours = computed(() => {
  if (!hourlyDistribution.value || hourlyDistribution.value.length === 0) return []
  
  const sorted = [...hourlyDistribution.value].sort((a, b) => b.entranceCount - a.entranceCount)
  return sorted.slice(0, 3).map(item => `${item.hour}:00`)
})

// 获取所有数据
const fetchAllData = async () => {
  loading.value = true
  try {
    const [overviewRes, trendRes, hourlyRes] = await Promise.all([
      getEntranceOverview(),
      getEntranceTrend(trendDays.value),
      getHourlyDistribution()
    ])

    if (overviewRes.code === 200) {
      entranceOverview.value = overviewRes.data
    }
    if (trendRes.code === 200) {
      entranceTrend.value = trendRes.data
    }
    if (hourlyRes.code === 200) {
      hourlyDistribution.value = hourlyRes.data
    }
  } catch (error) {
    console.error('获取入场统计数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 获取趋势数据
const fetchTrendData = async () => {
  try {
    const res = await getEntranceTrend(trendDays.value)
    if (res.code === 200) {
      entranceTrend.value = res.data
    }
  } catch (error) {
    console.error('获取趋势数据失败:', error)
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const parts = dateStr.split('-')
  return parts.length === 3 ? `${parts[1]}/${parts[2]}` : dateStr
}

// 计算趋势图柱状图高度
const getBarHeight = (count) => {
  if (!entranceTrend.value || entranceTrend.value.length === 0) return 0
  const max = Math.max(...entranceTrend.value.map(item => item.count))
  if (max === 0) return 0
  return (count / max) * 100
}

// 计算时段分布柱状图高度
const getHourlyBarHeight = (count) => {
  if (!hourlyDistribution.value || hourlyDistribution.value.length === 0) return 0
  const max = Math.max(...hourlyDistribution.value.map(item => item.entranceCount))
  if (max === 0) return 0
  return (count / max) * 100
}

// 判断是否为高峰时段
const isPeakHour = (hour) => {
  return peakHours.value.includes(`${hour}:00`)
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
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}

.overview-value.primary {
  color: #409EFF;
}

.overview-value.success {
  color: #67C23A;
}

.overview-value.info {
  color: #909399;
}

.overview-value.warning {
  color: #E6A23C;
}

/* 趋势图表 */
.trend-chart {
  padding: 16px 0;
}

.chart-legend {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.legend-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF 0%, #3a8ee6 100%);
}

.chart-content {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 300px;
  padding: 20px 0;
  border-bottom: 2px solid #e4e7ed;
  overflow-x: auto;
}

.chart-bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  min-width: 40px;
}

.bar-wrapper {
  flex: 1;
  width: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding: 0 4px;
}

.bar {
  width: 100%;
  max-width: 50px;
  background: linear-gradient(180deg, #409EFF 0%, #3a8ee6 100%);
  border-radius: 8px 8px 0 0;
  position: relative;
  transition: all 0.3s;
  min-height: 20px;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 8px;
}

.bar:hover {
  opacity: 0.8;
  transform: scaleY(1.02);
}

.bar-value {
  font-size: 12px;
  font-weight: 600;
  color: white;
}

.bar-label {
  margin-top: 8px;
  font-size: 11px;
  color: #909399;
  text-align: center;
}

/* 时段分布图表 */
.hourly-chart {
  padding: 16px 0;
}

.hourly-content {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 280px;
  padding: 20px 0;
  border-bottom: 2px solid #e4e7ed;
  overflow-x: auto;
}

.hourly-bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  min-width: 30px;
}

.hourly-bar-wrapper {
  flex: 1;
  width: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding: 0 2px;
}

.hourly-bar {
  width: 100%;
  max-width: 35px;
  background: linear-gradient(180deg, #909399 0%, #606266 100%);
  border-radius: 6px 6px 0 0;
  position: relative;
  transition: all 0.3s;
  min-height: 15px;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding-top: 6px;
}

.hourly-bar.is-peak {
  background: linear-gradient(180deg, #F56C6C 0%, #f23030 100%);
}

.hourly-bar:hover {
  opacity: 0.8;
  transform: scaleY(1.02);
}

.hourly-bar-value {
  font-size: 11px;
  font-weight: 600;
  color: white;
}

.hourly-bar-label {
  margin-top: 6px;
  font-size: 10px;
  color: #909399;
  text-align: center;
  writing-mode: vertical-rl;
  text-orientation: mixed;
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
  }

  .overview-value {
    font-size: 24px;
  }

  .chart-content,
  .hourly-content {
    height: 250px;
  }
}
</style>
