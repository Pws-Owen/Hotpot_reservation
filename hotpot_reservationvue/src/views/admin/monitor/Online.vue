<template>
  <div class="online-users">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>在线用户</span>
          <div>
            <el-button @click="handleRefresh">刷新</el-button>
            <el-button type="danger" @click="handleForceLogout" :disabled="selectedUsers.length === 0">
              强制下线
            </el-button>
          </div>
        </div>
      </template>
      
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="6">
          <el-statistic title="在线用户数" :value="onlineCount" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="管理员" :value="adminCount" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="前台服务员" :value="receptionistCount" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="普通用户" :value="userCount" />
        </el-col>
      </el-row>

      <el-table 
        :data="onlineUserList" 
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag>{{ row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="登录IP" width="150" />
        <el-table-column prop="location" label="登录地点" width="150" />
        <el-table-column prop="loginTime" label="登录时间" width="180" />
        <el-table-column prop="lastActiveTime" label="最后活跃时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" size="small" @click="handleForceLogoutSingle(row)">
              强制下线
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const onlineCount = ref(15)
const adminCount = ref(2)
const receptionistCount = ref(3)
const userCount = ref(10)
const selectedUsers = ref([])

const onlineUserList = ref([
  { id: 1, username: 'admin', realName: '管理员', role: '系统管理员', ip: '192.168.1.100', location: '北京市', loginTime: '2024-01-15 10:30:25', lastActiveTime: '2024-01-15 14:25:30' },
  { id: 2, username: 'receptionist01', realName: '前台1', role: '前台服务员', ip: '192.168.1.101', location: '北京市', loginTime: '2024-01-15 09:15:10', lastActiveTime: '2024-01-15 14:20:15' },
  { id: 3, username: 'user001', realName: '张三', role: '普通用户', ip: '192.168.1.102', location: '北京市', loginTime: '2024-01-15 11:00:00', lastActiveTime: '2024-01-15 14:18:45' }
])

onMounted(() => {
  loadOnlineUsers()
  // 定时刷新
  setInterval(() => {
    loadOnlineUsers()
  }, 30000) // 每30秒刷新一次
})

const loadOnlineUsers = () => {
  // 从API加载在线用户列表
}

const handleRefresh = () => {
  loadOnlineUsers()
  ElMessage.success('刷新成功')
}

const handleSelectionChange = (selection) => {
  selectedUsers.value = selection
}

const handleForceLogout = () => {
  if (selectedUsers.value.length === 0) {
    ElMessage.warning('请选择要下线的用户')
    return
  }
  
  ElMessageBox.confirm(`确定要强制下线选中的 ${selectedUsers.value.length} 个用户吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('强制下线成功')
    loadOnlineUsers()
  })
}

const handleForceLogoutSingle = (row) => {
  ElMessageBox.confirm(`确定要强制下线用户 ${row.username} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('强制下线成功')
    loadOnlineUsers()
  })
}
</script>

<style scoped>
.online-users {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

