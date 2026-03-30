<template>
  <div class="payment-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span></span>
          <el-button type="primary" @click="handleExport">导出</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单ID">
          <el-input-number v-model="searchForm.orderId" placeholder="请输入订单ID" clearable :min="1" style="width: 150px;" />
        </el-form-item>
        <el-form-item label="支付状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 180px;">
            <el-option label="待支付" :value="0" :key="0" />
            <el-option label="支付成功" :value="1" :key="1" />
            <el-option label="支付失败" :value="2" :key="2" />
            <el-option label="已退款" :value="3" :key="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="6">
          <el-statistic title="总支付金额" :value="totalAmount" prefix="¥" :precision="2" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="总支付笔数" :value="totalCount" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="微信支付" :value="wechatAmount" prefix="¥" :precision="2" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="支付宝" :value="alipayAmount" prefix="¥" :precision="2" />
        </el-col>
      </el-row>

      <el-table 
        :data="paymentList" 
        border
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="paymentId" label="支付ID" width="100" />
        <el-table-column prop="paymentNo" label="支付单号" width="150" show-overflow-tooltip />
        <el-table-column prop="orderId" label="订单ID" width="100" />
        <el-table-column prop="amount" label="支付金额" width="120">
          <template #default="{ row }">
            ¥{{ Number(row.amount || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="120">
          <template #default="{ row }">
            <el-tag>{{ getPaymentMethodText(row.paymentMethod) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="支付状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="transactionId" label="交易号" width="200" show-overflow-tooltip />
        <el-table-column prop="payTime" label="支付时间" width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button 
              type="warning" 
              size="small" 
              @click="handleRefund(row)" 
              v-if="row.status === 1"
            >
              退款
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPaymentPage, getPaymentById, refundPayment } from '@/api/payment'

const searchForm = reactive({
  orderId: null,
  status: undefined
})

const paymentList = ref([])
const selectedRows = ref([])
const loading = ref(false)

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 统计数据
const totalAmount = computed(() => {
  return paymentList.value
    .filter(p => p.status === 1)
    .reduce((sum, p) => sum + Number(p.amount || 0), 0)
})

const totalCount = computed(() => {
  return paymentList.value.filter(p => p.status === 1).length
})

const wechatAmount = computed(() => {
  return paymentList.value
    .filter(p => p.status === 1 && p.paymentMethod === 'WECHAT')
    .reduce((sum, p) => sum + Number(p.amount || 0), 0)
})

const alipayAmount = computed(() => {
  return paymentList.value
    .filter(p => p.status === 1 && p.paymentMethod === 'ALIPAY')
    .reduce((sum, p) => sum + Number(p.amount || 0), 0)
})

onMounted(() => {
  loadPaymentList()
})

const loadPaymentList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.currentPage,
      size: pagination.pageSize
    }
    if (searchForm.orderId) {
      params.orderId = searchForm.orderId
    }
    if (searchForm.status != null) {
      params.status = searchForm.status
    }
    
    const res = await getPaymentPage(params)
    if (res.code === 200 && res.data) {
      paymentList.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载支付记录失败:', error)
    ElMessage.error('加载支付记录失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  loadPaymentList()
}

const handleReset = () => {
  searchForm.orderId = null
  searchForm.status = undefined
  handleSearch()
}

const handleExport = async () => {
  try {
    // 确认导出
    await ElMessageBox.confirm(
      '确定要导出支付记录吗？将导出当前筛选条件下的所有数据。',
      '导出确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    loading.value = true
    ElMessage.info('正在导出数据，请稍候...')
    
    // 获取所有符合筛选条件的数据（不分页）
    const params = {
      current: 1,
      size: 10000 // 设置一个较大的值以获取所有数据
    }
    if (searchForm.orderId) {
      params.orderId = searchForm.orderId
    }
    if (searchForm.status != null) {
      params.status = searchForm.status
    }
    
    const res = await getPaymentPage(params)
    if (res.code === 200 && res.data) {
      const allPayments = res.data.records || []
      
      if (allPayments.length === 0) {
        ElMessage.warning('没有可导出的数据')
        return
      }
      
      // 转换为CSV格式
      const csvContent = convertToCSV(allPayments)
      
      // 创建下载链接
      const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      const url = URL.createObjectURL(blob)
      link.setAttribute('href', url)
      
      // 生成文件名
      const now = new Date()
      const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '')
      const timeStr = now.toTimeString().slice(0, 8).replace(/:/g, '')
      const filename = `支付记录_${dateStr}_${timeStr}.csv`
      
      link.setAttribute('download', filename)
      link.style.visibility = 'hidden'
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
      
      ElMessage.success(`导出成功！共导出 ${allPayments.length} 条记录`)
    } else {
      ElMessage.error('获取数据失败，无法导出')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('导出失败:', error)
      ElMessage.error('导出失败：' + (error.message || '未知错误'))
    }
  } finally {
    loading.value = false
  }
}

const convertToCSV = (payments) => {
  // CSV表头
  const headers = [
    '支付ID',
    '支付单号',
    '订单ID',
    '支付金额',
    '支付方式',
    '支付状态',
    '交易号',
    '支付时间',
    '退款时间',
    '备注',
    '创建时间'
  ]
  
  // 转换数据行
  const rows = payments.map(payment => {
    return [
      payment.paymentId || '',
      payment.paymentNo || '',
      payment.orderId || '',
      Number(payment.amount || 0).toFixed(2),
      getPaymentMethodText(payment.paymentMethod),
      getStatusText(payment.status),
      payment.transactionId || '',
      payment.payTime || '',
      payment.refundTime || '',
      (payment.remark || '').replace(/"/g, '""'), // 处理CSV中的引号
      payment.createTime || ''
    ]
  })
  
  // 组合CSV内容
  const csvRows = [
    headers.join(','),
    ...rows.map(row => row.map(cell => `"${cell}"`).join(','))
  ]
  
  return csvRows.join('\n')
}

const handleView = async (row) => {
  try {
    const res = await getPaymentById(row.paymentId)
    if (res.code === 200 && res.data) {
      const payment = res.data
      let detailsHtml = '<div style="text-align: left;">'
      detailsHtml += `<p><strong>支付单号：</strong>${payment.paymentNo || '-'}</p>`
      detailsHtml += `<p><strong>订单ID：</strong>${payment.orderId || '-'}</p>`
      detailsHtml += `<p><strong>支付方式：</strong>${getPaymentMethodText(payment.paymentMethod)}</p>`
      detailsHtml += `<p><strong>支付金额：</strong>¥${Number(payment.amount || 0).toFixed(2)}</p>`
      detailsHtml += `<p><strong>支付状态：</strong>${getStatusText(payment.status)}</p>`
      detailsHtml += `<p><strong>交易号：</strong>${payment.transactionId || '-'}</p>`
      if (payment.payTime) {
        detailsHtml += `<p><strong>支付时间：</strong>${payment.payTime}</p>`
      }
      if (payment.refundTime) {
        detailsHtml += `<p><strong>退款时间：</strong>${payment.refundTime}</p>`
      }
      if (payment.remark) {
        detailsHtml += `<p><strong>备注：</strong>${payment.remark}</p>`
      }
      detailsHtml += `<p><strong>创建时间：</strong>${payment.createTime || '-'}</p>`
      detailsHtml += '</div>'
      
      ElMessageBox.alert(detailsHtml, '支付详情', {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '关闭'
      })
    }
  } catch (error) {
    console.error('获取支付详情失败:', error)
    ElMessage.error('获取支付详情失败')
  }
}

const handleRefund = async (row) => {
  try {
    await ElMessageBox.confirm('确定要退款吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    // 输入退款原因（可选）
    const { value: reason } = await ElMessageBox.prompt(
      '请输入退款原因（可选）',
      '退款确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '请输入退款原因'
      }
    ).catch(() => {
      return { value: null }
    })

    // 调用退款接口
    await refundPayment(row.paymentId, reason || '管理员退款')
    ElMessage.success('退款成功')
    loadPaymentList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退款失败:', error)
      ElMessage.error(error.message || '退款失败')
    }
  }
}

const getPaymentMethodText = (method) => {
  const methodMap = {
    'WECHAT': '微信支付',
    'ALIPAY': '支付宝',
    'CASH': '现金',
    'CARD': '银行卡'
  }
  return methodMap[method] || method
}

const getStatusText = (status) => {
  const statusMap = {
    0: '待支付',
    1: '支付成功',
    2: '支付失败',
    3: '已退款'
  }
  return statusMap[status] || '未知'
}

const getStatusType = (status) => {
  const statusMap = {
    0: 'warning',   // 待支付
    1: 'success',  // 支付成功
    2: 'danger',   // 支付失败
    3: 'info'      // 已退款
  }
  return statusMap[status] || ''
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  loadPaymentList()
}

const handlePageChange = (page) => {
  pagination.currentPage = page
  loadPaymentList()
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}
</script>

<style scoped>
.payment-list {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>

