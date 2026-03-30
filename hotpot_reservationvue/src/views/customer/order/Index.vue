<template>
  <div class="order-container">
    <!-- 公共头部组件 -->
    <CustomerHeader :show-search="false" title="在线点餐" />
    
    <!-- 内容区 -->
    <div class="order-content">
      <!-- 头部区域：标题+返回按钮 -->
      <div class="order-header">
        <div class="header-left">
          <h1 class="order-title">在线点餐</h1>
          <p class="order-subtitle">新鲜食材 · 地道川味 · 随心挑选</p>
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
      
      <el-row :gutter="20">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>菜单</span>
            </div>
          </template>
          <el-table :data="menuItems" style="width: 100%" border>
            <el-table-column label="图片" width="100">
              <template #default="scope">
                <el-image
                  v-if="scope.row.imageUrl"
                  :src="scope.row.imageUrl"
                  fit="cover"
                  style="width: 60px; height: 60px; border-radius: 4px;"
                />
                <span v-else style="color: #909399; font-size: 12px;">暂无图片</span>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="菜品名称" width="180" show-overflow-tooltip />
            <el-table-column prop="description" label="描述" show-overflow-tooltip />
            <el-table-column prop="price" label="价格" width="100">
              <template #default="scope">
                ¥{{ scope.row.price.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="数量" width="150">
              <template #default="scope">
                <el-input-number
                  v-model="scope.row.quantity"
                  :min="0"
                  :max="99"
                  size="small"
                  @change="calculateTotal"
                />
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>购物车</span>
            </div>
          </template>
          <div v-if="cartItems.length === 0" class="empty-cart">
            <el-empty description="购物车为空" />
          </div>
          <div v-else>
            <div v-for="item in cartItems" :key="item.id" class="cart-item">
              <div class="item-info">
                <div class="item-name">{{ item.name }}</div>
                <div class="item-price">¥{{ item.price }} x {{ item.quantity }}</div>
              </div>
              <div class="item-total">¥{{ (item.price * item.quantity).toFixed(2) }}</div>
            </div>
            
            <el-divider />
            
            <div class="cart-total">
              <div class="total-label">总计</div>
              <div class="total-price">¥{{ totalPrice.toFixed(2) }}</div>
            </div>
            
            <div class="cart-actions">
              <el-button type="primary" @click="submitOrder" :loading="loading">
                提交订单
              </el-button>
              <el-button @click="clearCart">清空购物车</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import CustomerHeader from '@/components/customer/CustomerHeader.vue'
import { getMenuItemPage } from '@/api/menu'
import { createOrder } from '@/api/order'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const loading = ref(false)
const menuItems = ref([])

onMounted(() => {
  // 检查登录状态
  const userStore = useUserStore()
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/customer/login')
    return
  }
  loadMenuItems()
})

const loadMenuItems = async () => {
  try {
    const res = await getMenuItemPage({ current: 1, size: 100, status: 1 })
    if (res.code === 200 && res.data) {
      const items = res.data.records || []
      menuItems.value = items.map(item => ({
        itemId: item.itemId,
        name: item.itemName,
        description: item.description || '',
        price: Number(item.price) || 0,
        imageUrl: item.imageUrl || '',
        quantity: 0
      }))
    }
  } catch (error) {
    console.error('加载菜单失败:', error)
    ElMessage.error('加载菜单失败')
  }
}

const cartItems = computed(() => {
  return menuItems.value.filter(item => item.quantity > 0)
})

const totalPrice = computed(() => {
  return cartItems.value.reduce((total, item) => {
    return total + (item.price * item.quantity)
  }, 0)
})

const calculateTotal = () => {
  // 计算总价的逻辑已经通过计算属性实现
}

const submitOrder = async () => {
  if (cartItems.value.length === 0) {
    ElMessage.warning('请先添加菜品到购物车')
    return
  }
  
  loading.value = true
  
  try {
    const items = cartItems.value.map(item => ({
      itemId: item.itemId,
      quantity: item.quantity
    }))
    
    const orderData = {
      orderType: 1, // 1-堂食
      items: items,
      remark: ''
    }
    
    const res = await createOrder(orderData)
    if (res.code === 200) {
      ElMessage.success('订单提交成功！订单号：' + (res.data?.orderNo || ''))
      clearCart()
      router.push('/customer/order/list')
    } else {
      ElMessage.error(res.message || '订单提交失败')
    }
  } catch (error) {
    console.error('提交订单失败:', error)
    ElMessage.error(error.message || '订单提交失败，请重试')
  } finally {
    loading.value = false
  }
}

const clearCart = () => {
  menuItems.value.forEach(item => {
    item.quantity = 0
  })
}
</script>

<style scoped lang="scss">
.order-container {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f8f8;
}

.order-content {
  padding: 20px;
  max-width: 1600px;
  margin: 0 auto;
  box-sizing: border-box;
}

// 头部样式
.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 2px solid #ffe7e7;
  margin-bottom: 20px;

  .header-left {
    .order-title {
      font-size: 28px;
      font-weight: bold;
      color: #ff4d4f;
      margin: 0 0 5px 0;
    }

    .order-subtitle {
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

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-cart {
  padding: 20px 0;
}

.cart-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.item-info {
  flex: 1;
}

.item-name {
  font-weight: bold;
}

.item-price {
  color: #909399;
  font-size: 14px;
}

.item-total {
  font-weight: bold;
  color: #f56c6c;
}

.cart-total {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.total-label {
  font-weight: bold;
}

.total-price {
  font-weight: bold;
  color: #f56c6c;
  font-size: 18px;
}

.cart-actions {
  display: flex;
  gap: 10px;
}
</style>