<template>
  <div class="article-edit-view">
    <div class="page-header">
      <h2>编辑资料</h2>
    </div>
    
    <el-card class="form-card" shadow="never">
      <el-form 
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        :loading="loading"
      >
        <el-form-item label="资料标题" prop="title">
          <el-input 
            v-model="form.title" 
            placeholder="请输入资料标题"
            maxlength="255"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="分类" prop="categoryId">
          <el-select 
            v-model="form.categoryId" 
            placeholder="请选择分类"
            clearable
            style="width: 100%"
          >
            <el-option 
              v-for="category in categories" 
              :key="category.id" 
              :label="category.categoryName" 
              :value="category.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">草稿</el-radio>
            <el-radio :label="1">发布</el-radio>
            <el-radio :label="2">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="封面图片">
          <el-upload
            :action="uploadAction"
            :headers="uploadHeaders"
            :show-file-list="false"
            :before-upload="beforeImageUpload"
            :on-success="handleImageUploadSuccess"
            :on-error="handleImageUploadError"
            :disabled="uploading"
            class="image-uploader"
          >
            <div v-if="form.coverImage" class="image-preview">
              <img :src="getImageUrl(form.coverImage)" alt="封面图片" />
              <div class="image-actions">
                <el-button 
                  type="danger" 
                  size="small" 
                  circle 
                  @click.stop="removeCoverImage"
                >
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
            <el-button v-else type="primary" :loading="uploading">
              <el-icon><Plus /></el-icon>
              上传封面
            </el-button>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="摘要">
          <el-input 
            v-model="form.summary" 
            type="textarea"
            :rows="4"
            placeholder="请输入资料摘要"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="内容">
          <el-input 
            v-model="form.content" 
            type="textarea"
            :rows="8"
            placeholder="请输入资料内容"
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="附件上传">
          <FileUpload 
            ref="fileUploadRef"
            button-text="上传附件"
            :max-size="10"
            @upload-success="handleFileUploadSuccess"
            @upload-error="handleFileUploadError"
          />
        </el-form-item>
        
        <el-form-item label="发布选项">
          <el-checkbox v-model="form.isTop" label="置顶" />
          <el-checkbox v-model="form.isFeatured" label="推荐" />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            @click="handleSave"
            :loading="saving"
          >
            保存
          </el-button>
          <el-button 
            type="success" 
            @click="handlePublish"
            :loading="publishing"
            v-if="form.status !== 1"
          >
            发布
          </el-button>
          <el-button 
            type="warning" 
            @click="handleUnpublish"
            :loading="unpublishing"
            v-if="form.status === 1"
          >
            下架
          </el-button>
          <el-button @click="handleCancel">
            取消
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import FileUpload from '@/components/Article/FileUpload.vue'
import { getArticleDetail, updateArticle, publishArticle, unpublishArticle } from '@/api/article'

// 使用route和router
const route = useRoute()
const router = useRouter()
const formRef = ref()
const fileUploadRef = ref()

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const publishing = ref(false)
const unpublishing = ref(false)
const uploading = ref(false)
const categories = ref([])
const articleId = ref(route.params.id)

// 表单数据
const form = reactive({
  title: '',
  categoryId: null,
  status: 1,
  coverImage: '',
  summary: '',
  content: '',
  isTop: false,
  isFeatured: false
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入资料标题', trigger: 'blur' },
    { min: 1, max: 255, message: '标题长度在1-255个字符之间', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 上传相关
const uploadAction = '/api/v1/articles/files/upload'
const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('token')}`
}

// 方法
const loadArticle = async () => {
  if (!articleId.value) {
    ElMessage.error('资料ID不能为空')
    router.push('/article/list')
    return
  }
  
  loading.value = true
  try {
    const response = await getArticleDetail(articleId.value)
    if (response.code === 200) {
      const article = response.data
      form.title = article.title
      form.categoryId = article.categoryId
      form.status = article.status
      form.coverImage = article.coverImage
      form.summary = article.summary
      form.content = article.content
      form.isTop = article.isTop
      form.isFeatured = article.isFeatured
    } else {
      throw new Error(response.message || '获取资料详情失败')
    }
  } catch (error) {
    console.error('获取资料详情失败:', error)
    ElMessage.error(error.message || '获取资料详情失败')
    router.push('/article/list')
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  // 这里可以调用分类API获取分类列表
  // 为了简化，我们暂时使用模拟数据
  try {
    // 实际项目中应该调用分类API
    // const response = await getCategoryList()
    // if (response.code === 200) {
    //   categories.value = response.data
    // }
    
    // 模拟一些分类数据
    categories.value = [
      { id: 1, categoryName: '游泳技巧' },
      { id: 2, categoryName: '健康知识' },
      { id: 3, categoryName: '场馆公告' },
      { id: 4, categoryName: '活动通知' }
    ]
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const beforeImageUpload = (file) => {
  const allowedImageTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  const isImage = allowedImageTypes.includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }

  uploading.value = true
  return true
}

const handleImageUploadSuccess = (response, file) => {
  if (response.code === 200) {
    form.coverImage = response.data
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error(response.message || '封面上传失败')
  }
  uploading.value = false
}

const handleImageUploadError = (error) => {
  console.error('封面上传失败:', error)
  ElMessage.error('封面上传失败')
  uploading.value = false
}

const handleFileUploadSuccess = (filePath) => {
  // 文件上传成功，可以在这里处理附件信息
  console.log('附件上传成功:', filePath)
}

const handleFileUploadError = (error) => {
  console.error('附件上传失败:', error)
  ElMessage.error('附件上传失败')
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

const removeCoverImage = () => {
  form.coverImage = ''
}

const handleSave = async () => {
  // 验证表单
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
  } catch (error) {
    ElMessage.error('请填写必填项')
    return
  }
  
  // 防止重复提交
  if (saving.value) return
  
  saving.value = true
  try {
    const response = await updateArticle(articleId.value, {
      ...form
    })
    
    if (response.code === 200) {
      ElMessage.success('保存成功')
      // 跳转到列表页
      router.push('/article/list')
    } else {
      throw new Error(response.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const handlePublish = async () => {
  if (!articleId.value) return
  
  try {
    ElMessageBox.confirm('确定要发布此资料吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      publishing.value = true
      try {
        const response = await publishArticle(articleId.value)
        if (response.code === 200) {
          ElMessage.success('发布成功')
          form.status = 1
        } else {
          throw new Error(response.message || '发布失败')
        }
      } catch (error) {
        console.error('发布失败:', error)
        ElMessage.error(error.message || '发布失败')
      } finally {
        publishing.value = false
      }
    })
  } catch (error) {
    // 用户取消操作
  }
}

const handleUnpublish = async () => {
  if (!articleId.value) return
  
  try {
    ElMessageBox.confirm('确定要下架此资料吗？下架后将不可见。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      unpublishing.value = true
      try {
        const response = await unpublishArticle(articleId.value)
        if (response.code === 200) {
          ElMessage.success('下架成功')
          form.status = 2
        } else {
          throw new Error(response.message || '下架失败')
        }
      } catch (error) {
        console.error('下架失败:', error)
        ElMessage.error(error.message || '下架失败')
      } finally {
        unpublishing.value = false
      }
    })
  } catch (error) {
    // 用户取消操作
  }
}

const handleCancel = () => {
  // 确认离开
  ElMessageBox.confirm('确定要离开吗？未保存的内容将会丢失。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    router.go(-1)
  }).catch(() => {
    // 取消操作
  })
}

// 初始化
onMounted(() => {
  loadArticle()
  loadCategories()
})
</script>

<style scoped>
.article-edit-view {
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

.form-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.image-uploader {
  width: 200px;
  height: 150px;
}

.image-preview {
  position: relative;
  width: 200px;
  height: 150px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  overflow: hidden;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-actions {
  position: absolute;
  top: 0;
  right: 0;
  opacity: 0;
  transition: opacity 0.3s;
}

.image-preview:hover .image-actions {
  opacity: 1;
}

.image-actions .el-button {
  margin: 5px;
}
</style>