<template>
  <div class="article-list">
    <!-- 搜索筛选栏 -->
    <div class="filter-bar" v-if="showFilter">
      <el-form :model="filterForm" inline class="filter-form">
        <el-form-item label="标题">
          <el-input 
            v-model="filterForm.title" 
            placeholder="请输入标题"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        
        <el-form-item label="分类">
          <el-select 
            v-model="filterForm.categoryId" 
            placeholder="请选择分类"
            clearable
            @change="handleSearch"
          >
            <el-option 
              v-for="category in categories" 
              :key="category.id" 
              :label="category.categoryName" 
              :value="category.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select 
            v-model="filterForm.status" 
            placeholder="请选择状态"
            clearable
            @change="handleSearch"
          >
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
            <el-option label="已隐藏" :value="2" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD HH:mm:ss"
            @change="handleDateChange"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 资料列表 -->
    <div class="list-container">
      <div v-if="loading" class="loading">
        <el-skeleton :rows="4" animated />
      </div>
      
      <div v-else-if="articleList.length === 0" class="empty-state">
        <el-empty description="暂无资料" />
      </div>
      
      <div v-else class="articles-grid">
        <ArticleCard 
          v-for="article in articleList" 
          :key="article.id" 
          :article="article"
          :show-stats="showStats"
          @click="handleArticleClick"
        />
      </div>
    </div>
    
    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="showPagination">
      <el-pagination
        v-model:current-page="pagination.pageNum"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import ArticleCard from './ArticleCard.vue'
import { getArticleList, getPublicArticleList } from '@/api/article'

// 定义props
const props = defineProps({
  showFilter: {
    type: Boolean,
    default: true
  },
  showStats: {
    type: Boolean,
    default: true
  },
  showPagination: {
    type: Boolean,
    default: true
  },
  isPublic: {
    type: Boolean,
    default: false // false为管理员列表，true为公开列表
  },
  defaultParams: {
    type: Object,
    default: () => ({})
  }
})

// 定义emit
const emit = defineEmits(['article-click', 'load-complete'])

// 响应式数据
const loading = ref(false)
const articleList = ref([])
const categories = ref([]) // 分类列表
const dateRange = ref([])

// 筛选表单
const filterForm = reactive({
  title: '',
  categoryId: null,
  status: null,
  startTime: null,
  endTime: null
})

// 分页参数
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 方法
const loadArticles = async () => {
  loading.value = true
  try {
    // 合并筛选参数和分页参数
    const params = {
      ...filterForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      ...props.defaultParams
    }
    
    let response
    if (props.isPublic) {
      response = await getPublicArticleList(params)
    } else {
      response = await getArticleList(params)
    }
    
    if (response.code === 200) {
      articleList.value = response.data.records || response.data.list || []
      pagination.total = response.data.total || 0
      emit('load-complete', response.data)
    } else {
      throw new Error(response.message || '获取资料列表失败')
    }
  } catch (error) {
    console.error('获取资料列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadArticles()
}

const handleReset = () => {
  // 重置筛选条件
  filterForm.title = ''
  filterForm.categoryId = null
  filterForm.status = null
  filterForm.startTime = null
  filterForm.endTime = null
  dateRange.value = []
  
  pagination.pageNum = 1
  loadArticles()
}

const handleDateChange = (dates) => {
  if (dates && dates.length === 2) {
    filterForm.startTime = dates[0]
    filterForm.endTime = dates[1]
  } else {
    filterForm.startTime = null
    filterForm.endTime = null
  }
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.pageNum = 1
  loadArticles()
}

const handleCurrentChange = (page) => {
  pagination.pageNum = page
  loadArticles()
}

const handleArticleClick = (article) => {
  emit('article-click', article)
}

// 监听筛选条件变化
watch(filterForm, () => {
  if (props.autoLoad) {
    pagination.pageNum = 1
    loadArticles()
  }
}, { deep: true })

// 初始化
onMounted(() => {
  loadArticles()
})

// 暴露方法给父组件
defineExpose({
  refresh: loadArticles,
  getList: () => articleList.value,
  setFilters: (filters) => {
    Object.assign(filterForm, filters)
    pagination.pageNum = 1
    loadArticles()
  }
})
</script>

<style scoped>
.article-list {
  width: 100%;
}

.filter-bar {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.filter-form {
  margin: 0;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 20px;
}

.list-container {
  min-height: 300px;
}

.loading {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.empty-state {
  background: white;
  padding: 40px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  text-align: center;
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.pagination-wrapper {
  margin-top: 20px;
  text-align: center;
}

@media (max-width: 768px) {
  .articles-grid {
    grid-template-columns: 1fr;
  }
  
  .filter-form :deep(.el-form-item) {
    margin-bottom: 10px;
  }
}
</style>