<template>
  <div class="customer-header">
    <!-- 顶部栏 -->
    <div class="top-bar">
      <div class="container">
        <div class="top-left">
          <span class="greeting" v-if="userStore.isLoggedIn">
            欢迎您，{{ userInfo.realName || userInfo.username || '顾客' }}！
          </span>
          <span class="greeting" v-else>
            欢迎访问火锅店点餐系统！
          </span>
          <el-button 
            v-if="userStore.isLoggedIn"
            type="text" 
            class="logout-btn" 
            @click="handleLogout"
          >
            退出登录
          </el-button>
          <el-button 
            v-else
            type="text" 
            class="login-btn" 
            @click="router.push('/customer/login')"
          >
            登录
          </el-button>
        </div>
        <div class="top-right">
          <el-button type="text" @click="router.push('/customer/home')">网站首页</el-button>
          <el-button type="text" @click="router.push('/customer/profile')">个人中心</el-button>
          <el-button 
            v-if="userStore.isLoggedIn"
            type="text" 
            @click="router.push('/customer/order/list')"
          >
            我的订单
          </el-button>
          <el-button 
            v-if="userStore.isLoggedIn"
            type="text" 
            @click="router.push('/customer/reservation/list')"
          >
            我的预约
          </el-button>
          <el-button 
            v-if="userStore.isLoggedIn"
            type="text" 
            @click="router.push('/customer/review/my')"
          >
            我的评价
          </el-button>
          <el-button type="text" @click="router.push('/customer/announcement')">公告</el-button>
          <el-button type="text" @click="router.push('/customer/order/cart')" class="cart-btn">
            <el-badge :value="cartCount" :max="99" :hidden="cartCount === 0">
              <el-icon><ShoppingCart /></el-icon>
            </el-badge>
            购物车
          </el-button>
        </div>
      </div>
    </div>

    <!-- 标题和搜索栏 -->
    <div class="header-section" v-if="showSearch">
      <div class="container">
        <div class="header-content">
          <h1 class="system-title">{{ title }}</h1>
          <div class="search-box">
            <el-input
              v-model="searchKeyword"
              :placeholder="searchPlaceholder"
              size="large"
              class="search-input"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button type="danger" size="large" @click="handleSearch">搜索</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 导航栏 -->
    <div class="nav-bar" v-if="showNav">
      <div class="container">
        <div class="nav-items">
          <div class="nav-item" @click="router.push('/customer/home')">
            <span class="brand-name">火锅店点餐系统</span>
          </div>
          <div class="nav-item" @click="router.push('/customer/home')">首页</div>
          <div class="nav-item" @click="router.push('/customer/reservation')">位置预订</div>
          <div class="nav-item" @click="router.push('/customer/menu')">本店菜品</div>
          <div class="nav-item" @click="router.push('/customer/profile')">个人中心</div>
          <div class="nav-item" @click="router.push('/customer/review')">顾客评价</div>
          <div class="nav-item" @click="router.push('/customer/announcement')">公告</div>
          <div class="nav-item" @click="router.push('/customer/about')">关于我们</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { Search, ShoppingCart } from '@element-plus/icons-vue'

const props = defineProps({
  // 是否显示搜索栏
  showSearch: {
    type: Boolean,
    default: true
  },
  // 是否显示导航栏
  showNav: {
    type: Boolean,
    default: true
  },
  // 标题文本
  title: {
    type: String,
    default: '火锅店点餐系统'
  },
  // 搜索框占位文本
  searchPlaceholder: {
    type: String,
    default: '搜索物品'
  }
})

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const searchKeyword = ref('')

const userInfo = computed(() => {
  return {
    userId: userStore.userId,
    username: userStore.username,
    realName: userStore.realName || userStore.username,
    avatar: userStore.avatar || ''
  }
})

const cartCount = computed(() => {
  return cartStore.totalCount
})

// 搜索处理（直接跳转到菜单页并携带关键字）
const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push({
      path: '/customer/menu',
      query: { keyword: searchKeyword.value.trim() }
    })
  } else {
    ElMessage.info('请输入要搜索的内容')
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    router.push('/customer/login')
    ElMessage.success('已退出登录')
  } catch {
    // 用户取消
  }
}
</script>

<style scoped lang="scss">
.customer-header {
  width: 100%;
}

/* 与首页保持一致的居中宽度 */
.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  box-sizing: border-box;
}

/* 顶部栏样式 */
.top-bar {
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
  padding: 8px 0;
  font-size: 14px;

  .container {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .top-left {
    display: flex;
    align-items: center;
    gap: 10px;

    .greeting {
      color: #333;
    }

    .logout-btn {
      color: #ff4d4f;
      padding: 0;
      font-size: 14px;
    }
  }

  .top-right {
    display: flex;
    gap: 15px;
    align-items: center;

    .el-button {
      color: #666;
      padding: 0;
      font-size: 14px;

      &:hover {
        color: #ff4d4f;
      }
    }

    .cart-btn {
      display: flex;
      align-items: center;
      gap: 5px;
    }
    
    /* 修复 el-badge 的滚动事件警告 */
    :deep(.el-badge) {
      touch-action: pan-y;
    }
  }

  .login-btn {
    color: #409eff;
    
    &:hover {
      color: #66b1ff;
    }
  }
}

/* 标题和搜索栏样式 */
.header-section {
  background: #fff;
  padding: 20px 0;
  border-bottom: 2px solid #ff4d4f;

  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 30px;
  }

  .system-title {
    font-size: 32px;
    font-weight: bold;
    color: #ff4d4f;
    margin: 0;
    white-space: nowrap;
  }

  .search-box {
    display: flex;
    gap: 10px;
    flex: 1;
    max-width: 500px;

    .search-input {
      flex: 1;
    }

    .el-button {
      white-space: nowrap;
    }
  }
}

/* 导航栏样式 */
.nav-bar {
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
  padding: 0;

  .nav-items {
    display: flex;
    align-items: center;
    gap: 30px;
    padding: 15px 0;
  }

  .nav-item {
    cursor: pointer;
    color: #666;
    font-size: 16px;
    transition: color 0.3s;
    padding: 5px 0;

    &:hover {
      color: #ff4d4f;
    }

    .brand-name {
      font-weight: bold;
      color: #ff4d4f;
    }
  }
}

/* 响应式适配 */
@media (max-width: 768px) {
  .header-section .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .search-box {
    width: 100%;
    max-width: 100%;
  }

  .nav-bar .nav-items {
    flex-wrap: wrap;
    gap: 15px;
  }
}
</style>
