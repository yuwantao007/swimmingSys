<template>
  <div class="my-bookings-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>我的预约</h2>
      <p class="page-desc">查看您的课程预约记录，管理预约状态，已预约课程可生成入场码</p>
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
              <!-- 已预约状态显示入场码按钮 -->
              <el-button 
                v-if="booking.status === 1"
                type="success"
                plain
                @click="handleShowQrcode(booking)"
              >
                <el-icon><Checked /></el-icon>
                生成入场码
              </el-button>
              
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

  <!-- 二维码对话框 -->
  <el-dialog 
    v-model="qrcodeDialog.visible"
    title="入场二维码"
    width="600px"
    :close-on-click-modal="false"
    center
    class="qrcode-dialog"
  >
    <div v-if="qrcodeDialog.data" class="qrcode-dialog-content">
      <!-- 二维码显示区域 -->
      <div class="qrcode-display-area">
        <div class="qrcode-wrapper">
          <qrcode-vue3
            :value="qrcodeDialog.data.qrcodeToken"
            :size="currentQrcodeSize"
            :dotsOptions="{ type: 'rounded', color: '#409EFF' }"
            :cornersSquareOptions="{ type: 'extra-rounded', color: '#409EFF' }"
            class="qrcode-image"
          />
        </div>
        
        <!-- 缩放控制 -->
        <div class="qrcode-controls">
          <el-button-group>
            <el-button 
              :type="currentQrcodeSize === 200 ? 'primary' : ''"
              size="small"
              @click="currentQrcodeSize = 200"
            >
              小
            </el-button>
            <el-button 
              :type="currentQrcodeSize === 300 ? 'primary' : ''"
              size="small"
              @click="currentQrcodeSize = 300"
            >
              中
            </el-button>
            <el-button 
              :type="currentQrcodeSize === 400 ? 'primary' : ''"
              size="small"
              @click="currentQrcodeSize = 400"
            >
              大
            </el-button>
          </el-button-group>
          <el-button 
            type="primary"
            size="small"
            @click="showQrcodeFullscreen"
          >
            <el-icon><FullScreen /></el-icon>
            全屏展示
          </el-button>
        </div>
      </div>

      <!-- 信息区域 -->
      <div class="qrcode-info-area">
        <!-- 会员信息 -->
        <div class="info-section">
          <h4>会员信息</h4>
          <div class="info-item">
            <el-icon><UserFilled /></el-icon>
            <span>{{ qrcodeDialog.data.userName }}</span>
          </div>
        </div>

        <!-- 课程信息 -->
        <div v-if="qrcodeDialog.booking" class="info-section">
          <h4>课程信息</h4>
          <div class="info-item">
            <el-icon><Collection /></el-icon>
            <span>{{ qrcodeDialog.booking.courseName }}</span>
          </div>
          <div class="info-item">
            <el-icon><Clock /></el-icon>
            <span>{{ formatDateTime(qrcodeDialog.booking.courseStartTime) }} - {{ formatTime(qrcodeDialog.booking.courseEndTime) }}</span>
          </div>
          <div class="info-item">
            <el-icon><User /></el-icon>
            <span>教练：{{ qrcodeDialog.booking.coachName || '待定' }}</span>
          </div>
        </div>

        <!-- 令牌信息 -->
        <div class="info-section">
          <h4>入场令牌</h4>
          <div 
            class="token-display"
            @click="handleCopyToken"
            title="点击复制"
          >
            <code>{{ formatToken(qrcodeDialog.data.qrcodeToken) }}</code>
            <el-icon><CopyDocument /></el-icon>
          </div>
          <p class="token-hint">点击令牌可复制</p>
        </div>

        <!-- 生成时间 -->
        <div class="info-section">
          <div class="info-item">
            <el-icon><Clock /></el-icon>
            <span>生成时间：{{ formatDateTime(qrcodeDialog.data.generatedTime) }}</span>
          </div>
        </div>

        <!-- 警告提示 -->
        <el-alert 
          type="warning" 
          :closable="false"
          show-icon
        >
          <template #title>
            该二维码为一次性使用，扫码后将自动失效，请向前台展示扫描
          </template>
        </el-alert>
      </div>
    </div>

    <template #footer>
      <el-button @click="qrcodeDialog.visible = false">关闭</el-button>
      <el-button 
        type="primary"
        @click="handleRefreshQrcodeInDialog"
        :loading="qrcodeDialog.refreshing"
      >
        <el-icon><Refresh /></el-icon>
        重新生成
      </el-button>
    </template>
  </el-dialog>

  <!-- 全屏对话框 -->
  <el-dialog 
    v-model="fullscreenDialog.visible"
    fullscreen
    :show-close="true"
    class="fullscreen-qrcode-dialog"
  >
    <div class="fullscreen-content">
      <qrcode-vue3
        v-if="qrcodeDialog.data"
        :value="qrcodeDialog.data.qrcodeToken"
        :size="500"
        :dotsOptions="{ type: 'rounded', color: '#409EFF' }"
        :cornersSquareOptions="{ type: 'extra-rounded', color: '#409EFF' }"
        class="fullscreen-qrcode"
      />
      <p class="fullscreen-name">{{ qrcodeDialog.data?.userName }}</p>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, Refresh, Collection, User, Clock, Checked, 
  UserFilled, FullScreen, CopyDocument 
} from '@element-plus/icons-vue'
import { getMyBookings, cancelBooking } from '@/api/booking'
import QrcodeVue3 from 'qrcode-vue3'
import { generateEntranceQrcode } from '@/api/entrance'

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

// 二维码对话框状态
const qrcodeDialog = reactive({
  visible: false,
  data: null,
  booking: null,
  refreshing: false
})

// 全屏对话框状态
const fullscreenDialog = reactive({
  visible: false
})

// 当前二维码尺寸
const currentQrcodeSize = ref(300)

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

// 生成入场二维码（显示对话框）
const handleShowQrcode = async (booking) => {
  // 显示加载状态
  const loadingMsg = ElMessage({
    message: '正在生成入场码...',
    type: 'info',
    duration: 0
  })
  
  try {
    const res = await generateEntranceQrcode()
    loadingMsg.close()
    
    // 设置对话框数据
    qrcodeDialog.data = res.data
    qrcodeDialog.booking = booking
    qrcodeDialog.visible = true
    currentQrcodeSize.value = 300 // 重置尺寸为中等
    
    ElMessage.success('生成入场码成功')
  } catch (error) {
    loadingMsg.close()
    ElMessage.error(error.message || '生成失败')
  }
}

// 在对话框中重新生成二维码
const handleRefreshQrcodeInDialog = async () => {
  qrcodeDialog.refreshing = true
  try {
    const res = await generateEntranceQrcode()
    qrcodeDialog.data = res.data
    currentQrcodeSize.value = 300 // 重置尺寸
    ElMessage.success('重新生成成功')
  } catch (error) {
    ElMessage.error(error.message || '生成失败')
  } finally {
    qrcodeDialog.refreshing = false
  }
}

// 显示全屏二维码
const showQrcodeFullscreen = () => {
  fullscreenDialog.visible = true
}

// 复制令牌
const handleCopyToken = async () => {
  if (!qrcodeDialog.data) return
  
  try {
    await navigator.clipboard.writeText(qrcodeDialog.data.qrcodeToken)
    ElMessage.success('令牌已复制到剪贴板')
  } catch (error) {
    ElMessage.warning('复制失败，请手动复制')
  }
}

// 格式化令牌（前8位...后4位）
const formatToken = (token) => {
  if (!token) return ''
  if (token.length <= 12) return token
  return token.substring(0, 8) + '...' + token.substring(token.length - 4)
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
  min-width: 0;
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
  min-width: 120px;
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

/* 二维码对话框样式 */
.qrcode-dialog :deep(.el-dialog__body) {
  padding: 30px;
}

.qrcode-dialog-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 二维码显示区域 */
.qrcode-display-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 12px;
}

.qrcode-display-area .qrcode-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.qrcode-display-area .qrcode-image {
  border-radius: 8px;
}

.qrcode-controls {
  display: flex;
  gap: 12px;
  align-items: center;
}

/* 信息区域 */
.qrcode-info-area {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-section {
  padding: 16px;
  background: #f9fafc;
  border-radius: 8px;
  border-left: 3px solid #409EFF;
}

.info-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.info-section .info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 8px 0;
  font-size: 14px;
  color: #606266;
}

.info-section .info-item .el-icon {
  color: #909399;
}

/* 令牌显示 */
.token-display {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: white;
  border-radius: 8px;
  border: 1px solid #dcdfe6;
  cursor: pointer;
  transition: all 0.3s;
}

.token-display:hover {
  border-color: #409EFF;
  background: #ecf5ff;
}

.token-display code {
  font-family: 'Courier New', monospace;
  font-size: 14px;
  color: #409EFF;
  font-weight: 500;
}

.token-display .el-icon {
  color: #909399;
  transition: color 0.3s;
}

.token-display:hover .el-icon {
  color: #409EFF;
}

.token-hint {
  margin: 8px 0 0 0;
  font-size: 12px;
  color: #c0c4cc;
  text-align: center;
}

/* 全屏对话框 */
.fullscreen-qrcode-dialog :deep(.el-dialog__body) {
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
  padding: 40px;
}

.fullscreen-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.fullscreen-qrcode {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  border-radius: 12px;
  background: white;
  padding: 20px;
}

.fullscreen-name {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}
</style>
