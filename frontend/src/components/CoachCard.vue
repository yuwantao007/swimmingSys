<template>
  <div class="coach-card" @click="handleClick">
    <!-- 头像区域 -->
    <div class="coach-avatar">
      <el-avatar :size="80" :src="coach.avatar || ''">
        {{ coach.name?.charAt(0) || 'C' }}
      </el-avatar>
      <el-tag 
        class="status-tag" 
        :type="coach.status === 1 ? 'success' : 'danger'" 
        size="small"
      >
        {{ coach.status === 1 ? '在职' : '停用' }}
      </el-tag>
    </div>

    <!-- 信息区域 -->
    <div class="coach-info">
      <h3 class="coach-name">{{ coach.name }}</h3>
      <p class="coach-gender">
        <el-icon><User /></el-icon>
        {{ GENDER_NAME[coach.gender] || '未知' }}
      </p>
      <p class="coach-specialty">
        <el-icon><Trophy /></el-icon>
        {{ coach.specialty || '暂无' }}
      </p>
    </div>

    <!-- 查看详情按钮 -->
    <div class="coach-action">
      <el-button type="primary" size="small" link @click.stop="showDetail">
        查看详情
      </el-button>
    </div>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="教练详情"
      width="500px"
      :close-on-click-modal="true"
    >
      <div class="detail-content">
        <div class="detail-avatar">
          <el-avatar :size="100" :src="coach.avatar || ''">
            {{ coach.name?.charAt(0) || 'C' }}
          </el-avatar>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="教练姓名">{{ coach.name }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ GENDER_NAME[coach.gender] || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ coach.phone }}</el-descriptions-item>
          <el-descriptions-item label="擅长项目">{{ coach.specialty || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="教练简介">
            {{ coach.description || '暂无简介' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="coach.status === 1 ? 'success' : 'danger'" size="small">
              {{ coach.status === 1 ? '在职' : '停用' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { User, Trophy } from '@element-plus/icons-vue'

// 性别常量
const GENDER_NAME = {
  0: '未知',
  1: '男',
  2: '女'
}

// Props定义
const props = defineProps({
  /**
   * 教练信息对象
   */
  coach: {
    type: Object,
    required: true,
    default: () => ({
      id: null,
      name: '',
      phone: '',
      gender: 0,
      avatar: '',
      specialty: '',
      description: '',
      status: 1
    })
  },
  /**
   * 是否可点击
   */
  clickable: {
    type: Boolean,
    default: true
  }
})

// 事件定义
const emit = defineEmits(['click', 'view-detail'])

// 详情弹窗可见性
const detailVisible = ref(false)

// 点击卡片
const handleClick = () => {
  if (props.clickable) {
    emit('click', props.coach)
  }
}

// 显示详情
const showDetail = () => {
  detailVisible.value = true
  emit('view-detail', props.coach)
}
</script>

<style scoped>
.coach-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.coach-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.coach-avatar {
  position: relative;
  margin-bottom: 16px;
}

.status-tag {
  position: absolute;
  bottom: -4px;
  left: 50%;
  transform: translateX(-50%);
}

.coach-info {
  flex: 1;
  width: 100%;
}

.coach-name {
  margin: 0 0 12px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.coach-gender,
.coach-specialty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin: 8px 0;
  font-size: 14px;
  color: #606266;
}

.coach-specialty {
  color: #1A6B9F;
}

.coach-action {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
  width: 100%;
}

/* 详情弹窗样式 */
.detail-content {
  padding: 10px 0;
}

.detail-avatar {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

:deep(.el-descriptions__label) {
  width: 100px;
}
</style>
