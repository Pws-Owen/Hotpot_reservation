<template>
  <div class="reservation-success">
    <CustomerHeader :show-search="false" />
    
    <div class="success-content">
      <el-result
        icon="success"
        title="预约成功！"
        sub-title="您的预约已提交，我们会尽快为您安排座位"
      >
        <template #extra>
          <div class="reservation-info" v-if="reservationData">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="预约日期">
                {{ reservationData.date }}
              </el-descriptions-item>
              <el-descriptions-item label="预约时间">
                {{ reservationData.time }}
              </el-descriptions-item>
              <el-descriptions-item label="用餐人数">
                {{ reservationData.peopleCount }}人
              </el-descriptions-item>
              <el-descriptions-item label="联系方式">
                {{ reservationData.phone }}
              </el-descriptions-item>
              <el-descriptions-item label="预约状态">
                <el-tag type="warning">待确认</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </div>
          
          <div class="action-buttons">
            <el-button type="primary" @click="goToMyReservations">
              查看我的预约
            </el-button>
            <el-button @click="goToHome">返回首页</el-button>
          </div>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import CustomerHeader from '@/components/customer/CustomerHeader.vue'

const route = useRoute()
const router = useRouter()
const reservationData = ref(null)

onMounted(() => {
  // 从路由参数或query中获取预约信息
  if (route.params.data) {
    reservationData.value = JSON.parse(decodeURIComponent(route.params.data))
  } else if (route.query.data) {
    reservationData.value = JSON.parse(decodeURIComponent(route.query.data))
  }
})

const goToMyReservations = () => {
  router.push('/customer/reservation/list')
}

const goToHome = () => {
  router.push('/customer/home')
}
</script>

<style scoped lang="scss">
.reservation-success {
  min-height: 100vh;
  background: #f5f5f5;
}

.success-content {
  max-width: 800px;
  margin: 40px auto;
  padding: 20px;
  
  .reservation-info {
    margin: 30px 0;
    background: #fff;
    padding: 20px;
    border-radius: 8px;
  }
  
  .action-buttons {
    display: flex;
    gap: 16px;
    justify-content: center;
    margin-top: 30px;
  }
}
</style>

