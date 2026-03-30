<template>
  <div class="reservation-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span></span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增预约
          </el-button>
        </div>
      </template>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="预约日期">
          <el-date-picker
            v-model="searchForm.date"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 180px;">
            <el-option label="待确认" :value="0" />
            <el-option label="已确认" :value="1" />
            <el-option label="进行中" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
            <el-option label="已过期" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table 
        :data="reservations" 
        style="width: 100%"
        border
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="reservationId" label="ID" width="80" />
        <el-table-column prop="reservationNo" label="预约单号" width="150" />
        <el-table-column prop="contactName" label="客户姓名" width="120" />
        <el-table-column prop="contactPhone" label="联系电话" width="120" />
        <el-table-column prop="reservationDate" label="预约日期" width="120" />
        <el-table-column prop="reservationTime" label="预约时间" width="100" />
        <el-table-column prop="guestCount" label="用餐人数" width="100" />
        <el-table-column prop="tableId" label="餐桌ID" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="specialRequest" label="特殊要求" show-overflow-tooltip />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button
              size="small"
              type="primary"
              @click="handleView(scope.row)"
            >
              查看
            </el-button>
            <el-button
              size="small"
              type="success"
              @click="handleConfirm(scope.row)"
              :disabled="scope.row.status !== 0"
            >
              确认
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleCancel(scope.row)"
              :disabled="scope.row.status === 4"
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getReservationPage, confirmReservation, cancelReservation, getReservationById } from '@/api/reservation'

const router = useRouter()

const searchForm = reactive({
  date: '',
  status: null
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const reservations = ref([])
const selectedRows = ref([])
const loading = ref(false)

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
    if (searchForm.status !== null && searchForm.status !== '') {
      params.status = searchForm.status
    }
    if (searchForm.date) {
      params.date = searchForm.date
    }
    
    const res = await getReservationPage(params)
    if (res.code === 200 && res.data) {
      reservations.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载预约列表失败:', error)
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

const handleSearch = () => {
  pagination.currentPage = 1
  loadData()
}

const resetSearch = () => {
  searchForm.date = ''
  searchForm.status = null
  handleSearch()
}

const handleAdd = () => {
  router.push('/business/reservation/add')
}

const handleView = async (row) => {
  try {
    const res = await getReservationById(row.reservationId)
    if (res.code === 200 && res.data) {
      ElMessageBox.alert(
        `<div style="text-align: left;">
          <p><strong>预约单号：</strong>${res.data.reservationNo || '-'}</p>
          <p><strong>客户姓名：</strong>${res.data.contactName || '-'}</p>
          <p><strong>联系电话：</strong>${res.data.contactPhone || '-'}</p>
          <p><strong>预约日期：</strong>${res.data.reservationDate || '-'}</p>
          <p><strong>预约时间：</strong>${res.data.reservationTime || '-'}</p>
          <p><strong>用餐人数：</strong>${res.data.guestCount || '-'}</p>
          <p><strong>餐桌ID：</strong>${res.data.tableId || '-'}</p>
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
  }
}

const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要确认 ${row.contactName || row.username} 的预约吗？`,
      '确认预约',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    try {
      await confirmReservation(row.reservationId)
      ElMessage.success('预约已确认')
      loadData()
    } catch (error) {
      console.error('确认预约失败:', error)
    }
  } catch {
    // 用户取消
  }
}

const handleCancel = async (row) => {
  try {
    // 先确认是否要取消
    await ElMessageBox.confirm(
      `确定要取消 ${row.contactName || '该'} 的预约吗？`,
      '取消预约',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 用户确认后，询问取消原因
    try {
      const { value: reason } = await ElMessageBox.prompt(
        '请输入取消原因（可选）',
        '取消原因',
        {
          confirmButtonText: '确定',
          cancelButtonText: '跳过',
          inputType: 'textarea',
          inputPlaceholder: '请输入取消原因，留空则使用默认原因'
        }
      ).catch(() => {
        // 用户点击跳过，返回空值
        return { value: '' }
      })
      
      // 调用取消接口
      await cancelReservation(row.reservationId, reason || '管理员取消')
      ElMessage.success('预约已取消')
      loadData()
    } catch (error) {
      console.error('取消预约失败:', error)
    }
  } catch {
    // 用户在第一层确认对话框中点击了"取消"，不执行任何操作
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

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}
</script>

<style scoped>
.reservation-list {
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
}
</style>