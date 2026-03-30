<template>
  <div class="user-home">
    <!-- 公共头部组件：顶部栏 + 标题搜索 + 导航栏 -->
    <CustomerHeader />

    <!-- 主内容区 -->
    <div class="main-content">
      <div class="container">
        <!-- 第一行：轮播图和活动公告 -->
        <div class="top-section">
          <!-- 主横幅轮播（修复indicator-position属性） -->
          <div class="hero-banner">
            <el-carousel 
              height="400px" 
              class="main-carousel"
              :interval="3000"
              loop
              autoplay
              ref="carouselRef"
            >
              <el-carousel-item v-for="(item, index) in carouselItems" :key="`banner-${index}`">
                <div class="carousel-item-wrap">
                  <!-- 轮播图：首屏图片不使用懒加载，保证首屏加载速度 -->
                  <el-image 
                    :src="item.image" 
                    :alt="`轮播图${index + 1}`"
                    fit="cover"
                    class="carousel-img"
                    @load="handleImageLoad(index)"
                  >
                    <template #error>
                      <div class="carousel-placeholder">
                        <div class="hotpot-icon">🍲</div>
                        <div class="placeholder-text">火锅美味 尽在本店</div>
                      </div>
                    </template>
                    <template #loading>
                      <div class="carousel-placeholder">
                        <el-icon size="48"><Loading /></el-icon>
                      </div>
                    </template>
                  </el-image>
                </div>
              </el-carousel-item>
            </el-carousel>
          </div>

          <!-- 活动公告 -->
          <div class="sidebar">
            <div class="announcement-panel">
              <div class="panel-header">
                <h3>活动公告</h3>
              </div>
              <div class="announcement-list">
                <div
                  v-for="(announcement, index) in announcements"
                  :key="announcement.announcement_id"
                  class="announcement-item"
                >
                  <span class="announcement-number">{{ index + 1 }}.</span>
                  <div class="announcement-content">
                    <h4>{{ announcement.title }}</h4>
                    <p>{{ announcement.content }}</p>
                  </div>
                </div>
                <div v-if="announcements.length === 0" class="no-announcement">
                  暂无公告
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 最新套餐 -->
        <div class="products-section">
          <div class="section-header">
            <h2>最新套餐</h2>
            <p class="section-desc">本店最新精心挑选组合的商品集合</p>
          </div>
          <div class="products-grid">
            <div
              v-for="item in recommendedItems"
              :key="item.item_id"
              class="product-card"
              @click="viewProduct(item)"
            >
              <div class="product-image">
                <!-- 推荐菜品图片：使用懒加载优化性能 -->
                <img
                  v-if="item.image_url || item.imageUrl"
                  v-lazy="item.image_url || item.imageUrl"
                  :alt="item.item_name || item.itemName"
                  class="product-img"
                />
                <div v-else class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </div>
              <div class="product-info">
                <h3 class="product-name">{{ item.item_name || item.itemName }}</h3>
                <div class="product-price">¥{{ Number(item.price || 0).toFixed(1) }}</div>
                <div class="product-stock">剩余：{{ item.stock || 100 }}份</div>
                <el-button type="danger" size="small" @click.stop="addToCart(item)">
                  加入购物车
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 招牌单品 -->
        <div class="featured-section">
          <div class="section-header">
            <h2>招牌单品</h2>
            <p class="section-desc">本店人气爆款，到店必点</p>
          </div>
          <div class="featured-grid">
            <div
              v-for="item in featuredItems"
              :key="`featured-${item.item_id || item.id}`"
              class="featured-card"
              @click="viewProduct(item)"
            >
              <div class="featured-image">
                <!-- 招牌单品图片：使用懒加载优化性能 -->
                <img
                  v-if="item.image_url || item.imageUrl"
                  v-lazy="item.image_url || item.imageUrl"
                  :alt="item.name || item.item_name"
                  class="featured-img"
                />
                <div v-else class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
                <div class="featured-tag">招牌</div>
              </div>
              <div class="featured-info">
                <h3 class="featured-name">{{ item.name || item.item_name }}</h3>
                <div class="featured-price">¥{{ Number(item.price || 0).toFixed(1) }}</div>
                <div class="featured-stock" v-if="item.stock !== undefined">剩余：{{ item.stock }}份</div>
                <el-button type="danger" size="small" @click.stop="addToCart(item)">
                  加入购物车
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 顾客评价 -->
        <div class="reviews-section">
          <div class="section-header">
            <h2>顾客评价</h2>
            <p class="section-desc">真实用餐体验，口碑见证</p>
          </div>
          <div class="reviews-grid">
            <div
              v-for="review in customerReviews"
              :key="review.reviewId"
              class="review-card"
            >
              <div class="review-header">
                <div class="review-avatar">
                  <el-icon><User /></el-icon>
                </div>
                <div class="review-meta">
                  <h4 class="review-name">{{ review.name }}</h4>
                  <el-rate :model-value="review.rating" disabled size="small" />
                </div>
                <div class="review-time">{{ review.time }}</div>
              </div>
              <div class="review-content" v-html="getPlainTextFromHtml(review.content)"></div>
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
            <div v-if="customerReviews.length === 0" class="no-reviews">
              <el-icon><ChatDotRound /></el-icon>
              <p>暂无评价，快来成为第一个评价的顾客吧！</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="container">
        <div class="footer-top">
          <div class="footer-column">
            <h3 class="footer-title">快速导航</h3>
            <ul class="footer-links">
              <li @click="$router.push('/customer/home')">首页</li>
              <li @click="$router.push('/customer/menu')">菜品列表</li>
              <li @click="$router.push('/customer/order/list')">我的订单</li>
              <li @click="$router.push('/customer/profile')">个人中心</li>
            </ul>
          </div>
          <div class="footer-column">
            <h3 class="footer-title">联系方式</h3>
            <ul class="footer-links">
              <li>
                <el-icon><Phone /></el-icon>
                <span>{{ storePhone }}</span>
              </li>
              <li>
                <el-icon><Location /></el-icon>
                <span>{{ storeAddress }}</span>
              </li>
              <li>
                <el-icon><Clock /></el-icon>
                <span>{{ storeHours }}</span>
              </li>
            </ul>
          </div>
          <div class="footer-column">
            <h3 class="footer-title">关注我们</h3>
            <div class="footer-qrcode">
              <div class="qrcode-placeholder">
                <el-icon><Grid /></el-icon>
                <p>扫码点餐/领优惠</p>
              </div>
            </div>
          </div>
        </div>
        <div class="footer-bottom">
          <div class="copyright">
            © {{ new Date().getFullYear() }} 火锅店点餐系统 版权所有 | 斗音号: 6300277
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { getAnnouncementPage } from '@/api/announcement'
import { getMenuItemPage } from '@/api/menu'
import { getReviewPage } from '@/api/review'
import {
  Picture,
  ShoppingCartFull,
  Loading,
  User,
  Phone,
  Location,
  Clock,
  MapLocation,
  Grid,
  ChatDotRound
} from '@element-plus/icons-vue'
import CustomerHeader from '@/components/customer/CustomerHeader.vue'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

// 公告数据
const announcements = ref([])
// 推荐套餐数据
const recommendedItems = ref([])
// 轮播图数据
const carouselItems = ref([
  { image: '/image/banner01.jpg' },
  { image: '/image/banner02.jpg' },
  { image: '/image/banner03.jpg' }
])
// 招牌单品数据
const featuredItems = ref([])
// 顾客评价数据
const customerReviews = ref([])
// 店铺基础信息
const storePhone = ref('0769-88888888')
const storeAddress = ref('广东省东莞市XX区XX路XX号火锅城')
const storeHours = ref('10:00 - 24:00 (全年无休)')

// 轮播图Ref
const carouselRef = ref(null)

// 分页配置（若需要）
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

/**
 * 组件挂载时初始化数据
 * 并行加载首页所需的所有数据，提升加载速度
 */
onMounted(async () => {
  // 加载公告、推荐菜品、招牌单品和评价
  // 使用 Promise.all 并行加载，减少等待时间
  await Promise.all([
    loadAnnouncements(),
    loadRecommendedItems(),
    loadFeaturedItems(),
    loadCustomerReviews()
  ])

  // Element Plus 轮播图组件会自动处理 autoplay，无需手动调用方法
})

/**
 * 加载公告列表
 * 获取状态为已发布(status=1)的公告，最多10条
 */
const loadAnnouncements = async () => {
  try {
    const res = await getAnnouncementPage({
      current: 1,
      size: 10,
      status: 1
    })
    if (res.code === 200 && res.data) {
      announcements.value = (res.data.records || []).map(item => ({
        announcement_id: item.announcementId,
        title: item.title,
        content: item.content,
        type: item.type || 1,
        publish_time: item.publishTime || item.createTime
      }))
    }
  } catch (error) {
    console.error('加载公告失败:', error)
    announcements.value = []
  }
}

/**
 * 加载推荐菜品（最新套餐）
 * 获取状态为上架(status=1)的菜品，按创建时间倒序，取前20条
 */
const loadRecommendedItems = async () => {
  try {
    const res = await getMenuItemPage({
      current: 1,
      size: 20,
      status: 1
    })
    if (res.code === 200 && res.data) {
      const items = res.data.records || []
      recommendedItems.value = items.map(item => ({
        item_id: item.itemId,
        item_name: item.itemName,
        description: item.description || '',
        price: Number(item.price) || 0,
        stock: item.stock || 100,
        image_url: item.imageUrl || ''
      }))
    }
  } catch (error) {
    console.error('加载推荐菜品失败:', error)
    recommendedItems.value = []
  }
}

// 加载招牌单品
const loadFeaturedItems = async () => {
  try {
    const res = await getMenuItemPage({
      current: 1,
      size: 8, // 加载8个，取前4个作为招牌单品
      status: 1
    })
    if (res.code === 200 && res.data) {
      const items = res.data.records || []
      // 取前4个作为招牌单品
      featuredItems.value = items.slice(0, 4).map(item => ({
        id: item.itemId,
        item_id: item.itemId,
        name: item.itemName,
        item_name: item.itemName,
        price: Number(item.price) || 0,
        image_url: item.imageUrl || '',
        description: item.description || '',
        stock: item.stock || 100
      }))
    }
  } catch (error) {
    console.error('加载招牌单品失败:', error)
    featuredItems.value = []
  }
}

// 加载顾客评价
const loadCustomerReviews = async () => {
  try {
    const res = await getReviewPage({
      current: 1,
      size: 8, // 首页显示8条评价
      status: 1 // 只查询已显示的评价
    })
    if (res.code === 200 && res.data) {
      const reviews = res.data.records || []
      customerReviews.value = reviews.map(item => {
        // 处理图片：如果images是字符串，转换为数组
        let images = []
        if (item.images) {
          if (typeof item.images === 'string') {
            images = item.images.split(',').filter(img => img.trim())
          } else if (Array.isArray(item.images)) {
            images = item.images
          }
        }
        
        // 格式化时间
        const formatTime = (timeStr) => {
          if (!timeStr) return ''
          const date = new Date(timeStr)
          const year = date.getFullYear()
          const month = String(date.getMonth() + 1).padStart(2, '0')
          const day = String(date.getDate()).padStart(2, '0')
          return `${year}-${month}-${day}`
        }
        
        return {
          reviewId: item.reviewId,
          name: item.userName || item.realName || '匿名用户',
          rating: item.rating || 5,
          time: formatTime(item.createTime),
          content: item.content || '',
          images: images,
          reply: item.reply || ''
        }
      })
    }
  } catch (error) {
    console.error('加载顾客评价失败:', error)
    // 如果加载失败，保持空数组，不显示错误提示（避免影响用户体验）
    customerReviews.value = []
  }
}

// 图片加载后处理
const handleImageLoad = async (index) => {
  console.log(`轮播图 ${index + 1} 加载完成`)
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

// 查看商品详情
const viewProduct = (item) => {
  // 兼容套餐和单品的ID字段
  const itemId = item.item_id || item.id
  router.push({
    path: '/customer/menu',
    query: { itemId }
  })
}

// 添加到购物车
const addToCart = (item) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再添加商品到购物车')
    router.push('/customer/login')
    return
  }
  
  // 兼容套餐和单品的字段
  const cartItem = {
    itemId: item.item_id || item.id,
    name: item.item_name || item.name,
    price: Number(item.price) || 0,
    imageUrl: item.image_url || item.imageUrl || '',
    description: item.description || '',
    quantity: 1
  }
  cartStore.addItem(cartItem)
  cartStore.saveCart()
  ElMessage.success(`已添加 ${cartItem.name} 到购物车`)
}
</script>

<style scoped lang="scss">
// 核心修复：主页容器样式，确保可滚动
.user-home {
  width: 100%;
  background: #f5f5f5;
  position: relative;
  margin: 0;
  padding: 0;
  // 修复滚动：移除 overflow-y: auto，让页面自然滚动
  // 移除 min-height: 100vh，让内容自然扩展
  min-height: auto;
  height: auto;
  overflow: visible; // 允许内容溢出，由父容器处理滚动
  box-sizing: border-box;
}

.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 主内容区样式：核心修复滚动问题 */
.main-content {
  padding: 20px 0;
  overflow: visible; // 确保内容可溢出
  // 移除固定高度，让内容自然扩展
  min-height: auto;
}

/* 轮播图+公告区域 */
.top-section {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  align-items: flex-start;
  // 移除固定高度，允许内容自然撑开
  height: auto;
}

/* 轮播图样式（修复指示器位置，用CSS替代bottom属性） */
.hero-banner {
  flex: 1;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  height: 400px;

  .main-carousel {
    width: 100%;
    height: 100%;

    :deep(.el-carousel__container) {
      height: 100% !important;
    }

    :deep(.el-carousel__item) {
      height: 100% !important;
    }

    /* 用CSS将指示器定位到底部（替代无效的indicator-position="bottom"） */
    :deep(.el-carousel__indicators) {
      position: absolute;
      bottom: 20px;
      left: 0;
      right: 0;
      z-index: 10;
      text-align: center;
    }

    :deep(.el-carousel__indicator) {
      .el-carousel__button {
        width: 12px;
        height: 12px;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.6);
        margin: 0 6px;

        &.is-active {
          background: #fff;
          transform: scale(1.2);
        }
      }
    }
  }

  .carousel-item-wrap {
    width: 100%;
    height: 100%;
    position: relative;
  }

  .carousel-img {
    width: 100%;
    height: 100%;
    display: block;

    :deep(img) {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .carousel-placeholder {
    width: 100%;
    height: 100%;
    background: linear-gradient(135deg, #ff4d4f 0%, #ff7d45 100%);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #fff;

    .hotpot-icon {
      font-size: 80px;
      margin-bottom: 20px;
    }

    .placeholder-text {
      font-size: 24px;
      font-weight: bold;
      text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    }
  }
}

/* 侧边栏公告样式 */
.sidebar {
  flex: 0 0 280px;
  
  .announcement-panel {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    height: 400px;
    display: flex;
    flex-direction: column;
  }

  .panel-header {
    margin-bottom: 15px;
    padding-bottom: 10px;
    border-bottom: 2px solid #ffe7e7;

    h3 {
      font-size: 18px;
      font-weight: bold;
      color: #333;
      margin: 0;
    }
  }

  .announcement-list {
    flex: 1;
    overflow-y: auto;

    .announcement-item {
      display: flex;
      gap: 10px;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .announcement-number {
        flex-shrink: 0;
        color: #ff4d4f;
        font-weight: bold;
        font-size: 14px;
      }

      .announcement-content {
        flex: 1;

        h4 {
          font-size: 14px;
          font-weight: 500;
          color: #333;
          margin: 0 0 5px 0;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        p {
          font-size: 12px;
          color: #666;
          margin: 0;
          line-height: 1.5;
          overflow: hidden;
          text-overflow: ellipsis;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          line-clamp: 2;
          -webkit-box-orient: vertical;
        }
      }
    }

    .no-announcement {
      text-align: center;
      color: #999;
      padding: 40px 0;
      font-size: 14px;
    }
  }
}

/* 最新套餐样式 */
.products-section {
  width: 100%;
  background: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;

  .section-header {
    margin-bottom: 25px;
    padding-bottom: 15px;
    border-bottom: 2px solid #ffe7e7;

    h2 {
      font-size: 24px;
      font-weight: bold;
      color: #333;
      margin: 0 0 8px 0;
    }

    .section-desc {
      font-size: 14px;
      color: #999;
      margin: 0;
    }
  }

  .products-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    
    @media (max-width: 1200px) {
      grid-template-columns: repeat(3, 1fr);
    }
    
    @media (max-width: 768px) {
      grid-template-columns: repeat(2, 1fr);
    }
    
    @media (max-width: 480px) {
      grid-template-columns: 1fr;
    }
  }

  .product-card {
    background: #fff;
    border: 1px solid #e5e5e5;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      border-color: #ff4d4f;
    }
  }

  /* 懒加载图片样式 */
.product-img,
.featured-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  display: block;
  transition: opacity 0.3s;
}

.product-img[lazy="loading"],
.featured-img[lazy="loading"] {
  opacity: 0.5;
}

.product-img[lazy="loaded"],
.featured-img[lazy="loaded"] {
  opacity: 1;
}

.product-image {
    width: 100%;
    height: 180px;
    overflow: hidden;
    background: #f5f5f5;

    .el-image {
      width: 100%;
      height: 100%;
    }

    .image-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #f5f7fa 0%, #e4edf5 100%);

      .el-icon {
        font-size: 48px;
        color: #c0c4cc;
      }
    }
  }

  .product-info {
    padding: 15px;

    .product-name {
      font-size: 16px;
      font-weight: 500;
      color: #333;
      margin: 0 0 10px 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .product-price {
      font-size: 20px;
      font-weight: bold;
      color: #ff4d4f;
      margin: 0 0 8px 0;
    }

    .product-stock {
      font-size: 12px;
      color: #999;
      margin: 0 0 10px 0;
    }

    .el-button {
      width: 100%;
    }
  }
}

/* 招牌单品样式 - 统一使用最新套餐样式 */
.featured-section {
  width: 100%;
  background: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;

  .section-header {
    margin-bottom: 25px;
    padding-bottom: 15px;
    border-bottom: 2px solid #ffe7e7;

    h2 {
      font-size: 24px;
      font-weight: bold;
      color: #333;
      margin: 0 0 8px 0;
    }

    .section-desc {
      font-size: 14px;
      color: #999;
      margin: 0;
    }
  }

  .featured-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    
    @media (max-width: 1200px) {
      grid-template-columns: repeat(3, 1fr);
    }
    
    @media (max-width: 768px) {
      grid-template-columns: repeat(2, 1fr);
    }
    
    @media (max-width: 480px) {
      grid-template-columns: 1fr;
    }
  }

  .featured-card {
    background: #fff;
    border: 1px solid #e5e5e5;
    border-radius: 8px;
    overflow: hidden;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      border-color: #ff4d4f;
    }
  }

  .featured-image {
    width: 100%;
    height: 180px;
    position: relative;
    overflow: hidden;
    background: #f5f5f5;

    .featured-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      object-position: center;
      display: block;
    }

    .el-image {
      width: 100%;
      height: 100%;
    }

    .image-placeholder {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #f5f7fa 0%, #e4edf5 100%);

      .el-icon {
        font-size: 48px;
        color: #c0c4cc;
      }
    }

    .featured-tag {
      position: absolute;
      top: 8px;
      left: 8px;
      background: linear-gradient(135deg, #ff4d4f 0%, #ff7d45 100%);
      color: #fff;
      font-size: 12px;
      font-weight: 600;
      padding: 4px 10px;
      border-radius: 4px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
      z-index: 1;
    }
  }

  .featured-info {
    padding: 15px;

    .featured-name {
      font-size: 16px;
      font-weight: 500;
      color: #333;
      margin: 0 0 10px 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .featured-price {
      font-size: 20px;
      font-weight: bold;
      color: #ff4d4f;
      margin: 0 0 8px 0;
    }

    .featured-stock {
      font-size: 12px;
      color: #999;
      margin: 0 0 10px 0;
    }

    .el-button {
      width: 100%;
    }
  }
}

/* 顾客评价样式 - 统一使用最新套餐样式 */
.reviews-section {
  width: 100%;
  background: #fff;
  border-radius: 8px;
  padding: 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;

  .section-header {
    margin-bottom: 25px;
    padding-bottom: 15px;
    border-bottom: 2px solid #ffe7e7;

    h2 {
      font-size: 24px;
      font-weight: bold;
      color: #333;
      margin: 0 0 8px 0;
    }

    .section-desc {
      font-size: 14px;
      color: #999;
      margin: 0;
    }
  }

  .reviews-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 20px;
    
    @media (max-width: 1200px) {
      grid-template-columns: repeat(2, 1fr);
    }
    
    @media (max-width: 768px) {
      grid-template-columns: 1fr;
    }
  }

  .review-card {
    padding: 20px;
    border: 1px solid #f0f0f0;
    border-radius: 12px;
    background: linear-gradient(135deg, #fff 0%, #fafafa 100%);
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;

    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 4px 12px rgba(255, 77, 79, 0.15);
      border-color: #ff4d4f;
    }

    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 4px;
      height: 100%;
      background: linear-gradient(180deg, #ff4d4f 0%, #ff7d45 100%);
    }
  }

  .review-header {
    display: flex;
    align-items: flex-start;
    gap: 12px;
    margin-bottom: 15px;
  }

  .review-avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background: linear-gradient(135deg, #ffe7e7 0%, #ffd4d4 100%);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #ff4d4f;
    flex-shrink: 0;
    box-shadow: 0 2px 8px rgba(255, 77, 79, 0.2);

    .el-icon {
      font-size: 24px;
    }
  }

  .review-meta {
    flex: 1;
    min-width: 0;
  }

  .review-name {
    font-size: 15px;
    font-weight: 600;
    color: #333;
    margin: 0 0 8px 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .review-time {
    font-size: 12px;
    color: #999;
    margin-top: 8px;
    white-space: nowrap;
  }

  .review-content {
    font-size: 14px;
    color: #666;
    line-height: 1.8;
    margin-bottom: 15px;
    word-break: break-word;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .review-images {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    margin-bottom: 15px;
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
    margin-top: 15px;
    padding: 12px;
    background: linear-gradient(135deg, #fff5f5 0%, #ffe7e7 100%);
    border-radius: 8px;
    border-left: 3px solid #ff4d4f;
  }

  .reply-label {
    font-size: 12px;
    color: #ff4d4f;
    font-weight: 600;
    margin-bottom: 6px;
  }

  .reply-content {
    font-size: 13px;
    color: #666;
    line-height: 1.6;
    word-break: break-word;
  }

  .no-reviews {
    grid-column: 1 / -1;
    text-align: center;
    padding: 60px 20px;
    color: #999;

    .el-icon {
      font-size: 64px;
      color: #ddd;
      margin-bottom: 16px;
    }

    p {
      font-size: 14px;
      margin: 0;
    }
  }
}

/* 店铺信息样式 */
.store-info-section {
  width: 100%;
  margin-bottom: 20px;

  .store-info-card {
    background: linear-gradient(135deg, #fff5f5 0%, #ffe7e7 100%);
    border-radius: 8px;
    padding: 30px;
    display: flex;
    gap: 30px;
    align-items: center;
  }

  .store-info-left {
    flex: 2;
  }

  .store-title {
    font-size: 24px;
    font-weight: bold;
    color: #ff4d4f;
    margin: 0 0 15px 0;
  }

  .store-desc {
    font-size: 14px;
    color: #666;
    line-height: 1.6;
    margin: 0 0 20px 0;
  }

  .store-contact {
    display: flex;
    flex-direction: column;
    gap: 10px;
    margin-bottom: 20px;
  }

  .contact-item {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 14px;
    color: #333;

    .el-icon {
      color: #ff4d4f;
    }
  }

  .store-info-right {
    flex: 1;
    height: 200px;
  }

  .store-map-placeholder {
    width: 100%;
    height: 100%;
    background: #fff;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #999;
    border: 1px dashed #e5e5e5;

    .el-icon {
      font-size: 48px;
      margin-bottom: 10px;
      color: #ff4d4f;
    }
  }
}

/* 页脚样式 - 美化 */
.footer {
  width: 100vw;
  max-width: 100%;
  background: linear-gradient(135deg, #2c2c2c 0%, #1a1a1a 100%);
  color: #fff;
  padding: 35px 0 20px;
  margin-top: 40px;
  margin-left: calc(50% - 50vw);
  margin-right: calc(50% - 50vw);
  box-sizing: border-box;
  position: relative;
  clear: both;
  border-top: 3px solid #ff4d4f;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(255, 77, 79, 0.5), transparent);
  }
}

.footer-top {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 30px;
  margin-bottom: 25px;
  padding-bottom: 25px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  position: relative;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 60px;
    height: 2px;
    background: linear-gradient(90deg, transparent, #ff4d4f, transparent);
  }
}

.footer-column {
  transition: transform 0.3s ease;

  &:hover {
    transform: translateY(-2px);
  }
}

.footer-title {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 15px;
  color: #ff4d4f;
  position: relative;
  padding-bottom: 8px;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    width: 40px;
    height: 2px;
    background: linear-gradient(90deg, #ff4d4f, transparent);
  }
}

.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.footer-links li {
  font-size: 14px;
  color: #ccc;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  transition: all 0.3s ease;
  padding: 5px 0;
  position: relative;

  .el-icon {
    font-size: 16px;
    color: #ff4d4f;
    transition: transform 0.3s ease;
  }

  &:hover {
    color: #ff4d4f;
    padding-left: 5px;

    .el-icon {
      transform: scale(1.2);
    }
  }

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 0;
    height: 2px;
    background: #ff4d4f;
    transition: width 0.3s ease;
  }

  &:hover::before {
    width: 3px;
  }
}

.footer-qrcode {
  margin-bottom: 15px;
}

.qrcode-placeholder {
  width: 130px;
  height: 130px;
  background: linear-gradient(135deg, #3a3a3a 0%, #2a2a2a 100%);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #ccc;
  font-size: 12px;
  text-align: center;
  border: 2px solid rgba(255, 77, 79, 0.3);
  transition: all 0.3s ease;
  cursor: pointer;

  &:hover {
    border-color: #ff4d4f;
    transform: scale(1.05);
    box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3);
  }

  .el-icon {
    font-size: 36px;
    margin-bottom: 8px;
    color: #ff4d4f;
    transition: transform 0.3s ease;
  }

  &:hover .el-icon {
    transform: rotate(5deg) scale(1.1);
  }

  p {
    margin: 0;
    font-weight: 500;
  }
}

.footer-bottom {
  text-align: center;
  font-size: 12px;
  color: #999;
  padding-top: 20px;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 100px;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(255, 77, 79, 0.3), transparent);
  }

  .copyright {
    line-height: 1.8;
    letter-spacing: 0.5px;
  }
}

/* 响应式适配 */
@media (max-width: 1024px) {
  .top-section {
    flex-direction: column;
  }

  .hero-banner {
    width: 100%;
    height: 300px;
  }

  .sidebar {
    width: 100%;
    flex: 0 0 auto;

    .announcement-panel {
      height: auto;
      max-height: 400px;
    }
  }

  .store-info-card {
    flex-direction: column;
  }

  .store-info-right {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .hero-banner {
    height: 250px;
  }

  .products-grid, .featured-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }
  
  .reviews-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }
}
</style>
