<template>
  <div class="reservation-detail">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>预约详情</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="预约ID">{{ reservation.id }}</el-descriptions-item>
        <el-descriptions-item label="客户姓名">{{ reservation.customerName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ reservation.phone }}</el-descriptions-item>
        <el-descriptions-item label="预约日期">{{ reservation.date }}</el-descriptions-item>
        <el-descriptions-item label="预约时间">{{ reservation.time }}</el-descriptions-item>
        <el-descriptions-item label="用餐人数">{{ reservation.peopleCount }}</el-descriptions-item>
        <el-descriptions-item label="桌号">{{ reservation.tableNumber }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(reservation.status)">
            {{ getStatusText(reservation.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="特殊要求" :span="2">
          {{ reservation.requirements || '无' }}
        </el-descriptions-item>
      </el-descriptions>
      
      <div class="action-buttons" style="margin-top: 20px;">
        <el-button type="success" @click="handleConfirm" v-if="reservation.status === 'pending'">
          确认预约
        </el-button>
        <el-button type="warning" @click="handleCancel" v-if="reservation.status !== 'cancelled'">
          取消预约
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const reservation = ref({
  id: '',
  customerName: '',
  phone: '',
  date: '',
  time: '',
  peopleCount: 0,
  tableNumber: '',
  status: '',
  requirements: ''
})

onMounted(() => {
  const id = route.params.id
  // TODO: 从API获取预约详情
  // 这里先使用模拟数据
  reservation.value = {
    id: id,
    customerName: '张三',
    phone: '13800138000',
    date: '2024-12-13',
    time: '18:00',
    peopleCount: 4,
    tableNumber: 'A01',
    status: 'pending',
    requirements: '需要靠窗位置'
  }
})

const getStatusType = (status) => {
  const statusMap = {
    pending: 'warning',
    confirmed: 'success',
    completed: 'info',
    cancelled: 'danger'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    pending: '待确认',
    confirmed: '已确认',
    completed: '已完成',
    cancelled: '已取消'
  }
  return statusMap[status] || status
}

const handleConfirm = async () => {
  try {
    await ElMessageBox.confirm('确认要确认此预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // TODO: 调用API确认预约
    ElMessage.success('预约已确认')
    reservation.value.status = 'confirmed'
  } catch {
    // 用户取消
  }
}

const handleCancel = async () => {
  try {
    await ElMessageBox.confirm('确认要取消此预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    // TODO: 调用API取消预约
    ElMessage.success('预约已取消')
    reservation.value.status = 'cancelled'
  } catch {
    // 用户取消
  }
}
</script>

<style scoped>
.reservation-detail {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-buttons {
  text-align: right;
}
</style>

