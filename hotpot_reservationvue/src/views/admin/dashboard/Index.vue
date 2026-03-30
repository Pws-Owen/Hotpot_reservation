<template>
  <div class="dashboard" v-loading="loading">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #409EFF;">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.todayReservations }}</div>
              <div class="stat-label">今日预约</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #67C23A;">
              <el-icon><ShoppingBag /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.todayOrders }}</div>
              <div class="stat-label">今日订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #E6A23C;">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ formatMoney(stats.todayRevenue) }}</div>
              <div class="stat-label">今日营收</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" style="background-color: #F56C6C;">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalUsers }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="chart-header">
              <span>预约趋势</span>
              <el-radio-group v-model="reservationDays" @change="handleDaysChange" size="small">
                <el-radio-button :label="7">7天</el-radio-button>
                <el-radio-button :label="30">30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="reservationChartRef" style="width: 100%; height: 600px;"></div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <span>订单统计</span>
          </template>
          <div ref="orderChartRef" style="width: 100%; height: 600px;"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Calendar, ShoppingBag, Money, User } from '@element-plus/icons-vue'
import { getDashboardStats } from '@/api/admin/dashboard'
import { ElMessage } from 'element-plus'

const stats = ref({
  todayReservations: 0,
  todayOrders: 0,
  todayRevenue: 0,
  totalUsers: 0
})

const reservationChartRef = ref(null)
const orderChartRef = ref(null)
const reservationChart = ref(null)
const orderChart = ref(null)
const loading = ref(false)
const reservationDays = ref(7) // 默认7天

/**
 * 加载统计数据
 */
const loadDashboardStats = async () => {
  try {
    loading.value = true
    const response = await getDashboardStats(reservationDays.value)
    if (response.code === 200 && response.data) {
      const data = response.data
      
      // 更新统计数据
      stats.value = {
        todayReservations: data.todayReservations || 0,
        todayOrders: data.todayOrders || 0,
        todayRevenue: data.todayRevenue ? parseFloat(data.todayRevenue.toString()) : 0,
        totalUsers: data.totalUsers || 0
      }
      
      // 更新图表数据
      await nextTick()
      updateCharts(data)
    } else {
      ElMessage.error(response.message || '获取统计数据失败')
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 处理天数变更
 */
const handleDaysChange = () => {
  loadDashboardStats()
}

/**
 * 更新图表数据
 */
const updateCharts = (data) => {
  // 更新预约趋势图
  if (reservationChartRef.value && data.reservationTrend) {
    if (!reservationChart.value) {
      reservationChart.value = echarts.init(reservationChartRef.value)
    }
    
    const dates = data.reservationTrend.map(item => item.date)
    const counts = data.reservationTrend.map(item => item.count)
    
    reservationChart.value.setOption({
      title: {
        text: `近${reservationDays.value}天预约趋势`,
        left: 'center',
        textStyle: {
          fontSize: 20,
          fontWeight: 'bold'
        }
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'cross'
        },
        textStyle: {
          fontSize: 14
        }
      },
      grid: {
        left: '5%',
        right: '5%',
        bottom: '5%',
        top: '15%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: dates,
        axisLabel: {
          fontSize: 14
        }
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          fontSize: 14
        }
      },
      series: [{
        name: '预约数量',
        data: counts,
        type: 'line',
        smooth: true,
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [{
              offset: 0, color: 'rgba(64, 158, 255, 0.3)'
            }, {
              offset: 1, color: 'rgba(64, 158, 255, 0.1)'
            }]
          }
        },
        itemStyle: {
          color: '#409EFF'
        },
        lineStyle: {
          color: '#409EFF',
          width: 3
        },
        symbolSize: 8
      }]
    })
  }

  // 更新订单状态分布图
  if (orderChartRef.value && data.orderStatusDistribution) {
    if (!orderChart.value) {
      orderChart.value = echarts.init(orderChartRef.value)
    }
    
    const pieData = data.orderStatusDistribution.map(item => ({
      value: item.value,
      name: item.name
    }))
    
    orderChart.value.setOption({
      title: {
        text: '订单状态分布',
        left: 'center',
        textStyle: {
          fontSize: 20,
          fontWeight: 'bold'
        }
      },
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)',
        textStyle: {
          fontSize: 14
        }
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        top: 'middle',
        textStyle: {
          fontSize: 14
        }
      },
      series: [{
        name: '订单状态',
        type: 'pie',
        radius: ['50%', '80%'],
        center: ['60%', '55%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 3
        },
        label: {
          show: true,
          formatter: '{b}: {c} ({d}%)',
          fontSize: 14
        },
        labelLine: {
          length: 20,
          length2: 30
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 18,
            fontWeight: 'bold'
          },
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        data: pieData
      }]
    })
  }
}

/**
 * 初始化图表
 */
const initCharts = () => {
  // 等待DOM渲染完成后再初始化图表
  nextTick(() => {
    if (reservationChartRef.value && !reservationChart.value) {
      reservationChart.value = echarts.init(reservationChartRef.value)
    }
    if (orderChartRef.value && !orderChart.value) {
      orderChart.value = echarts.init(orderChartRef.value)
    }
  })
}

/**
 * 格式化金额显示
 */
const formatMoney = (amount) => {
  if (!amount && amount !== 0) return '0.00'
  return parseFloat(amount).toFixed(2)
}

onMounted(() => {
  // 初始化图表容器
  initCharts()
  // 加载统计数据
  loadDashboardStats()
  
  // 监听窗口大小变化，自动调整图表大小
  window.addEventListener('resize', () => {
    if (reservationChart.value) {
      reservationChart.value.resize()
    }
    if (orderChart.value) {
      orderChart.value.resize()
    }
  })
})
</script>

<style scoped>
.dashboard {
  padding: 0;
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

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

