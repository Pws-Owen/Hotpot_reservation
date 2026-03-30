<template>
  <div class="permission-tree">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>权限管理</span>
          <div>
            <el-button type="primary" @click="handleAdd">新增权限</el-button>
            <el-button @click="handleExpandAll">展开全部</el-button>
            <el-button @click="handleCollapseAll">折叠全部</el-button>
          </div>
        </div>
      </template>
      
      <el-tree
        :data="permissionTree"
        :props="treeProps"
        :default-expand-all="defaultExpandAll"
        node-key="id"
        :expand-on-click-node="false"
      >
        <template #default="{ node, data }">
          <div class="tree-node">
            <span class="node-label">{{ data.permissionName }}</span>
            <span class="node-code">({{ data.permissionCode }})</span>
            <span class="node-type">
              <el-tag :type="data.permissionType === 1 ? 'primary' : 'success'" size="small">
                {{ data.permissionType === 1 ? '菜单' : '按钮' }}
              </el-tag>
            </span>
            <span class="node-actions">
              <el-button type="primary" size="small" @click="handleAddChild(data)">新增子权限</el-button>
              <el-button type="success" size="small" @click="handleEdit(data)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(data)">删除</el-button>
            </span>
          </div>
        </template>
      </el-tree>
    </el-card>

    <el-dialog v-model="permissionDialogVisible" :title="permissionDialogTitle" width="600px">
      <el-form :model="currentPermission" label-width="100px">
        <el-form-item label="权限名称">
          <el-input v-model="currentPermission.permissionName" />
        </el-form-item>
        <el-form-item label="权限编码">
          <el-input v-model="currentPermission.permissionCode" />
        </el-form-item>
        <el-form-item label="权限类型">
          <el-radio-group v-model="currentPermission.permissionType">
            <el-radio :label="1">菜单</el-radio>
            <el-radio :label="2">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="父权限" v-if="currentPermission.permissionType === 1">
          <el-select v-model="currentPermission.parentId" placeholder="请选择父权限" clearable>
            <el-option
              v-for="item in menuPermissions"
              :key="item.id"
              :label="item.permissionName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="路由路径" v-if="currentPermission.permissionType === 1">
          <el-input v-model="currentPermission.path" />
        </el-form-item>
        <el-form-item label="组件路径" v-if="currentPermission.permissionType === 1">
          <el-input v-model="currentPermission.component" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="currentPermission.icon" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="currentPermission.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="currentPermission.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="currentPermission.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const defaultExpandAll = ref(false)
const permissionDialogVisible = ref(false)
const permissionDialogTitle = ref('新增权限')
const currentPermission = ref({})

const treeProps = {
  children: 'children',
  label: 'permissionName'
}

const permissionTree = ref([
  {
    id: 1,
    permissionName: '系统管理',
    permissionCode: 'system',
    permissionType: 1,
    parentId: 0,
    path: '/system',
    component: 'Layout',
    icon: 'system',
    sortOrder: 1,
    status: 1,
    children: [
      {
        id: 2,
        permissionName: '用户管理',
        permissionCode: 'user:list',
        permissionType: 1,
        parentId: 1,
        path: '/system/user',
        component: 'system/user/List',
        icon: 'user',
        sortOrder: 1,
        status: 1
      },
      {
        id: 3,
        permissionName: '角色管理',
        permissionCode: 'role:list',
        permissionType: 1,
        parentId: 1,
        path: '/system/user/role',
        component: 'system/user/Role',
        icon: 'user-filled',
        sortOrder: 2,
        status: 1
      }
    ]
  },
  {
    id: 10,
    permissionName: '业务管理',
    permissionCode: 'business',
    permissionType: 1,
    parentId: 0,
    path: '/business',
    component: 'Layout',
    icon: 'shopping-bag',
    sortOrder: 2,
    status: 1,
    children: [
      {
        id: 11,
        permissionName: '预约管理',
        permissionCode: 'reservation:list',
        permissionType: 1,
        parentId: 10,
        path: '/business/reservation',
        component: 'business/reservation/List',
        icon: 'calendar',
        sortOrder: 1,
        status: 1
      }
    ]
  }
])

const menuPermissions = ref([
  { id: 0, permissionName: '根节点' },
  { id: 1, permissionName: '系统管理' },
  { id: 10, permissionName: '业务管理' }
])

onMounted(() => {
  loadPermissionTree()
})

const loadPermissionTree = () => {
  // 从API加载权限树
}

const handleAdd = () => {
  currentPermission.value = {
    permissionName: '',
    permissionCode: '',
    permissionType: 1,
    parentId: 0,
    path: '',
    component: '',
    icon: '',
    sortOrder: 0,
    status: 1,
    description: ''
  }
  permissionDialogTitle.value = '新增权限'
  permissionDialogVisible.value = true
}

const handleAddChild = (data) => {
  currentPermission.value = {
    permissionName: '',
    permissionCode: '',
    permissionType: 1,
    parentId: data.id,
    path: '',
    component: '',
    icon: '',
    sortOrder: 0,
    status: 1,
    description: ''
  }
  permissionDialogTitle.value = '新增子权限'
  permissionDialogVisible.value = true
}

const handleEdit = (data) => {
  currentPermission.value = { ...data }
  permissionDialogTitle.value = '编辑权限'
  permissionDialogVisible.value = true
}

const handleDelete = (data) => {
  ElMessageBox.confirm('确定要删除这条权限吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('删除成功')
    loadPermissionTree()
  })
}

const handleSave = () => {
  ElMessage.success('保存成功')
  permissionDialogVisible.value = false
  loadPermissionTree()
}

const handleExpandAll = () => {
  defaultExpandAll.value = true
}

const handleCollapseAll = () => {
  defaultExpandAll.value = false
}
</script>

<style scoped>
.permission-tree {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tree-node {
  display: flex;
  align-items: center;
  flex: 1;
  font-size: 14px;
}

.node-label {
  font-weight: 500;
  margin-right: 8px;
}

.node-code {
  color: #909399;
  margin-right: 8px;
  font-size: 12px;
}

.node-type {
  margin-right: 8px;
}

.node-actions {
  margin-left: auto;
}
</style>

