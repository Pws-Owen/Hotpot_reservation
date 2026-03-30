<template>
  <div class="review-container">
    <!-- 公共头部组件 -->
    <CustomerHeader :show-search="false" title="我的评价" />
    
    <!-- 内容区 -->
    <div class="review-content">
      <!-- 头部区域：标题+返回按钮 -->
      <div class="review-header">
        <div class="header-left">
          <h1 class="review-title">我的评价</h1>
          <p class="review-subtitle">评价管理 · 订单评价 · 服务反馈</p>
        </div>
        <div class="header-actions">
          <!-- 返回首页按钮 -->
          <el-button 
            type="primary" 
            @click="$router.push('/customer/home')"
            class="back-btn"
          >
            <el-icon><ArrowLeft /></el-icon>
            返回首页
          </el-button>
        </div>
      </div>
      
      <el-card>
        <template #header>
          <span>评价管理</span>
        </template>
      
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="我的评价" name="my">
          <el-table :data="myReviews" style="width: 100%" border v-loading="loading">
            <el-table-column prop="orderNo" label="订单号" width="150" show-overflow-tooltip />
            <el-table-column label="评价内容" min-width="300">
              <template #default="scope">
                <div class="review-content-cell">
                  <!-- 富文本内容 -->
                  <div 
                    class="review-text-content" 
                    v-html="scope.row.content"
                    v-if="scope.row.content"
                  ></div>
                  <!-- 图片展示 -->
                  <div class="review-images-preview" v-if="getReviewImages(scope.row).length > 0">
                    
                  </div>
                  <!-- 无内容提示 -->
                  <div v-if="!scope.row.content && (!scope.row.images || scope.row.images.trim() === '')" class="no-content">
                    <span style="color: #c0c4cc;">暂无评价内容</span>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="rating" label="评分" width="120">
              <template #default="scope">
                <el-rate v-model="scope.row.rating" disabled show-score />
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="评价时间" width="180" />
            <el-table-column label="商家回复" min-width="150">
              <template #default="scope">
                <span v-if="scope.row.reply">{{ scope.row.reply }}</span>
                <span v-else style="color: #909399;">暂无回复</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="scope">
                <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
                <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="pagination">
            <el-pagination
              v-model:current-page="pagination.currentPage"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              :total="pagination.total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            />
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="待评价" name="pending">
          <el-table :data="pendingReviews" style="width: 100%" border>
            <el-table-column prop="orderNo" label="订单号" width="150" show-overflow-tooltip />
            <el-table-column prop="orderTime" label="订单时间" width="180" />
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button type="primary" @click="handleReview(scope.row)">去评价</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-empty v-if="pendingReviews.length === 0" description="暂无待评价订单" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 评价对话框 -->
    <el-dialog 
      v-model="reviewDialogVisible" 
      :title="reviewForm.reviewId ? '编辑评价' : '评价订单'" 
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form :model="reviewForm" label-width="80px">
        <el-form-item label="订单号">
          <span>{{ reviewForm.orderNo || '-' }}</span>
        </el-form-item>
        <el-form-item label="评分" required>
          <el-rate v-model="reviewForm.rating" />
        </el-form-item>
        <el-form-item label="评价内容" required>
          <div class="rich-editor-wrapper">
            <div ref="editorRef" class="quill-editor"></div>
          </div>
          <div class="editor-tip">
            <span>支持文字、图片等多种形式，让您的评价更生动</span>
          </div>
        </el-form-item>
        <el-form-item label="上传图片">
          <el-upload
            v-model:file-list="imageFileList"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :on-preview="handleImagePreview"
            :on-remove="handleImageRemove"
            :on-change="handleImageChange"
            :before-upload="beforeImageUpload"
            :limit="9"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
            <template #tip>
              <div class="el-upload__tip">
                支持jpg/png/gif格式，最多上传9张，单张不超过5MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleCancelReview">取消</el-button>
        <el-button type="primary" @click="handleSubmitReview" :loading="submitting">提交</el-button>
      </template>
    </el-dialog>
    
    <!-- 图片预览对话框 -->
    <el-dialog v-model="imagePreviewVisible" title="图片预览" width="800px">
      <el-image :src="previewImageUrl" fit="contain" style="width: 100%; max-height: 600px;" />
    </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus, Picture } from '@element-plus/icons-vue'
import Quill from 'quill'
import 'quill/dist/quill.snow.css'
import CustomerHeader from '@/components/customer/CustomerHeader.vue'
import { getReviewPage, createReview, updateReview, deleteReview } from '@/api/review'
import { getOrderPage } from '@/api/order'
import { uploadImage } from '@/api/upload'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const activeTab = ref('my')
const myReviews = ref([])
const pendingReviews = ref([])
const reviewDialogVisible = ref(false)
const submitting = ref(false)
const reviewForm = ref({
  reviewId: null,
  orderId: null,
  orderNo: '',
  rating: 5,
  content: '',
  images: ''
})
const editorRef = ref(null)
let quillEditor = null
const imageFileList = ref([])
const imagePreviewVisible = ref(false)
const previewImageUrl = ref('')

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

onMounted(() => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/customer/login')
    return
  }
  loadMyReviews()
  loadPendingOrders()
  initQuillEditor()
})

// 监听对话框显示，初始化编辑器
watch(reviewDialogVisible, (visible) => {
  if (visible) {
    nextTick(() => {
      initQuillEditor()
    })
  }
})

// 初始化Quill编辑器
const initQuillEditor = () => {
  if (!editorRef.value || quillEditor) return
  
  quillEditor = new Quill(editorRef.value, {
    theme: 'snow',
    placeholder: '请输入评价内容，支持文字、图片等多种形式...',
    modules: {
      toolbar: {
        container: [
          ['bold', 'italic', 'underline', 'strike'],
          ['blockquote', 'code-block'],
          [{ 'header': 1 }, { 'header': 2 }],
          [{ 'list': 'ordered'}, { 'list': 'bullet' }],
          [{ 'script': 'sub'}, { 'script': 'super' }],
          [{ 'indent': '-1'}, { 'indent': '+1' }],
          [{ 'direction': 'rtl' }],
          [{ 'size': ['small', false, 'large', 'huge'] }],
          [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
          [{ 'color': [] }, { 'background': [] }],
          [{ 'font': [] }],
          [{ 'align': [] }],
          ['clean'],
          ['link', 'image']
        ],
        handlers: {
          image: handleQuillImageUpload
        }
      }
    }
  })
  
  // 监听内容变化
  quillEditor.on('text-change', () => {
    reviewForm.value.content = quillEditor.root.innerHTML
  })
}

const loadMyReviews = async () => {
  loading.value = true
  try {
    const res = await getReviewPage({
      current: pagination.currentPage,
      size: pagination.pageSize,
      userId: userStore.userId
    })
    if (res.code === 200 && res.data) {
      myReviews.value = (res.data.records || []).map(item => ({
        reviewId: item.reviewId,
        orderId: item.orderId,
        orderNo: item.orderNo || '-',
        content: item.content || '',
        rating: item.rating || 5,
        reply: item.reply || '',
        images: item.images || '',
        createTime: item.createTime
      }))
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载我的评价失败:', error)
    if (error.response?.status === 404) {
      // 后端接口未实现
      myReviews.value = []
    } else {
      ElMessage.error('加载我的评价失败')
    }
  } finally {
    loading.value = false
  }
}

const loadPendingOrders = async () => {
  try {
    // 查询已支付或已完成的订单（可以评价的订单）
    const res = await getOrderPage({
      current: 1,
      size: 1000, // 获取足够多的订单
      userId: userStore.userId
    })
    
    if (res.code === 200 && res.data) {
      const orders = res.data.records || []
      
      // 获取所有已有评价的订单ID
      const reviewRes = await getReviewPage({
        current: 1,
        size: 1000,
        userId: userStore.userId
      })
      
      const reviewedOrderIds = new Set()
      if (reviewRes.code === 200 && reviewRes.data) {
        (reviewRes.data.records || []).forEach(review => {
          if (review.orderId) {
            reviewedOrderIds.add(review.orderId)
          }
        })
      }
      
      // 过滤出已支付或已完成，且没有评价的订单
      pendingReviews.value = orders
        .filter(order => {
          // 订单状态：1-已支付，2-制作中，3-已完成（这些状态都可以评价）
          const canReview = order.status === 1 || order.status === 2 || order.status === 3
          // 且该订单还没有评价
          const notReviewed = !reviewedOrderIds.has(order.orderId)
          return canReview && notReviewed
        })
        .map(order => ({
          id: order.orderId,
          orderId: order.orderId,
          orderNo: order.orderNo || `订单${order.orderId}`,
          orderTime: order.createTime || order.orderTime
        }))
    }
  } catch (error) {
    console.error('加载待评价订单失败:', error)
    ElMessage.error('加载待评价订单失败')
  }
}

// Quill编辑器图片上传处理
const handleQuillImageUpload = () => {
  const input = document.createElement('input')
  input.setAttribute('type', 'file')
  input.setAttribute('accept', 'image/*')
  input.click()
  
  input.onchange = async () => {
    const file = input.files[0]
    if (!file) return
    
    // 验证文件
    const isValidType = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
    const isValidSize = file.size / 1024 / 1024 < 5
    
    if (!isValidType) {
      ElMessage.error('图片格式不正确，仅支持 JPG、PNG、GIF、WEBP 格式')
      return
    }
    if (!isValidSize) {
      ElMessage.error('图片大小不能超过 5MB')
      return
    }
    
    try {
      // 获取当前光标位置
      const range = quillEditor.getSelection(true)
      
      // 插入加载提示
      quillEditor.insertText(range.index, '图片上传中...', 'user')
      quillEditor.setSelection(range.index + 6)
      
      // 上传图片
      const res = await uploadImage(file, 'review')
      if (res.code === 200 && res.data && res.data.url) {
        // 删除加载提示
        quillEditor.deleteText(range.index, 6)
        // 插入图片
        quillEditor.insertEmbed(range.index, 'image', res.data.url)
        quillEditor.setSelection(range.index + 1)
        ElMessage.success('图片上传成功')
      } else {
        quillEditor.deleteText(range.index, 6)
        ElMessage.error('图片上传失败')
      }
    } catch (error) {
      console.error('图片上传失败:', error)
      ElMessage.error('图片上传失败，请重试')
    }
  }
}

// 图片上传前验证
const beforeImageUpload = (file) => {
  const isValidType = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  const isValidSize = file.size / 1024 / 1024 < 5
  
  if (!isValidType) {
    ElMessage.error('图片格式不正确，仅支持 JPG、PNG、GIF、WEBP 格式')
    return false
  }
  if (!isValidSize) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return false // 阻止自动上传，手动处理
}

// 图片文件变化处理
const handleImageChange = async (file, fileList) => {
  if (file.status === 'ready') {
    try {
      // 上传图片
      const res = await uploadImage(file.raw, 'review')
      if (res.code === 200 && res.data && res.data.url) {
        // 更新文件列表，添加url
        file.url = res.data.url
        file.status = 'success'
        ElMessage.success('图片上传成功')
      } else {
        ElMessage.error('图片上传失败')
        // 移除失败的文件
        const index = imageFileList.value.findIndex(item => item.uid === file.uid)
        if (index > -1) {
          imageFileList.value.splice(index, 1)
        }
      }
    } catch (error) {
      console.error('图片上传失败:', error)
      ElMessage.error('图片上传失败，请重试')
      // 移除失败的文件
      const index = imageFileList.value.findIndex(item => item.uid === file.uid)
      if (index > -1) {
        imageFileList.value.splice(index, 1)
      }
    }
  }
}

// 图片预览
const handleImagePreview = (file) => {
  previewImageUrl.value = file.url || file.response?.data?.url
  imagePreviewVisible.value = true
}

// 移除图片
const handleImageRemove = (file) => {
  const index = imageFileList.value.findIndex(item => item.uid === file.uid)
  if (index > -1) {
    imageFileList.value.splice(index, 1)
  }
}

const handleReview = (row) => {
  reviewForm.value = {
    reviewId: null,
    orderId: row.orderId || row.id,
    orderNo: row.orderNo,
    rating: 5,
    content: '',
    images: ''
  }
  imageFileList.value = []
  if (quillEditor) {
    quillEditor.root.innerHTML = ''
  }
  reviewDialogVisible.value = true
}

// 取消评价
const handleCancelReview = () => {
  reviewDialogVisible.value = false
  // 延迟清理，避免动画冲突
  setTimeout(() => {
    reviewForm.value = {
      reviewId: null,
      orderId: null,
      orderNo: '',
      rating: 5,
      content: '',
      images: ''
    }
    imageFileList.value = []
    if (quillEditor) {
      quillEditor.root.innerHTML = ''
    }
  }, 300)
}

const handleSubmitReview = async () => {
  if (!reviewForm.value.rating) {
    ElMessage.warning('请选择评分')
    return
  }
  
  // 获取富文本编辑器内容
  const content = quillEditor ? quillEditor.root.innerHTML : reviewForm.value.content
  const textContent = quillEditor ? quillEditor.getText().trim() : reviewForm.value.content.trim()
  
  if (!textContent || textContent === '') {
    ElMessage.warning('请输入评价内容')
    return
  }
  
  // 收集所有图片URL
  const imageUrls = []
  
  // 从富文本编辑器中提取图片
  if (quillEditor) {
    const delta = quillEditor.getContents()
    delta.ops.forEach(op => {
      if (op.insert && typeof op.insert === 'object' && op.insert.image) {
        imageUrls.push(op.insert.image)
      }
    })
  }
  
  // 从上传组件中提取图片
  imageFileList.value.forEach(file => {
    if (file.url) {
      imageUrls.push(file.url)
    } else if (file.response && file.response.data && file.response.data.url) {
      imageUrls.push(file.response.data.url)
    }
  })
  
  // 去重
  const uniqueImageUrls = [...new Set(imageUrls)]
  const imagesStr = uniqueImageUrls.join(',')
  
  submitting.value = true
  try {
    if (reviewForm.value.reviewId) {
      // 更新评价
      const res = await updateReview(reviewForm.value.reviewId, {
        rating: reviewForm.value.rating,
        content: content,
        images: imagesStr
      })
      if (res.code === 200) {
        ElMessage.success('评价更新成功')
        handleCancelReview()
        loadMyReviews()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    } else {
      // 创建评价
      const res = await createReview({
        orderId: reviewForm.value.orderId,
        rating: reviewForm.value.rating,
        content: content,
        images: imagesStr
      })
      if (res.code === 200) {
        ElMessage.success('评价提交成功')
        handleCancelReview()
        loadMyReviews()
        loadPendingOrders()
      } else {
        ElMessage.error(res.message || '提交失败')
      }
    }
  } catch (error) {
    console.error('提交评价失败:', error)
    if (error.response?.status === 404) {
      ElMessage.warning('评价功能暂未实现')
    } else {
      ElMessage.error(error.message || '提交失败，请重试')
    }
  } finally {
    submitting.value = false
  }
}

const handleEdit = (row) => {
  reviewForm.value = {
    reviewId: row.reviewId,
    orderId: row.orderId,
    orderNo: row.orderNo,
    rating: row.rating,
    content: row.content || '',
    images: row.images || ''
  }
  
  // 加载图片列表
  imageFileList.value = []
  if (row.images) {
    const imageUrls = row.images.split(',').filter(url => url.trim())
    imageFileList.value = imageUrls.map((url, index) => ({
      uid: `image-${index}`,
      name: `image-${index}.jpg`,
      url: url,
      status: 'success'
    }))
  }
  
  // 设置富文本编辑器内容
  if (quillEditor) {
    quillEditor.root.innerHTML = row.content || ''
  }
  
  reviewDialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该评价吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const res = await deleteReview(row.reviewId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadMyReviews()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评价失败:', error)
      if (error.response?.status === 404) {
        ElMessage.warning('评价功能暂未实现')
      } else {
        ElMessage.error(error.message || '删除失败，请重试')
      }
    }
  }
}

const handleTabChange = (tab) => {
  if (tab === 'my') {
    loadMyReviews()
  } else if (tab === 'pending') {
    loadPendingOrders()
  }
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  loadMyReviews()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadMyReviews()
}

// 获取评价图片列表（从images字段和HTML内容中提取）
const getReviewImages = (review) => {
  const images = []
  
  // 从images字段获取图片
  if (review.images && review.images.trim()) {
    const imageUrls = review.images.split(',').filter(url => url.trim())
    images.push(...imageUrls)
  }
  
  // 从HTML内容中提取图片
  if (review.content) {
    const parser = new DOMParser()
    const doc = parser.parseFromString(review.content, 'text/html')
    const imgTags = doc.querySelectorAll('img')
    imgTags.forEach(img => {
      const src = img.getAttribute('src')
      if (src && !images.includes(src)) {
        images.push(src)
      }
    })
  }
  
  return images
}
</script>

<style scoped lang="scss">
.review-container {
  width: 100%;
  min-height: 100vh;
  background: #f5f5f5;
}

.review-content {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  box-sizing: border-box;
}

// 头部样式
.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 2px solid #ffe7e7;
  margin-bottom: 20px;

  .header-left {
    .review-title {
      font-size: 28px;
      font-weight: bold;
      color: #ff4d4f;
      margin: 0 0 5px 0;
    }

    .review-subtitle {
      font-size: 14px;
      color: #999;
      margin: 0;
    }
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 15px;

    .back-btn {
      display: flex;
      align-items: center;
      gap: 5px;
      padding: 8px 16px;
    }
  }
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

// 评价内容单元格样式
.review-content-cell {
  padding: 12px 0;
  
  .review-text-content {
    margin-bottom: 12px;
    line-height: 1.6;
    color: #606266;
    word-break: break-word;
    
    // 富文本内容样式
    :deep(p) {
      margin: 0 0 8px 0;
      &:last-child {
        margin-bottom: 0;
      }
    }
    
    :deep(img) {
      max-width: 50%;
      height: auto;
      border-radius: 4px;
      margin: 8px 0;
      display: block;
    }
    
    :deep(ul), :deep(ol) {
      margin: 8px 0;
      padding-left: 24px;
    }
    
    :deep(blockquote) {
      margin: 8px 0;
      padding: 8px 16px;
      border-left: 4px solid #dcdfe6;
      background: #f5f7fa;
      color: #606266;
    }
    
    :deep(code) {
      padding: 2px 6px;
      background: #f5f7fa;
      border-radius: 3px;
      font-family: 'Courier New', monospace;
      font-size: 0.9em;
    }
    
    :deep(pre) {
      padding: 12px;
      background: #f5f7fa;
      border-radius: 4px;
      overflow-x: auto;
      
      code {
        padding: 0;
        background: transparent;
      }
    }
    
    :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6) {
      margin: 12px 0 8px 0;
      font-weight: 600;
      line-height: 1.4;
    }
    
    :deep(h1) { font-size: 1.5em; }
    :deep(h2) { font-size: 1.3em; }
    :deep(h3) { font-size: 1.1em; }
    
    :deep(strong) {
      font-weight: 600;
    }
    
    :deep(em) {
      font-style: italic;
    }
    
    :deep(u) {
      text-decoration: underline;
    }
  }
  
  .review-images-preview {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    margin-top: 12px;
    
    .review-preview-img {
      width: 80px;
      height: 80px;
      border-radius: 4px;
      cursor: pointer;
      border: 1px solid #e4e7ed;
      transition: all 0.3s;
      
      &:hover {
        border-color: #409eff;
        transform: scale(1.05);
      }
      
      .image-error {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #f5f7fa;
        color: #c0c4cc;
        
        .el-icon {
          font-size: 24px;
        }
      }
    }
  }
  
  .no-content {
    padding: 20px 0;
    text-align: center;
  }
}

// 富文本编辑器样式
.rich-editor-wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: #fff;
  
  :deep(.ql-container) {
    min-height: 200px;
    max-height: 400px;
    overflow-y: auto;
    font-size: 14px;
  }
  
  :deep(.ql-editor) {
    min-height: 200px;
    padding: 12px 15px;
    
    &.ql-blank::before {
      color: #c0c4cc;
      font-style: normal;
    }
  }
  
  :deep(.ql-toolbar) {
    border-top: none;
    border-left: none;
    border-right: none;
    border-bottom: 1px solid #dcdfe6;
    padding: 8px;
  }
  
  :deep(.ql-toolbar .ql-stroke) {
    stroke: #606266;
  }
  
  :deep(.ql-toolbar .ql-fill) {
    fill: #606266;
  }
  
  :deep(.ql-toolbar button:hover),
  :deep(.ql-toolbar button.ql-active) {
    .ql-stroke {
      stroke: #409eff;
    }
    .ql-fill {
      fill: #409eff;
    }
  }
}

.editor-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

// 图片上传样式
:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  line-height: 100px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item-thumbnail) {
  object-fit: cover;
}
</style>

