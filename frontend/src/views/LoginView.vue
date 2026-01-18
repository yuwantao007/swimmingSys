<template>
  <div class="login-container">
    <!-- 水波背景动画 -->
    <div class="wave-bg">
      <div class="wave wave1"></div>
      <div class="wave wave2"></div>
      <div class="wave wave3"></div>
    </div>

    <div class="login-card">
      <!-- Logo 区域 -->
      <div class="login-logo">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="currentColor">
            <circle cx="15" cy="4" r="2" />
            <path d="M2 14c1.5 0 2.5 1 4 1s2.5-1 4-1 2.5 1 4 1 2.5-1 4-1 2.5 1 4 1v-2c-1.5 0-2.5-1-4-1s-2.5 1-4 1-2.5-1-4-1-2.5 1-4 1-2.5-1-4-1v2z"/>
            <path d="M10.75 11c.69 0 1.25-.56 1.25-1.25V8.5h2.5c.55 0 1-.45 1-1s-.45-1-1-1h-3.25c-.69 0-1.25.56-1.25 1.25v1.25H7.5c-.55 0-1 .45-1 1s.45 1 1 1h3.25z"/>
          </svg>
        </div>
      </div>

      <div class="login-header">
        <h1 class="login-title">游泳馆管理系统</h1>
        <p class="login-subtitle">欢迎回来，请登录您的账户</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="userAccount">
          <el-input
            v-model="loginForm.userAccount"
            placeholder="请输入用户账号"
            prefix-icon="User"
            size="large"
            clearable
          >
            <template #prefix>
              <svg class="input-icon" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
              </svg>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
            clearable
          >
            <template #prefix>
              <svg class="input-icon" viewBox="0 0 24 24" fill="currentColor">
                <path d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"/>
              </svg>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handleLogin"
          >
            <span v-if="!loading" class="button-content">
              <svg class="button-icon" viewBox="0 0 24 24" fill="currentColor">
                <path d="M10 17l5-5-5-5v10z"/>
              </svg>
              立即登录
            </span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form-item>

        <div class="login-footer">
          <span class="footer-text">还没有账号？</span>
          <el-button type="text" class="footer-link" @click="goToRegister">
            立即注册
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
import { login } from '../api/user'
import { useUserStore } from '../store/user'

const router = useRouter()
const userStore = useUserStore()

// 表单引用
const loginFormRef = ref(null)
const loading = ref(false)

// 登录表单数据
const loginForm = reactive({
  userAccount: '',
  password: ''
})

// 表单验证规则
const loginRules = {
  userAccount: [
    { required: true, message: '请输入用户账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

// 登录处理
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await login(loginForm)
        
        if (response.success) {
          const { token, ...userInfo } = response.data
          
          // 保存token和用户信息到store
          userStore.login(token, userInfo)
          
          ElMessage.success('登录成功！')
          
          // 跳转到首页或原跳转前的页面
          const redirect = router.currentRoute.value.query.redirect || '/home'
          router.push(redirect)
        }
      } catch (error) {
        console.error('登录失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

// 跳转到注册页
const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #F7FAFC 0%, #E3F2FD 30%, #90CAF9 70%, #1A6B9F 100%);
  padding: 20px;
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
  width: 300px;
  height: 300px;
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.circle2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  right: -50px;
  animation-delay: -7s;
}

.circle3 {
  width: 150px;
  height: 150px;
  top: 50%;
  right: 10%;
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

/* 登录卡片 */
.login-card {
  width: 100%;
  max-width: 440px;
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
.login-logo {
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
.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-title {
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

.login-subtitle {
  font-size: 14px;
  color: #7F8C8D;
  margin: 0;
  line-height: 1.6;
  font-weight: 400;
}

/* 表单区域 */
.login-form {
  margin-top: 32px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 24px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  background: #F7FAFC;
  box-shadow: 0 0 0 1px rgba(189, 195, 199, 0.3) inset;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  padding: 12px 16px;
}

.login-form :deep(.el-input__wrapper:hover) {
  background: #FFFFFF;
  box-shadow: 0 0 0 1px #2AA9E0 inset, 0 2px 8px rgba(42, 169, 224, 0.1);
  transform: translateY(-1px);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  background: #FFFFFF;
  box-shadow: 0 0 0 2px #2AA9E0 inset, 0 4px 12px rgba(42, 169, 224, 0.2);
  transform: translateY(-2px);
}

.login-form :deep(.el-input__inner) {
  font-size: 14px;
  color: #2C3E50;
}

.login-form :deep(.el-input__inner::placeholder) {
  color: #BDC3C7;
}

/* 自定义图标 */
.input-icon {
  width: 20px;
  height: 20px;
  color: #7F8C8D;
  transition: color 0.3s;
}

.login-form :deep(.el-input__wrapper.is-focus) .input-icon {
  color: #2AA9E0;
}

/* 登录按钮 */
.login-button {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #1A6B9F 0%, #2AA9E0 100%);
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  box-shadow: 0 4px 12px rgba(26, 107, 159, 0.3);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
}

.login-button::before {
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

.login-button:hover::before {
  width: 300px;
  height: 300px;
}

.login-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(26, 107, 159, 0.4);
}

.login-button:active {
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

.login-button:hover .button-icon {
  transform: translateX(4px);
}

/* 底部链接 */
.login-footer {
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
  .login-card {
    padding: 36px 28px;
    border-radius: 12px;
  }
  
  .login-title {
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
  
  .circle1, .circle2, .circle3 {
    display: none;
  }
}

@media (max-width: 480px) {
  .login-container {
    padding: 16px;
  }
  
  .login-card {
    padding: 32px 24px;
  }
  
  .login-title {
    font-size: 24px;
  }
}
</style>
