<template>
  <div class="login-container">
    <!-- 左侧品牌展示 -->
    <div class="brand-section">
      <div class="brand-content">
        <div class="brand-logo">
          <el-icon :size="80"><Setting /></el-icon>
        </div>
        <h1>管理系统</h1>
        <p>火锅店预约系统后台管理平台</p>
        <div class="features">
          <div class="feature-item">
            <el-icon><Monitor /></el-icon>
            <span>数据监控</span>
          </div>
          <div class="feature-item">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据分析</span>
          </div>
          <div class="feature-item">
            <el-icon><Operation /></el-icon>
            <span>业务管理</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 右侧登录表单 -->
    <div class="login-section">
      <div class="login-card">
        <div class="card-header">
          <h2>管理员登录</h2>
          <p>请使用管理员账号登录系统</p>
        </div>
        
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="rules"
          class="login-form"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleLogin"
              class="login-button"
            >
              登录
            </el-button>
          </el-form-item>
          
          <div class="login-footer">
            <el-link type="primary" @click="showForgotPassword = true" class="forgot-link">
              忘记密码？
            </el-link>
          </div>
        </el-form>
      </div>
    </div>
    
    <!-- 忘记密码对话框 -->
    <el-dialog v-model="showForgotPassword" title="忘记密码" width="400px">
      <el-form :model="forgotForm" label-width="100px">
        <el-form-item label="用户名">
          <el-input v-model="forgotForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="forgotForm.email" placeholder="请输入注册邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForgotPassword = false">取消</el-button>
        <el-button type="primary" @click="handleForgotPassword">发送重置链接</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Lock, Setting, Monitor, DataAnalysis, Operation } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginFormRef = ref(null)
const loading = ref(false)
const showForgotPassword = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const forgotForm = reactive({
  username: '',
  email: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const success = await userStore.login(loginForm, false)
        if (success) {
          if (!userStore.isAdmin) {
            ElMessage.warning('该账号没有管理权限，请使用管理员账号登录')
            userStore.logout()
            loading.value = false
            return
          }
          
          ElMessage.success('登录成功')
          const redirect = route.query.redirect || '/dashboard'
          await router.push(redirect)
        }
      } catch (error) {
        console.error('登录失败:', error)
        ElMessage.error(error.message || '登录失败，请检查用户名和密码')
      } finally {
        loading.value = false
      }
    }
  })
}

const handleForgotPassword = () => {
  // TODO: 实现忘记密码功能
  ElMessage.info('密码重置功能待实现')
  showForgotPassword.value = false
}
</script>

<style scoped lang="scss">
.login-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  display: flex;
  overflow: hidden;
}

// 左侧品牌展示区域
.brand-section {
  flex: 1;
  background: url('/image/banner03.jpg') center center / cover no-repeat;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  position: relative;
  overflow: hidden;
}

.brand-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 77, 79, 0.75) 0%, rgba(255, 120, 117, 0.7) 50%, rgba(255, 107, 107, 0.75) 100%);
  pointer-events: none;
  z-index: 1;
}

.brand-section::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(255, 255, 255, 0.08) 0%, transparent 50%);
  pointer-events: none;
  z-index: 2;
  animation: float 20s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
  }
}

.brand-content {
  max-width: 500px;
  z-index: 3;
  position: relative;
  text-align: center;
  animation: fadeInUp 0.8s ease-out;
}

.brand-logo {
  margin-bottom: 30px;
  animation: fadeInUp 0.8s ease-out 0.2s both;
  
  .el-icon {
    background: rgba(255, 255, 255, 0.2);
    padding: 20px;
    border-radius: 20px;
    backdrop-filter: blur(10px);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  }
}

.brand-content h1 {
  font-size: 48px;
  margin-bottom: 16px;
  line-height: 1.2;
  font-weight: 800;
  text-shadow: 
    0 2px 4px rgba(0, 0, 0, 0.5),
    0 4px 12px rgba(0, 0, 0, 0.3),
    0 0 20px rgba(0, 0, 0, 0.2);
  animation: fadeInUp 0.8s ease-out 0.3s both;
  letter-spacing: 2px;
}

.brand-content > p {
  font-size: 18px;
  margin-bottom: 50px;
  opacity: 0.95;
  text-shadow: 
    0 1px 3px rgba(0, 0, 0, 0.5),
    0 2px 8px rgba(0, 0, 0, 0.3);
  animation: fadeInUp 0.8s ease-out 0.4s both;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: 40px;
}

.feature-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 15px;
  font-size: 16px;
  padding: 14px 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  animation: fadeInUp 0.8s ease-out both;
  
  &:nth-child(1) {
    animation-delay: 0.5s;
  }
  
  &:nth-child(2) {
    animation-delay: 0.6s;
  }
  
  &:nth-child(3) {
    animation-delay: 0.7s;
  }
  
  &:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: translateX(8px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

.feature-item .el-icon {
  font-size: 24px;
  color: #fff;
  background: rgba(255, 255, 255, 0.25);
  padding: 10px;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.feature-item:hover .el-icon {
  background: rgba(255, 255, 255, 0.35);
  transform: scale(1.1) rotate(5deg);
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 右侧登录表单区域
.login-section {
  flex: 0 0 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: linear-gradient(to bottom, #fafafa 0%, #ffffff 100%);
  overflow-y: auto;
  position: relative;
}

.login-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 10% 20%, rgba(79, 195, 247, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 90% 80%, rgba(41, 182, 246, 0.05) 0%, transparent 50%);
  pointer-events: none;
}

.login-card {
  background: white;
  padding: 50px 40px;
  border-radius: 24px;
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.08),
    0 0 0 1px rgba(79, 195, 247, 0.05);
  width: 100%;
  max-width: 420px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
  animation: slideInRight 0.6s ease-out;
}

.login-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 24px;
  padding: 2px;
  background: linear-gradient(135deg, rgba(79, 195, 247, 0.1), rgba(41, 182, 246, 0.1));
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask-composite: exclude;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.login-card:hover {
  transform: translateY(-4px);
  box-shadow: 
    0 25px 70px rgba(0, 0, 0, 0.12),
    0 0 0 1px rgba(79, 195, 247, 0.1);
}

.login-card:hover::before {
  opacity: 1;
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.card-header {
  text-align: center;
  margin-bottom: 35px;
}

.card-header h2 {
  font-size: 32px;
  margin: 0 0 12px 0;
  color: #303133;
  font-weight: 700;
  background: linear-gradient(135deg, #4FC3F7 0%, #29B6F6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1px;
}

.card-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.login-form {
  .el-form-item {
    margin-bottom: 24px;
  }
}

// 输入框样式优化
.login-card :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #e4e7ed;
  background: #fafafa;
}

.login-card :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #c0c4cc;
  background: #fff;
}

.login-card :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(79, 195, 247, 0.15), 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #4FC3F7;
  background: #fff;
  transform: translateY(-1px);
}

.login-card :deep(.el-input__inner) {
  font-size: 15px;
  color: #303133;
}

.login-card :deep(.el-input__prefix) {
  color: #909399;
  transition: color 0.3s ease;
}

.login-card :deep(.el-input__wrapper.is-focus .el-input__prefix) {
  color: #4FC3F7;
}

.login-button {
  width: 100%;
  height: 50px;
  font-size: 17px;
  font-weight: 600;
  background: linear-gradient(135deg, #4FC3F7 0%, #29B6F6 100%);
  border: none;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(79, 195, 247, 0.3);
  letter-spacing: 1px;
  position: relative;
  overflow: hidden;
}

.login-button::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  transform: translate(-50%, -50%);
  transition: width 0.6s, height 0.6s;
}

.login-button:hover {
  background: linear-gradient(135deg, #29B6F6 0%, #4FC3F7 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(79, 195, 247, 0.5);
}

.login-button:hover::before {
  width: 300px;
  height: 300px;
}

.login-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(79, 195, 247, 0.4);
}

.login-footer {
  width: 100%;
  text-align: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.forgot-link {
  color: #4FC3F7;
  font-size: 14px;
  transition: color 0.3s ease;
  
  &:hover {
    color: #29B6F6;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .login-container {
    flex-direction: column;
  }
  
  .brand-section {
    flex: 0 0 200px;
    padding: 20px;
  }
  
  .brand-content h1 {
    font-size: 32px;
  }
  
  .brand-content > p {
    font-size: 14px;
    margin-bottom: 20px;
  }
  
  .features {
    display: none;
  }
  
  .login-section {
    flex: 1;
    padding: 20px;
  }
  
  .login-card {
    padding: 30px 20px;
  }
}
</style>

