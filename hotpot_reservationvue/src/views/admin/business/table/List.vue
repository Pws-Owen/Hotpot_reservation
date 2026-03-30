 <template>
  <div class="table-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span></span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增餐桌
          </el-button>
        </div>
      </template>
      <el-table 
        :data="tables" 
        style="width: 100%"
        border
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="tableId" label="ID" width="80" />
        <el-table-column prop="tableNumber" label="桌号" width="120" />
        <el-table-column prop="capacity" label="容量" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" show-overflow-tooltip />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="warning" size="small" @click="handleReset(scope.row)">重置</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>
    
    <!-- 新增/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="600px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="桌号" prop="tableNumber">
          <el-input v-model="form.tableNumber" placeholder="请输入桌号" />
        </el-form-item>
        <el-form-item label="容量" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" :max="20" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入位置" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">可用</el-radio>
            <el-radio :label="0">禁用</el-radio>
            <el-radio :label="2">已预订</el-radio>
            <el-radio :label="3">使用中</el-radio>
            <el-radio :label="4">维修中</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getTablePage, createTable, updateTable, deleteTable } from '@/api/table'

const tables = ref([])
const selectedRows = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const currentId = ref(null)
const loading = ref(false)

const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  tableNumber: '',
  capacity: 4,
  location: '',
  status: 1, // 1-可用
  description: ''
})

const rules = {
  tableNumber: [{ required: true, message: '请输入桌号', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'blur' }],
  location: [{ required: true, message: '请输入位置', trigger: 'blur' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑餐桌' : '新增餐桌')

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getTablePage({
      current: pagination.value.page,
      size: pagination.value.size
    })
    if (res.code === 200 && res.data) {
      tables.value = res.data.records || []
      pagination.value.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载餐桌列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  currentId.value = null
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  currentId.value = row.tableId
  Object.assign(form, {
    tableNumber: row.tableNumber,
    capacity: row.capacity,
    location: row.location || '',
    status: row.status != null ? row.status : 1,
    description: row.description || ''
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await updateTable(currentId.value, form)
          ElMessage.success('保存成功')
        } else {
          await createTable(form)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('保存失败:', error)
      }
    }
  })
}

const handleReset = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认要将餐桌"${row.tableNumber}"重置为可用状态吗？`,
      '重置餐桌',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    try {
      // 只更新状态为可用（1），保留其他字段不变
      await updateTable(row.tableId, {
        tableNumber: row.tableNumber,
        capacity: row.capacity,
        location: row.location,
        status: 1, // 重置为可用
        description: row.description
      })
      ElMessage.success('重置成功')
      loadData()
    } catch (error) {
      console.error('重置失败:', error)
      ElMessage.error(error.message || '重置失败')
    }
  } catch {
    // 用户取消
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认要删除此餐桌吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      try {
        await deleteTable(row.tableId)
        ElMessage.success('删除成功')
        loadData()
      } catch (error) {
        console.error('删除失败:', error)
      }
  } catch {
    // 用户取消
  }
}

const handlePageChange = (page) => {
  pagination.value.page = page
  loadData()
}

const handleSizeChange = (size) => {
  pagination.value.size = size
  pagination.value.page = 1
  loadData()
}

const resetForm = () => {
  Object.assign(form, {
    tableNumber: '',
    capacity: 4,
    location: '',
    status: 1, // 1-可用
    description: ''
  })
  formRef.value?.clearValidate()
}

const getStatusType = (status) => {
  const statusMap = {
    0: 'info',      // 禁用
    1: 'success',   // 可用
    2: 'warning',   // 已预订
    3: 'primary',   // 使用中
    4: 'danger'     // 维修中
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    0: '禁用',
    1: '可用',
    2: '已预订',
    3: '使用中',
    4: '维修中'
  }
  return statusMap[status] || '未知'
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}
</script>

<style scoped>
.table-list {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>

