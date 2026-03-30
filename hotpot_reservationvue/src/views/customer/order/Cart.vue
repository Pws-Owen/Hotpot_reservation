<template>
  <div class="cart-container">
    <!-- 公共头部组件：顶部栏 + 导航栏（隐藏搜索栏） -->
    <CustomerHeader :show-search="false" title="购物车" />
    
    <!-- 购物车内容区 -->
    <div class="cart-content">
      <!-- 头部区域：标题+返回按钮 -->
      <div class="cart-header">
        <div class="header-left">
          <h1 class="cart-title">购物车</h1>
          <p class="cart-subtitle">新鲜食材 · 地道川味 · 精选美食</p>
        </div>
        <div class="header-actions">
          <!-- 返回菜单按钮 -->
          <el-button 
            type="primary" 
            @click="$router.push('/customer/menu')"
            class="back-btn"
          >
            <el-icon><ArrowLeft /></el-icon>
            继续购物
          </el-button>
        </div>
      </div>
    
      <el-card v-if="cartStore.items.length === 0">
      <el-empty description="购物车为空">
        <el-button type="primary" @click="$router.push('/customer/menu')">
          去选购
        </el-button>
      </el-empty>
    </el-card>
    
    <el-row :gutter="20" v-else>
      <el-col :xs="24" :sm="24" :md="14" :lg="14">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>购物车商品 ({{ cartStore.totalCount }})</span>
              <el-button type="danger" size="small" @click="clearCart">清空购物车</el-button>
            </div>
          </template>
          
          <el-table :data="cartStore.items" border>
            <el-table-column label="图片" width="100">
              <template #default="scope">
                <!-- 购物车商品图片：使用懒加载 -->
                <img
                  v-lazy="scope.row.imageUrl || '/public/image/banner01.jpg'"
                  :alt="scope.row.name"
                  style="width: 60px; height: 60px; border-radius: 4px; object-fit: cover;"
                />
              </template>
            </el-table-column>
            <el-table-column prop="name" label="菜品名称" show-overflow-tooltip />
            <el-table-column prop="price" label="单价" width="120">
              <template #default="scope">
                ¥{{ scope.row.price.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="数量" width="150">
              <template #default="scope">
                <el-input-number
                  v-model="scope.row.quantity"
                  :min="1"
                  :max="99"
                  size="small"
                  @change="() => updateQuantity(scope.row)"
                />
              </template>
            </el-table-column>
            <el-table-column label="小计" width="120">
              <template #default="scope">
                ¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="scope">
                <el-button
                  type="danger"
                  size="small"
                  link
                  @click="removeItem(scope.row.itemId)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="24" :md="10" :lg="10">
        <el-card>
          <template #header>
            <span>订单总计</span>
          </template>
          
          <div class="summary">
            <div class="summary-row">
              <span>商品总数</span>
              <span>{{ cartStore.totalCount }} 件</span>
            </div>
            <div class="summary-row">
              <span>商品总额</span>
              <span>¥{{ cartStore.totalPrice.toFixed(2) }}</span>
            </div>
            <div class="summary-row">
              <span>优惠金额</span>
              <span class="discount">-¥0.00</span>
            </div>
            <el-divider />
            <div class="summary-row total">
              <span>应付总额</span>
              <span class="total-price">¥{{ cartStore.totalPrice.toFixed(2) }}</span>
            </div>
          </div>
          
          <div class="cart-actions">
            <el-button type="primary" size="large" @click="goToCheckout" style="width: 100%;">
              去结算
            </el-button>
            <el-button @click="$router.push('/customer/menu')" style="width: 100%; margin-top: 10px;">
              继续购物
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import CustomerHeader from '@/components/customer/CustomerHeader.vue'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cartStore = useCartStore()

const updateQuantity = (item) => {
  cartStore.updateQuantity(item.itemId, item.quantity)
  cartStore.saveCart()
}

const removeItem = async (itemId) => {
  try {
    const item = cartStore.items.find(i => i.itemId === itemId)
    const itemName = item ? item.name : '该商品'
    
    await ElMessageBox.confirm(
      `确定要从购物车中移除"${itemName}"吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    cartStore.removeItem(itemId)
    cartStore.saveCart()
    ElMessage.success('已移除')
  } catch (error) {
    // 用户取消删除
    if (error !== 'cancel') {
      console.error('删除商品失败:', error)
    }
  }
}

const clearCart = async () => {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    cartStore.clearCart()
    cartStore.saveCart()
    ElMessage.success('购物车已清空')
  } catch {
    // 用户取消
  }
}

const goToCheckout = () => {
  if (cartStore.items.length === 0) {
    ElMessage.warning('购物车为空')
    return
  }
  router.push('/customer/order/checkout')
}
</script>

<style scoped lang="scss">
.cart-container {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f8f8;
}

.cart-content {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  box-sizing: border-box;
}

// 头部样式
.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 2px solid #ffe7e7;
  margin-bottom: 20px;

  .header-left {
    .cart-title {
      font-size: 28px;
      font-weight: bold;
      color: #ff4d4f;
      margin: 0 0 5px 0;
    }

    .cart-subtitle {
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

.summary {
  padding: 10px 0;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: #606266;
}

.summary-row.total {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.total-price {
  color: #f56c6c;
  font-size: 20px;
}

.discount {
  color: #67c23a;
}

.cart-actions {
  margin-top: 20px;
}
</style>

