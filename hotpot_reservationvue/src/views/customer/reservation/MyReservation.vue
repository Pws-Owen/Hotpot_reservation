<template>
  <div class="my-reservations-page">
    <!-- 通用头部：顶部栏 + 导航 -->
    <CustomerHeader :show-search="false" title="我的预约" />

    <!-- 内容区 -->
    <div class="my-reservations-container">
      <!-- 头部区域：标题+返回按钮 -->
      <div class="reservations-header">
        <div class="header-left">
          <h1 class="reservations-title">我的预约</h1>
          <p class="reservations-subtitle">位置预订 · 用餐安排 · 预约管理</p>
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
        <el-table :data="reservations" style="width: 100%" border v-loading="loading">
          <el-table-column prop="reservationNo" label="预约单号" width="150" show-overflow-tooltip />
          <el-table-column prop="reservationDate" label="预约日期" width="120" />
          <el-table-column prop="reservationTime" label="预约时间" width="100" />
          <el-table-column prop="guestCount" label="用餐人数" width="100" />
          <el-table-column prop="contactName" label="联系人" width="120" />
          <el-table-column prop="contactPhone" label="联系电话" width="120" />
          <el-table-column prop="tableId" label="餐桌ID" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="specialRequest" label="特殊要求" show-overflow-tooltip />
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="scope">
              <el-button
                size="small"
                type="info"
                @click="handleView(scope.row)"
              >
                查看
              </el-button>
              <el-button
                size="small"
                type="primary"
                @click="handleOrderFood(scope.row)"
                :disabled="scope.row.status === 4 || scope.row.status === 3"
              >
                点菜
              </el-button>
              <el-button
                size="small"
                type="danger"
                @click="handleCancel(scope.row)"
                :disabled="scope.row.status === 4 || scope.row.status === 3"
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
    
    <div class="empty-state" v-if="!loading && reservations.length === 0">
      <el-empty description="暂无预约记录">
        <el-button type="primary" @click="$router.push('/customer/reservation')">
          立即预约
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
import { getReservationPage, getReservationById, cancelReservation } from '@/api/reservation'
import { useUserStore } from '@/stores/user'
import CustomerHeader from '@/components/customer/CustomerHeader.vue'

const router = useRouter()
const userStore = useUserStore()
const reservations = ref([])
const loading = ref(false)

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
    
    const res = await getReservationPage(params)
    if (res.code === 200 && res.data) {
      reservations.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载预约列表失败:', error)
    ElMessage.error('加载预约列表失败')
  } finally {
    loading.value = false
  }
}

const getStatusType = (status) => {
  const statusMap = {
    0: 'warning',   // 待确认
    1: 'success',  // 已确认
    2: 'primary',  // 进行中
    3: 'info',     // 已完成
    4: 'danger',   // 已取消
    5: 'info'      // 已过期
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    0: '待确认',
    1: '已确认',
    2: '进行中',
    3: '已完成',
    4: '已取消',
    5: '已过期'
  }
  return statusMap[status] || '未知'
}

const handleView = async (row) => {
  try {
    const res = await getReservationById(row.reservationId)
    if (res.code === 200 && res.data) {
      ElMessageBox.alert(
        `<div style="text-align: left;">
          <p><strong>预约单号：</strong>${res.data.reservationNo || '-'}</p>
          <p><strong>预约日期：</strong>${res.data.reservationDate || '-'}</p>
          <p><strong>预约时间：</strong>${res.data.reservationTime || '-'}</p>
          <p><strong>用餐人数：</strong>${res.data.guestCount || '-'}</p>
          <p><strong>联系人：</strong>${res.data.contactName || '-'}</p>
          <p><strong>联系电话：</strong>${res.data.contactPhone || '-'}</p>
          <p><strong>餐桌ID：</strong>${res.data.tableId || '系统分配'}</p>
          <p><strong>特殊要求：</strong>${res.data.specialRequest || '无'}</p>
          <p><strong>状态：</strong>${getStatusText(res.data.status)}</p>
          ${res.data.cancelReason ? `<p><strong>取消原因：</strong>${res.data.cancelReason}</p>` : ''}
        </div>`,
        '预约详情',
        {
          dangerouslyUseHTMLString: true,
          confirmButtonText: '关闭'
        }
      )
    }
  } catch (error) {
    console.error('获取预约详情失败:', error)
    ElMessage.error('获取预约详情失败')
  }
}

const handleOrderFood = (row) => {
  // 检查预约状态，只有已确认或进行中的预约才能点菜
  if (row.status !== 1 && row.status !== 2) {
    ElMessage.warning('只有已确认或进行中的预约才能点菜')
    return
  }
  
  // 检查是否有餐桌ID
  if (!row.tableId) {
    ElMessage.warning('该预约尚未分配餐桌，无法点菜')
    return
  }
  
  // 保存预约信息到 sessionStorage，用于提交订单时使用
  const reservationInfo = {
    reservationId: row.reservationId,
    tableId: row.tableId,
    reservationNo: row.reservationNo
  }
  sessionStorage.setItem('currentReservation', JSON.stringify(reservationInfo))
  
  // 跳转到菜品页面
  router.push('/customer/menu')
  ElMessage.success('已跳转到菜品页面，请选择菜品')
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消 ${row.reservationDate} ${row.reservationTime} 的预约吗？`,
      '取消预约',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const { value: reason } = await ElMessageBox.prompt(
      '请输入取消原因（可选）',
      '取消预约',
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
      await cancelReservation(row.reservationId, reason || '用户取消')
      ElMessage.success('预约已取消')
      loadData()
    } catch (error) {
      console.error('取消预约失败:', error)
      ElMessage.error(error.message || '取消预约失败')
    }
  } catch {
    // 用户取消
  }
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
.my-reservations-page {
  width: 100%;
  min-height: 100vh;
  background: #f5f5f5;
}

.my-reservations-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  box-sizing: border-box;
}

// 头部样式
.reservations-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 2px solid #ffe7e7;
  margin-bottom: 20px;

  .header-left {
    .reservations-title {
      font-size: 28px;
      font-weight: bold;
      color: #ff4d4f;
      margin: 0 0 5px 0;
    }

    .reservations-subtitle {
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

.empty-state {
  margin-top: 50px;
  text-align: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

// 响应式适配
@media (max-width: 768px) {
  .my-reservations-container {
    padding: 10px;
  }
}
</style>