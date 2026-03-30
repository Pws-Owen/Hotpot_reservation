<template>
  <div class="customer-report">
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
      
      <el-row :gutter="20" v-loading="loading">
        <el-col :span="6">
          <el-statistic title="新客户" :value="newCustomers" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="老客户" :value="oldCustomers" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="新老客户比例" :value="customerRatio" :precision="2" suffix="%" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="平均消费频次" :value="avgFrequency" :precision="2" />
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card>
            <template #header>新老客户比例</template>
            <div ref="customerRatioChartRef" style="width: 100%; height: 350px;"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>消费频次分析</template>
            <div ref="frequencyChartRef" style="width: 100%; height: 350px;"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-card style="margin-top: 20px;" v-loading="loading">
        <template #header>客户价值分析</template>
        <el-table :data="customerValueData" border>
          <el-table-column prop="level" label="客户等级" width="120" />
          <el-table-column prop="count" label="客户数量" width="120" />
          <el-table-column prop="avgConsumption" label="平均消费" width="120">
            <template #default="{ row }">
              ¥{{ parseFloat(row.avgConsumption || 0).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="totalConsumption" label="总消费" width="120">
            <template #default="{ row }">
              ¥{{ parseFloat(row.totalConsumption || 0).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="percentage" label="占比" width="100">
            <template #default="{ row }">
              {{ parseFloat(row.percentage || 0).toFixed(2) }}%
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getCustomerReport } from '@/api/report'

const dateRange = ref([])
const loading = ref(false)
const newCustomers = ref(0)
const oldCustomers = ref(0)
const customerRatio = ref(0)
const avgFrequency = ref(0)
const customerRatioChartRef = ref(null)
const frequencyChartRef = ref(null)
const reportData = ref(null)

let customerRatioChart = null
let frequencyChart = null

const customerValueData = ref([])

onMounted(() => {
  // 默认查询最近30天
  const endDate = new Date()
  const startDate = new Date()
  startDate.setDate(startDate.getDate() - 29)
  dateRange.value = [startDate, endDate]
  
  loadCustomerReport()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  if (customerRatioChart) {
    customerRatioChart.dispose()
  }
  if (frequencyChart) {
    frequencyChart.dispose()
  }
})

/**
 * 加载客户分析报表数据
 */
const loadCustomerReport = async () => {
  loading.value = true
  try {
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      const startDate = new Date(dateRange.value[0])
      const endDate = new Date(dateRange.value[1])
      params.startDate = formatDate(startDate)
      params.endDate = formatDate(endDate)
    }
    
    const res = await getCustomerReport(params)
    if (res.code === 200 && res.data) {
      reportData.value = res.data
      newCustomers.value = res.data.newCustomers || 0
      oldCustomers.value = res.data.oldCustomers || 0
      customerRatio.value = res.data.customerRatio || 0
      avgFrequency.value = res.data.avgFrequency || 0
      customerValueData.value = res.data.customerValues || []
      updateCharts()
    } else {
      ElMessage.error(res.message || '获取客户分析报表失败')
    }
  } catch (error) {
    console.error('获取客户分析报表失败:', error)
    ElMessage.error('获取客户分析报表失败')
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
  loadCustomerReport()
}

/**
 * 更新图表
 */
const updateCharts = () => {
  if (!reportData.value) return
  
  // 新老客户比例
  updateCustomerRatioChart()
  
  // 消费频次分析
  updateFrequencyChart()
}

/**
 * 更新新老客户比例图表
 */
const updateCustomerRatioChart = () => {
  if (!customerRatioChartRef.value) return
  
  if (!customerRatioChart) {
    customerRatioChart = echarts.init(customerRatioChartRef.value)
  }
  
  customerRatioChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: ['新客户', '老客户']
    },
    series: [{
      name: '客户类型',
      type: 'pie',
      radius: '60%',
      data: [
        { value: newCustomers.value, name: '新客户' },
        { value: oldCustomers.value, name: '老客户' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  })
}

/**
 * 更新消费频次分析图表
 */
const updateFrequencyChart = () => {
  if (!frequencyChartRef.value || !reportData.value) return
  
  if (!frequencyChart) {
    frequencyChart = echarts.init(frequencyChartRef.value)
  }
  
  const frequencyDistributions = reportData.value.frequencyDistributions || []
  const ranges = frequencyDistributions.map(item => item.range)
  const counts = frequencyDistributions.map(item => item.customerCount)
  
  frequencyChart.setOption({
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    xAxis: {
      type: 'category',
      data: ranges
    },
    yAxis: {
      type: 'value',
      name: '客户数'
    },
    series: [{
      name: '客户数',
      type: 'bar',
      data: counts,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
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
 * 调整图表大小
 */
const resizeCharts = () => {
  if (customerRatioChart) {
    customerRatioChart.resize()
  }
  if (frequencyChart) {
    frequencyChart.resize()
  }
}
</script>

<style scoped>
.customer-report {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

