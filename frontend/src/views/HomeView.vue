<template>
  <div class="home">
    <el-container>
      <el-header>
        <el-menu mode="horizontal" :ellipsis="false">
          <el-menu-item index="1">首页</el-menu-item>
          <el-menu-item index="2">会员管理</el-menu-item>
          <el-menu-item index="3">课程管理</el-menu-item>
          <el-menu-item index="4">入场管理</el-menu-item>
        </el-menu>
      </el-header>
      <el-main>
        <h1>欢迎使用游泳馆会员管理系统</h1>
        <p>这是一个基于 Vue.js 3.x + Element Plus 的前端系统</p>
      </el-main>
      <el-footer>
        <p>游泳馆会员管理系统 ©2026</p>
      </el-footer>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'HomeView'
}
</script>

<style scoped>
.home {
  min-height: 100vh;
}
.el-header {
  background-color: #409EFF;
  color: white;
  padding: 0 20px;
}
.el-main {
  background-color: #f0f2f5;
  padding: 20px;
  text-align: center;
}
.el-footer {
  background-color: #f5f5f5;
  color: #606266;
  text-align: center;
  line-height: 60px;
}
</style>