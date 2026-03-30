<template>
  <div class="my-orders-container">
    <!-- 公共头部组件 -->
    <CustomerHeader :show-search="false" title="我的订单" />
    
    <!-- 内容区 -->
    <div class="orders-content">
      <!-- 头部区域：标题+返回按钮 -->
      <div class="orders-header">
        <div class="header-left">
          <h1 class="orders-title">我的订单</h1>
          <p class="orders-subtitle">新鲜食材 · 地道川味 · 订单追踪</p>
        </div>
        <div class="header-actions">
          <!-- 返回首页按钮 -->
          <el-button 
            type="primary" 
            @click="$router.push('/customer/home')"
            class="back-btn"
          >
            <el-icon><ArrowLeft /></el-icon>
            返回首页
          </el-button>
        </div>
      </div>
      
      <el-card>
        <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 150px;">
            <el-option label="待支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="制作中" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
        </el-form>
        
        <el-table :data="orders" style="width: 100%" border v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="下单时间" width="180" />
        <el-table-column prop="tableNumber" label="桌子号" width="100">
          <template #default="scope">
            <span v-if="scope.row.tableNumber">{{ scope.row.tableNumber }}</span>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="reservationNo" label="预约号" width="150">
          <template #default="scope">
            <span v-if="scope.row.reservationNo">{{ scope.row.reservationNo }}</span>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="订单内容" min-width="200">
          <template #default="scope">
            <div v-if="scope.row.details && scope.row.details.length > 0">
              <div v-for="item in scope.row.details" :key="item.detailId" class="order-item">
                {{ item.itemName }} x {{ item.quantity }}
              </div>
            </div>
            <span v-else style="color: #909399;">暂无详情</span>
          </template>
        </el-table-column>
        <el-table-column prop="actualAmount" label="实付金额" width="120">
          <template #default="scope">
            ¥{{ Number(scope.row.actualAmount || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button
              size="small"
              type="info"
              @click="handleView(scope.row)"
            >
              查看
            </el-button>
            <el-button
              v-if="scope.row.status === 0"
              size="small"
              type="success"
              @click="handlePay(scope.row)"
            >
              支付
            </el-button>
            <el-button
              v-if="scope.row.status === 0"
              size="small"
              type="danger"
              @click="handleCancel(scope.row)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
        </div>
      </el-card>
    
    <div class="empty-state" v-if="!loading && orders.length === 0">
      <el-empty description="暂无订单记录">
        <el-button type="primary" @click="$router.push('/customer/order')">
          去点餐
        </el-button>
      </el-empty>
    </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import CustomerHeader from '@/components/customer/CustomerHeader.vue'
import { getOrderPage, getOrderById, cancelOrder } from '@/api/order'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const orders = ref([])
const loading = ref(false)

const searchForm = reactive({
  status: null
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

let refreshTimer = null

onMounted(() => {
  // 检查登录状态
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/customer/login')
    return
  }
  loadData()
  
  // 每30秒自动刷新一次，以便实时查看状态更新
  refreshTimer = setInterval(() => {
    if (document.visibilityState === 'visible') {
      loadData()
    }
  }, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.currentPage,
      size: pagination.pageSize,
      userId: userStore.userId
    }
    if (searchForm.status !== null) {
      params.status = searchForm.status
    }
    
    const res = await getOrderPage(params)
    if (res.code === 200 && res.data) {
      orders.value = res.data.records || []
      pagination.total = res.data.total || 0
      
      // 加载订单详情
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
    console.error('加载订单详情失败:', error)
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
      detailsHtml += `<p><strong>下单时间：</strong>${order.createTime || '-'}</p>`
      if (order.tableNumber) {
        detailsHtml += `<p><strong>桌子号：</strong>${order.tableNumber}</p>`
      }
      if (order.reservationNo) {
        detailsHtml += `<p><strong>预约号：</strong>${order.reservationNo}</p>`
      }
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

const handlePay = (row) => {
  // 直接跳转到支付页面
  router.push({
    path: '/customer/payment',
    query: { orderId: row.orderId }
  })
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消订单 ${row.orderNo} 吗？`,
      '取消订单',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const { value: reason } = await ElMessageBox.prompt(
      '请输入取消原因（可选）',
      '取消订单',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '请输入取消原因'
      }
    ).catch(() => {
      return { value: null }
    })
    
    try {
      await cancelOrder(row.orderId, reason || '用户取消')
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
  searchForm.status = null
  handleSearch()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  loadData()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  loadData()
}
</script>

<style scoped lang="scss">
.my-orders-container {
  width: 100%;
  min-height: 100vh;
  background-color: #f8f8f8;
}

.orders-content {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  box-sizing: border-box;
}

// 头部样式
.orders-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 2px solid #ffe7e7;
  margin-bottom: 20px;

  .header-left {
    .orders-title {
      font-size: 28px;
      font-weight: bold;
      color: #ff4d4f;
      margin: 0 0 5px 0;
    }

    .orders-subtitle {
      font-size: 14px;
      color: #999;
      margin: 0;
    }
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 15px;

    .back-btn {
      display: flex;
      align-items: center;
      gap: 5px;
      padding: 8px 16px;
    }
  }
}

.search-form {
  margin-bottom: 20px;
}

.order-item {
  margin-bottom: 5px;
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

.empty-state {
  margin-top: 50px;
  text-align: center;
}
</style>