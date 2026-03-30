<template>
  <div class="register-container">
    <!-- 左侧品牌展示 -->
    <div class="brand-section">
      <div class="brand-content">
        <h1>加入我们<br>开启美食之旅</h1>
        <p>注册成为会员，享受更多专属优惠和服务</p>
        <div class="features">
          <div class="feature-item">
            <el-icon><Star /></el-icon>
            <span>新用户专享优惠</span>
          </div>
          <div class="feature-item">
            <el-icon><Ticket /></el-icon>
            <span>会员积分奖励</span>
          </div>
          <div class="feature-item">
            <el-icon><Service /></el-icon>
            <span>优先预约服务</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 右侧注册表单 -->
    <div class="register-section">
      <div class="register-card">
        <div class="card-header">
          <h2>用户注册</h2>
          <p>填写信息完成注册</p>
        </div>
        
        <el-form
          ref="registerFormRef"
          :model="registerForm"
          :rules="rules"
          class="register-form"
        >
          <el-form-item prop="username">
            <el-input
              v-model="registerForm.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="registerForm.password"
              type="password"
              placeholder="请输入密码（至少6位）"
              size="large"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          
          <el-form-item prop="confirmPassword">
            <el-input
              v-model="registerForm.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          
          <el-form-item prop="realName">
            <el-input
              v-model="registerForm.realName"
              placeholder="请输入真实姓名"
              size="large"
              :prefix-icon="UserFilled"
              clearable
            />
          </el-form-item>
          
          <el-form-item prop="phone">
            <el-input
              v-model="registerForm.phone"
              placeholder="请输入手机号"
              size="large"
              :prefix-icon="Phone"
              clearable
            />
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              @click="handleRegister"
              class="register-button"
            >
              立即注册
            </el-button>
          </el-form-item>
          
          <div class="register-footer">
            <span class="footer-text">已有账号？</span>
            <el-link type="primary" @click="$router.push('/customer/login')" class="login-link">
              立即登录
            </el-link>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { User, Lock, Phone, UserFilled, Star, Ticket, Service } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: ''
})

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (registerForm.confirmPassword !== '') {
      registerFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePass, trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const success = await userStore.register(registerForm)
        if (success) {
          router.push('/customer/login')
        }
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped lang="scss">
.register-container {
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
  background: url('/image/banner02.jpg') center center / cover no-repeat;
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
  animation: fadeInUp 0.8s ease-out;
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
  animation: fadeInUp 0.8s ease-out 0.2s both;
  letter-spacing: 2px;
}

.brand-content > p {
  font-size: 18px;
  margin-bottom: 45px;
  opacity: 0.95;
  text-shadow: 
    0 1px 3px rgba(0, 0, 0, 0.5),
    0 2px 8px rgba(0, 0, 0, 0.3);
  animation: fadeInUp 0.8s ease-out 0.3s both;
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
    animation-delay: 0.4s;
  }
  
  &:nth-child(2) {
    animation-delay: 0.5s;
  }
  
  &:nth-child(3) {
    animation-delay: 0.6s;
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

// 右侧注册表单区域
.register-section {
  flex: 0 0 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: linear-gradient(to bottom, #fafafa 0%, #ffffff 100%);
  overflow-y: auto;
  position: relative;
}

.register-section::before {
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

.register-card {
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

.register-card::before {
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

.register-card:hover {
  transform: translateY(-4px);
  box-shadow: 
    0 25px 70px rgba(0, 0, 0, 0.12),
    0 0 0 1px rgba(255, 77, 79, 0.1);
}

.register-card:hover::before {
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
  font-size: 36px;
  margin: 0 0 12px 0;
  color: #303133;
  font-weight: 800;
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
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

.register-form {
  .el-form-item {
    margin-bottom: 22px;
  }
}

// 输入框样式优化
.register-card :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid #e4e7ed;
  background: #fafafa;
}

.register-card :deep(.el-input__wrapper:hover) {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border-color: #ffb3b3;
  background: #fff;
}

.register-card :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 3px rgba(255, 77, 79, 0.15), 0 4px 12px rgba(0, 0, 0, 0.1);
  border-color: #ff4d4f;
  background: #fff;
  transform: translateY(-1px);
}

.register-card :deep(.el-input__inner) {
  font-size: 15px;
  color: #303133;
}

.register-card :deep(.el-input__prefix) {
  color: #909399;
  transition: color 0.3s ease;
}

.register-card :deep(.el-input__wrapper.is-focus .el-input__prefix) {
  color: #ff4d4f;
}

.register-button {
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

.register-button::before {
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

.register-button:hover {
  background: linear-gradient(135deg, #ff7875 0%, #ff4d4f 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 77, 79, 0.5);
}

.register-button:hover::before {
  width: 300px;
  height: 300px;
}

.register-button:active {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.4);
}

.register-footer {
  width: 100%;
  text-align: center;
  margin-top: 25px;
  padding-top: 25px;
  border-top: 1px solid #f0f0f0;
}

.footer-text {
  color: #606266;
  font-size: 14px;
  margin-right: 5px;
}

.login-link {
  color: #ff4d4f;
  font-size: 14px;
  font-weight: 600;
  transition: color 0.3s ease;
  
  &:hover {
    color: #ff7875;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .register-container {
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
  
  .register-section {
    flex: 1;
    padding: 20px;
  }
  
  .register-card {
    padding: 30px 20px;
  }
}
</style>