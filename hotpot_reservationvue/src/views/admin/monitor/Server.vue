<template>
  <div class="server-monitor">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>服务器监控</span>
          <el-button @click="handleRefresh">刷新</el-button>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <template #header>CPU使用率</template>
            <div ref="cpuChartRef" style="width: 100%; height: 300px;"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>内存使用率</template>
            <div ref="memoryChartRef" style="width: 100%; height: 300px;"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px;">
        <el-col :span="12">
          <el-card>
            <template #header>磁盘使用率</template>
            <div ref="diskChartRef" style="width: 100%; height: 300px;"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>网络流量</template>
            <div ref="networkChartRef" style="width: 100%; height: 300px;"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-card style="margin-top: 20px;">
        <template #header>服务器信息</template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="服务器名称">HotPot-Server-01</el-descriptions-item>
          <el-descriptions-item label="操作系统">Linux Ubuntu 20.04</el-descriptions-item>
          <el-descriptions-item label="CPU核心数">8核</el-descriptions-item>
          <el-descriptions-item label="CPU使用率">45.2%</el-descriptions-item>
          <el-descriptions-item label="总内存">16GB</el-descriptions-item>
          <el-descriptions-item label="已用内存">8.5GB (53.1%)</el-descriptions-item>
          <el-descriptions-item label="总磁盘">500GB</el-descriptions-item>
          <el-descriptions-item label="已用磁盘">256GB (51.2%)</el-descriptions-item>
          <el-descriptions-item label="运行时间">15天 8小时 30分钟</el-descriptions-item>
          <el-descriptions-item label="最后更新">2024-01-15 14:30:25</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'

const cpuChartRef = ref(null)
const memoryChartRef = ref(null)
const diskChartRef = ref(null)
const networkChartRef = ref(null)
let refreshTimer = null

onMounted(() => {
  initCharts()
  // 定时刷新数据
  refreshTimer = setInterval(() => {
    updateCharts()
  }, 5000) // 每5秒刷新一次
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})

const handleRefresh = () => {
  updateCharts()
  ElMessage.success('刷新成功')
}

const initCharts = () => {
  // CPU使用率
  if (cpuChartRef.value) {
    const chart = echarts.init(cpuChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00']
      },
      yAxis: {
        type: 'value',
        max: 100,
        axisLabel: { formatter: '{value}%' }
      },
      series: [{
        name: 'CPU使用率',
        type: 'line',
        smooth: true,
        data: [25, 30, 35, 45, 50, 40, 35],
        areaStyle: {}
      }]
    })
  }

  // 内存使用率
  if (memoryChartRef.value) {
    const chart = echarts.init(memoryChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'gauge',
        data: [{ value: 53.1, name: '内存使用率' }],
        min: 0,
        max: 100,
        axisLabel: { formatter: '{value}%' }
      }]
    })
  }

  // 磁盘使用率
  if (diskChartRef.value) {
    const chart = echarts.init(diskChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        data: [
          { value: 256, name: '已用' },
          { value: 244, name: '可用' }
        ]
      }]
    })
  }

  // 网络流量
  if (networkChartRef.value) {
    const chart = echarts.init(networkChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00']
      },
      yAxis: { type: 'value' },
      series: [
        {
          name: '上传',
          type: 'bar',
          data: [120, 150, 180, 200, 180, 160, 140]
        },
        {
          name: '下载',
          type: 'bar',
          data: [200, 250, 300, 350, 320, 280, 240]
        }
      ]
    })
  }
}

const updateCharts = () => {
  // 更新图表数据（这里应该从API获取实时数据）
  initCharts()
}
</script>

<style scoped>
.server-monitor {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

