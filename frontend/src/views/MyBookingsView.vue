<template>
  <div class="my-bookings-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>我的预约</h2>
      <p class="page-desc">查看您的课程预约记录，管理预约状态</p>
    </div>

    <!-- 筛选区域 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <el-form-item label="预约状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="已预约" :value="1" />
            <el-option label="已取消" :value="0" />
            <el-option label="已完成" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 预约列表 -->
    <div v-loading="loading" class="booking-list">
      <template v-if="bookingList.length > 0">
        <div v-for="booking in bookingList" :key="booking.id" class="booking-card">
          <div class="booking-header">
            <div class="booking-status" :class="getStatusClass(booking.status)">
              {{ getStatusText(booking.status) }}
            </div>
            <span class="booking-time">预约时间：{{ booking.bookingTime }}</span>
          </div>
          
          <div class="booking-content">
            <div class="course-info">
              <h3 class="course-name">{{ booking.courseName }}</h3>
              <div class="info-grid">
                <div class="info-item">
                  <el-icon><Collection /></el-icon>
                  <span>{{ booking.courseType }}</span>
                </div>
                <div class="info-item">
                  <el-icon><User /></el-icon>
                  <span>教练：{{ booking.coachName || '待定' }}</span>
                </div>
                <div class="info-item">
                  <el-icon><Clock /></el-icon>
                  <span>{{ formatDateTime(booking.courseStartTime) }} - {{ formatTime(booking.courseEndTime) }}</span>
                </div>
              </div>
            </div>
            
            <div class="booking-actions">
              <el-button 
                v-if="booking.status === 1 && canCancel(booking)" 
                type="danger" 
                plain
                @click="handleCancel(booking)"
              >
                取消预约
              </el-button>
              <el-button 
                v-else-if="booking.status === 1 && !canCancel(booking)"
                type="info"
                plain
                disabled
              >
                课程已开始
              </el-button>
              <span v-if="booking.status === 0" class="cancel-time">
                取消时间：{{ booking.cancelTime }}
              </span>
            </div>
          </div>
        </div>
      </template>
      
      <el-empty v-else description="暂无预约记录">
        <el-button type="primary" @click="$router.push('/course/list')">
          去预约课程
        </el-button>
      </el-empty>
    </div>

    <!-- 分页 -->
    <div v-if="bookingList.length > 0" class="pagination-container">
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

    <!-- 统计卡片 -->
    <el-card class="stats-card" shadow="never">
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">总预约数</div>
        </div>
        <div class="stat-item stat-active">
          <div class="stat-value">{{ stats.active }}</div>
          <div class="stat-label">进行中</div>
        </div>
        <div class="stat-item stat-completed">
          <div class="stat-value">{{ stats.completed }}</div>
          <div class="stat-label">已完成</div>
        </div>
        <div class="stat-item stat-cancelled">
          <div class="stat-value">{{ stats.cancelled }}</div>
          <div class="stat-label">已取消</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Collection, User, Clock } from '@element-plus/icons-vue'
import { getMyBookings, cancelBooking } from '@/api/booking'

// 加载状态
const loading = ref(false)

// 预约列表
const bookingList = ref([])

// 查询表单
const queryForm = reactive({
  status: null
})

// 分页参数
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 统计数据
const stats = computed(() => {
  const total = pagination.total
  const active = bookingList.value.filter(b => b.status === 1).length
  const completed = bookingList.value.filter(b => b.status === 2).length
  const cancelled = bookingList.value.filter(b => b.status === 0).length
  return { total, active, completed, cancelled }
})

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = { 0: '已取消', 1: '已预约', 2: '已完成' }
  return statusMap[status] || '未知'
}

// 获取状态样式类
const getStatusClass = (status) => {
  const classMap = { 0: 'status-cancelled', 1: 'status-active', 2: 'status-completed' }
  return classMap[status] || ''
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

// 格式化时间
const formatTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

// 判断是否可以取消（课程开始前可以取消）
const canCancel = (booking) => {
  if (!booking.courseStartTime) return false
  const startTime = new Date(booking.courseStartTime)
  return startTime > new Date()
}

// 获取预约列表
const fetchBookingList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...queryForm
    }
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })
    
    const res = await getMyBookings(params)
    if (res.success) {
      bookingList.value = res.data.records || []
      pagination.total = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取预约列表失败')
    }
  } catch (error) {
    console.error('获取预约列表失败:', error)
    ElMessage.error('获取预约列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  fetchBookingList()
}

// 重置
const handleReset = () => {
  queryForm.status = null
  pagination.pageNum = 1
  fetchBookingList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchBookingList()
}

// 页码变化
const handlePageChange = (page) => {
  pagination.pageNum = page
  fetchBookingList()
}

// 取消预约
const handleCancel = (booking) => {
  ElMessageBox.confirm(
    `确定要取消课程 "${booking.courseName}" 的预约吗？`,
    '取消确认',
    {
      confirmButtonText: '确定取消',
      cancelButtonText: '暂不取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await cancelBooking(booking.id)
      if (res.success) {
        ElMessage.success('预约已取消')
        fetchBookingList()
      } else {
        ElMessage.error(res.message || '取消失败')
      }
    } catch (error) {
      console.error('取消预约失败:', error)
      ElMessage.error('取消失败')
    }
  }).catch(() => {})
}

// 页面加载
onMounted(() => {
  fetchBookingList()
})
</script>

<style scoped>
.my-bookings-container {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.page-desc {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

/* 预约列表 */
.booking-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 200px;
}

.booking-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.booking-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.booking-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.booking-status {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

.status-active {
  background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
  color: white;
}

.status-cancelled {
  background: #909399;
  color: white;
}

.status-completed {
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  color: white;
}

.booking-time {
  font-size: 13px;
  color: #909399;
}

.booking-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.course-info {
  flex: 1;
}

.course-name {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.info-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #606266;
}

.info-item .el-icon {
  color: #909399;
}

.booking-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.cancel-time {
  font-size: 12px;
  color: #909399;
}

/* 分页 */
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding: 16px;
  background: white;
  border-radius: 8px;
}

/* 统计卡片 */
.stats-card {
  margin-top: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  text-align: center;
}

.stat-item {
  padding: 16px;
  border-radius: 8px;
  background: #f5f7fa;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-active .stat-value {
  color: #67C23A;
}

.stat-completed .stat-value {
  color: #409EFF;
}

.stat-cancelled .stat-value {
  color: #909399;
}

/* 响应式 */
@media (max-width: 768px) {
  .booking-content {
    flex-direction: column;
    align-items: stretch;
  }
  
  .booking-actions {
    flex-direction: row;
    justify-content: flex-end;
    margin-top: 12px;
  }
  
  .info-grid {
    flex-direction: column;
    gap: 8px;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
