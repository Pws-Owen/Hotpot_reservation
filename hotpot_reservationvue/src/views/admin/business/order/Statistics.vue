<template>
  <div class="order-statistics">
    <div class="page-header">
      <h2>订单统计</h2>
    </div>
    
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #409EFF;">
              <el-icon><ShoppingBag /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalOrders }}</div>
              <div class="stat-label">总订单数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #67C23A;">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ stats.totalRevenue }}</div>
              <div class="stat-label">总营收</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #E6A23C;">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ stats.avgOrderAmount }}</div>
              <div class="stat-label">平均订单金额</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #F56C6C;">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.todayOrders }}</div>
              <div class="stat-label">今日订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>订单趋势</span>
          </template>
          <div ref="orderTrendChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>订单状态分布</span>
          </template>
          <div ref="orderStatusChartRef" style="width: 100%; height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { ShoppingBag, Money, TrendCharts, Clock } from '@element-plus/icons-vue'

const stats = ref({
  totalOrders: 0,
  totalRevenue: 0,
  avgOrderAmount: 0,
  todayOrders: 0
})

const orderTrendChartRef = ref(null)
const orderStatusChartRef = ref(null)

onMounted(() => {
  // TODO: 从API获取统计数据
  stats.value = {
    totalOrders: 1250,
    totalRevenue: 125680,
    avgOrderAmount: 100.5,
    todayOrders: 45
  }
  
  initCharts()
})

const initCharts = () => {
  // 订单趋势图
  if (orderTrendChartRef.value) {
    const chart = echarts.init(orderTrendChartRef.value)
    chart.setOption({
      title: {
        text: '近30天订单趋势'
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
        data: Array.from({ length: 30 }, () => Math.floor(Math.random() * 50) + 20),
        type: 'line',
        smooth: true
      }]
    })
  }
  
  // 订单状态分布图
  if (orderStatusChartRef.value) {
    const chart = echarts.init(orderStatusChartRef.value)
    chart.setOption({
      tooltip: {
        trigger: 'item'
      },
      series: [{
        type: 'pie',
        radius: '60%',
        data: [
          { value: 800, name: '已完成' },
          { value: 300, name: '进行中' },
          { value: 100, name: '待支付' },
          { value: 50, name: '已取消' }
        ]
      }]
    })
  }
}
</script>

<style scoped>
.order-statistics {
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
