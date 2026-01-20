<template>
  <div class="course-manage-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>课程管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增课程
      </el-button>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="课程名称">
          <el-input v-model="queryForm.courseName" placeholder="请输入课程名称" clearable />
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
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 100px">
            <el-option label="已发布" :value="1" />
            <el-option label="已下架" :value="0" />
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

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="courseList" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="courseName" label="课程名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="courseType" label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.courseType)" size="small">
              {{ row.courseType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="coachName" label="教练" width="90" />
        <el-table-column label="上课时间" width="150">
          <template #default="{ row }">
            <span>{{ formatDateTime(row.startTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="容量" width="90">
          <template #default="{ row }">
            <span>{{ row.currentCount }}/{{ row.capacity }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val) => handleStatusChange(row, val)"
              active-text="发布"
              inactive-text="下架"
              inline-prompt
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="info" link size="small" @click="handleViewBookings(row)">预约详情</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页控件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="650px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="courseForm"
        :rules="formRules"
        label-width="100px"
        class="course-form"
      >
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="courseForm.courseName" placeholder="请输入课程名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="课程类型" prop="courseType">
          <el-select v-model="courseForm.courseType" placeholder="请选择课程类型" style="width: 100%">
            <el-option label="基础班" value="基础班" />
            <el-option label="提高班" value="提高班" />
            <el-option label="私教课" value="私教课" />
          </el-select>
        </el-form-item>
        <el-form-item label="授课教练" prop="coachId">
          <el-select v-model="courseForm.coachId" placeholder="请选择教练" style="width: 100%">
            <el-option v-for="coach in coachList" :key="coach.id" :label="coach.name" :value="coach.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="courseForm.startTime"
                type="datetime"
                placeholder="选择开始时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="courseForm.endTime"
                type="datetime"
                placeholder="选择结束时间"
                format="YYYY-MM-DD HH:mm"
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="课程容量" prop="capacity">
          <el-input-number 
            v-model="courseForm.capacity" 
            :min="courseForm.currentCount || 1" 
            :max="100"
            style="width: 200px"
          />
          <span v-if="isEdit && courseForm.currentCount > 0" class="capacity-hint">
            （已有{{ courseForm.currentCount }}人预约，容量不能小于已预约人数）
          </span>
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input
            v-model="courseForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入课程描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="课程状态" prop="status">
          <el-switch
            v-model="courseForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="发布"
            inactive-text="下架"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 预约详情弹窗 -->
    <el-dialog
      v-model="bookingDialogVisible"
      title="课程预约详情"
      width="700px"
    >
      <div v-if="selectedCourse" class="booking-detail">
        <div class="course-summary">
          <h3>{{ selectedCourse.courseName }}</h3>
          <p>时间：{{ formatDateTime(selectedCourse.startTime) }} - {{ formatTime(selectedCourse.endTime) }}</p>
          <p>已预约：{{ selectedCourse.currentCount }} / {{ selectedCourse.capacity }} 人</p>
        </div>
        
        <el-table v-loading="bookingLoading" :data="bookingList" stripe max-height="400">
          <el-table-column prop="userName" label="会员姓名" width="100" />
          <el-table-column prop="bookingTime" label="预约时间" width="170" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getBookingStatusType(row.status)" size="small">
                {{ getBookingStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button 
                v-if="row.status === 1" 
                type="danger" 
                link 
                size="small" 
                @click="handleCancelBooking(row)"
              >
                取消预约
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="bookingDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { getCourseList, addCourse, updateCourse, deleteCourse, updateCourseStatus } from '@/api/course'
import { getBookingsByCourse, cancelBooking } from '@/api/booking'
import { getActiveCoachList } from '@/api/coach'

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)
const bookingLoading = ref(false)

// 课程列表
const courseList = ref([])
// 教练列表
const coachList = ref([])
// 预约列表
const bookingList = ref([])

// 查询表单
const queryForm = reactive({
  courseName: '',
  courseType: '',
  coachId: null,
  status: null
})

// 分页参数
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增课程')
const formRef = ref(null)
const isEdit = ref(false)
const editId = ref(null)

// 预约详情弹窗
const bookingDialogVisible = ref(false)
const selectedCourse = ref(null)

// 表单数据
const courseForm = reactive({
  courseName: '',
  courseType: '',
  coachId: null,
  startTime: '',
  endTime: '',
  capacity: 10,
  currentCount: 0,
  description: '',
  status: 1
})

// 表单验证规则
const formRules = {
  courseName: [
    { required: true, message: '请输入课程名称', trigger: 'blur' },
    { max: 100, message: '课程名称不能超过100个字符', trigger: 'blur' }
  ],
  courseType: [
    { required: true, message: '请选择课程类型', trigger: 'change' }
  ],
  coachId: [
    { required: true, message: '请选择授课教练', trigger: 'change' }
  ],
  startTime: [
    { required: true, message: '请选择开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择结束时间', trigger: 'change' }
  ],
  capacity: [
    { required: true, message: '请输入课程容量', trigger: 'blur' }
  ]
}

// 获取课程类型标签样式
const getTypeTagType = (type) => {
  const typeMap = {
    '基础班': 'success',
    '提高班': 'primary',
    '私教课': 'warning'
  }
  return typeMap[type] || 'info'
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
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 格式化时间
const formatTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

// 获取预约状态文本
const getBookingStatusText = (status) => {
  const statusMap = { 0: '已取消', 1: '已预约', 2: '已完成' }
  return statusMap[status] || '未知'
}

// 获取预约状态标签类型
const getBookingStatusType = (status) => {
  const typeMap = { 0: 'info', 1: 'success', 2: 'primary' }
  return typeMap[status] || 'info'
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
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
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
  queryForm.status = null
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

// 新增课程
const handleAdd = () => {
  isEdit.value = false
  editId.value = null
  dialogTitle.value = '新增课程'
  dialogVisible.value = true
}

// 编辑课程
const handleEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑课程'
  
  courseForm.courseName = row.courseName
  courseForm.courseType = row.courseType
  courseForm.coachId = row.coachId
  courseForm.startTime = row.startTime
  courseForm.endTime = row.endTime
  courseForm.capacity = row.capacity
  courseForm.currentCount = row.currentCount
  courseForm.description = row.description || ''
  courseForm.status = row.status
  
  dialogVisible.value = true
}

// 删除课程
const handleDelete = (row) => {
  let message = `确定要删除课程 "${row.courseName}" 吗？`
  if (row.currentCount > 0) {
    message = `该课程有 ${row.currentCount} 人已预约，删除后预约将失效。确定要删除吗？`
  }
  
  ElMessageBox.confirm(message, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteCourse(row.id)
      if (res.success) {
        ElMessage.success('删除成功')
        fetchCourseList()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除课程失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 修改课程状态
const handleStatusChange = async (row, val) => {
  const status = val ? 1 : 0
  const action = status === 1 ? '发布' : '下架'
  
  try {
    const res = await updateCourseStatus(row.id, status)
    if (res.success) {
      ElMessage.success(`${action}成功`)
      row.status = status
    } else {
      ElMessage.error(res.message || `${action}失败`)
    }
  } catch (error) {
    console.error(`${action}失败:`, error)
    ElMessage.error(`${action}失败`)
  }
}

// 查看预约详情
const handleViewBookings = async (row) => {
  selectedCourse.value = row
  bookingDialogVisible.value = true
  bookingLoading.value = true
  
  try {
    const res = await getBookingsByCourse(row.id, { pageNum: 1, pageSize: 100 })
    if (res.success) {
      bookingList.value = res.data.records || []
    } else {
      ElMessage.error(res.message || '获取预约列表失败')
    }
  } catch (error) {
    console.error('获取预约列表失败:', error)
    ElMessage.error('获取预约列表失败')
  } finally {
    bookingLoading.value = false
  }
}

// 取消预约
const handleCancelBooking = (row) => {
  ElMessageBox.confirm(`确定要取消 "${row.userName}" 的预约吗？`, '取消确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await cancelBooking(row.id)
      if (res.success) {
        ElMessage.success('取消预约成功')
        handleViewBookings(selectedCourse.value)
        fetchCourseList()
      } else {
        ElMessage.error(res.message || '取消预约失败')
      }
    } catch (error) {
      console.error('取消预约失败:', error)
      ElMessage.error('取消预约失败')
    }
  }).catch(() => {})
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    // 验证时间
    if (new Date(courseForm.startTime) >= new Date(courseForm.endTime)) {
      ElMessage.error('结束时间必须晚于开始时间')
      return
    }
    
    submitLoading.value = true
    try {
      const data = {
        courseName: courseForm.courseName,
        courseType: courseForm.courseType,
        coachId: courseForm.coachId,
        startTime: courseForm.startTime,
        endTime: courseForm.endTime,
        capacity: courseForm.capacity,
        description: courseForm.description,
        status: courseForm.status
      }
      
      let res
      if (isEdit.value) {
        res = await updateCourse(editId.value, data)
      } else {
        res = await addCourse(data)
      }
      
      if (res.success) {
        ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
        dialogVisible.value = false
        fetchCourseList()
      } else {
        ElMessage.error(res.message || (isEdit.value ? '更新失败' : '新增失败'))
      }
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error('提交失败')
    } finally {
      submitLoading.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  courseForm.courseName = ''
  courseForm.courseType = ''
  courseForm.coachId = null
  courseForm.startTime = ''
  courseForm.endTime = ''
  courseForm.capacity = 10
  courseForm.currentCount = 0
  courseForm.description = ''
  courseForm.status = 1
  
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 页面加载
onMounted(() => {
  fetchCourseList()
  fetchCoachList()
})
</script>

<style scoped>
.course-manage-container {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.course-form {
  padding: 20px 30px 0 0;
}

.capacity-hint {
  margin-left: 10px;
  font-size: 12px;
  color: #E6A23C;
}

/* 预约详情 */
.booking-detail {
  padding: 10px 0;
}

.course-summary {
  margin-bottom: 20px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.course-summary h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #303133;
}

.course-summary p {
  margin: 6px 0;
  font-size: 14px;
  color: #606266;
}

/* 响应式 */
@media (max-width: 768px) {
  .search-form {
    flex-direction: column;
  }
  
  .search-form .el-form-item {
    margin-right: 0;
    margin-bottom: 12px;
  }
}
</style>
