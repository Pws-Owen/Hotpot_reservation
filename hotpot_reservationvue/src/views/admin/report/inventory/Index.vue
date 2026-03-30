<template>
  <div class="inventory-report">
    <el-card>
      
      <el-alert
        v-if="warningCount > 0"
        title="库存预警"
        type="warning"
        :closable="false"
        style="margin-bottom: 20px;"
      >
        <template #default>
          <span>当前有 <strong>{{ warningCount }}</strong> 个菜品库存不足，请及时补货</span>
        </template>
      </el-alert>

      <el-alert
        v-else
        title="库存充足"
        type="success"
        :closable="false"
        style="margin-bottom: 20px;"
      >
        <template #default>
          <span>当前所有菜品库存充足</span>
        </template>
      </el-alert>

      <el-row :gutter="20" v-loading="loading">
        <el-col :span="12">
          <el-card>
            <template #header>库存预警列表</template>
            <el-table :data="warningData" border height="400" v-if="warningData.length > 0">
              <el-table-column prop="dishName" label="菜品名称" />
              <el-table-column prop="currentStock" label="当前库存" width="100" />
              <el-table-column prop="minStock" label="最低库存" width="100" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === '紧急' ? 'danger' : 'warning'">
                    {{ row.status }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
            <el-empty v-else description="暂无库存预警" />
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>菜品消耗统计（最近7天）</template>
            <div ref="consumptionChartRef" style="width: 100%; height: 400px;"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-card style="margin-top: 20px;" v-loading="loading">
        <template #header>采购建议</template>
        <el-table :data="purchaseData" border>
          <el-table-column prop="dishName" label="菜品名称" />
          <el-table-column prop="currentStock" label="当前库存" width="100" />
          <el-table-column prop="avgDailyConsumption" label="日均消耗" width="120">
            <template #default="{ row }">
              {{ Math.ceil(row.avgDailyConsumption || 0) }}
            </template>
          </el-table-column>
          <el-table-column prop="suggestedPurchase" label="建议采购量" width="120" />
          <el-table-column prop="estimatedDays" label="预计可用天数" width="120">
            <template #default="{ row }">
              <el-tag :type="row.estimatedDays < 3 ? 'danger' : (row.estimatedDays < 7 ? 'warning' : 'success')">
                {{ row.estimatedDays }} 天
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getInventoryReport } from '@/api/report'

const loading = ref(false)
const warningCount = ref(0)
const consumptionChartRef = ref(null)
const reportData = ref(null)

let consumptionChart = null

const warningData = ref([])
const purchaseData = ref([])

onMounted(() => {
  loadInventoryReport()
  window.addEventListener('resize', resizeChart)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeChart)
  if (consumptionChart) {
    consumptionChart.dispose()
  }
})

/**
 * 加载库存报表数据
 */
const loadInventoryReport = async () => {
  loading.value = true
  try {
    const res = await getInventoryReport()
    if (res.code === 200 && res.data) {
      reportData.value = res.data
      warningCount.value = res.data.warningCount || 0
      warningData.value = res.data.warnings || []
      purchaseData.value = res.data.purchaseSuggestions || []
      updateChart()
    } else {
      ElMessage.error(res.message || '获取库存报表失败')
      warningData.value = []
      purchaseData.value = []
    }
  } catch (error) {
    console.error('获取库存报表失败:', error)
    ElMessage.error('获取库存报表失败')
    warningData.value = []
    purchaseData.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 更新图表
 */
const updateChart = () => {
  if (!reportData.value) return
  
  // 菜品消耗统计
  updateConsumptionChart()
}

/**
 * 更新菜品消耗统计图表
 */
const updateConsumptionChart = () => {
  if (!consumptionChartRef.value || !reportData.value) return
  
  if (!consumptionChart) {
    consumptionChart = echarts.init(consumptionChartRef.value)
  }
  
  const dailyConsumptions = reportData.value.dailyConsumptions || []
  const dates = dailyConsumptions.map(item => {
    const date = new Date(item.date)
    return `${date.getMonth() + 1}/${date.getDate()}`
  })
  const consumptions = dailyConsumptions.map(item => item.consumption || 0)
  
  consumptionChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const param = params[0]
        return `${param.name}<br/>消耗量: ${param.value}`
      }
    },
    xAxis: {
      type: 'category',
      data: dates,
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      name: '消耗量'
    },
    series: [{
      name: '消耗量',
      type: 'line',
      smooth: true,
      data: consumptions,
      areaStyle: {
        opacity: 0.3,
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
          offset: 0,
          color: 'rgba(64, 158, 255, 0.8)'
        }, {
          offset: 1,
          color: 'rgba(64, 158, 255, 0)'
        }])
      },
      itemStyle: {
        color: '#409EFF'
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
const resizeChart = () => {
  if (consumptionChart) {
    consumptionChart.resize()
  }
}

</script>

<style scoped>
.inventory-report {
  padding: 0;
}
</style>

