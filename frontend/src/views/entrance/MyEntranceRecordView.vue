<template>
  <div class="my-entrance-records-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>我的入场记录</h2>
      <p class="page-desc">查看您的入场历史记录</p>
    </div>

    <!-- 查询区域 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="queryForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 200px"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="queryForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 200px"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button @click="loadRecords(true)">
            <el-icon><RefreshRight /></el-icon>
            刷新
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 桌面端表格 -->
    <el-card class="table-card desktop-table" shadow="hover" v-loading="loading">
      <el-table :data="tableData" stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="entranceTime" label="入场时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.entranceTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="verifierName" label="验证人" width="120" />
        <el-table-column prop="courseName" label="关联课程" width="150">
          <template #default="{ row }">
            {{ row.courseName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="qrcodeToken" label="入场令牌" min-width="200">
          <template #default="{ row }">
            <span class="token-text">{{ formatToken(row.qrcodeToken) }}</span>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="tableData.length === 0" description="暂无入场记录" />
    </el-card>

    <!-- 移动端卡片列表 -->
    <div class="card-list mobile-list" v-loading="loading">
      <div v-for="record in tableData" :key="record.id" class="record-card">
        <div class="card-header">
          <el-icon><Clock /></el-icon>
          <span class="time">{{ formatDateTime(record.entranceTime) }}</span>
        </div>
        <div class="card-body">
          <p class="info-item">
            <span class="label">验证人：</span>
            <span class="value">{{ record.verifierName }}</span>
          </p>
          <p class="info-item">
            <span class="label">关联课程：</span>
            <span class="value">{{ record.courseName || '无' }}</span>
          </p>
          <p class="info-item token">
            <span class="label">令牌：</span>
            <span class="value">{{ formatToken(record.qrcodeToken) }}</span>
          </p>
        </div>
      </div>
      <el-empty v-if="tableData.length === 0" description="暂无入场记录" />
    </div>

    <!-- 分页 -->
    <div v-if="pagination.total > 0" class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, RefreshRight, Clock } from '@element-plus/icons-vue'
import { getMyEntranceRecords } from '@/api/entrance'

// 加载状态
const loading = ref(false)

// 表格数据
const tableData = ref([])

// 查询表单
const queryForm = reactive({
  startTime: getDefaultStartTime(),
  endTime: getDefaultEndTime()
})

// 分页参数
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 自动刷新定时器
let refreshTimer = null

// 获取默认开始时间（30天前）
function getDefaultStartTime() {
  const date = new Date()
  date.setDate(date.getDate() - 30)
  return formatDateTimeForInput(date)
}

// 获取默认结束时间（今天）
function getDefaultEndTime() {
  return formatDateTimeForInput(new Date())
}

// 格式化日期时间（用于输入）
function formatDateTimeForInput(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day} 23:59:59`
}

// 加载记录
const loadRecords = async (showToast = false) => {
  loading.value = true
  try {
    const params = {
      ...queryForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    const res = await getMyEntranceRecords(params)
    tableData.value = res.data.records || []
    pagination.total = res.data.total || 0
    
    if (showToast) {
      ElMessage.success('数据已更新')
    }
  } catch (error) {
    ElMessage.error(error.message || '加载失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  loadRecords()
}

// 重置
const handleReset = () => {
  queryForm.startTime = getDefaultStartTime()
  queryForm.endTime = getDefaultEndTime()
  pagination.pageNum = 1
  loadRecords()
}

// 分页大小改变
const handleSizeChange = () => {
  pagination.pageNum = 1
  loadRecords()
}

// 页码改变
const handlePageChange = () => {
  loadRecords()
}

// 格式化令牌
const formatToken = (token) => {
  if (!token) return ''
  if (token.length <= 12) return token
  return token.substring(0, 8) + '...' + token.substring(token.length - 4)
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 启动自动刷新
const startAutoRefresh = () => {
  refreshTimer = setInterval(() => {
    loadRecords(true)
  }, 30000) // 30秒刷新一次
}

// 停止自动刷新
const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

onMounted(() => {
  loadRecords()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
})
</script>

<style scoped>
.my-entrance-records-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 24px;
  color: #303133;
  margin: 0 0 8px 0;
}

.page-desc {
  color: #909399;
  margin: 0;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.filter-form {
  margin-bottom: 0;
}

.table-card {
  border-radius: 12px;
  margin-bottom: 20px;
}

.token-text {
  font-family: 'Courier New', monospace;
  color: #409EFF;
  font-size: 13px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

/* 移动端卡片列表 */
.card-list {
  display: none;
}

.record-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.record-card .card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  color: #409EFF;
  font-weight: 600;
}

.record-card .time {
  font-size: 14px;
}

.record-card .card-body {
  color: #606266;
  font-size: 14px;
}

.info-item {
  margin: 8px 0;
  line-height: 1.6;
}

.info-item .label {
  color: #909399;
}

.info-item .value {
  color: #303133;
}

.info-item.token .value {
  font-family: 'Courier New', monospace;
  color: #409EFF;
  font-size: 12px;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .my-entrance-records-container {
    padding: 10px;
  }

  .page-header h2 {
    font-size: 20px;
  }

  .filter-form :deep(.el-form-item) {
    width: 100%;
    margin-right: 0;
  }

  .filter-form :deep(.el-date-editor) {
    width: 100% !important;
  }

  .desktop-table {
    display: none;
  }

  .card-list {
    display: block;
  }

  .pagination-container {
    margin-top: 16px;
  }

  .pagination-container :deep(.el-pagination) {
    justify-content: center;
    flex-wrap: wrap;
  }
}
</style>
