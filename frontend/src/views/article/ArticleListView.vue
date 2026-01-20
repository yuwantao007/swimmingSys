<template>
  <div class="article-list-view">
    <div class="page-header">
      <h2>资料管理</h2>
      <el-button 
        type="primary" 
        :icon="Plus"
        @click="handleAdd"
      >
        新增资料
      </el-button>
    </div>
    
    <ArticleList 
      ref="articleListRef"
      :show-filter="true"
      :show-stats="true"
      :show-pagination="true"
      :is-public="false"
      @article-click="handleEdit"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import ArticleList from '@/components/Article/ArticleList.vue'

// 使用router
const router = useRouter()
const articleListRef = ref()

// 方法
const handleAdd = () => {
  router.push('/article/add')
}

const handleEdit = (article) => {
  router.push(`/article/edit/${article.id}`)
}
</script>

<style scoped>
.article-list-view {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 10px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
  font-weight: 600;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .page-header .el-button {
    width: 100%;
  }
}
</style>