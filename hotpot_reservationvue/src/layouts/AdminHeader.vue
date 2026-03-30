<template>
  <el-header class="admin-header">
    <div class="header-left">
      <span class="page-title">{{ pageTitle }}</span>
    </div>
    <div class="header-right">
      <el-button
        :icon="FullScreen"
        circle
        @click="toggleFullscreen"
        class="fullscreen-btn"
        title="全屏"
      />
      <el-dropdown @command="handleCommand" trigger="click">
        <div class="user-info">
          <el-avatar :size="36" :src="userAvatar">
            <el-icon><User /></el-icon>
          </el-avatar>
          <span class="user-name">{{ userStore.realName || userStore.username }}</span>
          <el-icon class="el-icon--right"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              <span>退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </el-header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import { User, ArrowDown, SwitchButton, FullScreen } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const pageTitle = computed(() => {
  const title = route.meta.title || '管理后台'
  const parentMenu = route.meta.parentMenu
  if (parentMenu) {
    return `${parentMenu} / ${title}`
  }
  return title
})

// 用户头像（从userStore中获取，当头像更新时会自动响应）
const userAvatar = computed(() => {
  return userStore.avatar || ''
})

// 全屏切换
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen().catch(err => {
      console.log('无法进入全屏模式:', err)
    })
  } else {
    document.exitFullscreen().catch(err => {
      console.log('无法退出全屏模式:', err)
    })
  }
}

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      userStore.logout()
      // 跳转到登录页
      router.push('/login').catch(() => {
        // 如果路由跳转失败，使用 window.location 强制跳转
        window.location.href = '/login'
      })
    }).catch(() => {
      // 用户取消，不执行任何操作
    })
  }
}
</script>

<style scoped>
.admin-header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  line-height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
  flex: 1;
}

.page-title {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  height: 100%;
  gap: 12px;
}

.fullscreen-btn {
  border: none;
  background-color: transparent;
  color: #606266;
}

.fullscreen-btn:hover {
  background-color: #f5f7fa;
  color: #409EFF;
}

.header-right .user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 10px;
  border-radius: 4px;
  transition: background-color 0.3s;
  height: 100%;
}

.header-right .user-info:hover {
  background-color: #f5f7fa;
}

.user-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.el-dropdown-menu__item {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>

