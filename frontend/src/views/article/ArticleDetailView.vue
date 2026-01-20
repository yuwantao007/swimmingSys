<template>
  <div class="article-detail">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="6" animated />
    </div>
    
    <div v-else-if="article" class="detail-content">
      <!-- 文章头部信息 -->
      <div class="article-header">
        <h1 class="article-title">{{ article.title }}</h1>
        
        <div class="article-meta">
          <div class="meta-item">
            <el-avatar 
              :size="32" 
              :src="getAvatarUrl(article.authorId)" 
              class="author-avatar"
            >
              {{ getInitial(article.authorName) }}
            </el-avatar>
            <span class="author-name">{{ article.authorName || '未知' }}</span>
          </div>
          
          <div class="meta-item">
            <el-icon><Clock /></el-icon>
            <span>{{ formatDate(article.createdTime) }}</span>
          </div>
          
          <div class="meta-item" v-if="article.categoryName">
            <el-icon><FolderOpened /></el-icon>
            <el-tag type="primary" size="small">{{ article.categoryName }}</el-tag>
          </div>
          
          <div class="meta-item">
            <el-icon><View /></el-icon>
            <span>{{ article.viewCount || 0 }} 次浏览</span>
          </div>
        </div>
        
        <!-- 状态标签 -->
        <div class="article-status">
          <el-tag 
            :type="getStatusType(article.status)"
            size="small"
          >
            {{ getStatusText(article.status) }}
          </el-tag>
        </div>
      </div>
      
      <!-- 封面图片 -->
      <div v-if="article.coverImage" class="cover-image">
        <img :src="getImageUrl(article.coverImage)" :alt="article.title" />
      </div>
      
      <!-- 文章内容 -->
      <div class="article-content">
        <div v-if="article.content" class="content-text">
          {{ article.content }}
        </div>
        <div v-else-if="article.summary" class="content-summary">
          {{ article.summary }}
        </div>
      </div>
      
      <!-- 附件区域 -->
      <div v-if="attachments.length > 0" class="attachments-section">
        <h3>相关附件</h3>
        <el-row :gutter="16">
          <el-col 
            v-for="attachment in attachments" 
            :key="attachment.id" 
            :span="8"
          >
            <el-card class="attachment-card" shadow="hover">
              <div class="attachment-info">
                <el-icon><Document /></el-icon>
                <span class="file-name">{{ attachment.fileName }}</span>
              </div>
              <div class="attachment-meta">
                <el-tag size="small">{{ formatFileSize(attachment.fileSize) }}</el-tag>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="downloadAttachment(attachment)"
                >
                  下载
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 互动区域 -->
      <div class="interaction-section">
        <el-button 
          :type="liked ? 'warning' : 'default'" 
          :icon="Star"
          @click="handleLike"
        >
          {{ liked ? '已点赞' : '点赞' }} ({{ article.likeCount || 0 }})
        </el-button>
        
        <el-button 
          type="primary" 
          :icon="Share"
          @click="handleShare"
        >
          分享
        </el-button>
      </div>
      
      <!-- 相关资料推荐 -->
      <div class="recommendation-section" v-if="relatedArticles.length > 0">
        <h3>相关推荐</h3>
        <div class="recommendation-list">
          <ArticleCard 
            v-for="related in relatedArticles" 
            :key="related.id" 
            :article="related"
            :show-stats="false"
            @click="handleRelatedClick"
          />
        </div>
      </div>
    </div>
    
    <div v-else class="error-container">
      <el-result icon="error" title="资料不存在" subTitle="抱歉，您访问的资料不存在或已被删除">
        <template #extra>
          <el-button type="primary" @click="$router.go(-1)">返回</el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Clock, 
  FolderOpened, 
  View, 
  Document, 
  Share, 
  Star 
} from '@element-plus/icons-vue'
import ArticleCard from '@/components/Article/ArticleCard.vue'
import { getArticleDetail, increaseViewCount, toggleLike } from '@/api/article'

// 使用route和router
const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(true)
const article = ref(null)
const attachments = ref([])
const relatedArticles = ref([])
const liked = ref(false)

// 方法
const loadArticle = async () => {
  const id = route.params.id
  if (!id) {
    loading.value = false
    return
  }
  
  try {
    const response = await getArticleDetail(id)
    if (response.code === 200) {
      article.value = response.data
      
      // 增加浏览次数
      try {
        await increaseViewCount(id)
        // 更新本地浏览数
        if (article.value) {
          article.value.viewCount = (article.value.viewCount || 0) + 1
        }
      } catch (err) {
        console.error('增加浏览次数失败:', err)
      }
    } else {
      throw new Error(response.message || '获取资料详情失败')
    }
  } catch (error) {
    console.error('获取资料详情失败:', error)
    ElMessage.error(error.message || '获取资料详情失败')
  } finally {
    loading.value = false
  }
}

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
  return date.toLocaleString('zh-CN')
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const handleLike = async () => {
  if (!article.value) return
  
  try {
    const response = await toggleLike(article.value.id)
    if (response.code === 200) {
      liked.value = !liked.value
      // 更新本地点赞数
      if (article.value) {
        article.value.likeCount = (article.value.likeCount || 0) + (liked.value ? 1 : -1)
      }
      ElMessage.success(response.message || '操作成功')
    } else {
      throw new Error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('点赞失败:', error)
    ElMessage.error(error.message || '操作失败')
  }
}

const handleShare = () => {
  // 复制链接到剪贴板
  const shareUrl = window.location.href
  navigator.clipboard.writeText(shareUrl).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    // 如果不支持clipboard API，则使用提示
    ElMessage.info('请手动复制链接: ' + shareUrl)
  })
}

const downloadAttachment = (attachment) => {
  // 创建下载链接
  const link = document.createElement('a')
  // 使用后端API的基础URL，确保附件能正确下载
  const backendBaseUrl = window.location.origin.replace(':3000', ':8080'); // 假设后端运行在8080端口
  link.href = `${backendBaseUrl}/api/v1/files/access/${attachment.filePath}`
  link.download = attachment.fileName
  link.target = '_blank'
  link.click()
}

const handleRelatedClick = (relatedArticle) => {
  router.push(`/article/detail/${relatedArticle.id}`)
}

// 初始化
onMounted(() => {
  loadArticle()
})
</script>

<style scoped>
.article-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.loading-container {
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.detail-content {
  background: white;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.article-header {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.article-title {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  line-height: 1.3;
}

.article-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
  align-items: center;
  margin-bottom: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #606266;
  font-size: 14px;
}

.meta-item .el-icon {
  font-size: 16px;
}

.author-avatar {
  background: linear-gradient(135deg, #1A6B9F 0%, #2AA9E0 100%);
}

.author-name {
  font-weight: 500;
}

.article-status {
  margin-top: 8px;
}

.cover-image {
  width: 100%;
  height: 300px;
  overflow: hidden;
  border-radius: 8px;
  margin-bottom: 24px;
}

.cover-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.article-content {
  margin-bottom: 32px;
  line-height: 1.8;
  color: #303133;
  font-size: 16px;
}

.content-text {
  white-space: pre-wrap;
}

.attachments-section {
  margin-bottom: 32px;
  padding: 24px;
  background: #fafafa;
  border-radius: 8px;
}

.attachments-section h3 {
  margin-top: 0;
  margin-bottom: 16px;
  color: #303133;
}

.attachment-card {
  margin-bottom: 16px;
}

.attachment-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.file-name {
  font-size: 14px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.attachment-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.interaction-section {
  margin-bottom: 32px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  display: flex;
  gap: 16px;
  align-items: center;
}

.recommendation-section {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

.recommendation-section h3 {
  margin-top: 0;
  margin-bottom: 16px;
  color: #303133;
}

.recommendation-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.error-container {
  padding: 40px;
  text-align: center;
}

@media (max-width: 768px) {
  .article-detail {
    padding: 10px;
  }
  
  .detail-content {
    padding: 20px;
  }
  
  .article-title {
    font-size: 24px;
  }
  
  .article-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .cover-image {
    height: 200px;
  }
  
  .recommendation-list {
    grid-template-columns: 1fr;
  }
  
  .interaction-section {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>