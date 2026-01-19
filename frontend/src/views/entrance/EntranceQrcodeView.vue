<template>
  <div class="entrance-qrcode-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>生成入场码</h2>
      <p class="page-desc">生成入场二维码，向前台展示扫描入场</p>
    </div>

    <!-- 生成按钮区域 -->
    <el-card v-if="!qrcodeData" class="generate-card" shadow="never">
      <div class="generate-content">
        <el-icon class="qrcode-icon" :size="80"><Tickets /></el-icon>
        <p class="generate-tip">点击下方按钮生成入场二维码</p>
        <el-button 
          type="primary" 
          size="large"
          :loading="loading"
          @click="handleGenerate"
        >
          <el-icon><Plus /></el-icon>
          生成入场码
        </el-button>
        <el-alert 
          class="warning-alert"
          type="warning" 
          :closable="false"
          show-icon
        >
          <template #title>
            <span>该二维码为一次性使用，扫码后将自动失效，请向前台展示扫描</span>
          </template>
        </el-alert>
      </div>
    </el-card>

    <!-- 二维码展示区域 -->
    <el-card v-else class="qrcode-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">
            <el-icon><UserFilled /></el-icon>
            {{ qrcodeData.userName }} 的入场码
          </span>
          <el-button 
            type="primary" 
            size="small"
            @click="handleRegenerate"
          >
            <el-icon><Refresh /></el-icon>
            重新生成
          </el-button>
        </div>
      </template>

      <div class="qrcode-content">
        <!-- 二维码显示 -->
        <div class="qrcode-wrapper" @click="toggleSize">
          <qrcode-vue3
            :value="qrcodeData.qrcodeToken"
            :size="currentSize"
            :dotsOptions="{ type: 'rounded', color: '#409EFF' }"
            :cornersSquareOptions="{ type: 'extra-rounded', color: '#409EFF' }"
            image="/logo.png"
            :imageOptions="{ hideBackgroundDots: true, imageSize: 0.4, margin: 0 }"
            class="qrcode-image"
          />
          <p class="size-tip">点击二维码可缩放</p>
        </div>

        <!-- 全屏按钮 -->
        <el-button 
          type="primary" 
          size="large"
          class="fullscreen-btn"
          @click="showFullscreen = true"
        >
          <el-icon><FullScreen /></el-icon>
          全屏展示
        </el-button>

        <!-- 令牌信息 -->
        <div class="token-info">
          <p class="label">入场令牌</p>
          <div 
            class="token-text"
            @touchstart="handleTouchStart"
            @touchend="handleTouchEnd"
            @mousedown="handleTouchStart"
            @mouseup="handleTouchEnd"
            :title="isCopying ? '松开复制' : '长按复制'"
          >
            {{ formatToken(qrcodeData.qrcodeToken) }}
          </div>
          <p class="copy-hint">长按令牌可复制</p>
        </div>

        <!-- 生成时间 -->
        <div class="info-item">
          <el-icon><Clock /></el-icon>
          <span>生成时间：{{ formatDateTime(qrcodeData.generatedTime) }}</span>
        </div>

        <!-- 课程预约提示 -->
        <el-card 
          v-if="qrcodeData.courseBooking" 
          class="course-card" 
          shadow="never"
        >
          <template #header>
            <div class="course-header">
              <el-icon class="warning-icon"><WarningFilled /></el-icon>
              <span>课程预约提醒</span>
            </div>
          </template>
          <div class="course-content">
            <p class="course-item">
              <span class="label">课程名称：</span>
              <span class="value">{{ qrcodeData.courseBooking.courseName }}</span>
            </p>
            <p class="course-item">
              <span class="label">上课时间：</span>
              <span class="value">{{ formatDateTime(qrcodeData.courseBooking.startTime) }}</span>
            </p>
            <p class="course-item">
              <span class="label">教练姓名：</span>
              <span class="value">{{ qrcodeData.courseBooking.coachName }}</span>
            </p>
            <p class="course-tip">您有课程预约，请准时上课！</p>
          </div>
        </el-card>
      </div>
    </el-card>

    <!-- 全屏对话框 -->
    <el-dialog 
      v-model="showFullscreen"
      fullscreen
      :show-close="true"
      class="fullscreen-dialog"
    >
      <div class="fullscreen-content">
        <qrcode-vue3
          :value="qrcodeData?.qrcodeToken || ''"
          :size="500"
          :dotsOptions="{ type: 'rounded', color: '#409EFF' }"
          :cornersSquareOptions="{ type: 'extra-rounded', color: '#409EFF' }"
          class="fullscreen-qrcode"
        />
        <p class="fullscreen-name">{{ qrcodeData?.userName }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Tickets, Plus, UserFilled, Refresh, Clock, 
  WarningFilled, FullScreen 
} from '@element-plus/icons-vue'
import QrcodeVue3 from 'qrcode-vue3'
import { generateEntranceQrcode } from '@/api/entrance'

// 加载状态
const loading = ref(false)

// 二维码数据
const qrcodeData = ref(null)

// 全屏显示
const showFullscreen = ref(false)

// 二维码尺寸列表
const sizeList = [200, 300, 400]
const sizeIndex = ref(1)
const currentSize = computed(() => sizeList[sizeIndex.value])

// 长按复制相关
const isCopying = ref(false)
let touchTimer = null

// 生成入场二维码
const handleGenerate = async () => {
  loading.value = true
  try {
    const res = await generateEntranceQrcode()
    qrcodeData.value = res.data
    ElMessage.success('生成入场码成功')
  } catch (error) {
    ElMessage.error(error.message || '生成失败')
  } finally {
    loading.value = false
  }
}

// 重新生成
const handleRegenerate = async () => {
  qrcodeData.value = null
  await handleGenerate()
}

// 切换尺寸
const toggleSize = () => {
  sizeIndex.value = (sizeIndex.value + 1) % sizeList.length
}

// 长按开始
const handleTouchStart = () => {
  isCopying.value = true
  touchTimer = setTimeout(async () => {
    await copyToClipboard(qrcodeData.value.qrcodeToken)
    isCopying.value = false
  }, 800)
}

// 长按结束
const handleTouchEnd = () => {
  if (touchTimer) {
    clearTimeout(touchTimer)
    touchTimer = null
  }
  isCopying.value = false
}

// 复制到剪贴板
const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
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

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}
</script>

<style scoped>
.entrance-qrcode-container {
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

/* 生成卡片 */
.generate-card {
  border-radius: 12px;
}

.generate-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 20px;
}

.qrcode-icon {
  color: #409EFF;
  margin-bottom: 16px;
}

.generate-tip {
  font-size: 16px;
  color: #606266;
  margin-bottom: 24px;
}

.warning-alert {
  margin-top: 24px;
  width: 100%;
}

/* 二维码卡片 */
.qrcode-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.qrcode-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.qrcode-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  user-select: none;
}

.qrcode-image {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.qrcode-image:hover {
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.15);
  transform: scale(1.02);
}

.size-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.fullscreen-btn {
  width: 200px;
}

/* 令牌信息 */
.token-info {
  width: 100%;
  text-align: center;
}

.token-info .label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.token-text {
  font-family: 'Courier New', monospace;
  font-size: 16px;
  color: #409EFF;
  background: #f5f7fa;
  padding: 12px 20px;
  border-radius: 8px;
  cursor: pointer;
  user-select: none;
  transition: all 0.3s;
}

.token-text:active {
  background: #e4e7ed;
  transform: scale(0.98);
}

.copy-hint {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 8px;
}

/* 信息项 */
.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

/* 课程卡片 */
.course-card {
  width: 100%;
  border: 2px solid #E6A23C;
  border-radius: 8px;
  background: #fef5e7;
}

.course-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #E6A23C;
}

.warning-icon {
  font-size: 20px;
}

.course-content {
  color: #606266;
}

.course-item {
  margin: 8px 0;
  line-height: 1.8;
}

.course-item .label {
  color: #909399;
}

.course-item .value {
  color: #303133;
  font-weight: 500;
}

.course-tip {
  margin-top: 12px;
  padding: 8px 12px;
  background: #fff;
  border-radius: 4px;
  color: #E6A23C;
  font-weight: 500;
  text-align: center;
}

/* 全屏对话框 */
.fullscreen-dialog :deep(.el-dialog__body) {
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
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
}

.fullscreen-name {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .entrance-qrcode-container {
    padding: 10px;
  }

  .page-header h2 {
    font-size: 20px;
  }

  .generate-content {
    padding: 30px 15px;
  }

  .fullscreen-qrcode {
    width: 80vw !important;
    height: 80vw !important;
  }

  .fullscreen-name {
    font-size: 24px;
  }
}
</style>
