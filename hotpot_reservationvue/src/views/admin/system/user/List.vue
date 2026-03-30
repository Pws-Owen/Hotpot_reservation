<template>
  <div class="user-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span></span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增用户
          </el-button>
        </div>
      </template>
      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="searchForm.realName" placeholder="请输入真实姓名" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px;">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table 
        :data="tableData" 
        style="width: 100%"
        border
        v-loading="loading"
      >
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="userType" label="用户类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.userType === 1 ? 'primary' : 'success'">
              {{ scope.row.userType === 1 ? '系统用户' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" link @click="handleEdit(scope.row)">
              <el-icon><Edit /></el-icon> 编辑
            </el-button>
            <el-button type="warning" size="small" link @click="handleResetPassword(scope.row)">
              <el-icon><Key /></el-icon> 重置密码
            </el-button>
            <el-button 
              v-if="scope.row.userType === 1" 
              type="info" 
              size="small" 
              link 
              @click="handleAssignRoles(scope.row)"
            >
              <el-icon><UserFilled /></el-icon> 分配角色
            </el-button>
            <el-button type="danger" size="small" link @click="handleDelete(scope.row)">
              <el-icon><Delete /></el-icon> 删除
            </el-button>
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
          @size-change="loadData"
          @current-change="loadData"
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
        <template v-if="dialogTitle === '分配角色'">
          <!-- 分配角色时只显示角色选择 -->
          <el-form-item label="用户" v-if="form.realName">
            <el-input :value="form.realName" disabled />
          </el-form-item>
          <el-form-item label="角色" prop="roleIds">
            <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%">
              <el-option
                v-for="role in roleList"
                :key="role.roleId"
                :label="role.roleName"
                :value="role.roleId"
              />
            </el-select>
          </el-form-item>
        </template>
        <template v-else>
          <!-- 新增/编辑用户时显示完整表单 -->
          <el-form-item label="用户名" prop="username" v-if="!form.userId">
            <el-input v-model="form.username" placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="密码" prop="password" v-if="!form.userId">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="form.realName" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="用户类型" prop="userType" v-if="!form.userId">
            <el-radio-group v-model="form.userType">
              <el-radio :label="1">系统用户</el-radio>
              <el-radio :label="2">普通用户</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="form.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="角色" prop="roleIds" v-if="form.userType === 1 && !form.userId">
            <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%">
              <el-option
                v-for="role in roleList"
                :key="role.roleId"
                :label="role.roleName"
                :value="role.roleId"
              />
            </el-select>
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetPasswordDialogVisible"
      title="重置密码"
      width="400px"
    >
      <el-form :model="resetPasswordForm" :rules="resetPasswordRules" ref="resetPasswordFormRef" label-width="100px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetPasswordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="resetPasswordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetPasswordSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Key, UserFilled, Delete } from '@element-plus/icons-vue'
import { getUserPage, createUser, updateUser, deleteUser, resetPassword, getUserRoles, assignRoles } from '@/api/system/user'
import { getAllRoles } from '@/api/system/role'
import { formatDateTime } from '@/utils/date'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const roleList = ref([])

const searchForm = reactive({
  username: '',
  realName: '',
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const formRef = ref(null)
const form = reactive({
  userId: null,
  username: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  userType: 1,
  status: 1,
  roleIds: []
})

const resetPasswordDialogVisible = ref(false)
const resetPasswordFormRef = ref(null)
const resetPasswordForm = reactive({
  userId: null,
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const resetPasswordRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== resetPasswordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

onMounted(() => {
  loadData()
  loadRoles()
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      ...searchForm,
      userType: 1 // 只查询系统用户
    }
    const res = await getUserPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取用户列表失败')
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

const loadRoles = async () => {
  try {
    const res = await getAllRoles()
    if (res.code === 200) {
      roleList.value = res.data || []
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
  }
}

const resetSearch = () => {
  searchForm.username = ''
  searchForm.realName = ''
  searchForm.status = null
  pagination.current = 1
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增用户'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  dialogTitle.value = '编辑用户'
  form.userId = row.userId
  form.username = row.username
  form.realName = row.realName
  form.phone = row.phone
  form.email = row.email
  form.userType = row.userType
  form.status = row.status
  form.roleIds = [] // 编辑时不处理角色

  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (form.userId) {
        // 更新或分配角色
        if (dialogTitle.value === '分配角色') {
          // 分配角色
          const res = await assignRoles(form.userId, form.roleIds)
          if (res.code === 200) {
            ElMessage.success('分配角色成功')
            dialogVisible.value = false
            loadData()
          } else {
            ElMessage.error(res.message || '分配角色失败')
          }
        } else {
          // 编辑用户
          const updateData = {
            realName: form.realName,
            phone: form.phone,
            email: form.email,
            status: form.status
          }
          // 编辑时不更新角色
          const res = await updateUser(form.userId, updateData)
          if (res.code === 200) {
            ElMessage.success('更新成功')
            dialogVisible.value = false
            loadData()
          } else {
            ElMessage.error(res.message || '更新失败')
          }
        }
      } else {
        // 新增
        const createData = {
          username: form.username,
          password: form.password,
          realName: form.realName,
          phone: form.phone,
          email: form.email,
          userType: form.userType,
          status: form.status
        }
        // 只有系统用户才分配角色
        if (form.userType === 1) {
          createData.roleIds = form.roleIds
        }
        const res = await createUser(createData)
        if (res.code === 200) {
          ElMessage.success('创建成功')
          dialogVisible.value = false
          loadData()
        } else {
          ElMessage.error(res.message || '创建失败')
        }
      }
    } catch (error) {
      console.error('提交失败:', error)
      ElMessage.error('操作失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该用户吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteUser(row.userId)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handleResetPassword = (row) => {
  resetPasswordForm.userId = row.userId
  resetPasswordForm.newPassword = ''
  resetPasswordForm.confirmPassword = ''
  resetPasswordDialogVisible.value = true
}

const handleResetPasswordSubmit = async () => {
  if (!resetPasswordFormRef.value) return
  await resetPasswordFormRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      const res = await resetPassword(resetPasswordForm.userId, {
        newPassword: resetPasswordForm.newPassword
      })
      if (res.code === 200) {
        ElMessage.success('重置密码成功')
        resetPasswordDialogVisible.value = false
      } else {
        ElMessage.error(res.message || '重置密码失败')
      }
    } catch (error) {
      console.error('重置密码失败:', error)
      ElMessage.error('重置密码失败')
    } finally {
      submitting.value = false
    }
  })
}

const handleAssignRoles = async (row) => {
  form.userId = row.userId
  form.realName = row.realName
  form.status = row.status

  // 获取用户角色
  try {
    const res = await getUserRoles(row.userId)
    if (res.code === 200) {
      form.roleIds = res.data || []
    }
  } catch (error) {
    console.error('获取用户角色失败:', error)
  }

  dialogTitle.value = '分配角色'
  dialogVisible.value = true
}

const resetForm = () => {
  form.userId = null
  form.username = ''
  form.password = ''
  form.realName = ''
  form.phone = ''
  form.email = ''
  form.userType = 1
  form.status = 1
  form.roleIds = []
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

</script>

<style scoped>
.user-list {
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

