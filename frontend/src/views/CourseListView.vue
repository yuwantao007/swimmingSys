<template>
  <div class="course-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>课程列表</h2>
      <p class="page-desc">浏览可预约的游泳课程，选择适合您的课程进行预约</p>
    </div>

    <!-- 筛选区域 -->
    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <el-form-item label="课程名称">
          <el-input v-model="queryForm.courseName" placeholder="搜索课程" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="课程类型">
          <el-select v-model="queryForm.courseType" placeholder="全部" clearable style="width: 120px">
            <el-option label="基础班" value="基础班" />
            <el-option label="提高班" value="提高班" />
            <el-option label="私教课" value="私教课" />
          </el-select>
        </el-form-item>
        <el-form-item label="教练">
          <el-select v-model="queryForm.coachId" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="coach in coachList" :key="coach.id" :label="coach.name" :value="coach.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="queryForm.bookableOnly">只看可预约</el-checkbox>
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

    <!-- 课程卡片列表 -->
    <div v-loading="loading" class="course-grid">
      <template v-if="courseList.length > 0">
        <div v-for="course in courseList" :key="course.id" class="course-card">
          <!-- 课程图片 -->
          <div class="course-image">
            <img :src="getCourseImage(course.courseType)" :alt="course.courseName" />
            <div class="course-type-tag" :class="getTypeClass(course.courseType)">
              {{ course.courseType }}
            </div>
            <div class="course-status-badge" :class="{ 'status-full': course.remainingCount <= 0 }">
              <template v-if="course.remainingCount <= 0">已满</template>
              <template v-else-if="course.remainingCount <= 3">仅剩{{ course.remainingCount }}位</template>
              <template v-else>剩余{{ course.remainingCount }}位</template>
            </div>
          </div>
          
          <!-- 课程内容 -->
          <div class="course-content">
            <h3 class="course-name">{{ course.courseName }}</h3>
            
            <div class="course-info">
              <div class="info-item clickable" @click="handleViewCoach(course.coachId)">
                <el-icon><User /></el-icon>
                <span class="coach-link">教练：{{ course.coachName || '待定' }}</span>
              </div>
              <div class="info-item">
                <el-icon><Clock /></el-icon>
                <span>{{ formatDateTime(course.startTime) }}</span>
              </div>
              <div class="info-item">
                <el-icon><Timer /></el-icon>
                <span>时长：{{ calcDuration(course.startTime, course.endTime) }}分钟</span>
              </div>
              <div class="info-item">
                <el-icon><UserFilled /></el-icon>
                <span>容量：{{ course.currentCount }}/{{ course.capacity }}人</span>
              </div>
            </div>
            
            <p v-if="course.description" class="course-desc">{{ course.description }}</p>
            
            <div class="card-footer">
              <el-button 
                v-if="userStore.isMember"
                type="primary" 
                :disabled="!course.bookable"
                @click="handleBooking(course)"
              >
                {{ course.bookable ? '立即预约' : '无法预约' }}
              </el-button>
              <el-button v-else-if="userStore.isNonMember" type="info" disabled>
                请先成为会员
              </el-button>
              <el-button v-else type="primary" @click="showCourseDetail(course)">
                查看详情
              </el-button>
            </div>
          </div>
        </div>
      </template>
      
      <el-empty v-else description="暂无课程数据" />
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[8, 12, 20, 40]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 预约确认弹窗 -->
    <el-dialog
      v-model="bookingDialogVisible"
      title="预约确认"
      width="500px"
      :close-on-click-modal="false"
    >
      <div v-if="selectedCourse" class="booking-confirm">
        <div class="confirm-course-info">
          <h3>{{ selectedCourse.courseName }}</h3>
          <p><strong>课程类型：</strong>{{ selectedCourse.courseType }}</p>
          <p><strong>授课教练：</strong>{{ selectedCourse.coachName }}</p>
          <p><strong>上课时间：</strong>{{ formatDateTime(selectedCourse.startTime) }} - {{ formatTime(selectedCourse.endTime) }}</p>
          <p><strong>剩余名额：</strong>{{ selectedCourse.remainingCount }}人</p>
        </div>
        
        <!-- 时间冲突警告 -->
        <el-alert
          v-if="conflictInfo && conflictInfo.hasConflict"
          type="warning"
          :closable="false"
          class="conflict-alert"
        >
          <template #title>
            <span>时间冲突提醒</span>
          </template>
          <p>您在该时间段已预约了其他课程：</p>
          <p><strong>{{ conflictInfo.conflictBooking.courseName }}</strong></p>
          <p>时间：{{ formatDateTime(conflictInfo.conflictBooking.courseStartTime) }}</p>
          <el-checkbox v-model="forceReplace" style="margin-top: 10px">
            确认替换原预约
          </el-checkbox>
        </el-alert>
      </div>
      
      <template #footer>
        <el-button @click="bookingDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="bookingLoading"
          :disabled="conflictInfo && conflictInfo.hasConflict && !forceReplace"
          @click="confirmBookingAction"
        >
          确认预约
        </el-button>
      </template>
    </el-dialog>

    <!-- 课程详情弹窗（管理员查看） -->
    <el-dialog
      v-model="detailDialogVisible"
      title="课程详情"
      width="600px"
    >
      <div v-if="selectedCourse" class="course-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="课程名称" :span="2">{{ selectedCourse.courseName }}</el-descriptions-item>
          <el-descriptions-item label="课程类型">{{ selectedCourse.courseType }}</el-descriptions-item>
          <el-descriptions-item label="授课教练">{{ selectedCourse.coachName }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ formatDateTime(selectedCourse.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ formatDateTime(selectedCourse.endTime) }}</el-descriptions-item>
          <el-descriptions-item label="课程容量">{{ selectedCourse.capacity }}人</el-descriptions-item>
          <el-descriptions-item label="已预约">{{ selectedCourse.currentCount }}人</el-descriptions-item>
          <el-descriptions-item label="课程状态">
            <el-tag :type="selectedCourse.status === 1 ? 'success' : 'info'">
              {{ selectedCourse.status === 1 ? '已发布' : '已下架' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="可预约">
            <el-tag :type="selectedCourse.bookable ? 'success' : 'danger'">
              {{ selectedCourse.bookable ? '是' : '否' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="课程描述" :span="2">
            {{ selectedCourse.description || '暂无描述' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 教练详情弹窗 -->
    <el-dialog
      v-model="coachDetailVisible"
      title="教练详情"
      width="500px"
      :close-on-click-modal="true"
    >
      <div v-loading="coachLoading" class="coach-detail-content">
        <template v-if="selectedCoach">
          <div class="coach-detail-avatar">
            <el-avatar :size="100" :src="selectedCoach.avatar || ''">
              {{ selectedCoach.name?.charAt(0) || 'C' }}
            </el-avatar>
          </div>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="教练姓名">{{ selectedCoach.name }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ GENDER_NAME[selectedCoach.gender] || '未知' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ selectedCoach.phone }}</el-descriptions-item>
            <el-descriptions-item label="擅长项目">{{ selectedCoach.specialty || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="教练简介">
              {{ selectedCoach.description || '暂无简介' }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="selectedCoach.status === 1 ? 'success' : 'danger'" size="small">
                {{ selectedCoach.status === 1 ? '在职' : '停用' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </template>
      </div>
      <template #footer>
        <el-button @click="coachDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, User, Clock, Timer, UserFilled } from '@element-plus/icons-vue'
import { getCourseList } from '@/api/course'
import { requestBooking, confirmBooking } from '@/api/booking'
import { getActiveCoachList, getCoachById } from '@/api/coach'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

// 加载状态
const loading = ref(false)
const bookingLoading = ref(false)

// 课程列表
const courseList = ref([])
// 教练列表（用于筛选）
const coachList = ref([])

// 查询表单
const queryForm = reactive({
  courseName: '',
  courseType: '',
  coachId: null,
  bookableOnly: true
})

// 分页参数
const pagination = reactive({
  pageNum: 1,
  pageSize: 12,
  total: 0
})

// 预约相关
const bookingDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const selectedCourse = ref(null)
const conflictInfo = ref(null)
const forceReplace = ref(false)

// 教练详情相关
const coachDetailVisible = ref(false)
const selectedCoach = ref(null)
const coachLoading = ref(false)

// 获取课程类型样式类
const getTypeClass = (type) => {
  const typeMap = {
    '基础班': 'type-basic',
    '提高班': 'type-advanced',
    '私教课': 'type-private'
  }
  return typeMap[type] || 'type-basic'
}

// 获取课程默认图片
const getCourseImage = (courseType) => {
  const imageMap = {
    '基础班': 'https://images.unsplash.com/photo-1519315901367-f34ff9154487?w=400&h=250&fit=crop',
    '提高班': 'https://images.unsplash.com/photo-1560089000-7433a4ebbd64?w=400&h=250&fit=crop',
    '私教课': 'https://images.unsplash.com/photo-1576610616656-d3aa5d1f4534?w=400&h=250&fit=crop'
  }
  return imageMap[courseType] || imageMap['基础班']
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

// 计算课程时长（分钟）
const calcDuration = (start, end) => {
  if (!start || !end) return 0
  const startDate = new Date(start)
  const endDate = new Date(end)
  return Math.round((endDate - startDate) / (1000 * 60))
}

// 获取课程列表
const fetchCourseList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...queryForm
    }
    // 移除空值
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined || params[key] === false) {
        delete params[key]
      }
    })
    
    const res = await getCourseList(params)
    if (res.success) {
      courseList.value = res.data.records || []
      pagination.total = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取课程列表失败')
    }
  } catch (error) {
    console.error('获取课程列表失败:', error)
    ElMessage.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

// 获取教练列表
const fetchCoachList = async () => {
  try {
    const res = await getActiveCoachList()
    if (res.success) {
      coachList.value = res.data || []
    }
  } catch (error) {
    console.error('获取教练列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  fetchCourseList()
}

// 重置
const handleReset = () => {
  queryForm.courseName = ''
  queryForm.courseType = ''
  queryForm.coachId = null
  queryForm.bookableOnly = true
  pagination.pageNum = 1
  fetchCourseList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchCourseList()
}

// 页码变化
const handlePageChange = (page) => {
  pagination.pageNum = page
  fetchCourseList()
}

// 处理预约
const handleBooking = async (course) => {
  selectedCourse.value = course
  conflictInfo.value = null
  forceReplace.value = false
  bookingLoading.value = true
  
  try {
    // 先检测冲突
    const res = await requestBooking({ courseId: course.id })
    if (res.success) {
      conflictInfo.value = res.data
      bookingDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '检测预约冲突失败')
    }
  } catch (error) {
    console.error('检测预约冲突失败:', error)
    ElMessage.error('检测预约冲突失败')
  } finally {
    bookingLoading.value = false
  }
}

// 确认预约
const confirmBookingAction = async () => {
  if (!selectedCourse.value) return
  
  bookingLoading.value = true
  try {
    const data = {
      courseId: selectedCourse.value.id,
      forceReplace: forceReplace.value
    }
    // 如果是强制替换，需要传递要替换的预约ID
    if (forceReplace.value && conflictInfo.value?.conflictBooking) {
      data.replaceBookingId = conflictInfo.value.conflictBooking.id
    }
    
    const res = await confirmBooking(data)
    if (res.success) {
      ElMessage.success('预约成功！')
      bookingDialogVisible.value = false
      fetchCourseList()
    } else {
      ElMessage.error(res.message || '预约失败')
    }
  } catch (error) {
    console.error('预约失败:', error)
    ElMessage.error('预约失败，请稍后重试')
  } finally {
    bookingLoading.value = false
  }
}

// 查看课程详情（管理员）
const showCourseDetail = (course) => {
  selectedCourse.value = course
  detailDialogVisible.value = true
}

// 查看教练详情
const handleViewCoach = async (coachId) => {
  if (!coachId) {
    ElMessage.warning('教练信息不存在')
    return
  }
  
  coachLoading.value = true
  try {
    const res = await getCoachById(coachId)
    if (res.success) {
      selectedCoach.value = res.data
      coachDetailVisible.value = true
    } else {
      ElMessage.error(res.message || '获取教练信息失败')
    }
  } catch (error) {
    console.error('获取教练信息失败:', error)
    ElMessage.error('获取教练信息失败')
  } finally {
    coachLoading.value = false
  }
}

// 性别映射
const GENDER_NAME = {
  0: '未知',
  1: '男',
  2: '女'
}

// 页面加载
onMounted(() => {
  fetchCourseList()
  fetchCoachList()
})
</script>

<style scoped>
.course-list-container {
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

/* 课程卡片网格 */
.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  min-height: 200px;
}

.course-card {
  background: white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

/* 课程图片区域 */
.course-image {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.course-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.course-card:hover .course-image img {
  transform: scale(1.05);
}

/* 课程内容区域 */
.course-content {
  padding: 20px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.course-type-tag {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  backdrop-filter: blur(10px);
  z-index: 2;
}

.type-basic {
  background: linear-gradient(135deg, #67C23A 0%, #85ce61 100%);
  color: white;
}

.type-advanced {
  background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
  color: white;
}

.type-private {
  background: linear-gradient(135deg, #E6A23C 0%, #f0c78a 100%);
  color: white;
}

.course-status-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  background: rgba(103, 194, 58, 0.9);
  color: white;
  backdrop-filter: blur(10px);
  z-index: 2;
}

.course-status-badge.status-full {
  background: rgba(245, 108, 108, 0.9);
}

.course-name {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.course-info {
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
}

.info-item .el-icon {
  color: #909399;
}

.info-item.clickable {
  cursor: pointer;
  transition: color 0.3s ease;
}

.info-item.clickable:hover .coach-link {
  color: #409EFF;
}

.coach-link {
  transition: color 0.3s ease;
}

.course-desc {
  flex: 1;
  font-size: 13px;
  color: #909399;
  margin: 0 0 16px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  margin-top: auto;
}

.card-footer .el-button {
  width: 100%;
  border-radius: 8px;
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

/* 预约确认弹窗 */
.booking-confirm {
  padding: 10px 0;
}

.confirm-course-info h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: #303133;
}

.confirm-course-info p {
  margin: 8px 0;
  font-size: 14px;
  color: #606266;
}

.conflict-alert {
  margin-top: 20px;
}

/* 课程详情 */
.course-detail {
  padding: 10px 0;
}

/* 教练详情弹窗样式 */
.coach-detail-content {
  padding: 10px 0;
  min-height: 200px;
}

.coach-detail-avatar {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.coach-detail-content :deep(.el-descriptions__label) {
  width: 100px;
}

/* 响应式 */
@media (max-width: 768px) {
  .course-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-form {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-form .el-form-item {
    margin-right: 0;
    margin-bottom: 12px;
  }
}
</style>
