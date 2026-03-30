<template>
  <div class="reservation-calendar">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>预约日历视图</span>
          <div>
            <el-button @click="handlePrevMonth">上个月</el-button>
            <el-date-picker
              v-model="currentDate"
              type="month"
              format="YYYY年MM月"
              @change="handleDateChange"
            />
            <el-button @click="handleNextMonth">下个月</el-button>
          </div>
        </div>
      </template>
      
      <el-calendar v-model="currentDate">
        <template #date-cell="{ data }">
          <div class="calendar-cell">
            <div class="date-number">{{ data.day.split('-').slice(2).join('-') }}</div>
            <div class="reservation-count" v-if="getReservationCount(data.day) > 0">
              <el-badge :value="getReservationCount(data.day)" class="item">
                <el-tag size="small" type="info">预约</el-tag>
              </el-badge>
            </div>
          </div>
        </template>
      </el-calendar>

      <el-card style="margin-top: 20px;">
        <template #header>今日预约详情</template>
        <el-table :data="todayReservations" border>
          <el-table-column prop="time" label="时间" width="120" />
          <el-table-column prop="customerName" label="客户姓名" width="120" />
          <el-table-column prop="tableNumber" label="桌号" width="100" />
          <el-table-column prop="peopleCount" label="人数" width="80" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
              <el-button type="success" size="small" @click="handleConfirm(row)" v-if="row.status === '待确认'">确认</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const currentDate = ref(new Date())

const reservationData = ref({
  '2024-01-15': 3,
  '2024-01-16': 5,
  '2024-01-17': 2,
  '2024-01-18': 4
})

const todayReservations = ref([
  { time: '11:00', customerName: '张三', tableNumber: 'A01', peopleCount: 4, status: '已确认' },
  { time: '12:30', customerName: '李四', tableNumber: 'B05', peopleCount: 2, status: '待确认' },
  { time: '18:00', customerName: '王五', tableNumber: 'C10', peopleCount: 6, status: '已确认' }
])

onMounted(() => {
  loadReservationData()
})

const loadReservationData = () => {
  // 从API加载预约数据
}

const getReservationCount = (date) => {
  return reservationData.value[date] || 0
}

const handleDateChange = () => {
  loadReservationData()
}

const handlePrevMonth = () => {
  const date = new Date(currentDate.value)
  date.setMonth(date.getMonth() - 1)
  currentDate.value = date
  loadReservationData()
}

const handleNextMonth = () => {
  const date = new Date(currentDate.value)
  date.setMonth(date.getMonth() + 1)
  currentDate.value = date
  loadReservationData()
}

const getStatusType = (status) => {
  const statusMap = {
    '待确认': 'warning',
    '已确认': 'success',
    '进行中': 'info',
    '已完成': '',
    '已取消': 'danger'
  }
  return statusMap[status] || ''
}

const handleView = (row) => {
  ElMessage.info(`查看预约详情: ${row.customerName}`)
}

const handleConfirm = (row) => {
  ElMessage.success(`确认预约: ${row.customerName}`)
}
</script>

<style scoped>
.reservation-calendar {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.calendar-cell {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.date-number {
  font-size: 14px;
  margin-bottom: 5px;
}

.reservation-count {
  margin-top: 5px;
}
</style>

