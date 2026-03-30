<template>
  <div class="reservation-statistics">
    <div class="page-header">
      <h2>预约统计</h2>
    </div>
    
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #409EFF;">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalReservations }}</div>
              <div class="stat-label">总预约数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #67C23A;">
              <el-icon><Check /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.confirmedReservations }}</div>
              <div class="stat-label">已确认</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #E6A23C;">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pendingReservations }}</div>
              <div class="stat-label">待确认</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #F56C6C;">
              <el-icon><Close /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.cancelledReservations }}</div>
              <div class="stat-label">已取消</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>预约趋势</span>
          </template>
          <div ref="reservationTrendChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>预约状态分布</span>
          </template>
          <div ref="reservationStatusChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { Calendar, Check, Clock, Close } from '@element-plus/icons-vue'

const stats = ref({
  totalReservations: 0,
  confirmedReservations: 0,
  pendingReservations: 0,
  cancelledReservations: 0
})

const reservationTrendChartRef = ref(null)
const reservationStatusChartRef = ref(null)

onMounted(() => {
  // TODO: 从API获取统计数据
  stats.value = {
    totalReservations: 850,
    confirmedReservations: 650,
    pendingReservations: 120,
    cancelledReservations: 80
  }
  
  initCharts()
})

const initCharts = () => {
  // 预约趋势图
  if (reservationTrendChartRef.value) {
    const chart = echarts.init(reservationTrendChartRef.value)
    chart.setOption({
      title: {
        text: '近30天预约趋势'
      },
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: Array.from({ length: 30 }, (_, i) => `第${i + 1}天`)
      },
      yAxis: {
        type: 'value'
      },
      series: [{
        data: Array.from({ length: 30 }, () => Math.floor(Math.random() * 30) + 10),
        type: 'line',
        smooth: true
      }]
    })
  }
  
  // 预约状态分布图
  if (reservationStatusChartRef.value) {
    const chart = echarts.init(reservationStatusChartRef.value)
    chart.setOption({
      tooltip: {
        trigger: 'item'
      },
      series: [{
        type: 'pie',
        radius: '60%',
        data: [
          { value: 650, name: '已确认' },
          { value: 120, name: '待确认' },
          { value: 80, name: '已取消' }
        ]
      }]
    })
  }
}
</script>

<style scoped>
.reservation-statistics {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.chart-row {
  margin-top: 20px;
}
</style>
