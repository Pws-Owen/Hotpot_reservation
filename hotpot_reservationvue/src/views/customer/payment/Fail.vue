<template>
  <div class="payment-fail-container">
    <el-result
      icon="error"
      title="支付失败"
      sub-title="支付过程中出现问题，请重试或选择其他支付方式"
    >
      <template #extra>
        <el-button type="primary" @click="retryPayment">重新支付</el-button>
        <el-button @click="goToOrderList">返回订单列表</el-button>
      </template>
    </el-result>
    
    <el-card style="margin-top: 20px;" v-if="orderInfo">
      <template #header>
        <span>订单信息</span>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">
          {{ orderInfo.orderNo }}
        </el-descriptions-item>
        <el-descriptions-item label="订单金额">
          <span class="amount">¥{{ Number(orderInfo.actualAmount || 0).toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag type="warning">待支付</el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getOrderById } from '@/api/order'

const router = useRouter()
const route = useRoute()
const orderInfo = ref(null)

onMounted(async () => {
  const orderId = route.query.orderId
  if (orderId) {
    await loadOrderInfo(orderId)
  }
})

const loadOrderInfo = async (orderId) => {
  try {
    const res = await getOrderById(orderId)
    if (res.code === 200 && res.data) {
      orderInfo.value = res.data
    }
  } catch (error) {
    console.error('加载订单信息失败:', error)
  }
}

const retryPayment = () => {
  const orderId = route.query.orderId
  if (orderId) {
    router.push({
      path: '/customer/payment',
      query: { orderId }
    })
  } else {
    router.push('/customer/order/list')
  }
}

const goToOrderList = () => {
  router.push('/customer/order/list')
}
</script>

<style scoped>
.payment-fail-container {
  padding: 40px 20px;
  max-width: 800px;
  margin: 0 auto;
}

.amount {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}
</style>

