<template>
  <div class="role-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span></span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增角色
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="角色名称">
          <el-input v-model="searchForm.roleName" placeholder="请输入角色名称" clearable />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="searchForm.roleCode" placeholder="请输入角色编码" clearable />
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
        border
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="roleId" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
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
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="success" size="small" @click="handleViewUsers(scope.row)">查看用户</el-button>
            <el-button type="info" size="small" @click="handleAssignPermissions(scope.row)">分配权限</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
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
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码" :disabled="!!form.roleId" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入角色描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      title="分配权限"
      width="600px"
    >
      <el-tree
        ref="permissionTreeRef"
        :data="permissionTree"
        :props="{ children: 'children', label: 'permissionName' }"
        show-checkbox
        node-key="permissionId"
        :default-checked-keys="checkedPermissionIds"
        :default-expand-all="true"
      />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignPermissionsSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看用户对话框 -->
    <el-dialog
      v-model="userDialogVisible"
      :title="`${currentRoleName} - 用户列表`"
      width="800px"
    >
      <el-table :data="roleUsers" border v-loading="loading" style="width: 100%">
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userType" label="用户类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.userType === 1 ? 'warning' : 'success'">
              {{ scope.row.userType === 1 ? '系统用户' : '客户' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="roleUsers.length === 0" style="text-align: center; padding: 40px; color: #909399;">
        该角色下暂无用户
      </div>
      <template #footer>
        <el-button @click="userDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getRolePage, createRole, updateRole, deleteRole, assignPermissions, getRolePermissions, getRoleUsers } from '@/api/system/role'
import { getAllPermissions } from '@/api/system/permission'
import { getUserById } from '@/api/system/user'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])
const permissionTree = ref([])
const checkedPermissionIds = ref([])
const permissionTreeRef = ref(null)

const searchForm = reactive({
  roleName: '',
  roleCode: '',
  status: null
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const formRef = ref(null)
const form = reactive({
  roleId: null,
  roleName: '',
  roleCode: '',
  description: '',
  status: 1
})

const permissionDialogVisible = ref(false)
const currentRoleId = ref(null)

const userDialogVisible = ref(false)
const roleUsers = ref([])
const currentRoleName = ref('')

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '角色编码只能包含大写字母和下划线', trigger: 'blur' }
  ]
}

onMounted(() => {
  loadData()
  loadPermissions()
})

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    }
    const res = await getRolePage(params)
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取角色列表失败')
    }
  } catch (error) {
    console.error('获取角色列表失败:', error)
    ElMessage.error('获取角色列表失败')
  } finally {
    loading.value = false
  }
}

const loadPermissions = async () => {
  try {
    const res = await getAllPermissions()
    if (res.code === 200) {
      // 构建树形结构
      const permissions = res.data || []
      const tree = buildPermissionTree(permissions)
      permissionTree.value = tree
    }
  } catch (error) {
    console.error('获取权限列表失败:', error)
  }
}

const buildPermissionTree = (permissions) => {
  const map = new Map()
  const tree = []

  // 创建映射
  permissions.forEach(permission => {
    map.set(permission.permissionId, { ...permission, children: [] })
  })

  // 构建树
  permissions.forEach(permission => {
    const node = map.get(permission.permissionId)
    if (permission.parentId === 0 || !permission.parentId) {
      tree.push(node)
    } else {
      const parent = map.get(permission.parentId)
      if (parent) {
        parent.children.push(node)
      }
    }
  })

  return tree
}

const resetSearch = () => {
  searchForm.roleName = ''
  searchForm.roleCode = ''
  searchForm.status = null
  pagination.current = 1
  loadData()
}

const handleAdd = () => {
  dialogTitle.value = '新增角色'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑角色'
  form.roleId = row.roleId
  form.roleName = row.roleName
  form.roleCode = row.roleCode
  form.description = row.description
  form.status = row.status
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      if (form.roleId) {
        // 更新
        const res = await updateRole(form.roleId, {
          roleName: form.roleName,
          roleCode: form.roleCode,
          description: form.description,
          status: form.status
        })
        if (res.code === 200) {
          ElMessage.success('更新成功')
          dialogVisible.value = false
          loadData()
        } else {
          ElMessage.error(res.message || '更新失败')
        }
      } else {
        // 新增
        const res = await createRole({
          roleName: form.roleName,
          roleCode: form.roleCode,
          description: form.description,
          status: form.status
        })
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
    await ElMessageBox.confirm('确认要删除此角色吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteRole(row.roleId)
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

const handleAssignPermissions = async (row) => {
  currentRoleId.value = row.roleId
  
  // 获取角色已有权限
  try {
    const res = await getRolePermissions(row.roleId)
    if (res.code === 200) {
      checkedPermissionIds.value = res.data || []
    }
  } catch (error) {
    console.error('获取角色权限失败:', error)
    checkedPermissionIds.value = []
  }

  permissionDialogVisible.value = true
}

const handleAssignPermissionsSubmit = async () => {
  if (!permissionTreeRef.value) return

  submitting.value = true
  try {
    const checkedKeys = permissionTreeRef.value.getCheckedKeys()
    const halfCheckedKeys = permissionTreeRef.value.getHalfCheckedKeys()
    const permissionIds = [...checkedKeys, ...halfCheckedKeys]

    const res = await assignPermissions(currentRoleId.value, permissionIds)
    if (res.code === 200) {
      ElMessage.success('分配权限成功')
      permissionDialogVisible.value = false
    } else {
      ElMessage.error(res.message || '分配权限失败')
    }
  } catch (error) {
    console.error('分配权限失败:', error)
    ElMessage.error('分配权限失败')
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  form.roleId = null
  form.roleName = ''
  form.roleCode = ''
  form.description = ''
  form.status = 1
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const handleViewUsers = async (row) => {
  currentRoleId.value = row.roleId
  currentRoleName.value = row.roleName
  roleUsers.value = []
  userDialogVisible.value = true
  
  loading.value = true
  try {
    // 获取该角色的用户ID列表
    const res = await getRoleUsers(row.roleId)
    if (res.code === 200 && res.data && res.data.length > 0) {
      // 根据用户ID列表获取用户详细信息（批量获取）
      const userIds = res.data
      const userPromises = userIds.map(userId => getUserById(userId).catch(() => null))
      const userResults = await Promise.all(userPromises)
      
      roleUsers.value = userResults
        .filter(result => result && result.code === 200 && result.data)
        .map(result => result.data)
    } else {
      roleUsers.value = []
    }
  } catch (error) {
    console.error('获取角色用户失败:', error)
    ElMessage.error('获取角色用户失败')
    roleUsers.value = []
  } finally {
    loading.value = false
  }
}

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return new Date(dateTime).toLocaleString('zh-CN')
}
</script>

<style scoped>
.role-list {
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
