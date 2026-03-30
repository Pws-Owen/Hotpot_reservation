<template>
  <div class="common-layout">
    <el-container class="layout-container">
      <AdminSidebar />
      <el-container class="right-container">
        <AdminHeader />
        <div class="tags-bar">
          <div class="tags-container">
            <el-tag
              v-for="tag in visitedTags"
              :key="tag.path"
              :closable="tag.path !== '/dashboard'"
              :type="tag.path === currentPath ? 'primary' : 'info'"
              @close="handleCloseTag(tag)"
              @click="handleTagClick(tag)"
              class="page-tag"
            >
              {{ tag.title }}
            </el-tag>
          </div>
        </div>
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AdminSidebar from './AdminSidebar.vue'
import AdminHeader from './AdminHeader.vue'

const route = useRoute()
const router = useRouter()

// 访问过的页面标签
const visitedTags = ref([
  { path: '/dashboard', title: '首页' }
])

// 当前路径
const currentPath = computed(() => route.path)

// 监听路由变化，添加新标签
watch(() => route.path, (newPath) => {
  if (newPath && route.meta.title) {
    const title = route.meta.title
    // 检查是否已存在该标签
    const existingTag = visitedTags.value.find(tag => tag.path === newPath)
    if (!existingTag) {
      // 确保首页始终在第一位
      if (newPath === '/dashboard') {
        visitedTags.value.unshift({
          path: newPath,
          title: title
        })
      } else {
        visitedTags.value.push({
          path: newPath,
          title: title
        })
      }
    }
  }
}, { immediate: true })

// 关闭标签
const handleCloseTag = (tag) => {
  // 首页标签不能关闭
  if (tag.path === '/dashboard') {
    return
  }
  
  const index = visitedTags.value.findIndex(t => t.path === tag.path)
  if (index > -1) {
    visitedTags.value.splice(index, 1)
    
    // 如果关闭的是当前页面，跳转到最后一个标签或首页
    if (tag.path === currentPath.value) {
      if (visitedTags.value.length > 0) {
        const lastTag = visitedTags.value[visitedTags.value.length - 1]
        router.push(lastTag.path)
      } else {
        router.push('/dashboard')
      }
    }
  }
}

// 点击标签跳转
const handleTagClick = (tag) => {
  if (tag.path !== currentPath.value) {
    router.push(tag.path)
  }
}
</script>

<style scoped>
.common-layout {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  margin: 0;
  padding: 0;
  overflow: hidden;
}

.layout-container {
  height: 100vh;
  width: 100%;
}

.right-container {
  height: 100vh;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.tags-bar {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 8px 20px;
  height: 50px;
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.tags-container {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: nowrap;
  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-width: thin;
  scrollbar-color: #c0c4cc transparent;
}

.tags-container::-webkit-scrollbar {
  height: 4px;
}

.tags-container::-webkit-scrollbar-track {
  background: transparent;
}

.tags-container::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 2px;
}

.tags-container::-webkit-scrollbar-thumb:hover {
  background: #a0a4a8;
}

.page-tag {
  cursor: pointer;
  user-select: none;
  white-space: nowrap;
  flex-shrink: 0;
  height: 36px;
  line-height: 36px;
  padding: 0 16px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 6px;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.page-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.main-content {
  background-color: #f0f2f5;
  padding: 20px;
  flex: 1;
  overflow-y: auto;
  height: 0;
  min-height: 0;
}
</style>

