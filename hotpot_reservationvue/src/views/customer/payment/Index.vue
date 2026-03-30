<template>
  <div class="payment-container">
    <!-- 公共头部组件 -->
    <CustomerHeader :show-search="false" title="订单支付" />
    
    <div class="payment-content">
      <!-- 提示横幅 -->
      <div class="payment-notice">
        <el-icon><InfoFilled /></el-icon>
        <span>确认支付前请先核对订单信息</span>
      </div>
      
      <el-row :gutter="20" v-loading="loading">
        <el-col :span="16">
          <el-card class="order-info-card">
            <template #header>
              <span>订单信息</span>
            </template>
            
            <el-descriptions :column="2" border v-if="orderInfo">
              <el-descriptions-item label="订单号">
                {{ orderInfo.orderNo }}
              </el-descriptions-item>
              <el-descriptions-item label="下单时间">
                {{ formatDateTime(orderInfo.createTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="订单类型">
                {{ getOrderTypeText(orderInfo.orderType) }}
              </el-descriptions-item>
              <el-descriptions-item label="订单状态">
                <el-tag :type="getStatusType(orderInfo.status)">
                  {{ getStatusText(orderInfo.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="订单总额" :span="2">
                <span class="total-amount">¥{{ Number(orderInfo.actualAmount || 0).toFixed(2) }}</span>
              </el-descriptions-item>
            </el-descriptions>
            
            <!-- 订单详情 -->
            <el-divider />
            <h3>订单详情</h3>
            <el-table :data="orderDetails" border style="margin-top: 10px;">
              <el-table-column prop="itemName" label="菜品名称" show-overflow-tooltip />
              <el-table-column prop="price" label="单价" width="120">
                <template #default="scope">
                  ¥{{ Number(scope.row.price || 0).toFixed(2) }}
                </template>
              </el-table-column>
              <el-table-column prop="quantity" label="数量" width="100" />
              <el-table-column label="小计" width="120">
                <template #default="scope">
                  ¥{{ Number(scope.row.subtotal || 0).toFixed(2) }}
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card class="payment-card">
            <template #header>
              <span>选择支付方式</span>
            </template>
            
            <!-- 支付方式网格 -->
            <div class="payment-methods-grid">
              <div
                v-for="method in paymentMethods"
                :key="method.value"
                class="payment-method-item"
                :class="{ active: paymentMethod === method.value }"
                @click="paymentMethod = method.value"
              >
                <el-radio :model-value="paymentMethod" :label="method.value" class="payment-radio">
                  <div class="payment-method-content">
                    <div class="payment-icon" :style="{ background: method.color }">
                      <el-icon :size="32"><component :is="method.icon" /></el-icon>
                    </div>
                    <span class="payment-name">{{ method.label }}</span>
                  </div>
                </el-radio>
              </div>
            </div>
            
            <el-divider />
            
            <div class="payment-summary">
              <div class="summary-row">
                <span>订单总额</span>
                <span>¥{{ Number(orderInfo?.actualAmount || 0).toFixed(2) }}</span>
              </div>
              <div class="summary-row total">
                <span>应付金额</span>
                <span class="total-price">¥{{ Number(orderInfo?.actualAmount || 0).toFixed(2) }}</span>
              </div>
            </div>
            
            <div class="payment-actions">
              <el-button
                type="primary"
                size="large"
                @click="handlePay"
                :loading="paying"
                :disabled="!paymentMethod || orderInfo?.status !== 0"
                class="confirm-pay-btn"
              >
                {{ orderInfo?.status === 0 ? '确认支付' : '订单已支付' }}
              </el-button>
              <el-button @click="handleBack" class="back-btn">
                返回
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, CreditCard, InfoFilled, Wallet, Money } from '@element-plus/icons-vue'
import CustomerHeader from '@/components/customer/CustomerHeader.vue'
import { getOrderById, getOrderDetails, payOrder } from '@/api/order'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const paying = ref(false)
const orderInfo = ref(null)
const orderDetails = ref([])
const paymentMethod = ref('WECHAT')

// 支付方式配置（2行3列，共6个选项）
const paymentMethods = [
  {
    value: 'WECHAT',
    label: '微信支付',
    icon: ChatDotRound,
    color: 'linear-gradient(135deg, #07c160 0%, #06ad56 100%)'
  },
  {
    value: 'ALIPAY',
    label: '支付宝支付',
    icon: CreditCard,
    color: 'linear-gradient(135deg, #1677ff 0%, #0958d9 100%)'
  },
  {
    value: 'CCB',
    label: '建设银行',
    icon: Wallet,
    color: 'linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%)'
  },
  {
    value: 'ABC',
    label: '农业银行',
    icon: Wallet,
    color: 'linear-gradient(135deg, #059669 0%, #10b981 100%)'
  },
  {
    value: 'BOC',
    label: '中国银行',
    icon: Wallet,
    color: 'linear-gradient(135deg, #dc2626 0%, #ef4444 100%)'
  },
  {
    value: 'BOCOM',
    label: '交通银行',
    icon: Wallet,
    color: 'linear-gradient(135deg, #1e40af 0%, #3b82f6 100%)'
  }
]

onMounted(async () => {
  const orderId = route.query.orderId
  if (!orderId) {
    ElMessage.error('订单ID不能为空')
    router.push('/customer/order/list')
    return
  }
  await loadOrderInfo(orderId)
})

const loadOrderInfo = async (orderId) => {
  loading.value = true
  try {
    // 加载订单信息
    const orderRes = await getOrderById(orderId)
    if (orderRes.code === 200 && orderRes.data) {
      orderInfo.value = orderRes.data
    }
    
    // 加载订单详情
    const detailsRes = await getOrderDetails(orderId)
    if (detailsRes.code === 200 && detailsRes.data) {
      orderDetails.value = detailsRes.data
    }
  } catch (error) {
    console.error('加载订单信息失败:', error)
    ElMessage.error('加载订单信息失败')
  } finally {
    loading.value = false
  }
}

const handlePay = async () => {
  if (!paymentMethod.value) {
    ElMessage.warning('请选择支付方式')
    return
  }
  
  if (orderInfo.value.status !== 0) {
    ElMessage.warning('订单状态不正确，无法支付')
    return
  }
  
  // 获取支付方式名称
  const getPaymentMethodName = (value) => {
    const method = paymentMethods.find(m => m.value === value)
    return method ? method.label : '所选支付方式'
  }
  
  // 确认支付对话框
  try {
    await ElMessageBox.confirm(
      `确认使用${getPaymentMethodName(paymentMethod.value)}支付 ¥${Number(orderInfo.value.actualAmount || 0).toFixed(2)} 吗？`,
      '确认支付',
      {
        confirmButtonText: '确认支付',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      }
    )
  } catch {
    // 用户取消支付
    return
  }
  
  paying.value = true
  try {
    // 模拟支付延迟
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 调用支付接口（模拟支付，实际会调用后端接口）
    const res = await payOrder(orderInfo.value.orderId, paymentMethod.value)
    if (res.code === 200) {
      ElMessage.success('支付成功！')
      // 刷新订单信息
      await loadOrderInfo(orderInfo.value.orderId)
      // 延迟跳转到支付成功页面
      setTimeout(() => {
        router.push({
          path: '/customer/payment/success',
          query: { orderId: orderInfo.value.orderId }
        })
      }, 1000)
    } else {
      ElMessage.error(res.message || '支付失败')
      // 跳转到支付失败页面
      router.push({
        path: '/customer/payment/fail',
        query: { orderId: orderInfo.value.orderId }
      })
    }
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error(error.message || '支付失败，请重试')
    // 跳转到支付失败页面
    router.push({
      path: '/customer/payment/fail',
      query: { orderId: orderInfo.value.orderId }
    })
  } finally {
    paying.value = false
  }
}

const handleBack = () => {
  router.push('/customer/order/list')
}

const getOrderTypeText = (type) => {
  const map = {
    1: '堂食'
  }
  return map[type] || '未知'
}

const getStatusText = (status) => {
  const map = {
    0: '待支付',
    1: '已支付',
    2: '制作中',
    3: '已完成',
    4: '已取消',
    5: '已退款'
  }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = {
    0: 'warning',
    1: 'success',
    2: 'info',
    3: 'success',
    4: 'danger',
    5: 'info'
  }
  return map[status] || ''
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}
</script>

<style scoped lang="scss">
.payment-container {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f8f8;
}

.payment-content {
  padding: 20px;
  max-width: 1600px;
  margin: 0 auto;
  box-sizing: border-box;
}

// 提示横幅（浅绿色）
.payment-notice {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-left: 4px solid #10b981;
  padding: 12px 20px;
  margin-bottom: 20px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: #059669;
  font-size: 14px;
  font-weight: 500;

  .el-icon {
    font-size: 18px;
    color: #10b981;
  }
}

.total-amount {
  font-size: 24px;
  font-weight: bold;
  color: #ff4d4f;
}

// 支付方式网格（2行3列）
.payment-methods-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  padding: 10px 0;
  
  @media (max-width: 992px) {
    grid-template-columns: repeat(2, 1fr);
  }
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.payment-method-item {
  position: relative;
  border: 2px solid #e5e5e5;
  border-radius: 8px;
  padding: 20px 15px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fff;
  min-height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    border-color: #409eff;
    box-shadow: 0 2px 8px rgba(64, 158, 255, 0.2);
    transform: translateY(-2px);
  }

  &.active {
    border-color: #409eff;
    background: linear-gradient(135deg, #ecf5ff 0%, #d4edff 100%);
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  }

  .payment-radio {
    width: 100%;
    margin: 0;

    :deep(.el-radio__input) {
      position: absolute;
      top: 10px;
      right: 10px;
    }

    :deep(.el-radio__label) {
      width: 100%;
      padding-left: 0;
    }
  }
}

.payment-method-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.payment-icon {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  margin-bottom: 8px;
}

.payment-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  text-align: center;
}

.payment-summary {
  padding: 10px 0;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  color: #606266;
  font-size: 14px;
}

.summary-row.total {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin-top: 10px;
}

.total-price {
  color: #ff4d4f;
  font-size: 20px;
}

.payment-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.confirm-pay-btn {
  flex: 1;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border: 2px solid #ff4d4f;
  box-shadow: 0 2px 4px rgba(255, 77, 79, 0.2);
  
  &:hover {
    border-color: #ff4d4f;
    box-shadow: 0 4px 8px rgba(255, 77, 79, 0.3);
  }
}

.back-btn {
  flex: 0 0 100px;
  height: 48px;
}

// 响应式适配
@media (max-width: 768px) {
  .payment-methods-grid {
    grid-template-columns: 1fr;
  }

  .payment-actions {
    flex-direction: column;
  }

  .back-btn {
    flex: 1;
  }
}
</style>

