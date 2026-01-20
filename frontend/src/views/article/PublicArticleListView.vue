<template>
  <div class="public-article-list">
    <div class="page-header">
      <h2>资料中心</h2>
    </div>
    
    <ArticleList 
      :show-filter="true"
      :show-stats="true"
      :show-pagination="true"
      :is-public="true"
      :default-params="{ status: 1 }"
    />
  </div>
</template>

<script setup>
import ArticleList from '@/components/Article/ArticleList.vue'
</script>

<style scoped>
.public-article-list {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.page-header {
  margin-bottom: 20px;
  padding: 0 10px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
  font-weight: 600;
}
</style>