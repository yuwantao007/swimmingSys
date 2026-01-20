<template>
  <div class="article-carousel">
    <el-carousel 
      :height="carouselHeight" 
      :interval="interval"
      :autoplay="autoplay"
      :indicator-position="indicatorPosition"
      :arrow="arrow"
      @change="handleChange"
    >
      <el-carousel-item 
        v-for="(item, index) in carouselItems" 
        :key="index"
        @click="handleItemClick(item)"
      >
        <div class="carousel-item">
          <div class="item-content">
            <!-- 图片区域 -->
            <div class="item-image">
              <img :src="getImageUrl(item.coverImage)" :alt="item.title" />
            </div>
            
            <!-- 内容区域 -->
            <div class="item-info">
              <h3 class="item-title">{{ item.title }}</h3>
              <p class="item-summary" v-if="item.summary">
                {{ item.summary.substring(0, 100) }}...
              </p>
              
              <!-- 作者和时间信息 -->
              <div class="item-meta">
                <div class="author-info">
                  <el-avatar 
                    :size="20" 
                    :src="getAvatarUrl(item.authorId)" 
                    class="author-avatar"
                  >
                    {{ getInitial(item.authorName) }}
                  </el-avatar>
                  <span class="author-name">{{ item.authorName || '管理员' }}</span>
                </div>
                <div class="time">{{ formatDate(item.createdTime) }}</div>
              </div>
              
              <!-- 分类标签 -->
              <div class="category-tag" v-if="item.categoryName">
                <el-tag type="primary" size="small">{{ item.categoryName }}</el-tag>
              </div>
            </div>
          </div>
        </div>
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

// 定义props
const props = defineProps({
  articles: {
    type: Array,
    default: () => []
  },
  height: {
    type: String,
    default: '300px'
  },
  interval: {
    type: Number,
    default: 5000 // 5秒切换
  },
  autoplay: {
    type: Boolean,
    default: true
  },
  indicatorPosition: {
    type: String,
    default: 'bottom'
  },
  arrow: {
    type: String,
    default: 'always'
  }
})

// 定义emit
const emit = defineEmits(['change', 'click'])

// 使用router
const router = useRouter()

// 计算属性
const carouselHeight = computed(() => {
  return props.height
})

const carouselItems = computed(() => {
  // 只显示已发布的文章
  return props.articles.filter(article => article.status === 1)
})

// 方法
const getImageUrl = (imagePath) => {
  if (!imagePath) return ''
  if (imagePath.startsWith('http')) {
    return imagePath
  }
  return `${window.location.origin}/uploads/${imagePath}`
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

const handleChange = (currentIndex) => {
  emit('change', currentIndex)
}

const handleItemClick = (item) => {
  router.push(`/article/detail/${item.id}`)
  emit('click', item)
}
</script>

<style scoped>
.article-carousel {
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.carousel-item {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  cursor: pointer;
}

.item-content {
  display: flex;
  width: 90%;
  height: 85%;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.item-content:hover {
  transform: translateY(-5px);
}

.item-image {
  flex: 1;
  min-width: 40%;
  overflow: hidden;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
  min-width: 60%;
  padding: 24px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.item-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
  line-height: 1.4;
}

.item-summary {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin: 0 0 16px 0;
  flex-grow: 1;
}

.item-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
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

.time {
  font-size: 12px;
  color: #909399;
}

.category-tag {
  display: inline-block;
}

:deep(.el-carousel__container) {
  background: transparent;
}

:deep(.el-carousel__indicators) {
  background: transparent;
}

:deep(.el-carousel__indicator) {
  background: transparent;
}

:deep(.el-carousel__button) {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  width: 12px;
  height: 12px;
}

:deep(.el-carousel__arrow) {
  background: rgba(31, 45, 61, 0.5);
  border-radius: 50%;
  width: 40px;
  height: 40px;
}

:deep(.el-carousel__arrow:hover) {
  background: rgba(31, 45, 61, 0.8);
}

@media (max-width: 768px) {
  .item-content {
    flex-direction: column;
    height: auto;
  }
  
  .item-image {
    min-width: 100%;
    height: 200px;
  }
  
  .item-info {
    min-width: 100%;
    padding: 16px;
  }
  
  .item-title {
    font-size: 18px;
  }
}
</style>