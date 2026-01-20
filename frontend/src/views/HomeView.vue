<template>
  <div class="home-container">
    <!-- 顶部用户信息卡片区域 -->
    <div class="user-info-card">
      <div class="user-card-content">
        <div class="user-avatar">
          <el-avatar :size="64" :src="userStore.userInfo.avatar || ''">
            {{ userStore.userInfo.userName?.charAt(0) || 'U' }}
          </el-avatar>
        </div>
        <div class="user-welcome">
          <h2>{{ greeting }}，{{ userStore.userInfo.userName || '用户' }}！</h2>
          <p v-if="userStore.isMember">会员身份：{{ userStore.roleName }}</p>
          <p v-else>非会员用户</p>
          
          <!-- 会员到期时间提醒 -->
          <div v-if="userStore.isMember && userStore.userInfo.expirationTime" class="expiration-info">
            <el-tag 
              :type="daysUntilExpiration <= 7 ? 'danger' : daysUntilExpiration <= 30 ? 'warning' : 'success'"
              size="small"
            >
              <el-icon><Clock /></el-icon>
              会员到期：{{ userStore.userInfo.expirationTime }}
              <span v-if="daysUntilExpiration > 0">（{{ daysUntilExpiration }}天后到期）</span>
              <span v-else-if="daysUntilExpiration <= 0">（已过期）</span>
            </el-tag>
          </div>
          
          <!-- 预约课程状态提示 -->
          <div v-if="upcomingBooking" class="booking-status">
            <el-tag type="primary" size="small">
              <el-icon><Calendar /></el-icon>
              即将上课：{{ upcomingBooking.courseName }} {{ upcomingBooking.bookingDate }}
            </el-tag>
          </div>
        </div>
        <div class="user-actions">
          <el-button type="primary" @click="router.push('/user/profile')" plain>个人中心</el-button>
          <el-button @click="router.push('/booking/my')" plain>我的预约</el-button>
        </div>
      </div>
    </div>

    <!-- 轮播推荐区域 -->
    <div class="carousel-section">
      <div class="section-title">
        <h3>推荐课程</h3>
      </div>
      <el-carousel 
        v-if="recommendedCourses.length > 0" 
        height="240px" 
        :interval="5000" 
        type="card" 
        arrow="always"
      >
        <el-carousel-item 
          v-for="course in recommendedCourses" 
          :key="course.id"
          @click="viewCourseDetail(course)"
        >
          <div class="carousel-item">
            <img 
              :src="getCourseImageUrl(course.coverImage, course.courseType)" 
              alt="课程封面" 
              class="carousel-image"
              @error="handleImageError"
            />
            <div class="carousel-overlay">
              <h4>{{ course.courseName }}</h4>
              <p>{{ course.description }}</p>
              <div class="course-meta">
                <span><el-icon><Clock /></el-icon> {{ course.duration }}分钟</span>
                <span><el-icon><User /></el-icon> {{ course.maxStudents }}人</span>
              </div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
      <div v-else class="empty-carousel">
        <el-empty description="暂无推荐课程" :image-size="100" />
      </div>
    </div>

    <!-- 课程/内容展示区域 -->
    <div class="courses-section">
      <div class="section-title">
        <h3>热门课程</h3>
        <el-button type="primary" size="small" @click="router.push('/course/list')">更多课程</el-button>
      </div>
      <el-row :gutter="20">
        <el-col 
          :xs="24" 
          :sm="12" 
          :md="8" 
          v-for="course in popularCourses" 
          :key="course.id"
          class="course-col"
        >
          <el-card 
            shadow="hover" 
            class="course-card"
            @click="viewCourseDetail(course)"
          >
            <template #header>
              <div class="course-header">
                <span class="course-name">{{ course.courseName }}</span>
                <el-tag 
                  :type="getCourseStatusTag(course.status)" 
                  size="small"
                >
                  {{ getCourseStatusText(course.status) }}
                </el-tag>
              </div>
            </template>
            <div class="course-body">
              <img 
                :src="getCourseImageUrl(course.coverImage, course.courseType)" 
                alt="课程封面" 
                class="course-image"
                @error="handleImageError"
              />
              <div class="course-info">
                <p class="course-desc">{{ course.description }}</p>
                <div class="course-details">
                  <div class="detail-item">
                    <el-icon><Clock /></el-icon>
                    <span>{{ course.duration }}分钟</span>
                  </div>
                  <div class="detail-item">
                    <el-icon><User /></el-icon>
                    <span>{{ course.currentStudents }}/{{ course.maxStudents }}人</span>
                  </div>
                  <div class="detail-item">
                    <el-icon><Calendar /></el-icon>
                    <span>{{ course.schedule }}</span>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 底部内容区域 -->
    <div class="content-section">
      <div class="section-title">
        <h3>游泳知识 & 健康资讯</h3>
        <el-button type="primary" size="small" @click="router.push('/articles/public')">更多资讯</el-button>
      </div>
      <el-row :gutter="20">
        <el-col 
          :xs="24" 
          :sm="12" 
          :md="8" 
          v-for="article in featuredArticles" 
          :key="article.id"
          class="article-col"
        >
          <el-card 
            shadow="hover" 
            class="article-card"
            @click="viewArticleDetail(article)"
          >
            <img 
              :src="getArticleImageUrl(article.coverImage)" 
              alt="文章封面" 
              class="article-image"
              @error="handleImageError"
            />
            <div class="article-content">
              <h4 class="article-title">{{ article.title }}</h4>
              <p class="article-summary">{{ article.summary }}</p>
              <div class="article-meta">
                <span class="author">{{ article.authorName }}</span>
                <span class="date">{{ formatDate(article.createdTime) }}</span>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  User, 
  Calendar, 
  Clock,
  Document,
  ArrowRight
} from '@element-plus/icons-vue'
import { useUserStore } from '../store/user'
import { getCourseList } from '@/api/course'
import { getPublicArticleList } from '@/api/article'

const router = useRouter()
const userStore = useUserStore()

// 推荐课程数据
const recommendedCourses = ref([])
// 热门课程数据
const popularCourses = ref([])
// 精选文章数据
const featuredArticles = ref([])
// 即将上课的预约
const upcomingBooking = ref(null)

// 计算会员到期天数
const daysUntilExpiration = computed(() => {
  if (!userStore.userInfo.expirationTime) return null
  const expirationDate = new Date(userStore.userInfo.expirationTime)
  const today = new Date()
  const timeDiff = expirationDate.getTime() - today.getTime()
  return Math.ceil(timeDiff / (1000 * 3600 * 24))
})

// 根据时间段显示问候语
const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '凌晨好'
  if (hour < 12) return '上午好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

// 获取推荐课程
const fetchRecommendedCourses = async () => {
  try {
    const response = await getCourseList({
      pageNum: 1,
      pageSize: 5,
      sortBy: 'isFeatured',
      sortOrder: 'desc'
    })
    
    if (response.code === 200) {
      recommendedCourses.value = (response.data.records || response.data.list || []).slice(0, 3)
    }
  } catch (error) {
    console.error('获取推荐课程失败:', error)
    ElMessage.error('获取推荐课程失败')
  }
}

// 获取热门课程
const fetchPopularCourses = async () => {
  try {
    const response = await getCourseList({
      pageNum: 1,
      pageSize: 6,
      sortBy: 'enrollmentCount',
      sortOrder: 'desc'
    })
    
    if (response.code === 200) {
      popularCourses.value = (response.data.records || response.data.list || []).slice(0, 3)
    }
  } catch (error) {
    console.error('获取热门课程失败:', error)
    ElMessage.error('获取热门课程失败')
  }
}

// 获取精选文章
const fetchFeaturedArticles = async () => {
  try {
    const response = await getPublicArticleList({
      pageNum: 1,
      pageSize: 3,
      status: 1,
      sortBy: 'viewCount',
      sortOrder: 'desc'
    })
    
    if (response.code === 200) {
      featuredArticles.value = response.data.records || response.data.list || []
    }
  } catch (error) {
    console.error('获取精选文章失败:', error)
    ElMessage.error('获取精选文章失败')
  }
}

// 获取课程图片URL
const getCourseImageUrl = (imagePath, courseType = '') => {
  // 如果有具体的图片路径，使用后端API访问
  if (imagePath && !imagePath.startsWith('http')) {
    const backendBaseUrl = window.location.origin.replace(':3000', ':8080') // 假设后端运行在8080端口
    return `${backendBaseUrl}/api/v1/files/access/${imagePath}`
  }
  
  // 如果是网络图片直接返回
  if (imagePath && imagePath.startsWith('http')) return imagePath
  
  // 如果没有图片，根据课程类型返回不同默认图片
  const imageMap = {
    '基础班': 'https://images.unsplash.com/photo-1519315901367-f34ff9154487?w=400&h=250&fit=crop',
    '提高班': 'https://images.unsplash.com/photo-1560089000-7433a4ebbd64?w=400&h=250&fit=crop',
    '私教课': 'https://images.unsplash.com/photo-1576610616656-d3aa5d1f4534?w=400&h=250&fit=crop'
  }
  return imageMap[courseType] || imageMap['基础班']
}

// 获取文章图片URL
const getArticleImageUrl = (imagePath) => {
  // 如果有具体的图片路径，使用后端API访问
  if (imagePath && !imagePath.startsWith('http')) {
    const backendBaseUrl = window.location.origin.replace(':3000', ':8080') // 假设后端运行在8080端口
    return `${backendBaseUrl}/api/v1/files/access/${imagePath}`
  }
  
  // 如果是网络图片直接返回
  if (imagePath && imagePath.startsWith('http')) return imagePath
  
  // 如果没有图片，返回默认图片
  return 'https://images.unsplash.com/photo-1519315901367-f34ff9154487?w=400&h=250&fit=crop'
}

// 查看课程详情
const viewCourseDetail = (course) => {
  router.push(`/course/detail/${course.id}`)
}

// 查看文章详情
const viewArticleDetail = (article) => {
  router.push(`/articles/detail/${article.id}`)
}

// 获取课程状态标签类型
const getCourseStatusTag = (status) => {
  switch (status) {
    case 1: return 'success' // 已发布
    case 2: return 'info'    // 草稿
    case 3: return 'warning' // 已关闭
    default: return 'info'
  }
}

// 获取课程状态文本
const getCourseStatusText = (status) => {
  switch (status) {
    case 1: return '已发布'
    case 2: return '草稿'
    case 3: return '已关闭'
    default: return '未知'
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 图片加载错误处理
const handleImageError = (event) => {
  // 使用空白图片作为占位符，避免无限循环
  event.target.src = 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7'
  event.target.style.backgroundColor = '#f5f5f5'
  event.target.style.display = 'flex'
  event.target.style.alignItems = 'center'
  event.target.style.justifyContent = 'center'
  event.target.textContent = '图片加载失败'
  event.target.style.color = '#999'
  event.target.style.fontSize = '14px'
}

onMounted(async () => {
  // 加载所有数据，使用try-catch避免错误导致的问题
  try {
    await Promise.all([
      fetchRecommendedCourses(),
      fetchPopularCourses(),
      fetchFeaturedArticles()
    ])
  } catch (error) {
    console.error('首页数据加载失败:', error)
  }
})
</script>

<style scoped>
.home-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

/* 顶部用户信息卡片 */
.user-info-card {
  background: linear-gradient(135deg, #1A6B9F 0%, #2AA9E0 100%);
  border-radius: 12px;
  padding: 24px;
  color: white;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(26, 107, 159, 0.2);
}

.user-card-content {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.user-avatar :deep(.el-avatar) {
  background: rgba(255, 255, 255, 0.2);
  font-size: 24px;
  font-weight: 600;
  border: 3px solid rgba(255, 255, 255, 0.3);
}

.user-welcome h2 {
  margin: 0 0 8px 0;
  font-size: 22px;
  font-weight: 600;
}

.user-welcome p {
  margin: 0 0 8px 0;
  font-size: 14px;
  opacity: 0.9;
}

.expiration-info, .booking-status {
  margin-top: 8px;
}

.user-actions {
  margin-left: auto;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

/* 轮播推荐区域 */
.carousel-section {
  margin-bottom: 32px;
}

.section-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.carousel-item {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  height: 100%;
  cursor: pointer;
}

.carousel-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.carousel-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.8), transparent);
  color: white;
  padding: 20px;
  text-align: left;
}

.carousel-overlay h4 {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
}

.carousel-overlay p {
  margin: 0 0 12px 0;
  font-size: 13px;
  opacity: 0.9;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
}

.course-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.empty-carousel {
  text-align: center;
  padding: 40px 0;
}

/* 课程展示区域 */
.courses-section {
  margin-bottom: 32px;
}

.course-col {
  margin-bottom: 20px;
}

.course-card {
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 8px;
}

.course-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-name {
  font-weight: 600;
  color: #303133;
}

.course-body {
  display: flex;
}

.course-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  object-fit: cover;
  margin-right: 12px;
}

.course-info {
  flex: 1;
}

.course-desc {
  margin: 0 0 8px 0;
  font-size: 13px;
  color: #606266;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

/* 底部内容区域 */
.content-section {
  margin-bottom: 24px;
}

.article-col {
  margin-bottom: 20px;
}

.article-card {
  cursor: pointer;
  transition: all 0.3s;
  border-radius: 8px;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.article-image {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 8px 8px 0 0;
}

.article-content {
  padding: 12px;
}

.article-title {
  margin: 0 0 8px 0;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-summary {
  margin: 0 0 12px 0;
  font-size: 13px;
  color: #606266;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.article-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .home-container {
    padding: 10px;
  }
  
  .user-card-content {
    flex-direction: column;
    text-align: center;
  }
  
  .user-actions {
    margin-left: 0;
    width: 100%;
    justify-content: center;
  }
  
  .section-title {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .carousel-section {
    margin-bottom: 24px;
  }
  
  .courses-section, .content-section {
    margin-bottom: 24px;
  }
}
</style>