<template>
  <div class="reservation-fail">
    <CustomerHeader :show-search="false" />
    
    <div class="fail-content">
      <el-result
        icon="error"
        title="预约失败"
        :sub-title="errorMessage || '预约提交失败，请稍后重试'"
      >
        <template #extra>
          <div class="action-buttons">
            <el-button type="primary" @click="goBack">
              重新预约
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
const errorMessage = ref('')

onMounted(() => {
  // 从路由参数或query中获取错误信息
  if (route.params.message) {
    errorMessage.value = decodeURIComponent(route.params.message)
  } else if (route.query.message) {
    errorMessage.value = decodeURIComponent(route.query.message)
  }
})

const goBack = () => {
  router.push('/customer/reservation')
}

const goToHome = () => {
  router.push('/customer/home')
}
</script>

<style scoped lang="scss">
.reservation-fail {
  min-height: 100vh;
  background: #f5f5f5;
}

.fail-content {
  max-width: 600px;
  margin: 40px auto;
  padding: 20px;
  
  .action-buttons {
    display: flex;
    gap: 16px;
    justify-content: center;
    margin-top: 30px;
  }
}
</style>

