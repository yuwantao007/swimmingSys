<template>
  <div class="file-upload">
    <el-upload
      ref="uploadRef"
      :action="uploadAction"
      :headers="uploadHeaders"
      :accept="acceptTypes"
      :auto-upload="false"
      :multiple="false"
      :limit="1"
      :on-change="handleFileChange"
      :on-exceed="handleExceed"
      :before-upload="beforeUpload"
      :file-list="fileList"
      :show-file-list="showFileList"
    >
      <template #trigger>
        <el-button type="primary" :icon="Upload">
          {{ buttonText }}
        </el-button>
      </template>
      <template #tip>
        <div class="el-upload__tip">
          支持上传图片和文本文件，最大支持{{ maxSize }}MB
          <br />
          图片格式：{{ allowedImageTypes.join(', ') }}
          <br />
          文本格式：{{ allowedTextTypes.join(', ') }}
        </div>
      </template>
    </el-upload>
    
    <!-- 文件列表 -->
    <div v-if="showFileList && fileList.length > 0" class="file-list">
      <el-table :data="fileList" style="width: 100%; margin-top: 10px;">
        <el-table-column prop="name" label="文件名" width="300">
          <template #default="{ row }">
            <div class="file-info">
              <el-icon><Document /></el-icon>
              <span class="file-name">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="size" label="大小" width="100">
          <template #default="{ row }">
            {{ formatFileSize(row.size) }}
          </template>
        </el-table-column>
        <el-table-column prop="raw.type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag size="small" :type="getFileTypeTagType(row.raw.type)">
              {{ getFileTypeName(row.raw.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row, $index }">
            <el-button type="primary" link size="small" @click="handlePreview(row)">
              预览
            </el-button>
            <el-button type="danger" link size="small" @click="handleRemove($index)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 上传进度 -->
    <el-progress 
      v-if="uploading" 
      :percentage="uploadProgress" 
      :status="uploadStatus"
      class="progress-bar"
    />
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Document } from '@element-plus/icons-vue'
import { uploadFile } from '@/api/article'

// 定义props
const props = defineProps({
  buttonText: {
    type: String,
    default: '选择文件'
  },
  maxSize: {
    type: Number,
    default: 10 // MB
  },
  showFileList: {
    type: Boolean,
    default: true
  },
  accept: {
    type: String,
    default: '.jpg,.jpeg,.png,.gif,.bmp,.webp,.txt,.doc,.docx,.pdf,.rtf'
  }
})

// 定义emit
const emit = defineEmits(['upload-success', 'upload-error', 'change'])

// 响应式数据
const uploadRef = ref()
const fileList = ref([])
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadStatus = ref('')

// 允许的文件类型
const allowedImageTypes = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp']
const allowedTextTypes = ['.txt', '.doc', '.docx', '.pdf', '.rtf']

// 计算属性
const acceptTypes = computed(() => {
  return props.accept
})

const uploadAction = computed(() => {
  // 这里我们不直接使用action，而是手动处理上传
  return ''
})

const uploadHeaders = computed(() => {
  // 如果需要认证头，可以从store中获取token
  const token = localStorage.getItem('token')
  return {
    Authorization: `Bearer ${token}`
  }
})

// 方法
const handleFileChange = (file, fileList) => {
  // 只保留最新的文件
  if (fileList.length > 1) {
    fileList.splice(0, 1)
  }
  this.fileList = fileList
  
  // 触发change事件
  emit('change', file, fileList)
}

const handleExceed = () => {
  ElMessage.warning('最多只能上传一个文件')
}

const beforeUpload = (file) => {
  // 文件类型验证
  const fileExt = '.' + file.name.split('.').pop().toLowerCase()
  const allowedTypes = [...allowedImageTypes, ...allowedTextTypes]
  
  if (!allowedTypes.includes(fileExt)) {
    ElMessage.error(`不支持的文件类型: ${fileExt}，仅支持图片和文本格式`)
    return false
  }
  
  // 文件大小验证
  const maxSizeBytes = props.maxSize * 1024 * 1024
  if (file.size > maxSizeBytes) {
    ElMessage.error(`文件大小不能超过${props.maxSize}MB`)
    return false
  }
  
  return true
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getFileTypeTagType = (fileType) => {
  if (fileType.startsWith('image/')) {
    return 'success'
  } else if (fileType.includes('text/') || 
             fileType.includes('document') || 
             fileType.includes('pdf')) {
    return 'primary'
  }
  return 'info'
}

const getFileTypeName = (fileType) => {
  if (fileType.startsWith('image/')) {
    return '图片'
  } else if (fileType.includes('text/')) {
    return '文本'
  } else if (fileType.includes('pdf')) {
    return 'PDF'
  } else if (fileType.includes('word')) {
    return 'Word'
  }
  return '文件'
}

const handlePreview = (file) => {
  // 如果是图片，可以预览
  if (file.raw.type.startsWith('image/')) {
    const url = URL.createObjectURL(file.raw)
    window.open(url, '_blank')
  } else {
    ElMessage.info('请下载文件查看内容')
  }
}

const handleRemove = (index) => {
  fileList.value.splice(index, 1)
}

// 上传文件方法
const uploadFileManually = async () => {
  if (fileList.value.length === 0) {
    ElMessage.warning('请选择要上传的文件')
    return
  }
  
  const file = fileList.value[0].raw
  
  // 验证文件
  if (!beforeUpload(file)) {
    return
  }
  
  try {
    uploading.value = true
    uploadProgress.value = 0
    uploadStatus.value = ''
    
    // 创建FormData对象
    const formData = new FormData()
    formData.append('file', file)
    
    // 上传文件
    const response = await uploadFile(formData)
    
    if (response.code === 200) {
      uploading.value = false
      uploadProgress.value = 100
      uploadStatus.value = 'success'
      
      ElMessage.success(response.message || '上传成功')
      emit('upload-success', response.data)
      
      // 清空文件列表
      fileList.value = []
      
      return response.data
    } else {
      throw new Error(response.message || '上传失败')
    }
  } catch (error) {
    uploading.value = false
    uploadStatus.value = 'exception'
    ElMessage.error(error.message || '上传失败')
    emit('upload-error', error)
  }
}

// 暴露方法给父组件
defineExpose({
  uploadFile: uploadFileManually,
  clearFiles: () => {
    fileList.value = []
  },
  getFileList: () => fileList.value
})
</script>

<style scoped>
.file-upload {
  width: 100%;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-name {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.progress-bar {
  margin-top: 10px;
}

:deep(.el-upload-dragger) {
  width: 100%;
}
</style>