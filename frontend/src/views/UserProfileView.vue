<template>
  <div class="profile-container">
    <!-- 个人信息卡片 -->
    <div class="profile-card">
      <div class="profile-header">
        <div class="avatar-section">
          <el-avatar :size="80" :src="userStore.userInfo.avatar || ''">
            {{ userStore.userInfo.userName?.charAt(0) || 'U' }}
          </el-avatar>
          <div class="user-basic">
            <h2>{{ userStore.userInfo.userName || '用户' }}</h2>
            <div class="user-tags">
              <el-tag :type="getRoleTagType(userStore.userInfo.role)" size="small">
                {{ userStore.roleName }}
              </el-tag>
              <el-tag :type="userStore.userInfo.status === 1 ? 'success' : 'danger'" size="small">
                {{ userStore.userInfo.status === 1 ? '正常' : '禁用' }}
              </el-tag>
            </div>
          </div>
        </div>
        <el-button type="primary" @click="handleEdit">
          <el-icon><Edit /></el-icon>编辑资料
        </el-button>
      </div>

      <el-divider />

      <div class="profile-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户账号">
            {{ userStore.userInfo.userAccount || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="用户名">
            {{ userStore.userInfo.userName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="手机号">
            {{ userStore.userInfo.phone || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ userStore.userInfo.email || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="性别">
            {{ GENDER_NAME[userStore.userInfo.gender] || '未知' }}
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">
            {{ userStore.userInfo.createdTime || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑个人资料"
      width="500px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form
        ref="formRef"
        :model="editForm"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="用户账号">
          <el-input :model-value="userStore.userInfo.userAccount" disabled />
        </el-form-item>
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="editForm.userName" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="editForm.gender">
            <el-radio :label="0">未知</el-radio>
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 管理员可见的字段 -->
        <template v-if="userStore.isAdmin">
          <el-divider content-position="left">管理员专属</el-divider>
          <el-form-item label="角色">
            <el-select v-model="editForm.role" style="width: 100%">
              <el-option label="管理员" :value="0" />
              <el-option label="会员" :value="1" />
              <el-option label="非会员" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-switch
              v-model="editForm.status"
              :active-value="1"
              :inactive-value="0"
              active-text="启用"
              inactive-text="禁用"
            />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit } from '@element-plus/icons-vue'
import { getCurrentUser, updateUser } from '../api/user'
import { useUserStore, ROLE, GENDER_NAME } from '../store/user'

const userStore = useUserStore()

// 弹窗相关
const dialogVisible = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)

// 编辑表单
const editForm = reactive({
  userName: '',
  phone: '',
  email: '',
  gender: 0,
  role: 2,
  status: 1
})

// 表单验证规则
const formRules = {
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
  ]
}

// 获取角色标签类型
const getRoleTagType = (role) => {
  if (role === ROLE.ADMIN) return 'danger'
  if (role === ROLE.MEMBER) return 'success'
  return 'info'
}

// 刷新用户信息
const refreshUserInfo = async () => {
  try {
    const res = await getCurrentUser()
    if (res.success) {
      userStore.setUserInfo(res.data)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

// 打开编辑弹窗
const handleEdit = () => {
  editForm.userName = userStore.userInfo.userName || ''
  editForm.phone = userStore.userInfo.phone || ''
  editForm.email = userStore.userInfo.email || ''
  editForm.gender = userStore.userInfo.gender ?? 0
  editForm.role = userStore.userInfo.role ?? 2
  editForm.status = userStore.userInfo.status ?? 1
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        // 构建更新数据
        const data = {
          userName: editForm.userName,
          phone: editForm.phone,
          email: editForm.email,
          gender: editForm.gender
        }
        
        // 管理员可以修改角色和状态
        if (userStore.isAdmin) {
          data.role = editForm.role
          data.status = editForm.status
        }
        
        const res = await updateUser(userStore.userId, data)
        if (res.success) {
          ElMessage.success('更新成功')
          dialogVisible.value = false
          // 更新本地用户信息
          userStore.updateUserInfo(data)
          // 刷新用户信息
          await refreshUserInfo()
        }
      } catch (error) {
        console.error('更新失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  formRef.value?.clearValidate()
}

// 初始化
onMounted(() => {
  refreshUserInfo()
})
</script>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 0 auto;
}

.profile-card {
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-section :deep(.el-avatar) {
  background: linear-gradient(135deg, #1A6B9F 0%, #2AA9E0 100%);
  font-size: 28px;
  font-weight: 600;
}

.user-basic h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.user-tags {
  display: flex;
  gap: 8px;
}

.profile-content {
  margin-top: 8px;
}

.profile-content :deep(.el-descriptions__label) {
  width: 100px;
  font-weight: 500;
}

/* 响应式 */
@media (max-width: 768px) {
  .profile-card {
    padding: 20px;
  }
  
  .profile-header {
    flex-direction: column;
    gap: 20px;
  }
  
  .avatar-section {
    flex-direction: column;
    text-align: center;
  }
  
  .user-basic h2 {
    font-size: 20px;
  }
  
  .user-tags {
    justify-content: center;
  }
  
  .profile-content :deep(.el-descriptions) {
    --el-descriptions-item-bordered-label-background: #fafafa;
  }
}
</style>
