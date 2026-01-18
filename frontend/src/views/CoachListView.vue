<template>
  <div class="coach-list-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>教练管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>
        新增教练
      </el-button>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card" shadow="never">
      <el-form :inline="true" :model="queryForm" class="search-form">
        <el-form-item label="姓名">
          <el-input v-model="queryForm.name" placeholder="请输入教练姓名" clearable />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="queryForm.phone" placeholder="请输入联系电话" clearable />
        </el-form-item>
        <el-form-item label="擅长项目">
          <el-input v-model="queryForm.specialty" placeholder="请输入擅长项目" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="在职" :value="1" />
            <el-option label="停用" :value="0" />
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
      <el-table 
        v-loading="loading" 
        :data="coachList" 
        stripe 
        style="width: 100%"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            <span>{{ GENDER_NAME[row.gender] || '未知' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="specialty" label="擅长项目" min-width="150" show-overflow-tooltip />
        <el-table-column prop="description" label="简介" min-width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '在职' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
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
      width="600px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="coachForm"
        :rules="formRules"
        label-width="100px"
        class="coach-form"
      >
        <el-form-item label="教练姓名" prop="name">
          <el-input v-model="coachForm.name" placeholder="请输入教练姓名" maxlength="50" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="coachForm.phone" placeholder="请输入联系电话" maxlength="11" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="coachForm.gender">
            <el-radio :label="0">未知</el-radio>
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="擅长项目" prop="specialty">
          <el-input v-model="coachForm.specialty" placeholder="请输入擅长项目，如：蛙泳、自由泳" maxlength="200" />
        </el-form-item>
        <el-form-item label="教练简介" prop="description">
          <el-input
            v-model="coachForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入教练简介"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="头像链接" prop="avatar">
          <el-input v-model="coachForm.avatar" placeholder="请输入头像URL（可选）" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="coachForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="在职"
            inactive-text="停用"
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { getCoachList, addCoach, updateCoach, deleteCoach,getActiveCoachList} from '@/api/coach'

// 性别常量
const GENDER_NAME = {
  // 0: '未知',
  1: '男',
  2: '女'
}

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)

// 教练列表数据
const coachList = ref([])

// 查询表单
const queryForm = reactive({
  name: '',
  phone: '',
  specialty: '',
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
const dialogTitle = ref('新增教练')
const formRef = ref(null)
const isEdit = ref(false)
const editId = ref(null)

// 表单数据
const coachForm = reactive({
  name: '',
  phone: '',
  gender: 0,
  specialty: '',
  description: '',
  avatar: '',
  status: 1
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入教练姓名', trigger: 'blur' },
    { max: 50, message: '姓名不能超过50个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  specialty: [
    { required: true, message: '请输入擅长项目', trigger: 'blur' },
    { max: 200, message: '擅长项目不能超过200个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '简介不能超过500个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 获取教练列表
const fetchCoachList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...queryForm
    }
    // 移除空值
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })
    
    const res = await getCoachList(params)
    if (res.success) {
      coachList.value = res.data.records || []
      pagination.total = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取教练列表失败')
    }
  } catch (error) {
    console.error('获取教练列表失败:', error)
    ElMessage.error('获取教练列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  fetchCoachList()
}

// 重置
const handleReset = () => {
  queryForm.name = ''
  queryForm.phone = ''
  queryForm.specialty = ''
  queryForm.status = null
  pagination.pageNum = 1
  fetchCoachList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  fetchCoachList()
}

// 页码变化
const handlePageChange = (page) => {
  pagination.pageNum = page
  fetchCoachList()
}

// 排序变化
const handleSortChange = () => {
  fetchCoachList()
}

// 新增教练
const handleAdd = () => {
  isEdit.value = false
  editId.value = null
  dialogTitle.value = '新增教练'
  dialogVisible.value = true
}

// 编辑教练
const handleEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑教练'
  
  // 填充表单数据
  coachForm.name = row.name
  coachForm.phone = row.phone
  coachForm.gender = row.gender
  coachForm.specialty = row.specialty
  coachForm.description = row.description || ''
  coachForm.avatar = row.avatar || ''
  coachForm.status = row.status
  
  dialogVisible.value = true
}

// 删除教练
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除教练 "${row.name}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deleteCoach(row.id)
      if (res.success) {
        ElMessage.success('删除成功')
        fetchCoachList()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除教练失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitLoading.value = true
    try {
      const data = { ...coachForm }
      let res
      
      if (isEdit.value) {
        res = await updateCoach(editId.value, data)
      } else {
        res = await addCoach(data)
      }
      
      if (res.success) {
        ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
        dialogVisible.value = false
        fetchCoachList()
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
  coachForm.name = ''
  coachForm.phone = ''
  coachForm.gender = 0
  coachForm.specialty = ''
  coachForm.description = ''
  coachForm.avatar = ''
  coachForm.status = 1
  
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchCoachList()
})
</script>

<style scoped>
.coach-list-container {
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

.coach-form {
  padding: 20px 30px 0 0;
}
</style>
