<template>
  <el-card 
    class="article-card" 
    shadow="hover"
    @click="handleClick"
  >
    <template #header>
      <div class="card-header">
        <span class="title" :title="article.title">{{ article.title }}</span>
        <el-tag 
          v-if="article.status !== undefined" 
          :type="getStatusType(article.status)"
          size="small"
          class="status-tag"
        >
          {{ getStatusText(article.status) }}
        </el-tag>
      </div>
    </template>
    
    <div class="card-body">
      <!-- 封面图片 -->
      <div v-if="article.coverImage" class="cover-image">
        <img :src="getImageUrl(article.coverImage)" :alt="article.title" />
      </div>
      
      <!-- 摘要内容 -->
      <div class="summary" v-if="article.summary">
        {{ article.summary }}
      </div>
      
      <!-- 内容预览（如果没有摘要） -->
      <div class="content-preview" v-else-if="article.content">
        {{ article.content.substring(0, 100) }}...
      </div>
    </div>
    
    <div class="card-footer">
      <div class="author-info">
        <el-avatar 
          :size="24" 
          :src="getAvatarUrl(article.authorId)" 
          class="author-avatar"
        >
          {{ getInitial(article.authorName) }}
        </el-avatar>
        <span class="author-name">{{ article.authorName || '未知' }}</span>
      </div>
      
      <div class="meta-info">
        <div class="category" v-if="article.categoryName">
          <el-tag type="info" size="small">{{ article.categoryName }}</el-tag>
        </div>
        <div class="time">{{ formatDate(article.createdTime) }}</div>
      </div>
    </div>
    
    <!-- 统计信息 -->
    <div class="stats" v-if="showStats">
      <div class="stat-item">
        <el-icon><View /></el-icon>
        <span>{{ article.viewCount || 0 }}</span>
      </div>
      <div class="stat-item">
        <el-icon><Star /></el-icon>
        <span>{{ article.likeCount || 0 }}</span>
      </div>
      <div class="stat-item">
        <el-icon><ChatLineRound /></el-icon>
        <span>{{ article.commentCount || 0 }}</span>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { View, Star, ChatLineRound } from '@element-plus/icons-vue'

// 定义props
const props = defineProps({
  article: {
    type: Object,
    required: true
  },
  showStats: {
    type: Boolean,
    default: true
  },
  clickable: {
    type: Boolean,
    default: true
  }
})

// 定义emit
const emit = defineEmits(['click'])

// 使用router
const router = useRouter()

// 计算属性
const getStatusType = (status) => {
  switch (status) {
    case 0: return 'info' // 草稿
    case 1: return 'success' // 发布
    case 2: return 'warning' // 隐藏
    default: return 'info'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 0: return '草稿'
    case 1: return '已发布'
    case 2: return '已隐藏'
    default: return '未知'
  }
}

// 方法
const getImageUrl = (imagePath) => {
  if (!imagePath) return ''
  if (imagePath.startsWith('http')) {
    return imagePath
  }
  // 使用后端API的基础URL，通过API端点访问上传的文件
  const backendBaseUrl = window.location.origin.replace(':3000', ':8080'); // 假设后端运行在8080端口
  return `${backendBaseUrl}/api/v1/files/access/${imagePath}`
}

const getAvatarUrl = (userId) => {
  // 这里可以返回用户头像URL，如果有的话
  return ''
}

const getInitial = (name) => {
  if (!name) return '?'
  return name.charAt(0).toUpperCase()
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

const handleClick = () => {
  if (props.clickable) {
    router.push(`/article/detail/${props.article.id}`)
    emit('click', props.article)
  }
}
</script>

<style scoped>
.article-card {
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 16px;
  border-radius: 8px;
  overflow: hidden;
}

.article-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: calc(100% - 60px);
}

.status-tag {
  flex-shrink: 0;
}

.card-body {
  padding: 0 0 16px 0;
}

.cover-image {
  width: 100%;
  height: 120px;
  overflow: hidden;
  border-radius: 4px;
  margin-bottom: 12px;
}

.cover-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.summary,
.content-preview {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-avatar {
  background: linear-gradient(135deg, #1A6B9F 0%, #2AA9E0 100%);
}

.author-name {
  font-size: 13px;
  color: #909399;
}

.meta-info {
  text-align: right;
}

.category {
  margin-bottom: 4px;
}

.time {
  font-size: 12px;
  color: #909399;
}

.stats {
  display: flex;
  justify-content: space-around;
  padding: 12px 0 0;
  border-top: 1px solid #ebeef5;
  margin-top: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.stat-item .el-icon {
  font-size: 14px;
}
</style>