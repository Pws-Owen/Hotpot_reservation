<template>
  <div class="review-container">
    <!-- 公共头部组件 -->
    <CustomerHeader :show-search="false" />
    
    <!-- 内容区 -->
    <div class="review-content">
      <!-- 头部区域：标题+返回按钮 -->
      <div class="review-page-header">
        <div class="header-left">
          <h1 class="review-page-title">评价中心</h1>
          <p class="review-page-subtitle">顾客评价 · 服务反馈 · 真实体验</p>
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
          <span>所有评价</span>
        </template>
      
      <!-- 评价统计 -->
      <div class="rating-statistics" v-if="ratingStats.total > 0">
        <div class="rating-overview">
          <div class="rating-score">
            <div class="score-number">{{ ratingStats.averageRating.toFixed(1) }}</div>
            <div class="score-label">满分5分</div>
          </div>
          <div class="rating-stars">
            <el-rate :model-value="ratingStats.averageRating" disabled size="large" />
            <div class="rating-count">{{ ratingStats.total }}个评分</div>
          </div>
        </div>
        <div class="rating-distribution">
          <div 
            v-for="(count, index) in ratingStats.distribution" 
            :key="5 - index"
            class="rating-bar-item"
          >
            <span class="rating-level">{{ 5 - index }}分</span>
            <div class="rating-bar">
              <div 
                class="rating-bar-fill" 
                :style="{ width: ratingStats.total > 0 ? (count / ratingStats.total * 100) + '%' : '0%' }"
              ></div>
            </div>
            <span class="rating-count-text">{{ count }}</span>
          </div>
        </div>
      </div>
      
      <div class="reviews-divider" v-if="ratingStats.total > 0 && allReviews.length > 0"></div>
      
      <div class="reviews-grid" v-if="allReviews.length > 0">
        <div
          v-for="review in allReviews"
          :key="review.reviewId"
          class="review-card"
        >
          <div class="review-header">
            <div class="review-avatar">
              <el-icon><User /></el-icon>
            </div>
            <div class="review-meta">
              <h4 class="review-name">{{ review.userName }}</h4>
              <el-rate v-model="review.rating" disabled size="small" />
            </div>
            <div class="review-time">{{ formatTime(review.createTime) }}</div>
          </div>
          <div class="review-content-text" v-html="getPlainTextFromHtml(review.content)"></div>
          <div class="review-images" v-if="review.images && review.images.length > 0">
            <el-image
              v-for="(img, i) in review.images"
              :key="i"
              :src="img"
              fit="cover"
              class="review-img"
              :preview-src-list="review.images"
            />
          </div>
          <div class="review-reply" v-if="review.reply">
            <div class="reply-label">商家回复：</div>
            <div class="reply-content">{{ review.reply }}</div>
          </div>
        </div>
      </div>
      <el-empty v-else-if="!loading" description="暂无评价" :image-size="150" />
      
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
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft, User } from '@element-plus/icons-vue'
import CustomerHeader from '@/components/customer/CustomerHeader.vue'
import { getReviewPage } from '@/api/review'

const loading = ref(false)
const allReviews = ref([])
const ratingStats = ref({
  total: 0,
  averageRating: 0,
  distribution: [0, 0, 0, 0, 0] // 5分到1分的数量
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 12,
  total: 0
})

onMounted(() => {
  loadRatingStats()
  loadReviews()
})

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 加载评价统计（查询所有用户的评价）
const loadRatingStats = async () => {
  try {
    const res = await getReviewPage({
      current: 1,
      size: 1000, // 获取所有评价用于统计
      status: 1 // 只查询已显示的评价，不传userId表示查询所有用户的评价
    })
    if (res.code === 200 && res.data) {
      const allReviewsForStats = res.data.records || []
      const total = allReviewsForStats.length
      
      if (total > 0) {
        // 计算平均分
        const sum = allReviewsForStats.reduce((acc, item) => acc + (item.rating || 5), 0)
        const average = sum / total
        
        // 计算分布（5分到1分）
        const distribution = [0, 0, 0, 0, 0]
        allReviewsForStats.forEach(item => {
          const rating = item.rating || 5
          if (rating >= 1 && rating <= 5) {
            distribution[5 - rating]++
          }
        })
        
        ratingStats.value = {
          total,
          averageRating: average,
          distribution
        }
      } else {
        ratingStats.value = {
          total: 0,
          averageRating: 0,
          distribution: [0, 0, 0, 0, 0]
        }
      }
    }
  } catch (error) {
    console.error('加载评价统计失败:', error)
  }
}

// 加载评价列表（查询所有用户的评价）
const loadReviews = async () => {
  loading.value = true
  try {
    const res = await getReviewPage({
      current: pagination.currentPage,
      size: pagination.pageSize,
      status: 1 // 只查询已显示的评价，不传userId表示查询所有用户的评价
    })
    if (res.code === 200 && res.data) {
      allReviews.value = (res.data.records || []).map(item => {
        // 处理图片：如果images是字符串，转换为数组
        let images = []
        if (item.images) {
          if (typeof item.images === 'string') {
            images = item.images.split(',').filter(img => img.trim())
          } else if (Array.isArray(item.images)) {
            images = item.images
          }
        }
        
        return {
          reviewId: item.reviewId,
          orderNo: item.orderNo || '-',
          userName: item.userName || item.realName || '匿名用户',
          content: item.content || '',
          rating: item.rating || 5,
          images: images,
          reply: item.reply || '',
          createTime: item.createTime
        }
      })
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载评价列表失败:', error)
    // 如果后端接口未实现，显示友好提示
    if (error.response?.status === 404) {
      ElMessage.warning('评价功能暂未实现')
    } else {
      ElMessage.error('加载评价列表失败')
    }
  } finally {
    loading.value = false
  }
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  loadReviews()
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
  loadReviews()
}

// 从HTML中提取纯文本，移除所有HTML标签（特别是图片标签）
const getPlainTextFromHtml = (html) => {
  if (!html) return ''
  
  // 创建临时DOM元素
  const tempDiv = document.createElement('div')
  tempDiv.innerHTML = html
  
  // 移除所有图片标签
  const images = tempDiv.querySelectorAll('img')
  images.forEach(img => img.remove())
  
  // 获取纯文本内容
  let text = tempDiv.textContent || tempDiv.innerText || ''
  
  // 清理多余的空白字符
  text = text.replace(/\s+/g, ' ').trim()
  
  // 如果文本为空，返回提示
  if (!text) {
    return '<span style="color: #c0c4cc;">暂无文字评价</span>'
  }
  
  // 限制长度，避免过长
  if (text.length > 200) {
    text = text.substring(0, 200) + '...'
  }
  
  // 转义HTML特殊字符，防止XSS
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
    .replace(/\n/g, '<br>')
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

// 页面头部样式
.review-page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 2px solid #ffe7e7;
  margin-bottom: 20px;

  .header-left {
    .review-page-title {
      font-size: 28px;
      font-weight: bold;
      color: #ff4d4f;
      margin: 0 0 5px 0;
    }

    .review-page-subtitle {
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

/* 评价统计样式 */
.rating-statistics {
  display: flex;
  gap: 40px;
  padding: 20px 0;
  margin-bottom: 20px;

  @media (max-width: 768px) {
    flex-direction: column;
    gap: 20px;
  }
}

.reviews-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, #e5e5e5, transparent);
  margin: 20px 0;
}

.rating-overview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 200px;
  padding: 20px;
  background: linear-gradient(135deg, #fff5f5 0%, #ffe7e7 100%);
  border-radius: 12px;
}

.rating-score {
  text-align: center;
  margin-bottom: 15px;

  .score-number {
    font-size: 48px;
    font-weight: bold;
    color: #ff4d4f;
    line-height: 1;
    margin-bottom: 5px;
  }

  .score-label {
    font-size: 14px;
    color: #999;
  }
}

.rating-stars {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;

  .rating-count {
    font-size: 14px;
    color: #666;
  }
}

.rating-distribution {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  justify-content: center;
}

.rating-bar-item {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 300px;

  .rating-level {
    width: 35px;
    font-size: 14px;
    color: #666;
    text-align: right;
  }

  .rating-bar {
    flex: 1;
    height: 20px;
    background: #f0f0f0;
    border-radius: 10px;
    overflow: hidden;
    position: relative;
  }

  .rating-bar-fill {
    height: 100%;
    background: linear-gradient(90deg, #ff4d4f 0%, #ff7d45 100%);
    border-radius: 10px;
    transition: width 0.5s ease;
  }

  .rating-count-text {
    width: 40px;
    font-size: 14px;
    color: #666;
    text-align: left;
  }
}

.reviews-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 30px;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.review-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    transform: translateY(-2px);
  }

  .review-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 15px;
  }

  .review-avatar {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: #ffe7e7;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #ff4d4f;
    flex-shrink: 0;
  }

  .review-meta {
    flex: 1;
  }

  .review-name {
    font-size: 14px;
    font-weight: 500;
    color: #303133;
    margin: 0 0 5px 0;
  }

  .review-time {
    font-size: 12px;
    color: #909399;
    white-space: nowrap;
  }

  .review-content-text {
    font-size: 14px;
    color: #606266;
    line-height: 1.8;
    margin-bottom: 12px;
    word-break: break-word;
  }

  .review-images {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    margin-bottom: 12px;
  }

  .review-img {
    width: 70px;
    height: 70px;
    border-radius: 6px;
    cursor: pointer;
    border: 2px solid #f0f0f0;
    transition: all 0.3s;

    &:hover {
      border-color: #ff4d4f;
      transform: scale(1.05);
    }
  }

  .review-reply {
    margin-top: 12px;
    padding: 12px;
    background: #f5f7fa;
    border-left: 3px solid #ff4d4f;
    border-radius: 4px;

    .reply-label {
      font-size: 12px;
      color: #ff4d4f;
      font-weight: 500;
      margin-bottom: 6px;
    }

    .reply-content {
      font-size: 13px;
      color: #606266;
      line-height: 1.6;
    }
  }
}

.pagination {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}
</style>

