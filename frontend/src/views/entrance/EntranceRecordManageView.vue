<template>
  <div class="entrance-record-manage-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>入场记录管理</h2>
      <p class="page-desc">管理所有会员的入场记录</p>
    </div>

    <!-- 查询区域 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <el-form-item label="会员ID">
          <el-input
            v-model.number="queryForm.userId"
            placeholder="输入会员ID"
            clearable
            style="width: 150px"
          />
        </el-form-item>
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
          <el-button type="success" @click="handleExport" :loading="exporting">
            <el-icon><Download /></el-icon>
            导出Excel
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 桌面端表格 -->
    <el-card class="table-card desktop-table" shadow="hover" v-loading="loading">
      <el-table :data="tableData" stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="userName" label="会员姓名" width="120" />
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
        <el-table-column prop="qrcodeToken" label="入场令牌" min-width="150">
          <template #default="{ row }">
            <span class="token-text">{{ formatToken(row.qrcodeToken) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleViewDetail(row)">
              查看
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="tableData.length === 0" description="暂无入场记录" />
    </el-card>

    <!-- 移动端卡片列表 -->
    <div class="card-list mobile-list" v-loading="loading">
      <div v-for="record in tableData" :key="record.id" class="record-card">
        <div class="card-header">
          <span class="user-name">{{ record.userName }}</span>
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
        <div class="card-footer">
          <el-button type="primary" size="small" @click="handleViewDetail(record)">
            查看详情
          </el-button>
          <el-button type="danger" size="small" @click="handleDelete(record)">
            删除
          </el-button>
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

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="入场记录详情" width="600px">
      <div v-if="detailData" class="detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="记录ID">{{ detailData.id }}</el-descriptions-item>
          <el-descriptions-item label="会员姓名">{{ detailData.userName }}</el-descriptions-item>
          <el-descriptions-item label="会员ID">{{ detailData.userId }}</el-descriptions-item>
          <el-descriptions-item label="入场时间">{{ formatDateTime(detailData.entranceTime) }}</el-descriptions-item>
          <el-descriptions-item label="验证人">{{ detailData.verifierName }}</el-descriptions-item>
          <el-descriptions-item label="关联课程">{{ detailData.courseName || '无' }}</el-descriptions-item>
          <el-descriptions-item label="入场令牌">
            <div class="token-detail">
              <span>{{ detailData.qrcodeToken }}</span>
              <el-button size="small" @click="copyToken(detailData.qrcodeToken)">
                复制
              </el-button>
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, RefreshRight, Download } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'
import { getAllEntranceRecords, getEntranceRecordById, deleteEntranceRecord } from '@/api/entrance'

// 加载状态
const loading = ref(false)

// 导出状态
const exporting = ref(false)

// 表格数据
const tableData = ref([])

// 查询表单
const queryForm = reactive({
  userId: null,
  startTime: getDefaultStartTime(),
  endTime: getDefaultEndTime()
})

// 分页参数
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 详情对话框
const detailVisible = ref(false)
const detailData = ref(null)

// 自动刷新定时器
let refreshTimer = null

// 获取默认开始时间（当天0点）
function getDefaultStartTime() {
  const date = new Date()
  date.setHours(0, 0, 0, 0)
  return formatDateTimeForInput(date)
}

// 获取默认结束时间（当天23:59:59）
function getDefaultEndTime() {
  const date = new Date()
  return formatDateTimeForInput(date)
}

// 格式化日期时间（用于输入）
function formatDateTimeForInput(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
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
    const res = await getAllEntranceRecords(params)
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
  queryForm.userId = null
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

// 查看详情
const handleViewDetail = async (row) => {
  try {
    const res = await getEntranceRecordById(row.id)
    detailData.value = res.data
    detailVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '获取详情失败')
  }
}

// 删除记录
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除该入场记录吗？删除后将无法恢复，请谨慎操作。',
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    await deleteEntranceRecord(row.id)
    ElMessage.success('删除成功')
    await loadRecords()
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 导出Excel
const handleExport = async () => {
  try {
    exporting.value = true
    ElMessage.info('正在导出，请稍候...')
    
    // 获取所有数据（不分页）
    const params = {
      ...queryForm,
      pageNum: 1,
      pageSize: 10000
    }
    const res = await getAllEntranceRecords(params)
    const records = res.data.records || []
    
    if (records.length === 0) {
      ElMessage.warning('没有数据可导出')
      return
    }
    
    // 转换数据格式
    const exportData = records.map((item, index) => ({
      '序号': index + 1,
      '会员姓名': item.userName,
      '会员ID': item.userId,
      '入场时间': formatDateTime(item.entranceTime),
      '验证人': item.verifierName,
      '关联课程': item.courseName || '无',
      '入场令牌': item.qrcodeToken
    }))
    
    // 创建工作表
    const ws = XLSX.utils.json_to_sheet(exportData)
    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, '入场记录')
    
    // 生成文件名
    const fileName = `入场记录_${formatDateTimeForFile(new Date())}.xlsx`
    
    // 导出文件
    XLSX.writeFile(wb, fileName)
    
    ElMessage.success('导出成功')
    
  } catch (error) {
    ElMessage.error(error.message || '导出失败')
  } finally {
    exporting.value = false
  }
}

// 复制令牌
const copyToken = async (token) => {
  try {
    await navigator.clipboard.writeText(token)
    ElMessage.success('令牌已复制到剪贴板')
  } catch (error) {
    ElMessage.warning('复制失败，请手动复制')
  }
}

// 格式化令牌
const formatToken = (token) => {
  if (!token) return ''
  if (token.length <= 12) return token
  return token.substring(0, 8) + '...' + token.substring(token.length - 4)
}

// 格式化日期时间（用于显示）
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

// 格式化日期时间（用于文件名）
function formatDateTimeForFile(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}${month}${day}_${hours}${minutes}${seconds}`
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
.entrance-record-manage-container {
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

/* 详情内容 */
.detail-content {
  padding: 10px 0;
}

.token-detail {
  display: flex;
  align-items: center;
  gap: 12px;
  font-family: 'Courier New', monospace;
  color: #409EFF;
  word-break: break-all;
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
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.record-card .user-name {
  font-weight: 600;
  color: #303133;
  font-size: 16px;
}

.record-card .time {
  color: #909399;
  font-size: 12px;
}

.record-card .card-body {
  color: #606266;
  font-size: 14px;
  margin-bottom: 12px;
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

.card-footer {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .entrance-record-manage-container {
    padding: 10px;
  }

  .page-header h2 {
    font-size: 20px;
  }

  .filter-form :deep(.el-form-item) {
    width: 100%;
    margin-right: 0;
  }

  .filter-form :deep(.el-input),
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
