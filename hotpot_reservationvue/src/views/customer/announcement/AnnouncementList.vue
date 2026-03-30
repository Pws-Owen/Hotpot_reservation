<template>
  <div class="announcement-page">
    <!-- 通用头部：顶部栏 + 导航 -->
    <CustomerHeader :show-search="false" />

    <div class="announcement-container">
      <!-- 头部区域：标题+返回按钮 -->
      <div class="announcement-header">
        <div class="header-left">
          <h1 class="announcement-title">公告通知</h1>
          <p class="announcement-subtitle">最新公告 · 重要通知 · 活动信息</p>
        </div>
        <div class="header-actions">
          <!-- 返回首页按钮 -->
          <el-button 
            type="primary" 
            @click="$router.push('/customer/home')"
            class="back-btn"
          >
            <el-icon><ArrowLeft /></el-icon>
            返回首页
          </el-button>
        </div>
      </div>
      
    <el-card>
      <template #header>
        <span>公告列表</span>
      </template>
      
      <el-timeline v-loading="loading">
        <el-timeline-item
          v-for="item in announcements"
          :key="item.id"
          :timestamp="item.createTime"
          placement="top"
        >
          <el-card>
            <h4>{{ item.title }}</h4>
            <p style="white-space: pre-wrap;">{{ item.content }}</p>
            <div class="meta" v-if="item.author">
              <span>发布人：{{ item.author }}</span>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
      
      <el-empty v-if="!loading && announcements.length === 0" description="暂无公告" />
    </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { getAnnouncementPage } from '@/api/announcement'
import CustomerHeader from '@/components/customer/CustomerHeader.vue'

const announcements = ref([])
const loading = ref(false)

onMounted(() => {
  loadAnnouncements()
})

const loadAnnouncements = async () => {
  loading.value = true
  try {
    const res = await getAnnouncementPage({
      current: 1,
      size: 100,
      status: 1 // 只查询已发布的公告
    })
    if (res.code === 200 && res.data) {
      announcements.value = (res.data.records || []).map(item => ({
        id: item.announcementId,
        title: item.title,
        content: item.content,
        author: item.author || '管理员',
        createTime: item.createTime
      }))
    }
  } catch (error) {
    console.error('加载公告列表失败:', error)
    ElMessage.error('加载公告列表失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.announcement-page {
  background: #f5f5f5;
  min-height: 100vh;
}

.announcement-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  background-color: #f8f8f8;
  min-height: calc(100vh - 100px);
}

// 头部样式（与我的订单保持一致）
.announcement-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 2px solid #ffe7e7;
  margin-bottom: 20px;

  .header-left {
    .announcement-title {
      font-size: 28px;
      font-weight: 600;
      color: #ff4d4f;
      margin: 0 0 5px 0;
    }

    .announcement-subtitle {
      font-size: 14px;
      color: #999;
      margin: 0;
    }
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 15px;

    .back-btn {
      display: flex;
      align-items: center;
      gap: 5px;
      padding: 8px 16px;
    }
  }
}

.meta {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
}

// 响应式适配
@media (max-width: 768px) {
  .announcement-container {
    padding: 10px;
  }
}
</style>

