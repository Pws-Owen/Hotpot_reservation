<template>
  <div class="announcement-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span></span>
          <el-button type="primary" @click="handleAdd">新增公告</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="公告标题">
          <el-input v-model="searchForm.title" placeholder="请输入公告标题" clearable />
        </el-form-item>
        <el-form-item label="公告类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable style="width: 180px;">
            <el-option label="系统公告" :value="1" :key="1" />
            <el-option label="活动公告" :value="2" :key="2" />
            <el-option label="通知公告" :value="3" :key="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px;">
            <el-option label="已发布" :value="1" :key="1" />
            <el-option label="已禁用" :value="0" :key="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table 
        :data="announcementList" 
        border
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="announcementId" label="ID" width="80" />
        <el-table-column prop="title" label="公告标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="type" label="公告类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isTop" label="是否置顶" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isTop === 1 ? 'danger' : 'info'">
              {{ row.isTop === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishTime" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.publishTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button type="success" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              :type="row.isTop === 1 ? 'warning' : 'info'" 
              size="small" 
              @click="handleToggleTop(row)"
              :loading="row.toggleLoading"
            >
              {{ row.isTop === 1 ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 查看/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="800px"
    >
      <el-form :model="formData" label-width="100px" v-if="formData">
        <el-form-item label="公告标题" required>
          <el-input 
            v-model="formData.title" 
            placeholder="请输入公告标题" 
            :disabled="!isEdit && formData.announcementId"
          />
        </el-form-item>
        <el-form-item label="公告类型" required>
          <el-select 
            v-model="formData.type" 
            placeholder="请选择类型" 
            style="width: 100%;"
            :disabled="!isEdit && formData.announcementId"
          >
            <el-option label="系统公告" :value="1" />
            <el-option label="活动公告" :value="2" />
            <el-option label="通知公告" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="公告内容" required>
          <el-input 
            v-model="formData.content" 
            type="textarea" 
            :rows="8" 
            placeholder="请输入公告内容"
            :disabled="!isEdit && formData.announcementId"
          />
        </el-form-item>
        <el-form-item label="是否置顶">
          <el-switch 
            v-model="formData.isTopSwitch" 
            :disabled="!isEdit && formData.announcementId"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group 
            v-model="formData.status"
            :disabled="!isEdit && formData.announcementId"
          >
            <el-radio :label="1">已发布</el-radio>
            <el-radio :label="0">已禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="发布时间">
          <el-date-picker
            v-model="formData.publishTime"
            type="datetime"
            placeholder="选择发布时间"
            style="width: 100%;"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DDTHH:mm:ss"
            :disabled="!isEdit && formData.announcementId"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ isEdit || !formData?.announcementId ? '取消' : '关闭' }}</el-button>
        <el-button 
          v-if="isEdit || !formData?.announcementId"
          type="primary" 
          @click="handleSubmit" 
          :loading="submitLoading"
        >
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getAnnouncementPage, 
  getAnnouncementById, 
  createAnnouncement, 
  updateAnnouncement, 
  deleteAnnouncement 
} from '@/api/announcement'

const searchForm = reactive({
  title: '',
  type: undefined,
  status: undefined
})

const selectedRows = ref([])
const announcementList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formData = ref(null)

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

onMounted(() => {
  loadAnnouncementList()
})

/**
 * 加载公告列表
 */
const loadAnnouncementList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      title: searchForm.title || undefined,
      type: searchForm.type != null ? searchForm.type : undefined,
      status: searchForm.status != null ? searchForm.status : undefined
    }
    
    const res = await getAnnouncementPage(params)
    if (res.code === 200) {
      announcementList.value = (res.data.records || []).map(item => ({
        ...item,
        toggleLoading: false
      }))
      pagination.total = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取公告列表失败')
      announcementList.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取公告列表失败:', error)
    ElMessage.error('获取公告列表失败')
    announcementList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  pagination.current = 1
  loadAnnouncementList()
}

/**
 * 重置搜索
 */
const handleReset = () => {
  searchForm.title = ''
  searchForm.type = undefined
  searchForm.status = undefined
  pagination.current = 1
  loadAnnouncementList()
}

/**
 * 新增公告
 */
const handleAdd = () => {
  isEdit.value = false
  formData.value = {
    title: '',
    content: '',
    type: 1,
    isTop: 0,
    isTopSwitch: false,
    status: 1,
    publishTime: new Date().toISOString().slice(0, 19).replace('T', ' ')
  }
  dialogVisible.value = true
}

/**
 * 查看公告
 */
const handleView = async (row) => {
  try {
    const res = await getAnnouncementById(row.announcementId)
    if (res.code === 200 && res.data) {
      isEdit.value = false
      formData.value = {
        ...res.data,
        isTopSwitch: res.data.isTop === 1,
        publishTime: res.data.publishTime ? formatDateTimeForPicker(res.data.publishTime) : null
      }
      dialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取公告详情失败')
    }
  } catch (error) {
    console.error('获取公告详情失败:', error)
    ElMessage.error('获取公告详情失败')
  }
}

/**
 * 编辑公告
 */
const handleEdit = async (row) => {
  try {
    const res = await getAnnouncementById(row.announcementId)
    if (res.code === 200 && res.data) {
      isEdit.value = true
      formData.value = {
        ...res.data,
        isTopSwitch: res.data.isTop === 1,
        publishTime: res.data.publishTime ? formatDateTimeForPicker(res.data.publishTime) : null
      }
      dialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取公告详情失败')
    }
  } catch (error) {
    console.error('获取公告详情失败:', error)
    ElMessage.error('获取公告详情失败')
  }
}

/**
 * 切换置顶状态
 */
const handleToggleTop = async (row) => {
  row.toggleLoading = true
  try {
    const newIsTop = row.isTop === 1 ? 0 : 1
    const res = await updateAnnouncement(row.announcementId, {
      ...row,
      isTop: newIsTop
    })
    if (res.code === 200) {
      ElMessage.success(newIsTop === 1 ? '已置顶' : '已取消置顶')
      loadAnnouncementList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('切换置顶状态失败:', error)
    ElMessage.error('操作失败')
  } finally {
    row.toggleLoading = false
  }
}

/**
 * 删除公告
 */
const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除这条公告吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteAnnouncement(row.announcementId)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadAnnouncementList()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除公告失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {
    // 用户取消
  })
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  if (!formData.value.title || !formData.value.title.trim()) {
    ElMessage.warning('请输入公告标题')
    return
  }
  if (!formData.value.content || !formData.value.content.trim()) {
    ElMessage.warning('请输入公告内容')
    return
  }
  
  submitLoading.value = true
  try {
    // 处理发布时间格式：如果是字符串格式（YYYY-MM-DD HH:mm:ss），转换为 ISO 格式
    let publishTimeValue = formData.value.publishTime
    if (publishTimeValue && typeof publishTimeValue === 'string' && publishTimeValue.includes(' ')) {
      // 将 "YYYY-MM-DD HH:mm:ss" 格式转换为 ISO 格式 "YYYY-MM-DDTHH:mm:ss"
      publishTimeValue = publishTimeValue.replace(' ', 'T')
    } else if (!publishTimeValue) {
      publishTimeValue = new Date().toISOString().slice(0, 19)
    }
    
    const data = {
      title: formData.value.title.trim(),
      content: formData.value.content.trim(),
      type: formData.value.type,
      isTop: formData.value.isTopSwitch ? 1 : 0,
      status: formData.value.status,
      publishTime: publishTimeValue
    }
    
    let res
    if (isEdit.value) {
      res = await updateAnnouncement(formData.value.announcementId, data)
    } else {
      res = await createAnnouncement(data)
    }
    
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadAnnouncementList()
    } else {
      ElMessage.error(res.message || (isEdit.value ? '更新失败' : '创建失败'))
    }
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitLoading.value = false
  }
}

/**
 * 获取类型文本
 */
const getTypeText = (type) => {
  const typeMap = {
    1: '系统公告',
    2: '活动公告',
    3: '通知公告'
  }
  return typeMap[type] || '未知'
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const statusMap = {
    1: '已发布',
    0: '已禁用'
  }
  return statusMap[status] || '未知'
}

/**
 * 获取状态类型
 */
const getStatusType = (status) => {
  const statusMap = {
    1: 'success',
    0: 'info'
  }
  return statusMap[status] || ''
}

/**
 * 格式化日期时间
 */
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/**
 * 格式化日期时间用于日期选择器
 */
const formatDateTimeForPicker = (dateTime) => {
  if (!dateTime) return null
  const date = new Date(dateTime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

/**
 * 分页大小改变
 */
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.current = 1
  loadAnnouncementList()
}

/**
 * 当前页改变
 */
const handlePageChange = (current) => {
  pagination.current = current
  loadAnnouncementList()
}

/**
 * 选择改变
 */
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

/**
 * 对话框标题
 */
const dialogTitle = computed(() => isEdit.value ? '编辑公告' : (formData.value ? '查看公告' : '新增公告'))
</script>

<style scoped>
.announcement-list {
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

