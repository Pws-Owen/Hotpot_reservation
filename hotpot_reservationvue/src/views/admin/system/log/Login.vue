<template>
  <div class="login-log">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>登录日志</span>
          <el-button type="primary" @click="handleExport">导出</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="登录IP">
          <el-input v-model="searchForm.ip" placeholder="请输入IP地址" clearable />
        </el-form-item>
        <el-form-item label="登录状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILED" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table 
        :data="logList" 
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="ip" label="登录IP" width="150" />
        <el-table-column prop="location" label="登录地点" width="150" />
        <el-table-column prop="browser" label="浏览器" width="150" />
        <el-table-column prop="os" label="操作系统" width="150" />
        <el-table-column prop="status" label="登录状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'">
              {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="登录信息" show-overflow-tooltip />
        <el-table-column prop="loginTime" label="登录时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
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
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'

const searchForm = ref({
  username: '',
  ip: '',
  status: '',
  dateRange: []
})

const selectedRows = ref([])

const logList = ref([
  { id: 1, username: 'admin', ip: '192.168.1.100', location: '北京市', browser: 'Chrome', os: 'Windows 10', status: 'SUCCESS', message: '登录成功', loginTime: '2024-01-15 10:30:25' },
  { id: 2, username: 'receptionist', ip: '192.168.1.101', location: '北京市', browser: 'Firefox', os: 'Windows 11', status: 'SUCCESS', message: '登录成功', loginTime: '2024-01-15 09:15:10' },
  { id: 3, username: 'admin', ip: '192.168.1.102', location: '未知', browser: 'Unknown', os: 'Unknown', status: 'FAILED', message: '密码错误', loginTime: '2024-01-15 08:45:30' }
])

const pagination = ref({
  page: 1,
  size: 10,
  total: 3
})

onMounted(() => {
  loadLogList()
})

const loadLogList = () => {
  // 从API加载登录日志
}

const handleSearch = () => {
  loadLogList()
}

const handleReset = () => {
  searchForm.value = {
    username: '',
    ip: '',
    status: '',
    dateRange: []
  }
  loadLogList()
}

const handleExport = () => {
  ElMessage.success('导出功能开发中...')
}

const handleView = (row) => {
  ElMessage.info(`查看登录日志详情: ${row.username}`)
}

const handleSizeChange = () => {
  loadLogList()
}

const handlePageChange = () => {
  loadLogList()
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}
</script>

<style scoped>
.login-log {
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

