<template>
  <div class="table-layout">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>餐桌布局图</span>
          <div>
            <el-button @click="handleRefresh">刷新</el-button>
            <el-button type="primary" @click="handleAddTable">新增餐桌</el-button>
          </div>
        </div>
      </template>
      
      <div class="layout-container">
        <div 
          v-for="table in tables" 
          :key="table.id"
          :class="['table-item', getTableStatusClass(table.status)]"
          @click="handleTableClick(table)"
        >
          <div class="table-number">{{ table.tableNumber }}</div>
          <div class="table-status">{{ getStatusText(table.status) }}</div>
          <div class="table-info" v-if="table.reservation">
            <div>客户: {{ table.reservation.customerName }}</div>
            <div>时间: {{ table.reservation.time }}</div>
          </div>
        </div>
      </div>

      <el-dialog v-model="tableDialogVisible" title="餐桌详情" width="500px">
        <el-form :model="currentTable" label-width="100px">
          <el-form-item label="桌号">
            <el-input v-model="currentTable.tableNumber" disabled />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="currentTable.status">
              <el-option label="可用" value="available" />
              <el-option label="已占用" value="occupied" />
              <el-option label="不可用" value="unavailable" />
            </el-select>
          </el-form-item>
          <el-form-item label="座位数">
            <el-input-number v-model="currentTable.capacity" :min="2" :max="20" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="tableDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveTable">保存</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const tableDialogVisible = ref(false)
const currentTable = ref({})

const tables = ref([
  { id: 1, tableNumber: 'A01', status: 'available', capacity: 4 },
  { id: 2, tableNumber: 'A02', status: 'occupied', capacity: 6, reservation: { customerName: '张三', time: '18:00' } },
  { id: 3, tableNumber: 'B01', status: 'unavailable', capacity: 4 },
  { id: 4, tableNumber: 'B02', status: 'available', capacity: 2 },
  { id: 5, tableNumber: 'C01', status: 'occupied', capacity: 8, reservation: { customerName: '李四', time: '19:30' } }
])

onMounted(() => {
  loadTableData()
})

const loadTableData = () => {
  // 从API加载餐桌数据
}

const getTableStatusClass = (status) => {
  return `status-${status}`
}

const getStatusText = (status) => {
  const statusMap = {
    'available': '可用',
    'occupied': '已占用',
    'unavailable': '不可用'
  }
  return statusMap[status] || status
}

const handleTableClick = (table) => {
  currentTable.value = { ...table }
  tableDialogVisible.value = true
}

const handleRefresh = () => {
  loadTableData()
  ElMessage.success('刷新成功')
}

const handleAddTable = () => {
  currentTable.value = {
    tableNumber: '',
    status: 'available',
    capacity: 4
  }
  tableDialogVisible.value = true
}

const handleSaveTable = () => {
  ElMessage.success('保存成功')
  tableDialogVisible.value = false
  loadTableData()
}
</script>

<style scoped>
.table-layout {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.layout-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 20px;
  padding: 20px;
}

.table-item {
  border: 2px solid #ddd;
  border-radius: 8px;
  padding: 15px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.table-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.status-available {
  background-color: #f0f9ff;
  border-color: #67c23a;
}

.status-occupied {
  background-color: #fef0f0;
  border-color: #f56c6c;
}

.status-unavailable {
  background-color: #f4f4f5;
  border-color: #909399;
}

.table-number {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 10px;
}

.table-status {
  font-size: 14px;
  margin-bottom: 10px;
}

.table-info {
  font-size: 12px;
  color: #666;
  margin-top: 10px;
}
</style>

