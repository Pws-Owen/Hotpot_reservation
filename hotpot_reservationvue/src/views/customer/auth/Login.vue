<template>
  <div class="customer-login-container">
    <!-- 左侧品牌展示 -->
    <div class="brand-section">
      <div class="brand-content">
        <h1>欢迎来到<br>热辣火锅</h1>
        <p>品味正宗川渝风味，享受热情周到服务</p>
        <div class="features">
          <div class="feature-item">
            <el-icon><Food /></el-icon>
            <span>百种新鲜食材</span>
          </div>
          <div class="feature-item">
            <el-icon><Calendar /></el-icon>
            <span>在线预约免排队</span>
          </div>
          <div class="feature-item">
            <el-icon><Discount /></el-icon>
            <span>会员专享优惠</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 右侧登录表单 -->
    <div class="login-section">
      <div class="login-card">
        <h2>用户登录</h2>
        <p>登录后享受更多服务</p>
        
        <!-- 密码登录表单 -->
        <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules">
          <el-form-item prop="username">
            <el-input
              v-model="passwordForm.username"
              placeholder="手机号/用户名"
              size="large"
              :prefix-icon="User"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="passwordForm.password"
              type="password"
              placeholder="密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              @keyup.enter="handlePasswordLogin"
            />
          </el-form-item>
          
          <div class="form-options">
            <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            <a href="#" class="forgot-password" @click.prevent="handleForgotPassword">忘记密码？</a>
          </div>
          
          <el-button
            type="primary"
            size="large"
            class="login-button"
            :loading="loading"
            @click="handlePasswordLogin"
          >
            登录
          </el-button>
        </el-form>
        
        <!-- 注册链接 -->
        <div class="register-link">
          还没有账号？<router-link to="/customer/register">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  Food, 
  Calendar, 
  Discount, 
  User, 
  Lock
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const rememberMe = ref(false)

const passwordFormRef = ref(null)

// 密码登录表单
const passwordForm = reactive({
  username: '',
  password: ''
})

const passwordRules = {
  username: [{ required: true, message: '请输入手机号或用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 检查是否有记住的用户名
const rememberedUsername = localStorage.getItem('remember_username')
if (rememberedUsername) {
  passwordForm.username = rememberedUsername
  rememberMe.value = true
}

const handlePasswordLogin = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
        // 先不显示成功消息，等角色检查通过后再显示
        const success = await userStore.login(passwordForm, false)
        
        if (success) {
          // 检查角色权限：管理员账号不应该在用户端登录
          if (userStore.isAdmin && !userStore.isCustomer) {
            ElMessage.warning('管理员账号请使用管理端登录')
            userStore.logout()
            loading.value = false
            return
          }
          
          // 角色检查通过，显示成功消息
          ElMessage.success('登录成功')
          
          if (rememberMe.value) {
            localStorage.setItem('remember_username', passwordForm.username)
          } else {
            localStorage.removeItem('remember_username')
          }
          
          // 登录成功后跳转到用户端首页
          const redirect = router.currentRoute.value.query.redirect || '/customer/home'
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
  ElMessage.info('忘记密码功能待实现')
}
</script>

<style scoped>
.customer-login-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  display: flex;
  overflow: hidden;
}

.brand-section {
  flex: 1;
  background: url('/image/banner01.jpg') center center / cover no-repeat;
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
}

.brand-content h1 {
  font-size: 52px;
  margin-bottom: 24px;
  line-height: 1.2;
  font-weight: 800;
  text-shadow: 
    0 2px 4px rgba(0, 0, 0, 0.5),
    0 4px 12px rgba(0, 0, 0, 0.3),
    0 0 20px rgba(0, 0, 0, 0.2);
  animation: fadeInUp 0.8s ease-out;
  letter-spacing: 2px;
}

.brand-content p {
  font-size: 18px;
  margin-bottom: 45px;
  opacity: 0.95;
  text-shadow: 
    0 1px 3px rgba(0, 0, 0, 0.5),
    0 2px 8px rgba(0, 0, 0, 0.3);
  animation: fadeInUp 0.8s ease-out 0.2s both;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 16px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  animation: fadeInUp 0.8s ease-out both;
  
  &:nth-child(1) {
    animation-delay: 0.3s;
  }
  
  &:nth-child(2) {
    animation-delay: 0.4s;
  }
  
  &:nth-child(3) {
    animation-delay: 0.5s;
  }
  
  &:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: translateX(8px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

.feature-item .el-icon {
  font-size: 26px;
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
    radial-gradient(circle at 10% 20%, rgba(255, 77, 79, 0.03) 0%, transparent 50%),
    radial-gradient(circle at 90% 80%, rgba(255, 77, 79, 0.03) 0%, transparent 50%);
  pointer-events: none;
}

.login-card {
  background: white;
  padding: 50px 40px;
  border-radius: 24px;
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.08),
    0 0 0 1px rgba(255, 77, 79, 0.05);
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
  background: linear-gradient(135deg, rgba(255, 77, 79, 0.1), rgba(255, 120, 117, 0.1));
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
    0 0 0 1px rgba(255, 77, 79, 0.1);
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

.login-card h2 {
  font-size: 36px;
  margin-bottom: 12px;
  color: #ff4d4f;
  font-weight: 800;
  text-align: center;
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 1px;
}

.login-card > p {
  color: #666;
  margin-bottom: 35px;
  font-size: 14px;
  text-align: center;
}


/* 优化输入框样式 */
.login-card :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #e4e7ed;
  background: #fafafa;
}

.login-card :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #ffb3b3;
  background: #fff;
}

.login-card :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(255, 77, 79, 0.15), 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #ff4d4f;
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
  color: #ff4d4f;
}

.login-card :deep(.el-form-item) {
  margin-bottom: 22px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
  margin-top: 8px;
}

.login-card :deep(.el-checkbox) {
  .el-checkbox__label {
    color: #606266;
    font-size: 14px;
  }
  
  .el-checkbox__input.is-checked .el-checkbox__inner {
    background-color: #ff4d4f;
    border-color: #ff4d4f;
  }
  
  .el-checkbox__input.is-checked + .el-checkbox__label {
    color: #ff4d4f;
  }
}

.forgot-password {
  color: #ff4d4f;
  text-decoration: none;
  font-size: 14px;
  cursor: pointer;
  transition: color 0.3s ease;
}

.forgot-password:hover {
  color: #ff7875;
  text-decoration: underline;
}

.login-button {
  width: 100%;
  height: 50px;
  font-size: 17px;
  margin-top: 12px;
  font-weight: 600;
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  border: none;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3);
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
  background: linear-gradient(135deg, #ff7875 0%, #ff4d4f 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 77, 79, 0.5);
}

.login-button:hover::before {
  width: 300px;
  height: 300px;
}

.login-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.4);
}


.register-link {
  text-align: center;
  margin-top: 35px;
  padding-top: 25px;
  border-top: 1px solid #f0f0f0;
  color: #666;
  font-size: 14px;
}

.register-link a {
  color: #ff4d4f;
  text-decoration: none;
  font-weight: 600;
  margin-left: 5px;
  transition: color 0.3s ease;
}

.register-link a:hover {
  color: #ff7875;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .customer-login-container {
    flex-direction: column;
  }
  
  .brand-section {
    flex: 0 0 200px;
    padding: 20px;
  }
  
  .brand-content h1 {
    font-size: 32px;
  }
  
  .brand-content p {
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

