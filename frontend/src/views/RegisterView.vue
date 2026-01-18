<template>
  <div class="register-container">
    <!-- 水波背景动画 -->
    <div class="wave-bg">
      <div class="wave wave1"></div>
      <div class="wave wave2"></div>
      <div class="wave wave3"></div>
    </div>

    <div class="register-card">
      <!-- Logo 区域 -->
      <div class="register-logo">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <circle cx="15" cy="4" r="2" />
            <path d="M2 14c1.5 0 2.5 1 4 1s2.5-1 4-1 2.5 1 4 1 2.5-1 4-1 2.5 1 4 1v-2c-1.5 0-2.5-1-4-1s-2.5 1-4 1-2.5-1-4-1-2.5 1-4 1-2.5-1-4-1v2z"/>
            <path d="M10.75 11c.69 0 1.25-.56 1.25-1.25V8.5h2.5c.55 0 1-.45 1-1s-.45-1-1-1h-3.25c-.69 0-1.25.56-1.25 1.25v1.25H7.5c-.55 0-1 .45-1 1s.45 1 1 1h3.25z"/>
          </svg>
        </div>
      </div>

      <div class="register-header">
        <h1 class="register-title">用户注册</h1>
        <p class="register-subtitle">创建您的账户，开始游泳之旅</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        label-position="top"
      >
        <el-form-item label="用户账号" prop="userAccount">
          <el-input
            v-model="registerForm.userAccount"
            placeholder="4-16位字母、数字、下划线"
            prefix-icon="User"
            size="large"
            clearable
          />
        </el-form-item>

        <el-form-item label="用户名" prop="userName">
          <el-input
            v-model="registerForm.userName"
            placeholder="请输入您的姓名"
            prefix-icon="Avatar"
            size="large"
            clearable
          />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="至少6位"
                prefix-icon="Lock"
                size="large"
                show-password
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="再次输入密码"
                prefix-icon="Lock"
                size="large"
                show-password
                clearable
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="手机号码" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入11位手机号"
            prefix-icon="Phone"
            size="large"
            maxlength="11"
            clearable
          />
        </el-form-item>

        <el-form-item label="邮箱地址" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱地址"
            prefix-icon="Message"
            size="large"
            clearable
          />
        </el-form-item>

        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="registerForm.gender" size="large">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="register-button"
            :loading="loading"
            @click="handleRegister"
          >
            <span v-if="!loading" class="button-content">
              <svg class="button-icon" viewBox="0 0 24 24" fill="currentColor">
                <path d="M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z"/>
              </svg>
              立即注册
            </span>
            <span v-else>注册中...</span>
          </el-button>
        </el-form-item>

        <div class="register-footer">
          <span class="footer-text">已有账号？</span>
          <el-button type="text" class="footer-link" @click="goToLogin">
            立即登录
          </el-button>
        </div>
      </el-form>
    </div>

    <!-- 装饰元素 -->
    <div class="decoration-circles">
      <div class="circle circle1"></div>
      <div class="circle circle2"></div>
      <div class="circle circle3"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../api/user'

const router = useRouter()

// 表单引用
const registerFormRef = ref(null)
const loading = ref(false)

// 注册表单数据
const registerForm = reactive({
  userAccount: '',
  userName: '',
  password: '',
  confirmPassword: '',
  phone: '',
  email: '',
  gender: 1
})

// 自定义验证规则
const validateAccount = (rule, value, callback) => {
  const reg = /^[a-zA-Z0-9_]{4,16}$/
  if (!value) {
    callback(new Error('请输入用户账号'))
  } else if (!reg.test(value)) {
    callback(new Error('账号格式不正确，只能包含4-16位字母、数字、下划线'))
  } else {
    callback()
  }
}

const validatePassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 6 || value.length > 32) {
    callback(new Error('密码长度必须在6-32位之间'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次密码输入不一致'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  const reg = /^1[3-9]\d{9}$/
  if (!value) {
    callback(new Error('请输入手机号码'))
  } else if (!reg.test(value)) {
    callback(new Error('手机号码格式不正确'))
  } else {
    callback()
  }
}

const validateEmail = (rule, value, callback) => {
  const reg = /^[a-zA-Z0-9_.-]+@[a-zA-Z0-9-]+(\.[a-zA-Z0-9-]+)*\.[a-zA-Z0-9]{2,6}$/
  if (!value) {
    callback(new Error('请输入邮箱地址'))
  } else if (!reg.test(value)) {
    callback(new Error('邮箱地址格式不正确'))
  } else {
    callback()
  }
}

// 表单验证规则
const registerRules = {
  userAccount: [
    { required: true, validator: validateAccount, trigger: 'blur' }
  ],
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度必须在2-50位之间', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ],
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' }
  ],
  email: [
    { required: true, validator: validateEmail, trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ]
}

// 注册处理
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await register(registerForm)
        
        if (response.success) {
          ElMessage.success('注册成功！请登录')
          
          // 跳转到登录页
          setTimeout(() => {
            router.push('/login')
          }, 1500)
        }
      } catch (error) {
        console.error('注册失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

// 跳转到登录页
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #F7FAFC 0%, #E3F2FD 30%, #90CAF9 70%, #1A6B9F 100%);
  padding: 40px 20px;
  position: relative;
  overflow: hidden;
}

/* 水波背景动画 */
.wave-bg {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: 0;
  opacity: 0.15;
}

.wave {
  position: absolute;
  width: 200%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(42, 169, 224, 0.3), transparent);
  animation: wave 15s linear infinite;
}

.wave1 {
  animation-delay: 0s;
  opacity: 0.7;
}

.wave2 {
  animation-delay: -5s;
  opacity: 0.5;
}

.wave3 {
  animation-delay: -10s;
  opacity: 0.3;
}

@keyframes wave {
  0% {
    transform: translateX(-50%) translateY(0) rotate(0deg);
  }
  50% {
    transform: translateX(-25%) translateY(-20px) rotate(180deg);
  }
  100% {
    transform: translateX(0) translateY(0) rotate(360deg);
  }
}

/* 装饰圆圈 */
.decoration-circles {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: 0;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(42, 169, 224, 0.1);
  animation: float 20s ease-in-out infinite;
}

.circle1 {
  width: 250px;
  height: 250px;
  top: -80px;
  right: -80px;
  animation-delay: 0s;
}

.circle2 {
  width: 180px;
  height: 180px;
  bottom: -40px;
  left: -40px;
  animation-delay: -7s;
}

.circle3 {
  width: 120px;
  height: 120px;
  top: 40%;
  left: 5%;
  animation-delay: -14s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-30px) rotate(180deg);
  }
}

/* 注册卡片 */
.register-card {
  width: 100%;
  max-width: 640px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(26, 107, 159, 0.15), 
              0 0 0 1px rgba(255, 255, 255, 0.5) inset;
  padding: 48px 40px;
  position: relative;
  z-index: 1;
  animation: slideUpFadeIn 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes slideUpFadeIn {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* Logo 区域 */
.register-logo {
  text-align: center;
  margin-bottom: 32px;
}

.logo-icon {
  width: 72px;
  height: 72px;
  margin: 0 auto;
  background: linear-gradient(135deg, #1A6B9F 0%, #2AA9E0 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 24px rgba(26, 107, 159, 0.3);
  animation: logoFloat 3s ease-in-out infinite;
}

.logo-icon svg {
  width: 40px;
  height: 40px;
}

@keyframes logoFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8px);
  }
}

/* 头部区域 */
.register-header {
  text-align: center;
  margin-bottom: 36px;
}

.register-title {
  font-size: 32px;
  font-weight: 700;
  background: linear-gradient(135deg, #1A6B9F 0%, #2AA9E0 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0 0 12px 0;
  line-height: 1.2;
  letter-spacing: 1px;
}

.register-subtitle {
  font-size: 14px;
  color: #7F8C8D;
  margin: 0;
  line-height: 1.6;
  font-weight: 400;
}

/* 表单区域 */
.register-form {
  margin-top: 32px;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 22px;
}

.register-form :deep(.el-form-item__label) {
  font-size: 14px;
  font-weight: 600;
  color: #2C3E50;
  margin-bottom: 8px;
  letter-spacing: 0.3px;
}

.register-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  background: #F7FAFC;
  box-shadow: 0 0 0 1px rgba(189, 195, 199, 0.3) inset;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 11px 15px;
}

.register-form :deep(.el-input__wrapper:hover) {
  background: #FFFFFF;
  box-shadow: 0 0 0 1px #2AA9E0 inset, 0 2px 8px rgba(42, 169, 224, 0.1);
  transform: translateY(-1px);
}

.register-form :deep(.el-input__wrapper.is-focus) {
  background: #FFFFFF;
  box-shadow: 0 0 0 2px #2AA9E0 inset, 0 4px 12px rgba(42, 169, 224, 0.2);
  transform: translateY(-2px);
}

.register-form :deep(.el-input__inner) {
  font-size: 14px;
  color: #2C3E50;
}

.register-form :deep(.el-input__inner::placeholder) {
  color: #BDC3C7;
}

/* 单选框样式 */
.register-form :deep(.el-radio-group) {
  display: flex;
  gap: 24px;
}

.register-form :deep(.el-radio) {
  margin-right: 0;
}

.register-form :deep(.el-radio__label) {
  font-size: 14px;
  color: #2C3E50;
  font-weight: 500;
}

.register-form :deep(.el-radio__input.is-checked .el-radio__inner) {
  background: linear-gradient(135deg, #1A6B9F 0%, #2AA9E0 100%);
  border-color: #2AA9E0;
}

.register-form :deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #2AA9E0;
}

/* 注册按钮 */
.register-button {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #1A6B9F 0%, #2AA9E0 100%);
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  margin-top: 12px;
  box-shadow: 0 4px 12px rgba(26, 107, 159, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.register-button::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.register-button:hover::before {
  width: 300px;
  height: 300px;
}

.register-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(26, 107, 159, 0.4);
}

.register-button:active {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(26, 107, 159, 0.3);
}

.button-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  position: relative;
  z-index: 1;
}

.button-icon {
  width: 18px;
  height: 18px;
  transition: transform 0.3s;
}

.register-button:hover .button-icon {
  transform: scale(1.1);
}

/* 底部链接 */
.register-footer {
  text-align: center;
  margin-top: 28px;
  padding-top: 24px;
  border-top: 1px solid rgba(189, 195, 199, 0.2);
}

.footer-text {
  font-size: 14px;
  color: #7F8C8D;
  font-weight: 400;
}

.footer-link {
  font-size: 14px;
  color: #2AA9E0;
  padding: 0 4px;
  font-weight: 600;
  transition: all 0.3s;
}

.footer-link:hover {
  color: #1A6B9F;
  transform: translateX(2px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-card {
    padding: 36px 28px;
    border-radius: 12px;
    max-width: 100%;
  }
  
  .register-title {
    font-size: 28px;
  }
  
  .logo-icon {
    width: 64px;
    height: 64px;
  }
  
  .logo-icon svg {
    width: 36px;
    height: 36px;
  }
  
  .register-form :deep(.el-col) {
    width: 100% !important;
    max-width: 100%;
  }
  
  .register-form :deep(.el-row) {
    display: block;
  }
  
  .circle1, .circle2, .circle3 {
    display: none;
  }
}

@media (max-width: 480px) {
  .register-container {
    padding: 20px 16px;
  }
  
  .register-card {
    padding: 32px 24px;
  }
  
  .register-title {
    font-size: 24px;
  }
  
  .register-form :deep(.el-radio-group) {
    gap: 16px;
  }
}
</style>
