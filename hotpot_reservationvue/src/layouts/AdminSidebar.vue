<template>
  <el-aside :width="isCollapse ? '64px' : '200px'" class="admin-sidebar">
    <div class="logo">
      <h2 v-if="!isCollapse">火锅店预约管理系统</h2>
    </div>
    <el-menu
      :default-active="activeMenu"
      router
      class="sidebar-menu"
      :collapse="isCollapse"
      :unique-opened="true"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
    >
      <!-- 首页 - 所有角色可见 -->
      <el-menu-item index="/dashboard">
        <el-icon><DataBoard /></el-icon>
        <span>首页</span>
      </el-menu-item>

      <!-- 核心功能模块 - 一级菜单 -->
      <!-- 1. 预约管理 -->
      <el-menu-item v-if="hasPermission('reservation')" index="/business/reservation">
        <el-icon><Calendar /></el-icon>
        <span>预约管理</span>
      </el-menu-item>

      <!-- 2. 餐桌管理 -->
      <el-menu-item v-if="hasPermission('table')" index="/business/table">
        <el-icon><Grid /></el-icon>
        <span>餐桌管理</span>
      </el-menu-item>

      <!-- 3. 订单管理 -->
      <el-menu-item v-if="hasPermission('order')" index="/business/order">
        <el-icon><ShoppingBag /></el-icon>
        <span>订单管理</span>
      </el-menu-item>

      <!-- 4. 菜单分类 - 仅管理员 -->
      <el-menu-item v-if="hasPermission('menu')" index="/business/menu/category">
        <el-icon><Folder /></el-icon>
        <span>菜单分类</span>
      </el-menu-item>

      <!-- 5. 菜品管理 - 仅管理员 -->
      <el-menu-item v-if="hasPermission('menu')" index="/business/menu/item">
        <el-icon><Menu /></el-icon>
        <span>菜品管理</span>
      </el-menu-item>

      <!-- 6. 客户管理 -->
      <el-menu-item v-if="hasPermission('customer')" index="/business/customer">
        <el-icon><User /></el-icon>
        <span>客户管理</span>
      </el-menu-item>

      <!-- 7. 支付记录 -->
      <el-menu-item v-if="hasPermission('payment')" index="/business/payment">
        <el-icon><Money /></el-icon>
        <span>支付记录</span>
      </el-menu-item>

      <!-- 8. 公告管理 - 仅管理员 -->
      <el-menu-item v-if="hasPermission('announcement')" index="/business/announcement">
        <el-icon><Bell /></el-icon>
        <span>公告管理</span>
      </el-menu-item>

      <!-- 9. 评论管理 - 仅管理员 -->
      <el-menu-item v-if="hasPermission('review')" index="/business/review">
        <el-icon><ChatLineRound /></el-icon>
        <span>评论管理</span>
      </el-menu-item>

      <!-- 9. 报表统计 - 下拉菜单 -->
      <el-sub-menu v-if="hasPermission('report')" index="report">
        <template #title>
          <el-icon><DataAnalysis /></el-icon>
          <span>报表统计</span>
        </template>
        <el-menu-item index="/report/sales">
          <span>销售报表</span>
        </el-menu-item>
        <el-menu-item index="/report/customer">
          <span>客户分析</span>
        </el-menu-item>
        <el-menu-item index="/report/inventory">
          <span>库存报表</span>
        </el-menu-item>
      </el-sub-menu>

      <!-- 系统设置 - 仅管理员可见 -->
      <el-sub-menu v-if="isAdmin" index="system">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>系统设置</span>
        </template>
        <el-menu-item index="/system/user/user-role">
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/system/user">
          <span>角色管理</span>
        </el-menu-item>
        <el-menu-item index="/system/user/role">
          <span>权限设置</span>
        </el-menu-item>
        <el-menu-item index="/system/log/operation">
          <span>操作日志</span>
        </el-menu-item>
      </el-sub-menu>
    </el-menu>
    <div class="sidebar-footer">
      <el-button
        :icon="isCollapse ? Expand : Fold"
        circle
        @click="toggleCollapse"
        class="collapse-btn"
        title="收缩菜单"
      />
    </div>
  </el-aside>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { USER_ROLE } from '@/constants'
import { 
  DataBoard, 
  Calendar, 
  Grid, 
  Menu, 
  ShoppingCart,
  User,
  Money,
  Bell,
  ChatLineRound,
  DataAnalysis,
  Setting,
  ShoppingBag,
  Fold,
  Expand,
  Folder
} from '@element-plus/icons-vue'

// 侧边栏折叠状态
const isCollapse = ref(false)

// 切换折叠状态
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const route = useRoute()
const userStore = useUserStore()
const activeMenu = computed(() => route.path)

// 当前用户角色
const currentRole = computed(() => {
  const roles = userStore.roles || []
  if (roles.includes(USER_ROLE.ADMIN)) return 'admin'
  if (roles.includes(USER_ROLE.RECEPTIONIST)) return 'receptionist'
  return 'user'
})

const isAdmin = computed(() => currentRole.value === 'admin')

// 角色显示名称
const roleName = computed(() => {
  switch(currentRole.value) {
    case 'admin': return '管理员'
    case 'receptionist': return '前台'
    default: return '用户'
  }
})

// 权限检查（根据数据库权限表设计）
const hasPermission = (permissionKey) => {
  // 如果是管理员，拥有所有权限
  if (isAdmin.value) return true
  
  // 前台服务员权限配置（基于数据库中的 sys_permission 和 sys_role_permission 表）
  const receptionistPermissions = {
    // 业务核心功能
    reservation: true,    // 预约管理
    table: true,         // 餐桌管理
    order: true,         // 订单管理
    payment: true,       // 支付记录
    customer: true,      // 客户管理
    report: true,        // 报表统计
    // 以下功能前台不可见
    menu: false,         // 菜品管理（仅管理员）
    review: false,       // 评论管理（仅管理员）
    announcement: false   // 公告管理（仅管理员）
  }
  
  return receptionistPermissions[permissionKey] || false
}
</script>

<style scoped>
.admin-sidebar {
  background-color: #304156;
  overflow: hidden;
  height: 100vh;
  flex-shrink: 0;
  position: relative;
}

.logo {
  height: 70px;
  padding: 10px;
  text-align: center;
  color: #fff;
  background-color: #2b3a4a;
  border-bottom: 1px solid #1f2d3d;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.logo h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
  line-height: 1.4;
}

.user-role {
  font-size: 12px;
  color: #8a9ba8;
  margin-top: 4px;
}

.sidebar-menu {
  border-right: none;
  height: calc(100vh - 70px - 60px);
  overflow-y: auto;
}

.sidebar-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b3a4a;
  border-top: 1px solid #1f2d3d;
}

.collapse-btn {
  border: none;
  background-color: transparent;
  color: #bfcbd9;
}

.collapse-btn:hover {
  background-color: #304156;
  color: #409EFF;
}

.highlight-badge {
  display: inline-block;
  padding: 2px 6px;
  margin-left: 8px;
  font-size: 10px;
  line-height: 1;
  color: #fff;
  background-color: #f56c6c;
  border-radius: 10px;
  font-weight: 500;
}
</style>
