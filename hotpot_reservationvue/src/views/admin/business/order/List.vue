<template>
  <div class="order-list">
    <el-card>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单日期">
          <el-date-picker
            v-model="searchForm.date"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 180px;">
            <el-option label="待支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="制作中" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
            <el-option label="已退款" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table 
        :data="orders" 
        style="width: 100%"
        border
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="orderId" label="订单ID" min-width="80" />
        <el-table-column prop="orderNo" label="订单号" min-width="150" show-overflow-tooltip />
        <el-table-column prop="realName" label="客户姓名" min-width="120">
          <template #default="{ row }">
            {{ row.realName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="tableId" label="餐桌ID" min-width="100" />
        <el-table-column label="订单内容" min-width="250" show-overflow-tooltip>
          <template #default="scope">
            <div v-if="scope.row.details && scope.row.details.length > 0" class="order-content">
              <div v-for="(item, index) in scope.row.details" :key="index" class="order-item">
                {{ item.itemName }} x {{ item.quantity }}
              </div>
            </div>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="actualAmount" label="订单金额" min-width="120">
          <template #default="scope">
            ¥{{ Number(scope.row.actualAmount || scope.row.totalPrice || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" style="cursor: pointer;" @click="handleStatusClick(scope.row)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleView(scope.row)">
              查看详情
            </el-button>
            <el-button type="danger" size="small" @click="handleCancel(scope.row)" :disabled="scope.row.status === 4 || scope.row.status === 3 || scope.row.status === 5">
              取消订单
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
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderPage, cancelOrder, getOrderById, updateOrderStatus } from '@/api/order'

const router = useRouter()

const searchForm = reactive({
  date: '',
  status: null
})

const orders = ref([])
const selectedRows = ref([])
const loading = ref(false)

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.currentPage,
      size: pagination.pageSize
    }
    if (searchForm.status !== null) {
      params.status = searchForm.status
    }
    if (searchForm.date) {
      params.date = searchForm.date
    }
    
    const res = await getOrderPage(params)
    if (res.code === 200 && res.data) {
      orders.value = res.data.records || []
      pagination.total = res.data.total || 0
      
      // 加载每个订单的详情
      for (const order of orders.value) {
        await loadOrderDetails(order)
      }
    }
  } catch (error) {
    console.error('加载订单列表失败:', error)
    ElMessage.error('加载订单列表失败')
  } finally {
    loading.value = false
  }
}

const loadOrderDetails = async (order) => {
  try {
    const res = await getOrderById(order.orderId)
    if (res.code === 200 && res.data && res.data.details) {
      order.details = res.data.details
    }
  } catch (error) {
    console.error(`加载订单 ${order.orderId} 详情失败:`, error)
    // 加载失败不影响列表显示
  }
}

const getStatusType = (status) => {
  const statusMap = {
    0: 'warning',   // 待支付
    1: 'success',  // 已支付
    2: 'primary',  // 制作中
    3: 'info',     // 已完成
    4: 'danger',   // 已取消
    5: 'info'      // 已退款
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    0: '待支付',
    1: '已支付',
    2: '制作中',
    3: '已完成',
    4: '已取消',
    5: '已退款'
  }
  return statusMap[status] || '未知'
}

const handleView = async (row) => {
  try {
    const res = await getOrderById(row.orderId)
    if (res.code === 200 && res.data) {
      const order = res.data
      let detailsHtml = '<div style="text-align: left;">'
      detailsHtml += `<p><strong>订单号：</strong>${order.orderNo || '-'}</p>`
      detailsHtml += `<p><strong>客户姓名：</strong>${row.realName || '-'}</p>`
      detailsHtml += `<p><strong>餐桌ID：</strong>${order.tableId || '-'}</p>`
      detailsHtml += `<p><strong>订单类型：</strong>${getOrderTypeText(order.orderType)}</p>`
      detailsHtml += `<p><strong>订单状态：</strong>${getStatusText(order.status)}</p>`
      detailsHtml += `<p><strong>订单总价：</strong>¥${Number(order.totalPrice || 0).toFixed(2)}</p>`
      detailsHtml += `<p><strong>实付金额：</strong>¥${Number(order.actualAmount || 0).toFixed(2)}</p>`
      
      if (order.details && order.details.length > 0) {
        detailsHtml += '<p><strong>订单详情：</strong></p><ul>'
        order.details.forEach(item => {
          detailsHtml += `<li>${item.itemName} x ${item.quantity} = ¥${Number(item.subtotal || 0).toFixed(2)}</li>`
        })
        detailsHtml += '</ul>'
      }
      
      if (order.remark) {
        detailsHtml += `<p><strong>备注：</strong>${order.remark}</p>`
      }
      
      detailsHtml += `<p><strong>创建时间：</strong>${order.createTime || '-'}</p>`
      if (order.payTime) {
        detailsHtml += `<p><strong>支付时间：</strong>${order.payTime}</p>`
      }
      if (order.finishTime) {
        detailsHtml += `<p><strong>完成时间：</strong>${order.finishTime}</p>`
      }
      
      detailsHtml += '</div>'
      
      ElMessageBox.alert(detailsHtml, '订单详情', {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '关闭'
      })
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  }
}

const getOrderTypeText = (type) => {
  const typeMap = {
    1: '堂食',
    2: '外卖',
    3: '预约点餐'
  }
  return typeMap[type] || '未知'
}

const handleStatusClick = async (row) => {
  try {
    const { value: newStatus } = await ElMessageBox({
      title: '修改订单状态',
      message: '请选择新的订单状态',
      showCancelButton: true,
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputType: 'select',
      inputOptions: {
        0: '待支付',
        1: '已支付',
        2: '制作中',
        3: '已完成',
        4: '已取消',
        5: '已退款'
      },
      inputValue: row.status
    }).catch(() => {
      return { value: null }
    })
    
    if (newStatus !== null && newStatus !== row.status) {
      await updateOrderStatus(row.orderId, newStatus)
      ElMessage.success('订单状态已更新')
      loadData()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('更新订单状态失败:', error)
      ElMessage.error(error.message || '更新订单状态失败')
    }
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确认要取消此订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    try {
      await cancelOrder(row.orderId, '管理员取消')
      ElMessage.success('订单已取消')
      loadData()
    } catch (error) {
      console.error('取消订单失败:', error)
      ElMessage.error(error.message || '取消订单失败')
    }
  } catch {
    // 用户取消
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  loadData()
}

const resetSearch = () => {
  searchForm.date = ''
  searchForm.status = null
  handleSearch()
}

const handlePageChange = (page) => {
  pagination.currentPage = page
  loadData()
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
  loadData()
}
</script>

<style scoped>
.order-list {
  padding: 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.search-form {
  margin-bottom: 20px;
}

.order-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.order-item {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
}
</style>

