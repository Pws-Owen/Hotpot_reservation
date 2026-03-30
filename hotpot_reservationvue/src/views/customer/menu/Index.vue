<template>
  <div class="menu-page">
    <!-- 通用头部组件：顶部栏 + 标题搜索 + 导航栏（这里只要顶部+导航，不要搜索） -->
    <CustomerHeader :show-search="false" />

    <div class="menu-container">
      <!-- 头部区域：标题+搜索+购物车 -->
      <div class="menu-header">
        <div class="header-left">
          <h1 class="menu-title">本店菜单</h1>
          <p class="menu-subtitle">新鲜食材 · 地道川味 · 随心挑选</p>
        </div>
        <div class="header-actions">
          <!-- 搜索框 -->
          <el-input
            v-model="searchKeyword"
            placeholder="搜索菜品名称/关键词"
            class="search-input"
            @keyup.enter="handleSearch"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <!-- 购物车按钮 -->
          <el-button 
            type="danger" 
            @click="$router.push('/customer/order/cart')"
            class="cart-btn"
          >
            <el-badge :value="cartStore.totalCount" :max="99" :hidden="cartStore.totalCount === 0">
              <el-icon><ShoppingCartFull /></el-icon>
            </el-badge>
            购物车
          </el-button>
        </div>
      </div>
      
      <!-- 筛选区域：分类+筛选 -->
      <div class="filter-section">
        <div class="category-filter">
          <el-radio-group v-model="selectedCategoryId" @change="handleCategoryChange">
            <el-radio-button :label="null" border>全部菜品</el-radio-button>
            <el-radio-button 
              v-for="cat in categories" 
              :key="cat.categoryId" 
              :label="cat.categoryId"
              border
            >
              {{ cat.categoryName }}
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>
      
      <!-- 菜品列表 -->
      <div class="menu-list" v-loading="loading" element-loading-text="加载菜品中...">
        <!-- 空状态 -->
        <div class="empty-state" v-if="!loading && menuItems.length === 0">
          <el-empty 
            image="https://cube.elemecdn.com/7/c1/0a849e75940885cf1405333559959095.png"
            description="暂无符合条件的菜品"
          >
            <el-button type="primary" @click="resetFilter">重置筛选</el-button>
          </el-empty>
        </div>
        
        <!-- 菜品卡片网格 -->
        <div class="menu-grid">
          <div 
            v-for="item in menuItems" 
            :key="item.itemId"
            class="menu-col"
          >
            <el-card 
              class="menu-card" 
              shadow="never"
            >
              <!-- 菜品图片：使用懒加载优化性能，减少首屏加载时间 -->
              <div class="menu-image-wrap">
                <img
                  v-if="item.imageUrl"
                  v-lazy="item.imageUrl"
                  :alt="item.itemName"
                  class="menu-image"
                />
                <div v-else class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                  <span>暂无图片</span>
                </div>
                <!-- 招牌标签 -->
                <div class="tag-sign" v-if="item.isFeatured">招牌</div>
                <!-- 库存预警 -->
                <div class="stock-warning" v-if="item.stock <= 10 && item.stock > 0">
                  仅剩{{ item.stock }}份
                </div>
              </div>
              
              <!-- 菜品信息 -->
              <div class="menu-info">
                <h3 class="menu-name">{{ item.itemName }}</h3>
                <p class="menu-desc">{{ item.description || '暂无菜品描述' }}</p>
                <div class="menu-price-row">
                  <span class="menu-price">¥{{ item.price.toFixed(2) }}</span>
                  <span class="menu-stock" v-if="item.stock !== undefined">
                    库存：{{ item.stock }}份
                  </span>
                </div>
                <!-- 加购按钮 -->
                <el-button 
                  type="danger" 
                  size="small" 
                  class="add-cart-btn"
                  @click.stop="addToCart(item)"
                  :disabled="item.stock <= 0"
                >
                  <template v-if="item.stock <= 0">
                    <el-icon><SoldOut /></el-icon>
                    <span>已售罄</span>
                  </template>
                  <template v-else>
                    <el-icon><Plus /></el-icon>
                    <span>加入购物车</span>
                  </template>
                </el-button>
              </div>
            </el-card>
          </div>
        </div>
        
        <!-- 分页控件 -->
        <div class="pagination-wrap" v-if="total > 0">
          <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :page-sizes="[12, 24, 36, 48]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handlePageChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { debounce } from 'lodash' // 需安装 lodash: npm i lodash
import { getMenuItemPage, getCategories } from '@/api/menu'
import requestCache from '@/utils/cache'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
// 导入图标
import { 
  Search, ShoppingCartFull, Picture, Loading, 
  Plus, SoldOut 
} from '@element-plus/icons-vue'
import CustomerHeader from '@/components/customer/CustomerHeader.vue'

// 初始化实例
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

// 状态管理
const loading = ref(false)
const menuItems = ref([])
const categories = ref([])
const selectedCategoryId = ref(null)
const searchKeyword = ref('')
const total = ref(0) // 总菜品数

// 分页配置（一行4个，每页显示12个，3行）
const pagination = ref({
  currentPage: 1,
  pageSize: 12
})

// 防抖处理：避免快速切换分类/分页重复请求
const debouncedLoadMenuItems = debounce(async () => {
  await loadMenuItems()
}, 300)

// 挂载时加载分类和菜品
onMounted(() => {
  loadCategories()
  loadMenuItems()
})

// 监听搜索关键词变化
watch(searchKeyword, () => {
  pagination.value.currentPage = 1 // 搜索时重置页码
  debouncedLoadMenuItems()
})

/**
 * 加载菜品分类（使用缓存优化）
 * 分类数据不常变化，使用缓存减少重复请求
 * 缓存时间：10分钟
 */
const loadCategories = async () => {
  try {
    // 先检查缓存，如果存在且未过期，直接使用
    const cached = requestCache.get('/menu/categories')
    if (cached) {
      categories.value = cached
      return
    }
    
    // 缓存不存在或已过期，从服务器获取
    const res = await getCategories()
    if (res.code === 200 && res.data) {
      categories.value = res.data
      // 缓存分类数据，10分钟过期
      requestCache.set('/menu/categories', {}, res.data, 10 * 60 * 1000)
    }
  } catch (error) {
    console.error('加载分类失败:', error)
    ElMessage.error('菜品分类加载失败，请稍后重试')
  }
}

/**
 * 加载菜品列表
 * 支持分类筛选和关键词搜索
 * 使用防抖优化搜索性能，避免频繁请求
 */
const loadMenuItems = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.value.currentPage,
      size: pagination.value.pageSize,
      status: 1, // 只查询上架的菜品
      categoryId: selectedCategoryId.value
    }
    
    // 如果有关键词，添加搜索参数（支持模糊查询）
    const keyword = searchKeyword.value.trim()
    if (keyword) {
      params.itemName = keyword
    }
    
    const res = await getMenuItemPage(params)
    if (res.code === 200 && res.data) {
      menuItems.value = (res.data.records || []).map(item => ({
        itemId: item.itemId,
        itemName: item.itemName,
        description: item.description || '',
        price: Number(item.price) || 0,
        imageUrl: item.imageUrl || '',
        stock: Number(item.stock) || 0,
        isFeatured: item.isFeatured || false // 招牌标识（需后端字段支持）
      }))
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('加载菜单失败:', error)
    ElMessage.error('菜品加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 分类切换处理
const handleCategoryChange = () => {
  pagination.value.currentPage = 1 // 切换分类重置页码
  debouncedLoadMenuItems()
}

// 分页变化处理
const handlePageChange = () => {
  debouncedLoadMenuItems()
}

// 搜索处理
const handleSearch = () => {
  pagination.value.currentPage = 1
  debouncedLoadMenuItems()
}

// 重置筛选条件
const resetFilter = () => {
  selectedCategoryId.value = null
  searchKeyword.value = ''
  pagination.value.currentPage = 1
  loadMenuItems()
}

// 加入购物车（增加登录校验）
const addToCart = (item) => {
  // 未登录提示
  if (!userStore.isLoggedIn) {
    ElMessageBox.confirm(
      '添加购物车需要先登录，是否前往登录？',
      '提示',
      {
        confirmButtonText: '前往登录',
        cancelButtonText: '取消',
        type: 'warning'
      }
    ).then(() => {
      router.push('/customer/login')
    }).catch(() => {
      ElMessage.info('已取消登录')
    })
    return
  }

  // 库存校验
  if (item.stock <= 0) {
    ElMessage.warning(`${item.itemName}已售罄，无法加入购物车`)
    return
  }

  // 添加到购物车
  cartStore.addItem({
    itemId: item.itemId,
    name: item.itemName,
    price: item.price,
    imageUrl: item.imageUrl,
    description: item.description,
    quantity: 1
  })
  cartStore.saveCart()
  ElMessage.success(`已添加「${item.itemName}」到购物车`)
}

</script>

<style scoped lang="scss">
// 页面整体容器
.menu-page {
  background-color: #f8f8f8;
}

.menu-container {
  padding: 20px;
  /* 与首页和头部组件保持一致的内容宽度 */
  max-width: 1400px;
  margin: 0 auto;
  background-color: #f8f8f8;
  box-sizing: border-box;
}

// 头部样式
.menu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 2px solid #ffe7e7;
  margin-bottom: 20px;

  .header-left {
    .menu-title {
      font-size: 28px;
      font-weight: bold;
      color: #ff4d4f;
      margin: 0 0 5px 0;
    }

    .menu-subtitle {
      font-size: 14px;
      color: #999;
      margin: 0;
    }
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 15px;

    .search-input {
      width: 280px;
    }

    .cart-btn {
      display: flex;
      align-items: center;
      gap: 5px;
      padding: 8px 16px;
    }
  }
}

// 筛选区域
.filter-section {
  margin-bottom: 25px;

  .category-filter {
    :deep(.el-radio-group) {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;

      .el-radio-button {
        padding: 6px 18px;
        border-radius: 6px;
        font-size: 14px;

        &:first-child {
          --el-radio-button-bg-color: #fff5f5;
          --el-radio-button-text-color: #ff4d4f;
        }

        &.is-active {
          --el-radio-button-checked-bg-color: #ff4d4f;
          --el-radio-button-checked-text-color: #fff;
        }
      }
    }
  }
}

// 菜品列表
.menu-list {
  // 确保列表区域可滚动
  overflow: visible;

  // 使用CSS Grid布局实现一行四个商品
  .menu-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 20px;
    
    // 响应式调整
    @media (max-width: 992px) {
      grid-template-columns: repeat(3, 1fr);
    }
    
    @media (max-width: 768px) {
      grid-template-columns: repeat(2, 1fr);
    }
    
    @media (max-width: 480px) {
      grid-template-columns: 1fr;
    }
  }

  .menu-col {
    transition: all 0.3s ease;
  }

  .menu-card {
    height: 100%;
    border-radius: 12px;
    overflow: hidden;
    border: 1px solid #f0f0f0;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
      border-color: #ff4d4f;
    }
  }

  // 菜品图片区域（调整为适合4列布局）
  .menu-image-wrap {
    position: relative;
    height: 200px;
    overflow: hidden;

    /* 懒加载图片样式 */
.menu-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: opacity 0.3s;
}

.menu-image[lazy="loading"] {
  opacity: 0.5;
}

.menu-image[lazy="loaded"] {
  opacity: 1;
}

.menu-image {
      width: 100%;
      height: 100%;
    }

    .image-placeholder {
      width: 100%;
      height: 100%;
      background: #f5f5f5;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #ccc;

      .el-icon {
        font-size: 40px;
        margin-bottom: 8px;
      }

      span {
        font-size: 12px;
      }
    }

    .image-loading {
      width: 100%;
      height: 100%;
      background: #f5f5f5;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #ff4d4f;
    }

    // 标签样式
    .tag-sign {
      position: absolute;
      top: 10px;
      left: 10px;
      background: #ff4d4f;
      color: #fff;
      font-size: 12px;
      padding: 2px 8px;
      border-radius: 4px;
      font-weight: bold;
    }

    .stock-warning {
      position: absolute;
      bottom: 10px;
      right: 10px;
      background: rgba(255, 77, 79, 0.9);
      color: #fff;
      font-size: 12px;
      padding: 2px 8px;
      border-radius: 4px;
    }
  }

  // 菜品信息区域（调整为适合4列布局）
  .menu-info {
    padding: 16px;

    .menu-name {
      font-size: 16px;
      font-weight: 500;
      color: #333;
      margin: 0 0 10px 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .menu-desc {
      font-size: 13px;
      color: #999;
      margin: 0 0 14px 0;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
      min-height: 39px;
    }

    .menu-price-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .menu-price {
        font-size: 20px;
        font-weight: bold;
        color: #ff4d4f;
      }

      .menu-stock {
        font-size: 12px;
        color: #666;
      }
    }

    .add-cart-btn {
      width: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 5px;
      padding: 8px 0;
    }
  }

  // 空状态
  .empty-state {
    padding: 60px 0;
    text-align: center;
  }

  // 分页样式
  .pagination-wrap {
    margin-top: 30px;
    text-align: center;
    // 确保分页不被遮挡，且在底部显示
    padding-bottom: 20px;

    :deep(.el-pagination) {
      --el-pagination-text-color: #666;
      --el-pagination-active-color: #ff4d4f;
      justify-content: center;
    }
  }
}

// 响应式适配
@media (max-width: 1200px) {
  .menu-header .header-actions .search-input {
    width: 220px;
  }
}

@media (max-width: 768px) {
  .menu-header {
    flex-direction: column;
    align-items: flex-start;

    .header-actions {
      width: 100%;
      justify-content: space-between;

      .search-input {
        width: calc(100% - 120px);
      }
    }
  }

  .category-filter :deep(.el-radio-group) {
    flex-wrap: wrap;
  }

  .menu-list .menu-card .menu-image-wrap {
    height: 150px;
  }
}

@media (max-width: 480px) {
  .menu-container {
    padding: 10px;
    // 移动端确保滚动顺畅
    overflow-y: auto;
  }

  .menu-header .header-actions {
    flex-direction: column;
    gap: 10px;

    .search-input {
      width: 100%;
    }

    .cart-btn {
      width: 100%;
    }
  }
}
</style>
