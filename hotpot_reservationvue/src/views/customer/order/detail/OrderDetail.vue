<template>
  <div class="order-detail" v-loading="loading">
    <el-card v-if="order">
      <template #header>
        <div class="card-header">
          <span>订单详情</span>
          <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">
          <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="订单类型">
          {{ order.orderType === 1 ? '堂食' : order.orderType === 2 ? '外卖' : '未知' }}
        </el-descriptions-item>
        <el-descriptions-item label="订单总价">¥{{ Number(order.totalPrice || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="优惠金额" v-if="order.discountAmount > 0">
          -¥{{ Number(order.discountAmount).toFixed(2) }}
        </el-descriptions-item>
        <el-descriptions-item label="实付金额">
          <span class="actual-amount">¥{{ Number(order.actualAmount || 0).toFixed(2) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ order.createTime }}</el-descriptions-item>
        <el-descriptions-item label="支付时间" v-if="order.payTime">{{ order.payTime }}</el-descriptions-item>
        <el-descriptions-item label="完成时间" v-if="order.finishTime">{{ order.finishTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2" v-if="order.remark">
          {{ order.remark }}
        </el-descriptions-item>
      </el-descriptions>
      
      <el-divider />
      
      <h3>订单明细</h3>
      <el-table :data="order.items" style="width: 100%" border>
        <el-table-column prop="itemName" label="菜品名称" min-width="200" show-overflow-tooltip />
        <el-table-column prop="quantity" label="数量" width="100" align="center" />
        <el-table-column prop="price" label="单价" width="120" align="right">
          <template #default="scope">
            ¥{{ Number(scope.row.price).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="subtotal" label="小计" width="120" align="right">
          <template #default="scope">
            <span class="subtotal">¥{{ Number(scope.row.subtotal).toFixed(2) }}</span>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="order-summary">
        <div class="summary-row">
          <span class="summary-label">商品总数：</span>
          <span class="summary-value">{{ order.items.reduce((sum, item) => sum + item.quantity, 0) }} 件</span>
        </div>
        <div class="summary-row">
          <span class="summary-label">订单总价：</span>
          <span class="summary-value">¥{{ Number(order.totalPrice || 0).toFixed(2) }}</span>
        </div>
        <div class="summary-row" v-if="order.discountAmount > 0">
          <span class="summary-label">优惠金额：</span>
          <span class="summary-value discount">-¥{{ Number(order.discountAmount).toFixed(2) }}</span>
        </div>
        <div class="summary-row total">
          <span class="summary-label">实付金额：</span>
          <span class="summary-value total-amount">¥{{ Number(order.actualAmount || 0).toFixed(2) }}</span>
        </div>
      </div>
      
      <div class="order-actions" v-if="order.status === 0">
        <el-button type="primary" @click="handlePay">立即支付</el-button>
        <el-button @click="handleCancel">取消订单</el-button>
      </div>
    </el-card>
    <el-empty v-else-if="!loading" description="订单不存在" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetails, cancelOrder } from '@/api/order'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const order = ref(null)
const loading = ref(false)

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/customer/login')
    return
  }
  
  const id = route.params.id
  if (id) {
    await loadOrderDetail(id)
  } else {
    ElMessage.error('订单ID不存在')
    router.push('/customer/order/list')
  }
})

const loadOrderDetail = async (id) => {
  loading.value = true
  try {
    const res = await getOrderDetails(id)
    if (res.code === 200 && res.data) {
      const data = res.data
      order.value = {
        orderId: data.orderId,
        orderNo: data.orderNo,
        status: data.status, // 整数状态
        totalPrice: data.totalPrice,
        discountAmount: data.discountAmount || 0,
        actualAmount: data.actualAmount,
        orderType: data.orderType,
        remark: data.remark || '',
        createTime: data.createTime,
        payTime: data.payTime,
        finishTime: data.finishTime,
        items: (data.details || []).map(detail => ({
          itemName: detail.itemName || '',
          quantity: detail.quantity || 0,
          price: Number(detail.price) || 0,
          subtotal: (Number(detail.quantity) || 0) * (Number(detail.price) || 0)
        }))
      }
    } else {
      ElMessage.error(res.message || '加载订单详情失败')
      router.push('/customer/order/list')
    }
  } catch (error) {
    console.error('加载订单详情失败:', error)
    ElMessage.error('加载订单详情失败')
    router.push('/customer/order/list')
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  // 状态：0-待支付，1-已支付，2-制作中，3-已完成，4-已取消
  const statusMap = {
    0: 'warning',
    1: 'success',
    2: 'info',
    3: 'success',
    4: 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    0: '待支付',
    1: '已支付',
    2: '制作中',
    3: '已完成',
    4: '已取消'
  }
  return statusMap[status] || '未知'
}

const handlePay = () => {
  if (order.value && order.value.orderId) {
    router.push({
      path: '/customer/payment',
      query: { orderId: order.value.orderId }
    })
  }
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    if (order.value && order.value.orderId) {
      const res = await cancelOrder(order.value.orderId)
      if (res.code === 200) {
        ElMessage.success('订单已取消')
        await loadOrderDetail(order.value.orderId)
      } else {
        ElMessage.error(res.message || '取消订单失败')
      }
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
      ElMessage.error('取消订单失败')
    }
  }
}
</script>

<style scoped lang="scss">
.order-detail {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  min-height: calc(100vh - 100px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.actual-amount {
  font-size: 18px;
  font-weight: bold;
  color: #ff4d4f;
}

.order-summary {
  margin-top: 20px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  text-align: right;
}

.summary-row {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;

  &:last-child {
    margin-bottom: 0;
  }

  &.total {
    margin-top: 10px;
    padding-top: 10px;
    border-top: 1px solid #e4e7ed;
  }
}

.summary-label {
  color: #606266;
  margin-right: 20px;
}

.summary-value {
  color: #303133;
  font-weight: 500;
  min-width: 120px;
  text-align: right;

  &.discount {
    color: #67c23a;
  }

  &.total-amount {
    font-size: 20px;
    font-weight: bold;
    color: #ff4d4f;
  }
}

.subtotal {
  font-weight: 500;
  color: #303133;
}

.order-actions {
  margin-top: 30px;
  text-align: right;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}
</style>

