<template>
  <div class="entrance-verify-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>扫码验证</h2>
      <p class="page-desc">扫描会员入场二维码或手动输入令牌验证入场</p>
    </div>

    <!-- 模式切换 -->
    <el-card class="mode-card" shadow="never">
      <el-radio-group v-model="verifyMode" class="mode-switch" @change="handleModeChange">
        <el-radio-button label="scanner">
          <el-icon><Camera /></el-icon>
          摄像头扫描
        </el-radio-button>
        <el-radio-button label="manual">
          <el-icon><Edit /></el-icon>
          手动输入
        </el-radio-button>
      </el-radio-group>
    </el-card>

    <!-- 扫码模式 -->
    <el-card v-if="verifyMode === 'scanner'" class="scanner-card" shadow="hover">
      <!-- 权限请求提示 -->
      <el-alert
        v-if="!cameraPermissionGranted && !cameraError"
        type="info"
        :closable="false"
        show-icon
        class="permission-alert"
      >
        <template #title>
          <span>摄像头权限请求</span>
        </template>
        <p>使用扫码功能需要访问您的摄像头。</p>
        <p>请在浏览器弹出的权限请求中点击"允许"。</p>
      </el-alert>

      <!-- 权限被拒绝提示 -->
      <el-alert
        v-if="cameraError && cameraError.name === 'NotAllowedError'"
        type="warning"
        :closable="false"
        show-icon
        class="permission-alert"
      >
        <template #title>
          <span>摄像头权限被拒绝</span>
        </template>
        <p>您拒绝了摄像头访问权限，无法使用扫码功能。</p>
        <p>请在浏览器设置中允许摄像头访问，或使用手动输入模式。</p>
        <el-button type="primary" plain @click="retryCamera" class="retry-btn">
          <el-icon><Refresh /></el-icon>
          重新请求权限
        </el-button>
        <el-button @click="verifyMode = 'manual'" class="retry-btn">
          <el-icon><Edit /></el-icon>
          切换到手动输入
        </el-button>
      </el-alert>

      <!-- 摄像头不可用提示 -->
      <el-alert
        v-if="cameraError && cameraError.name !== 'NotAllowedError'"
        type="error"
        :closable="false"
        show-icon
        class="permission-alert"
      >
        <template #title>
          <span>摄像头不可用</span>
        </template>
        <p>{{ cameraErrorMessage }}</p>
        <p>建议切换到手动输入模式继续操作。</p>
        <el-button type="primary" plain @click="retryCamera" class="retry-btn">
          <el-icon><Refresh /></el-icon>
          重试
        </el-button>
        <el-button @click="verifyMode = 'manual'" class="retry-btn">
          <el-icon><Edit /></el-icon>
          切换到手动输入
        </el-button>
      </el-alert>

      <div class="scanner-container">
        <!-- 加载状态 -->
        <div v-if="cameraLoading && !cameraError" class="camera-loading">
          <el-icon class="loading-icon" :size="60"><Loading /></el-icon>
          <p>正在打开摄像头...</p>
          <p class="loading-tip">如果长时间无响应，请检查浏览器权限设置</p>
        </div>

        <!-- 摄像头视频流 -->
        <qrcode-stream 
          v-show="!cameraError"
          @detect="onDetect"
          @error="onCameraError"
          @camera-on="onCameraOn"
          class="scanner-video"
          :track="paintOutline"
        >
          <div v-if="cameraReady" class="scanner-frame"></div>
        </qrcode-stream>
        
        <p v-if="cameraReady" class="scanner-tip">请将二维码对准扫描框</p>
      </div>
    </el-card>

    <!-- 手动输入模式 -->
    <el-card v-else class="manual-card" shadow="hover">
      <div class="manual-container">
        <el-input 
          v-model="manualToken" 
          placeholder="请输入或粘贴入场令牌"
          clearable
          size="large"
          class="token-input"
        >
          <template #append>
            <el-button @click="handlePaste">
              <el-icon><DocumentCopy /></el-icon>
              粘贴
            </el-button>
          </template>
        </el-input>
        <el-button 
          type="primary" 
          size="large"
          :loading="verifying"
          :disabled="!manualToken"
          @click="handleManualVerify"
          class="verify-btn"
        >
          <el-icon><Check /></el-icon>
          验证入场
        </el-button>
      </div>
    </el-card>

    <!-- 验证结果展示 -->
    <transition name="fade">
      <el-card 
        v-if="verifyResult" 
        :class="['result-card', resultClass]"
        shadow="always"
      >
        <div class="result-content">
          <el-icon :class="['result-icon', resultClass]" :size="80">
            <SuccessFilled v-if="verifyResult.success" />
            <CircleCloseFilled v-else />
          </el-icon>
          
          <h3 class="result-title">
            {{ verifyResult.success ? '验证成功，允许入场' : '验证失败' }}
          </h3>
          
          <div v-if="verifyResult.success" class="success-content">
            <p class="user-name">{{ verifyResult.data.userName }}</p>
            <p class="entrance-time">
              <el-icon><Clock /></el-icon>
              入场时间：{{ formatDateTime(verifyResult.data.entranceTime) }}
            </p>
            
            <!-- 课程提示 -->
            <el-card 
              v-if="verifyResult.data.courseInfo" 
              class="course-reminder"
              shadow="never"
            >
              <template #header>
                <div class="reminder-header">
                  <el-icon class="warning-icon"><WarningFilled /></el-icon>
                  <span>课程提醒</span>
                </div>
              </template>
              <div class="reminder-content">
                <p>课程：{{ verifyResult.data.courseInfo.courseName }}</p>
                <p>时间：{{ formatDateTime(verifyResult.data.courseInfo.startTime) }}</p>
                <p>教练：{{ verifyResult.data.courseInfo.coachName }}</p>
                <p class="reminder-tip">{{ verifyResult.data.courseInfo.tip }}</p>
              </div>
            </el-card>
          </div>
          
          <div v-else class="error-content">
            <p class="error-message">{{ verifyResult.message }}</p>
          </div>
        </div>
      </el-card>
    </transition>

    <!-- 最近验证记录 -->
    <el-card v-if="recentRecords.length > 0" class="recent-card" shadow="never">
      <template #header>
        <span class="recent-title">
          <el-icon><List /></el-icon>
          最近验证记录
        </span>
      </template>
      <div class="recent-list">
        <div 
          v-for="record in recentRecords" 
          :key="record.id"
          :class="['recent-item', record.success ? 'success' : 'failed']"
        >
          <el-icon class="record-icon">
            <SuccessFilled v-if="record.success" />
            <CircleCloseFilled v-else />
          </el-icon>
          <span class="record-name">{{ record.userName }}</span>
          <span class="record-time">{{ formatTime(record.time) }}</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onUnmounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Camera, Edit, Loading, DocumentCopy, Check,
  SuccessFilled, CircleCloseFilled, Clock,
  WarningFilled, List, Refresh
} from '@element-plus/icons-vue'
import { QrcodeStream } from 'vue-qrcode-reader'
import { verifyEntrance } from '@/api/entrance'

// 验证模式
const verifyMode = ref('scanner')

// 摄像头状态
const cameraReady = ref(false)
const cameraLoading = ref(true)
const cameraError = ref(null)
const cameraPermissionGranted = ref(false)

// 手动输入令牌
const manualToken = ref('')

// 验证中
const verifying = ref(false)

// 验证结果
const verifyResult = ref(null)

// 结果样式类
const resultClass = ref('')

// 摄像头错误信息
const cameraErrorMessage = computed(() => {
  if (!cameraError.value) return ''
  
  const error = cameraError.value
  if (error.name === 'NotAllowedError') {
    return '摄像头权限被拒绝'
  } else if (error.name === 'NotFoundError') {
    return '未检测到摄像头设备'
  } else if (error.name === 'NotReadableError') {
    return '摄像头正被其他应用占用'
  } else if (error.name === 'OverconstrainedError') {
    return '摄像头不支持所需的配置'
  } else if (error.name === 'StreamApiNotSupportedError') {
    return '当前浏览器不支持摄像头功能'
  } else if (error.name === 'InsecureContextError') {
    return '请使用HTTPS协议访问（本地localhost除外）'
  } else {
    return error.message || '摄像头初始化失败'
  }
})

// 最近验证记录（最多5条）
const recentRecords = ref([])

// 自动重置定时器
let resetTimer = null

// 摄像头就绪
const onCameraOn = () => {
  cameraReady.value = true
  cameraLoading.value = false
  cameraPermissionGranted.value = true
  cameraError.value = null
  ElMessage.success('摄像头已就绪，可以开始扫码')
}

// 摄像头错误
const onCameraError = (error) => {
  console.error('摄像头错误:', error)
  cameraReady.value = false
  cameraLoading.value = false
  cameraError.value = error
  
  // 根据不同错误类型提供不同提示
  if (error.name === 'NotAllowedError') {
    ElMessage.warning('摄像头权限被拒绝，请在浏览器设置中允许访问')
  } else if (error.name === 'NotFoundError') {
    ElMessage.error('未检测到摄像头设备')
  } else if (error.name === 'NotReadableError') {
    ElMessage.error('摄像头正被其他应用占用，请关闭其他使用摄像头的应用')
  } else if (error.name === 'StreamApiNotSupportedError') {
    ElMessage.error('当前浏览器不支持摄像头功能，请使用Chrome、Firefox或Edge浏览器')
  } else if (error.name === 'InsecureContextError') {
    ElMessage.error('请使用HTTPS协议访问')
  } else {
    ElMessage.error('摄像头初始化失败：' + (error.message || '未知错误'))
  }
}

// 重试摄像头
const retryCamera = () => {
  cameraError.value = null
  cameraReady.value = false
  cameraLoading.value = true
  cameraPermissionGranted.value = false
  ElMessage.info('正在重新请求摄像头权限...')
}

// 绘制检测框
const paintOutline = (detectedCodes, ctx) => {
  if (!detectedCodes || detectedCodes.length === 0) return
  
  for (const detectedCode of detectedCodes) {
    const { boundingBox } = detectedCode
    const { x, y, width, height } = boundingBox
    
    ctx.strokeStyle = '#67C23A'
    ctx.lineWidth = 3
    ctx.strokeRect(x, y, width, height)
  }
}

// 检测到二维码
const onDetect = async (detectedCodes) => {
  if (detectedCodes && detectedCodes.length > 0) {
    const token = detectedCodes[0].rawValue
    await performVerify(token)
  }
}

// 模式切换
const handleModeChange = () => {
  resetResult()
  if (verifyMode.value === 'scanner') {
    // 切换到扫码模式时重置摄像头状态
    cameraReady.value = false
    cameraLoading.value = true
    cameraError.value = null
    cameraPermissionGranted.value = false
  }
}

// 粘贴令牌
const handlePaste = async () => {
  try {
    const text = await navigator.clipboard.readText()
    manualToken.value = text.trim()
    ElMessage.success('令牌已粘贴')
  } catch (error) {
    ElMessage.warning('请手动粘贴令牌')
  }
}

// 手动验证
const handleManualVerify = async () => {
  if (!manualToken.value.trim()) {
    ElMessage.warning('请输入入场令牌')
    return
  }
  await performVerify(manualToken.value.trim())
  manualToken.value = ''
}

// 执行验证
const performVerify = async (token) => {
  if (verifying.value) return
  
  verifying.value = true
  try {
    const res = await verifyEntrance({ qrcodeToken: token })
    
    // 验证成功
    verifyResult.value = {
      success: true,
      data: res.data,
      message: ''
    }
    resultClass.value = 'success'
    
    // 添加到最近记录
    addRecentRecord({
      id: Date.now(),
      success: true,
      userName: res.data.userName,
      time: new Date()
    })
    
    // 3秒后自动重置
    autoReset()
    
  } catch (error) {
    // 验证失败
    verifyResult.value = {
      success: false,
      data: null,
      message: error.message || '验证失败'
    }
    resultClass.value = 'error'
    
    // 添加到最近记录
    addRecentRecord({
      id: Date.now(),
      success: false,
      userName: '验证失败',
      time: new Date()
    })
    
    // 5秒后自动重置
    autoReset(5000)
  } finally {
    verifying.value = false
  }
}

// 自动重置
const autoReset = (delay = 3000) => {
  if (resetTimer) {
    clearTimeout(resetTimer)
  }
  resetTimer = setTimeout(() => {
    resetResult()
  }, delay)
}

// 重置结果
const resetResult = () => {
  verifyResult.value = null
  resultClass.value = ''
}

// 添加最近记录
const addRecentRecord = (record) => {
  recentRecords.value.unshift(record)
  if (recentRecords.value.length > 5) {
    recentRecords.value.pop()
  }
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${hours}:${minutes}:${seconds}`
}

// 清理定时器
onUnmounted(() => {
  if (resetTimer) {
    clearTimeout(resetTimer)
  }
})
</script>

<style scoped>
.entrance-verify-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
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

/* 模式切换 */
.mode-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.mode-switch {
  display: flex;
  justify-content: center;
  width: 100%;
}

.mode-switch :deep(.el-radio-button) {
  flex: 1;
}

.mode-switch :deep(.el-radio-button__inner) {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

/* 扫码卡片 */
.scanner-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.permission-alert {
  margin-bottom: 20px;
}

.permission-alert p {
  margin: 8px 0;
  line-height: 1.6;
}

.retry-btn {
  margin-top: 12px;
  margin-right: 8px;
}

.scanner-container {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.scanner-video {
  width: 100%;
  max-width: 500px;
  border-radius: 8px;
  overflow: hidden;
}

.scanner-frame {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 250px;
  height: 250px;
  border: 3px solid #409EFF;
  border-radius: 12px;
  box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.3);
  animation: scan 2s ease-in-out infinite;
}

@keyframes scan {
  0%, 100% { border-color: #409EFF; }
  50% { border-color: #67C23A; }
}

.camera-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 20px;
  color: #909399;
}

.loading-tip {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 8px;
}

.loading-icon {
  animation: rotate 2s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.scanner-tip {
  margin-top: 16px;
  color: #909399;
  font-size: 14px;
}

/* 手动输入卡片 */
.manual-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.manual-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 20px;
}

.token-input {
  font-size: 16px;
}

.verify-btn {
  width: 100%;
}

/* 验证结果卡片 */
.result-card {
  margin-bottom: 20px;
  border-radius: 12px;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.result-card.success {
  border: 2px solid #67C23A;
  background: #f0f9ff;
}

.result-card.error {
  border: 2px solid #F56C6C;
  background: #fef0f0;
}

.result-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

.result-icon {
  margin-bottom: 16px;
}

.result-icon.success {
  color: #67C23A;
}

.result-icon.error {
  color: #F56C6C;
}

.result-title {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #303133;
}

.success-content,
.error-content {
  width: 100%;
  text-align: center;
}

.user-name {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 12px;
}

.entrance-time {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #606266;
  font-size: 16px;
  margin-bottom: 20px;
}

.error-message {
  font-size: 18px;
  color: #F56C6C;
  font-weight: 500;
}

/* 课程提醒卡片 */
.course-reminder {
  width: 100%;
  border: 2px solid #E6A23C;
  background: #fef5e7;
  margin-top: 20px;
}

.reminder-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #E6A23C;
}

.warning-icon {
  font-size: 20px;
}

.reminder-content {
  color: #606266;
  line-height: 1.8;
}

.reminder-content p {
  margin: 8px 0;
}

.reminder-tip {
  margin-top: 12px;
  padding: 8px 12px;
  background: #fff;
  border-radius: 4px;
  color: #E6A23C;
  font-weight: 600;
  text-align: center;
}

/* 最近记录 */
.recent-card {
  border-radius: 12px;
}

.recent-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

.recent-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recent-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 8px;
  background: #f5f7fa;
}

.recent-item.success {
  border-left: 4px solid #67C23A;
}

.recent-item.failed {
  border-left: 4px solid #F56C6C;
}

.record-icon {
  font-size: 20px;
}

.recent-item.success .record-icon {
  color: #67C23A;
}

.recent-item.failed .record-icon {
  color: #F56C6C;
}

.record-name {
  flex: 1;
  font-weight: 500;
  color: #303133;
}

.record-time {
  color: #909399;
  font-size: 14px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .entrance-verify-container {
    padding: 10px;
  }

  .page-header h2 {
    font-size: 20px;
  }

  .user-name {
    font-size: 24px;
  }

  .result-title {
    font-size: 20px;
  }

  .scanner-frame {
    width: 200px;
    height: 200px;
  }
}
</style>
