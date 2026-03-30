<template>
  <div class="checkout-container">
    <!-- 公共头部组件 -->
    <CustomerHeader :show-search="false" title="订单确认" />
    
    <!-- 内容区 -->
    <div class="checkout-content">
      <!-- 头部区域：标题+操作 -->
      <div class="checkout-header">
        <div class="header-left">
          <h1 class="checkout-title">订单确认</h1>
          <p class="checkout-subtitle">新鲜食材 · 地道川味 · 精选美食</p>
        </div>
        <div class="header-actions">
          <!-- 返回购物车按钮 -->
          <el-button 
            type="primary" 
            @click="$router.back()"
            class="back-btn"
          >
            <el-icon><ArrowLeft /></el-icon>
            返回修改
          </el-button>
        </div>
      </div>
    
      <el-row :gutter="20">
      <el-col :span="16">
        <!-- 订单信息 -->
        <el-card class="order-info-card">
          <template #header>
            <span>订单信息</span>
          </template>
          
          <!-- 订单信息表单 -->
          <el-form :model="orderForm" label-width="100px">
            <el-form-item label="订单类型">
              <el-tag :type="reservationInfo ? 'warning' : 'success'" size="large">
                {{ reservationInfo ? '预约点餐' : '堂食' }}
              </el-tag>
              <span style="margin-left: 10px; color: #999; font-size: 14px;">
                {{ reservationInfo ? '基于预约的点餐订单' : '店内用餐，新鲜美味' }}
              </span>
            </el-form-item>
            
            <el-form-item v-if="reservationInfo" label="关联预约">
              <el-tag type="info">预约号：{{ reservationInfo.reservationNo }}</el-tag>
              <span style="margin-left: 10px; color: #999; font-size: 14px;">餐桌ID：{{ reservationInfo.tableId }}</span>
            </el-form-item>
            
            <el-form-item label="备注">
              <el-input
                v-model="orderForm.remark"
                type="textarea"
                :rows="3"
                placeholder="请输入备注信息（选填），如：不要香菜、少辣等"
              />
            </el-form-item>
          </el-form>
        </el-card>
        
        <!-- 菜品清单 -->
        <el-card class="items-card" style="margin-top: 20px;">
          <template #header>
            <span>菜品清单</span>
          </template>
          <el-table :data="cartStore.items" border>
            <el-table-column label="图片" width="100">
              <template #default="scope">
                <!-- 订单确认页商品图片：使用懒加载 -->
                <img
                  v-lazy="scope.row.imageUrl || 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjAiIGhlaWdodD0iNjAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjYwIiBoZWlnaHQ9IjYwIiBmaWxsPSIjZjBmMGYwIi8+PHRleHQgeD0iNTAwIiB5PSI1MDAiIGZvbnQtc2l6ZT0iMTIiIGZpbGw9IiM5OTkiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGR5PSIuM2VtIj7lm77niYfliqDovb3lpLHotKU8L3RleHQ+PC9zdmc+'"
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
            <el-table-column prop="quantity" label="数量" width="100" />
            <el-table-column label="小计" width="120">
              <template #default="scope">
                ¥{{ (scope.row.price * scope.row.quantity).toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="summary-card">
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
          
          <div class="checkout-actions">
            <el-button type="primary" size="large" @click="submitOrder" :loading="submitting" style="width: 100%;">
              提交订单
            </el-button>
            <el-button @click="$router.back()" style="width: 100%; margin-top: 10px;">
              返回修改
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import CustomerHeader from '@/components/customer/CustomerHeader.vue'
import { useCartStore } from '@/stores/cart'
import { createOrder } from '@/api/order'
import { getMenuItemById } from '@/api/menu'

const router = useRouter()
const cartStore = useCartStore()
const submitting = ref(false)
const reservationInfo = ref(null)

const orderForm = reactive({
  orderType: 1, // 1-堂食
  remark: ''
})

onMounted(() => {
  // 检查购物车是否为空
  if (cartStore.items.length === 0) {
    ElMessage.warning('购物车为空，请先添加商品')
    router.push('/customer/order/cart')
    return
  }
  
  // 从 sessionStorage 获取预约信息（如果是从预约页面跳转过来的）
  try {
    const reservationStr = sessionStorage.getItem('currentReservation')
    if (reservationStr) {
      reservationInfo.value = JSON.parse(reservationStr)
    }
  } catch (error) {
    console.error('读取预约信息失败:', error)
  }
})

/**
 * 提交订单
 * 流程：
 * 1. 库存校验：检查所有商品的库存是否充足
 * 2. 创建订单：调用后端API创建订单
 * 3. 清空购物车：订单创建成功后清空购物车
 * 4. 跳转支付：跳转到支付页面完成支付
 */
const submitOrder = async () => {
  // 先进行库存校验，避免订单创建后因库存不足而失败
  try {
    const stockErrors = []
    
    // 批量检查所有商品的库存
    for (const cartItem of cartStore.items) {
      try {
        const itemRes = await getMenuItemById(cartItem.itemId)
        if (itemRes.code === 200 && itemRes.data) {
          const item = itemRes.data
          const stock = Number(item.stock) || 0
          
          if (stock <= 0) {
            stockErrors.push(`${cartItem.name} 已售罄`)
          } else if (stock < cartItem.quantity) {
            stockErrors.push(`${cartItem.name} 库存不足，当前库存：${stock}，您需要：${cartItem.quantity}`)
          }
        }
      } catch (error) {
        console.error(`检查商品 ${cartItem.name} 库存失败:`, error)
        // 如果获取库存失败，继续提交，让后端验证
      }
    }
    
    // 如果有库存错误，提示用户
    if (stockErrors.length > 0) {
      ElMessage.warning({
        message: '以下商品库存不足：\n' + stockErrors.join('\n'),
        duration: 5000,
        showClose: true
      })
      // 跳转回购物车
      router.push('/customer/order/cart')
      return
    }
  } catch (error) {
    console.error('库存校验失败:', error)
    // 库存校验失败不影响提交，让后端验证
  }
  
  submitting.value = true
  
  try {
    const orderData = {
      orderType: reservationInfo.value ? 3 : 1, // 如果有预约信息，订单类型为3-预约点餐，否则为1-堂食
      items: cartStore.items.map(item => ({
        itemId: item.itemId,
        quantity: item.quantity
      })),
      remark: orderForm.remark || ''
    }
    
    // 如果有预约信息，添加到订单数据中
    if (reservationInfo.value) {
      orderData.reservationId = reservationInfo.value.reservationId
      orderData.tableId = reservationInfo.value.tableId
    }
    
    const res = await createOrder(orderData)
    if (res.code === 200) {
      ElMessage.success('订单创建成功！订单号：' + (res.data?.orderNo || ''))
      
      // 清空购物车
      cartStore.clearCart()
      cartStore.saveCart()
      
      // 清除预约信息（订单已创建，不再需要）
      if (reservationInfo.value) {
        sessionStorage.removeItem('currentReservation')
        reservationInfo.value = null
      }
      
      // 跳转到支付页面
      router.push({
        path: '/customer/payment',
        query: { orderId: res.data?.orderId || res.data?.id }
      })
    } else {
      ElMessage.error(res.message || '订单创建失败')
    }
  } catch (error) {
    console.error('提交订单失败:', error)
    ElMessage.error(error.message || '订单提交失败，请重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.checkout-container {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f8f8;
}

.checkout-content {
  padding: 20px;
  max-width: 1600px;
  margin: 0 auto;
  box-sizing: border-box;
}

// 头部样式
.checkout-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 2px solid #ffe7e7;
  margin-bottom: 20px;

  .header-left {
    .checkout-title {
      font-size: 28px;
      font-weight: bold;
      color: #ff4d4f;
      margin: 0 0 5px 0;
    }

    .checkout-subtitle {
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

.checkout-actions {
  margin-top: 20px;
}

// 响应式适配
@media (max-width: 768px) {
  .checkout-header {
    flex-direction: column;
    align-items: flex-start;

    .header-actions {
      width: 100%;
      justify-content: flex-end;
    }
  }
}
</style>
