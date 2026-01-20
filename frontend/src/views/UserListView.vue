<template>
  <div class="user-list-container">
    <!-- 搜索区域 -->
    <div class="search-card">
      <el-form :model="queryForm" inline class="search-form">
        <el-form-item label="用户账号">
          <el-input v-model="queryForm.userAccount" placeholder="请输入用户账号" clearable />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="queryForm.userName" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="queryForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="queryForm.role" placeholder="请选择角色" clearable style="width: 120px">
            <el-option label="全部" :value="null" />
            <el-option label="管理员" :value="0" />
            <el-option label="会员" :value="1" />
            <el-option label="非会员" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择状态" clearable style="width: 120px">
            <el-option label="全部" :value="null" />
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="过期状态">
          <el-select v-model="queryForm.expirationStatus" placeholder="请选择过期状态" clearable style="width: 130px">
            <el-option label="全部" value="all" />
            <el-option label="已过期" value="expired" />
            <el-option label="即将过期" value="expiring" />
            <el-option label="正常" value="normal" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作区域 -->
    <div class="action-bar">
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增用户
      </el-button>
      <el-button type="warning" :disabled="selectedUsers.length === 0" @click="handleBatchExpiration">
        批量设置过期时间 ({{ selectedUsers.length }})
      </el-button>
    </div>

    <!-- 表格区域 -->
    <div class="table-card">
      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column label="头像" width="70" align="center">
          <template #default="{ row }">
            <el-avatar :size="36" :src="row.avatar || ''">
              {{ row.userName?.charAt(0) || 'U' }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="userAccount" label="用户账号" width="120" />
        <el-table-column prop="userName" label="用户名" width="100" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="150" show-overflow-tooltip />
        <el-table-column label="性别" width="70" align="center">
          <template #default="{ row }">
            {{ GENDER_NAME[row.gender] || '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="角色" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)" size="small">
              {{ ROLE_NAME[row.role] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="过期时间" width="180" align="center">
          <template #default="{ row }">
            <div v-if="row.expirationTime">
              <div>{{ row.expirationTime }}</div>
              <el-tag v-if="row.expired" type="danger" size="small" style="margin-top: 4px">
                已过期
              </el-tag>
              <el-tag v-else-if="row.expirationWarning" type="warning" size="small" style="margin-top: 4px">
                {{ row.daysUntilExpiration }}天后过期
              </el-tag>
              <el-tag v-else type="success" size="small" style="margin-top: 4px">
                {{ row.daysUntilExpiration }}天后过期
              </el-tag>
            </div>
            <el-tag v-else type="info" size="small">永久有效</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '新增用户' : '编辑用户'"
      width="550px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="userForm"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="用户账号" prop="userAccount">
          <el-input 
            v-model="userForm.userAccount" 
            placeholder="请输入用户账号" 
            :disabled="dialogType === 'edit'"
          />
        </el-form-item>
        <el-form-item v-if="dialogType === 'add'" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item v-if="dialogType === 'edit'" label="新密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="不修改请留空" show-password />
        </el-form-item>
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="userForm.userName" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="userForm.gender">
            <el-radio :label="0">未知</el-radio>
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" :value="0" />
            <el-option label="会员" :value="1" />
            <el-option label="非会员" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch
            v-model="userForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
        <el-form-item label="过期时间">
          <el-date-picker
            v-model="userForm.expirationTime"
            type="datetime"
            placeholder="选择过期时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
            clearable
          />
          <div style="color: #909399; font-size: 12px; margin-top: 4px">
            不设置则永久有效
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量设置过期时间弹窗 -->
    <el-dialog
      v-model="batchDialogVisible"
      title="批量设置过期时间"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form label-width="100px">
        <el-form-item label="操作类型">
          <el-radio-group v-model="batchForm.operationType">
            <el-radio label="set">设置固定时间</el-radio>
            <el-radio label="extend">延长天数</el-radio>
            <el-radio label="shorten">缩短天数</el-radio>
            <el-radio label="clear">清空（永久有效）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="batchForm.operationType === 'set'" label="过期时间">
          <el-date-picker
            v-model="batchForm.expirationTime"
            type="datetime"
            placeholder="选择过期时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item v-if="['extend', 'shorten'].includes(batchForm.operationType)" label="天数">
          <el-input-number v-model="batchForm.days" :min="1" :max="3650" style="width: 100%" />
        </el-form-item>
        <el-alert
          v-if="batchForm.operationType === 'clear'"
          title="清空后用户将永久有效，不会过期"
          type="warning"
          :closable="false"
          style="margin-bottom: 16px"
        />
        <el-alert
          :title="`已选择 ${selectedUsers.length} 个用户`"
          type="info"
          :closable="false"
        />
      </el-form>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleBatchSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getUserList, getUserById, addUser, updateUser, deleteUser, batchSetExpiration } from '../api/user'
import { ROLE, ROLE_NAME, GENDER_NAME } from '../store/user'

// 加载状态
const loading = ref(false)
const submitLoading = ref(false)

// 表格数据
const tableData = ref([])
const total = ref(0)

// 查询表单
const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  userAccount: '',
  userName: '',
  phone: '',
  email: '',
  role: null,
  status: null,
  expirationStatus: 'all',
  warningDays: 7
})

// 多选相关
const selectedUsers = ref([])

// 弹窗相关
const dialogVisible = ref(false)
const dialogType = ref('add')
const formRef = ref(null)
const currentUserId = ref(null)

// 用户表单
const userForm = reactive({
  userAccount: '',
  password: '',
  userName: '',
  phone: '',
  email: '',
  gender: 0,
  role: 2,
  status: 1,
  expirationTime: null
})

// 批量操作弹窗
const batchDialogVisible = ref(false)
const batchForm = reactive({
  operationType: 'set',
  expirationTime: null,
  days: 30
})

// 表单验证规则
const formRules = {
  userAccount: [
    { required: true, message: '请输入用户账号', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]{4,16}$/, message: '4-16位字母、数字、下划线', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度6-32位', trigger: 'blur' }
  ],
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 获取角色标签类型
const getRoleTagType = (role) => {
  if (role === ROLE.ADMIN) return 'danger'
  if (role === ROLE.MEMBER) return 'success'
  return 'info'
}

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const params = { ...queryForm }
    // 清理空值
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null) {
        delete params[key]
      }
    })
    const res = await getUserList(params)
    if (res.success) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  queryForm.pageNum = 1
  fetchUserList()
}

// 重置
const handleReset = () => {
  queryForm.pageNum = 1
  queryForm.userAccount = ''
  queryForm.userName = ''
  queryForm.phone = ''
  queryForm.email = ''
  queryForm.role = null
  queryForm.status = null
  queryForm.expirationStatus = 'all'
  queryForm.warningDays = 7
  fetchUserList()
}

// 表格多选
const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

// 批量设置过期时间
const handleBatchExpiration = () => {
  batchDialogVisible.value = true
}

// 提交批量操作
const handleBatchSubmit = async () => {
  if (selectedUsers.value.length === 0) {
    ElMessage.warning('请先选择用户')
    return
  }

  // 参数校验
  if (batchForm.operationType === 'set' && !batchForm.expirationTime) {
    ElMessage.warning('请选择过期时间')
    return
  }
  if (['extend', 'shorten'].includes(batchForm.operationType) && (!batchForm.days || batchForm.days <= 0)) {
    ElMessage.warning('请输入有效的天数')
    return
  }

  try {
    submitLoading.value = true
    const userIds = selectedUsers.value.map(user => user.id)
    const data = {
      userIds,
      operationType: batchForm.operationType,
      expirationTime: batchForm.expirationTime,
      days: batchForm.days
    }

    const res = await batchSetExpiration(data)
    if (res.success) {
      ElMessage.success(res.data.message || '批量操作成功')
      batchDialogVisible.value = false
      fetchUserList()
    }
  } catch (error) {
    console.error('批量操作失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 分页大小改变
const handleSizeChange = () => {
  queryForm.pageNum = 1
  fetchUserList()
}

// 当前页改变
const handleCurrentChange = () => {
  fetchUserList()
}

// 新增用户
const handleAdd = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = async (row) => {
  dialogType.value = 'edit'
  currentUserId.value = row.id
  try {
    const res = await getUserById(row.id)
    if (res.success) {
      const data = res.data
      userForm.userAccount = data.userAccount
      userForm.userName = data.userName
      userForm.phone = data.phone
      userForm.email = data.email
      userForm.gender = data.gender
      userForm.role = data.role
      userForm.status = data.status
      userForm.expirationTime = data.expirationTime || null
      userForm.password = ''
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取用户详情失败:', error)
  }
}

// 删除用户
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除用户"${row.userName}"吗？`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteUser(row.id)
      if (res.success) {
        ElMessage.success('删除成功')
        fetchUserList()
      }
    } catch (error) {
      console.error('删除用户失败:', error)
    }
  }).catch(() => {})
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        const data = { ...userForm }
        // 编辑时如果密码为空则不提交
        if (dialogType.value === 'edit' && !data.password) {
          delete data.password
        }
        
        let res
        if (dialogType.value === 'add') {
          res = await addUser(data)
        } else {
          res = await updateUser(currentUserId.value, data)
        }
        
        if (res.success) {
          ElMessage.success(dialogType.value === 'add' ? '新增成功' : '更新成功')
          dialogVisible.value = false
          fetchUserList()
        }
      } catch (error) {
        console.error('提交失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  userForm.userAccount = ''
  userForm.password = ''
  userForm.userName = ''
  userForm.phone = ''
  userForm.email = ''
  userForm.gender = 0
  userForm.role = 2
  userForm.status = 1
  userForm.expirationTime = null
  currentUserId.value = null
  formRef.value?.clearValidate()
}

// 初始化
onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.user-list-container {
  padding: 0;
}

.search-card {
  background: white;
  border-radius: 8px;
  padding: 20px 20px 0;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 16px;
  margin-right: 16px;
}

.action-bar {
  margin-bottom: 16px;
}

.table-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.pagination-wrapper {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 响应式 */
@media (max-width: 768px) {
  .search-form :deep(.el-form-item) {
    width: 100%;
    margin-right: 0;
  }
  
  .search-form :deep(.el-input),
  .search-form :deep(.el-select) {
    width: 100% !important;
  }
}
</style>
