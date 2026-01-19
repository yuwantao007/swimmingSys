<template>
  <div class="statistics-container">
    <div class="page-header">
      <h2>会员统计</h2>
      <div class="header-actions">
        <el-select v-model="trendPeriod" placeholder="选择周期" @change="fetchTrendData" style="width: 120px; margin-right: 12px;">
          <el-option label="近6个月" :value="6" />
          <el-option label="近12个月" :value="12" />
        </el-select>
        <el-button :icon="Refresh" @click="fetchAllData" :loading="loading">刷新</el-button>
      </div>
    </div>

    <el-skeleton :rows="8" animated v-if="loading && !memberOverview" />

    <div v-else class="statistics-content">
      <!-- 会员总览 -->
      <el-card shadow="never" class="overview-card">
        <template #header>
          <div class="card-header">
            <span>会员总览</span>
          </div>
        </template>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="6">
            <div class="overview-item">
              <div class="overview-label">会员总数</div>
              <div class="overview-value primary">{{ memberOverview?.totalMembers || 0 }}</div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <div class="overview-item">
              <div class="overview-label">启用会员</div>
              <div class="overview-value success">{{ memberOverview?.activeMemberCount || 0 }}</div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <div class="overview-item">
              <div class="overview-label">停用会员</div>
              <div class="overview-value danger">{{ memberOverview?.inactiveMemberCount || 0 }}</div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <div class="overview-item">
              <div class="overview-label">启用率</div>
              <div class="overview-value info">{{ formatPercent(memberOverview?.activeRate) }}</div>
            </div>
          </el-col>
        </el-row>

        <el-divider />

        <el-row :gutter="20">
          <el-col :xs="24" :sm="8">
            <div class="overview-item">
              <div class="overview-label">今日新增</div>
              <div class="overview-value">{{ memberOverview?.todayNewMembers || 0 }}</div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="8">
            <div class="overview-item">
              <div class="overview-label">本周新增</div>
              <div class="overview-value">{{ memberOverview?.weekNewMembers || 0 }}</div>
            </div>
          </el-col>
          <el-col :xs="24" :sm="8">
            <div class="overview-item">
              <div class="overview-label">本月新增</div>
              <div class="overview-value">{{ memberOverview?.monthNewMembers || 0 }}</div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 会员分布 -->
      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :xs="24" :sm="12">
          <el-card shadow="never">
            <template #header>
              <div class="card-header">
                <span>角色分布</span>
              </div>
            </template>
            <div class="distribution-list">
              <div class="distribution-item">
                <div class="distribution-label">
                  <el-tag type="danger" size="small">管理员</el-tag>
                </div>
                <div class="distribution-value">
                  {{ memberDistribution?.roleDistribution?.adminCount || 0 }}
                </div>
              </div>
              <div class="distribution-item">
                <div class="distribution-label">
                  <el-tag type="primary" size="small">会员</el-tag>
                </div>
                <div class="distribution-value">
                  {{ memberDistribution?.roleDistribution?.memberCount || 0 }}
                </div>
              </div>
              <div class="distribution-item">
                <div class="distribution-label">
                  <el-tag type="info" size="small">非会员</el-tag>
                </div>
                <div class="distribution-value">
                  {{ memberDistribution?.roleDistribution?.nonMemberCount || 0 }}
                </div>
              </div>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12">
          <el-card shadow="never">
            <template #header>
              <div class="card-header">
                <span>性别分布</span>
                <el-tag size="small" type="info">不含未知</el-tag>
              </div>
            </template>
            <div class="distribution-list">
              <div class="distribution-item">
                <div class="distribution-label">
                  <el-tag type="primary" size="small">男性</el-tag>
                </div>
                <div class="distribution-value">
                  {{ memberDistribution?.genderDistribution?.maleCount || 0 }}
                  <span class="distribution-percent">({{ formatPercent(memberDistribution?.genderDistribution?.maleRate) }})</span>
                </div>
              </div>
              <div class="distribution-item">
                <div class="distribution-label">
                  <el-tag type="danger" size="small">女性</el-tag>
                </div>
                <div class="distribution-value">
                  {{ memberDistribution?.genderDistribution?.femaleCount || 0 }}
                  <span class="distribution-percent">({{ formatPercent(memberDistribution?.genderDistribution?.femaleRate) }})</span>
                </div>
              </div>
            </div>
            <div class="gender-progress" style="margin-top: 16px;">
              <el-progress :percentage="memberDistribution?.genderDistribution?.maleRate || 0" :show-text="false" :stroke-width="20" color="#409EFF" />
              <div style="display: flex; justify-content: space-between; margin-top: 8px; font-size: 12px; color: #909399;">
                <span>男性 {{ formatPercent(memberDistribution?.genderDistribution?.maleRate) }}</span>
                <span>女性 {{ formatPercent(memberDistribution?.genderDistribution?.femaleRate) }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 会员增长趋势 -->
      <el-card shadow="never" style="margin-top: 20px;">
        <template #header>
          <div class="card-header">
            <span>会员增长趋势</span>
          </div>
        </template>
        <div v-if="memberTrend && memberTrend.length > 0" class="trend-chart">
          <div class="chart-legend">
            <div class="legend-item">
              <span class="legend-dot"></span>
              <span>新增会员数</span>
            </div>
          </div>
          <div class="chart-content">
            <div v-for="item in memberTrend" :key="item.period" class="chart-bar-item">
              <div class="bar-label">{{ formatMonth(item.period) }}</div>
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
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { 
  getMemberOverview, 
  getMemberDistribution, 
  getMemberTrend 
} from '@/api/statistics'

const loading = ref(false)
const trendPeriod = ref(6)
const memberOverview = ref(null)
const memberDistribution = ref(null)
const memberTrend = ref([])

// 获取所有数据
const fetchAllData = async () => {
  loading.value = true
  try {
    const [overviewRes, distributionRes, trendRes] = await Promise.all([
      getMemberOverview(),
      getMemberDistribution(),
      getMemberTrend('month', trendPeriod.value)
    ])

    if (overviewRes.code === 200) {
      memberOverview.value = overviewRes.data
    }
    if (distributionRes.code === 200) {
      memberDistribution.value = distributionRes.data
    }
    if (trendRes.code === 200) {
      memberTrend.value = trendRes.data
    }
  } catch (error) {
    console.error('获取会员统计数据失败:', error)
    ElMessage.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

// 获取趋势数据
const fetchTrendData = async () => {
  try {
    const res = await getMemberTrend('month', trendPeriod.value)
    if (res.code === 200) {
      memberTrend.value = res.data
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

// 格式化月份
const formatMonth = (period) => {
  if (!period) return ''
  const parts = period.split('-')
  return parts.length === 2 ? `${parts[1]}月` : period
}

// 计算柱状图高度
const getBarHeight = (count) => {
  if (!memberTrend.value || memberTrend.value.length === 0) return 0
  const max = Math.max(...memberTrend.value.map(item => item.count))
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

.overview-value.danger {
  color: #F56C6C;
}

.overview-value.info {
  color: #909399;
}

/* 分布列表 */
.distribution-list {
  padding: 8px 0;
}

.distribution-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.distribution-item:last-child {
  border-bottom: none;
}

.distribution-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
}

.distribution-percent {
  font-size: 14px;
  color: #909399;
  font-weight: normal;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.chart-content {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 300px;
  padding: 20px 0;
  border-bottom: 2px solid #e4e7ed;
}

.chart-bar-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
}

.bar-wrapper {
  flex: 1;
  width: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  padding: 0 8px;
}

.bar {
  width: 100%;
  max-width: 60px;
  background: linear-gradient(180deg, #667eea 0%, #764ba2 100%);
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
  font-size: 12px;
  color: #909399;
  text-align: center;
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

  .chart-content {
    height: 250px;
  }
}
</style>
