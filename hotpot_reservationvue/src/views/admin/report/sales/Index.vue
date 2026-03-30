<template>
  <div class="sales-report">
    <el-card>
      <template #header>
        <div class="card-header">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="handleDateChange"
          />
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="24">
          <el-card>
            <template #header>每日销售统计</template>
            <div ref="dailySalesChartRef" style="width: 100%; height: 400px;"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card>
            <template #header>菜品销售排行</template>
            <div ref="dishRankingChartRef" style="width: 100%; height: 350px;"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>时段分析</template>
            <div ref="timeAnalysisChartRef" style="width: 100%; height: 350px;"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-card style="margin-top: 20px;" v-loading="loading">
        <template #header>销售明细</template>
        <el-table :data="salesData" border>
          <el-table-column prop="date" label="日期" width="120" />
          <el-table-column prop="totalAmount" label="总金额" width="120">
            <template #default="{ row }">
              ¥{{ parseFloat(row.totalAmount || 0).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="orderCount" label="订单数" width="100" />
          <el-table-column prop="avgAmount" label="平均金额" width="120">
            <template #default="{ row }">
              ¥{{ parseFloat(row.avgAmount || 0).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="topDish" label="热销菜品" />
        </el-table>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getSalesReport } from '@/api/report'

const dateRange = ref([])
const dailySalesChartRef = ref(null)
const dishRankingChartRef = ref(null)
const timeAnalysisChartRef = ref(null)
const loading = ref(false)

let dailySalesChart = null
let dishRankingChart = null
let timeAnalysisChart = null

const salesData = ref([])
const reportData = ref(null)

onMounted(() => {
  // 默认查询最近30天
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 29)
  dateRange.value = [startDate, endDate]
  
  loadSalesReport()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  if (dailySalesChart) {
    dailySalesChart.dispose()
  }
  if (dishRankingChart) {
    dishRankingChart.dispose()
  }
  if (timeAnalysisChart) {
    timeAnalysisChart.dispose()
  }
})

/**
 * 加载销售报表数据
 */
const loadSalesReport = async () => {
  loading.value = true
  try {
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      const startDate = new Date(dateRange.value[0])
      const endDate = new Date(dateRange.value[1])
      params.startDate = formatDate(startDate)
      params.endDate = formatDate(endDate)
    }
    
    const res = await getSalesReport(params)
    if (res.code === 200 && res.data) {
      reportData.value = res.data
      salesData.value = res.data.salesDetails || []
      updateCharts()
    } else {
      ElMessage.error(res.message || '获取销售报表失败')
      salesData.value = []
    }
  } catch (error) {
    console.error('获取销售报表失败:', error)
    ElMessage.error('获取销售报表失败')
    salesData.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 格式化日期
 */
const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * 日期范围改变
 */
const handleDateChange = () => {
  loadSalesReport()
}

/**
 * 更新图表
 */
const updateCharts = () => {
  if (!reportData.value) return
  
  // 每日销售统计
  updateDailySalesChart()
  
  // 菜品销售排行
  updateDishRankingChart()
  
  // 时段分析
  updateTimeAnalysisChart()
}

/**
 * 更新每日销售统计图表
 */
const updateDailySalesChart = () => {
  if (!dailySalesChartRef.value || !reportData.value) return
  
  if (!dailySalesChart) {
    dailySalesChart = echarts.init(dailySalesChartRef.value)
  }
  
  const dailyStats = reportData.value.dailySalesStats || []
  const dates = dailyStats.map(item => {
    const date = new Date(item.date)
    return `${date.getMonth() + 1}/${date.getDate()}`
  })
  const amounts = dailyStats.map(item => parseFloat(item.totalAmount || 0))
  
  dailySalesChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const param = params[0]
        const index = param.dataIndex
        const stat = dailyStats[index]
        return `${param.name}<br/>销售额: ¥${parseFloat(stat.totalAmount || 0).toFixed(2)}<br/>订单数: ${stat.orderCount}`
      }
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '销售额(元)',
      axisLabel: {
        formatter: (value) => {
          if (value >= 10000) {
            return (value / 10000).toFixed(1) + '万'
          }
          return value
        }
      }
    },
    series: [{
      name: '销售额',
      type: 'bar',
      data: amounts,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
          offset: 0,
          color: 'rgba(64, 158, 255, 0.8)'
        }, {
          offset: 1,
          color: 'rgba(64, 158, 255, 0.2)'
        }])
      }
    }]
  })
}

/**
 * 更新菜品销售排行图表
 */
const updateDishRankingChart = () => {
  if (!dishRankingChartRef.value || !reportData.value) return
  
  if (!dishRankingChart) {
    dishRankingChart = echarts.init(dishRankingChartRef.value)
  }
  
  const dishRanks = reportData.value.dishSalesRanks || []
  const dishNames = dishRanks.map(item => item.dishName).reverse()
  const salesCounts = dishRanks.map(item => item.salesCount).reverse()
  
  dishRankingChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params) => {
        const param = params[0]
        const index = param.dataIndex
        const rank = dishRanks[dishRanks.length - 1 - index]
        return `${param.name}<br/>销售数量: ${rank.salesCount}<br/>销售金额: ¥${parseFloat(rank.salesAmount || 0).toFixed(2)}`
      }
    },
    grid: {
      left: '20%',
      right: '10%'
    },
    xAxis: {
      type: 'value',
      name: '销售数量'
    },
    yAxis: {
      type: 'category',
      data: dishNames,
      axisLabel: {
        interval: 0
      }
    },
    series: [{
      type: 'bar',
      data: salesCounts,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [{
          offset: 0,
          color: 'rgba(103, 194, 58, 0.8)'
        }, {
          offset: 1,
          color: 'rgba(103, 194, 58, 0.2)'
        }])
      }
    }]
  })
}

/**
 * 更新时段分析图表
 */
const updateTimeAnalysisChart = () => {
  if (!timeAnalysisChartRef.value || !reportData.value) return
  
  if (!timeAnalysisChart) {
    timeAnalysisChart = echarts.init(timeAnalysisChartRef.value)
  }
  
  const timeAnalyses = reportData.value.timeAnalyses || []
  const timeSlots = timeAnalyses.map(item => item.timeSlot)
  const orderCounts = timeAnalyses.map(item => item.orderCount)
  
  timeAnalysisChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const param = params[0]
        return `${param.name}<br/>订单数: ${param.value}`
      }
    },
    xAxis: {
      type: 'category',
      data: timeSlots,
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      name: '订单数'
    },
    series: [{
      name: '订单数',
      type: 'line',
      smooth: true,
      data: orderCounts,
      areaStyle: {
        opacity: 0.3,
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
          offset: 0,
          color: 'rgba(230, 162, 60, 0.8)'
        }, {
          offset: 1,
          color: 'rgba(230, 162, 60, 0)'
        }])
      },
      itemStyle: {
        color: '#E6A23C'
      },
      lineStyle: {
        width: 3
      }
    }]
  })
}

/**
 * 调整图表大小
 */
const resizeCharts = () => {
  if (dailySalesChart) {
    dailySalesChart.resize()
  }
  if (dishRankingChart) {
    dishRankingChart.resize()
  }
  if (timeAnalysisChart) {
    timeAnalysisChart.resize()
  }
}
</script>

<style scoped>
.sales-report {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

