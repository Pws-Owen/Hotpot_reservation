<template>
  <div class="review-list">
    <el-card>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" clearable />
        </el-form-item>
        <el-form-item label="评分">
          <el-select v-model="searchForm.rating" placeholder="选择评分" clearable style="width: 150px;">
            <el-option label="5星" :value="5" :key="5" />
            <el-option label="4星" :value="4" />
            <el-option label="3星" :value="3" />
            <el-option label="2星" :value="2" />
            <el-option label="1星" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table 
        :data="reviews" 
        style="width: 100%"
        border
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="reviewId" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" width="150" />
        <el-table-column prop="realName" label="客户姓名" width="120">
          <template #default="scope">
            {{ scope.row.realName || scope.row.userName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="120">
          <template #default="scope">
            <el-rate v-model="scope.row.rating" disabled show-score />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评论内容" show-overflow-tooltip min-width="200">
          <template #default="scope">
            <div class="content-cell">
              <div v-html="getPlainTextFromHtml(scope.row.content)"></div>
              <div class="review-images-cell" v-if="scope.row.images && getReviewImages(scope.row.images).length > 0">
                <el-image
                  v-for="(img, index) in getReviewImages(scope.row.images)"
                  :key="index"
                  :src="img"
                  fit="cover"
                  class="review-img-cell"
                  :preview-src-list="getReviewImages(scope.row.images)"
                  :initial-index="index"
                />
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评论时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '显示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleView(scope.row)">查看</el-button>
            <el-button type="success" size="small" @click="handleReply(scope.row)">回复</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 查看/回复对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="600px"
    >
      <div v-if="currentReview">
        <p><strong>订单号：</strong>{{ currentReview.orderNo || '-' }}</p>
        <p><strong>客户：</strong>{{ currentReview.realName || currentReview.userName || '-' }}</p>
        <p><strong>评分：</strong>
          <el-rate v-model="currentReview.rating" disabled />
        </p>
        <p><strong>评论内容：</strong></p>
        <div class="review-content" v-html="currentReview.content"></div>
        <div v-if="currentReview.images" class="review-images">
          <el-image
            v-for="(img, index) in getReviewImages(currentReview.images)"
            :key="index"
            :src="img"
            :preview-src-list="getReviewImages(currentReview.images)"
            fit="cover"
            class="review-image-item"
          />
        </div>
        <el-divider v-if="currentReview.reply" />
        <div v-if="currentReview.reply">
          <p><strong>商家回复：</strong></p>
          <p>{{ currentReview.reply }}</p>
          <p v-if="currentReview.replyTime" style="color: #909399; font-size: 12px;">
            {{ formatDateTime(currentReview.replyTime) }}
          </p>
        </div>
        <el-form v-if="isReply" :model="replyForm" label-width="80px" style="margin-top: 20px;">
          <el-form-item label="回复内容">
            <el-input v-model="replyForm.content" type="textarea" :rows="4" placeholder="请输入回复内容" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSubmitReply" :loading="replyLoading">提交回复</el-button>
            <el-button @click="dialogVisible = false">取消</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getReviewPage, replyReview } from '@/api/review'

const reviews = ref([])
const selectedRows = ref([])
const dialogVisible = ref(false)
const isReply = ref(false)
const currentReview = ref(null)
const loading = ref(false)
const replyLoading = ref(false)

const searchForm = reactive({
  orderNo: '',
  rating: undefined
})

const replyForm = reactive({
  content: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const dialogTitle = computed(() => isReply.value ? '回复评论' : '查看评论')

onMounted(() => {
  loadData()
})

/**
 * 加载评论数据
 */
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      rating: searchForm.rating != null ? searchForm.rating : undefined,
      orderNo: searchForm.orderNo && searchForm.orderNo.trim() ? searchForm.orderNo.trim() : undefined
    }
    
    const res = await getReviewPage(params)
    if (res.code === 200) {
      reviews.value = res.data.records || []
      pagination.total = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取评论列表失败')
      reviews.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取评论列表失败:', error)
    ElMessage.error('获取评论列表失败')
    reviews.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  pagination.current = 1
  loadData()
}

/**
 * 重置搜索
 */
const resetSearch = () => {
  Object.assign(searchForm, {
    orderNo: '',
    rating: undefined
  })
  pagination.current = 1
  loadData()
}

/**
 * 查看评论详情
 */
const handleView = (row) => {
  isReply.value = false
  currentReview.value = { ...row }
  dialogVisible.value = true
}

/**
 * 回复评论
 */
const handleReply = (row) => {
  isReply.value = true
  currentReview.value = { ...row }
  replyForm.content = row.reply || ''
  dialogVisible.value = true
}

/**
 * 提交回复
 */
const handleSubmitReply = async () => {
  if (!replyForm.content || !replyForm.content.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  
  replyLoading.value = true
  try {
    const res = await replyReview(currentReview.value.reviewId, replyForm.content.trim())
    if (res.code === 200) {
      ElMessage.success('回复成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message || '回复失败')
    }
  } catch (error) {
    console.error('回复评论失败:', error)
    ElMessage.error('回复失败')
  } finally {
    replyLoading.value = false
  }
}

/**
 * 分页大小改变
 */
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadData()
}

/**
 * 当前页改变
 */
const handleCurrentChange = (current) => {
  pagination.current = current
  loadData()
}

/**
 * 选择改变
 */
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

/**
 * 格式化日期时间
 */
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/**
 * 从HTML中提取纯文本
 */
const getPlainTextFromHtml = (html) => {
  if (!html) return '-'
  // 创建一个临时div元素
  const tempDiv = document.createElement('div')
  tempDiv.innerHTML = html
  // 获取纯文本内容
  return tempDiv.textContent || tempDiv.innerText || ''
}

/**
 * 获取评论图片列表
 */
const getReviewImages = (images) => {
  if (!images) return []
  if (typeof images === 'string') {
    return images.split(',').filter(img => img && img.trim())
  } else if (Array.isArray(images)) {
    return images.filter(img => img && img.trim())
  }
  return []
}
</script>

<style scoped>
.review-list {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
}

/* 对话框中的评论内容样式 */
:deep(.review-content) {
  max-height: 300px;
  overflow-y: auto;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 10px;
  line-height: 1.6;
  color: #606266;
  word-break: break-word;
}

/* 富文本内容中的图片样式 */
:deep(.review-content img) {
  max-width: 200px;
  max-height: 200px;
  height: auto;
  border-radius: 4px;
  margin: 8px 8px 8px 0;
  display: inline-block;
  vertical-align: middle;
  cursor: pointer;
  transition: all 0.3s;
}

:deep(.review-content img:hover) {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

/* 评论图片列表样式 */
.review-images {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.review-image-item {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  cursor: pointer;
  transition: all 0.3s;
}

.review-image-item:hover {
  border-color: #409eff;
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

/* 表格单元格中的内容样式 */
.content-cell {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 表格中的评论图片样式 */
.review-images-cell {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.review-img-cell {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  cursor: pointer;
  transition: all 0.3s;
  flex-shrink: 0;
}

.review-img-cell:hover {
  border-color: #409eff;
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
