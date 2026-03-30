<template>
  <div class="order-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单详情</span>
        </div>
      </template>
      <div v-if="order">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="客户姓名">{{ order.customerName }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ order.phone }}</el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ order.amount }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ order.createTime }}</el-descriptions-item>
        </el-descriptions>
        
        <el-divider />
        
        <h3>订单明细</h3>
        <el-table :data="order.items" style="width: 100%">
          <el-table-column prop="itemName" label="菜品名称" />
          <el-table-column prop="quantity" label="数量" width="100" />
          <el-table-column prop="price" label="单价" width="100" />
          <el-table-column prop="subtotal" label="小计" width="100" />
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const order = ref(null)

onMounted(() => {
  // TODO: 根据ID从API获取订单详情
  const id = route.params.id
  if (id) {
    // 模拟数据
    order.value = {
      id: id,
      orderNo: 'ORD20241213001',
      customerName: '张三',
      phone: '13800138000',
      amount: 268.00,
      status: 'completed',
      createTime: '2024-12-13 12:00:00',
      items: [
        { itemName: '麻辣火锅', quantity: 1, price: 128, subtotal: 128 },
        { itemName: '肥牛', quantity: 2, price: 48, subtotal: 96 },
        { itemName: '金针菇', quantity: 1, price: 18, subtotal: 18 },
        { itemName: '饮料', quantity: 2, price: 13, subtotal: 26 }
      ]
    }
  }
})

const getStatusType = (status) => {
  const statusMap = {
    pending: 'warning',
    paid: 'success',
    completed: 'success',
    cancelled: 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    pending: '待支付',
    paid: '已支付',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || '未知'
}
</script>

<style scoped>
.order-detail {
  padding: 0;
}
</style>

