<template>
  <div class="profile-page">
    <!-- 通用头部：顶部栏 + 导航，这里不需要搜索栏 -->
    <CustomerHeader :show-search="false" />

    <div class="profile-container">
      <!-- 头部区域：标题+返回按钮 -->
      <div class="profile-header">
        <div class="header-left">
          <h1 class="profile-title">个人中心</h1>
          <p class="profile-subtitle">新鲜食材 · 地道川味 · 会员服务</p>
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
      
      <el-row :gutter="24">
        <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
          <!-- 个人信息卡片 -->
          <el-card class="profile-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span><el-icon><User /></el-icon> 个人信息</span>
              </div>
            </template>
            <div class="profile-info">
              <div class="avatar-container">
                <el-upload
                  class="avatar-uploader"
                  :http-request="handleAvatarUpload"
                  :show-file-list="false"
                  :before-upload="beforeAvatarUpload"
                  accept="image/*"
                >
                  <div class="avatar-wrapper">
                    <el-avatar 
                      :size="100" 
                      :src="profileForm.avatar || userStore.avatar || 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iI2YwZjBmMCIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjE0IiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+55So5oi3PC90ZXh0Pjwvc3ZnPg=='" 
                      v-loading="avatarUploading"
                    />
                    <div class="avatar-edit" v-if="!avatarUploading">
                      <el-icon><Camera /></el-icon>
                    </div>
                    <div class="avatar-edit avatar-uploading" v-else>
                      <el-icon class="is-loading"><Loading /></el-icon>
                    </div>
                  </div>
                </el-upload>
              </div>
              
              <div class="user-info">
                <h2>{{ userStore.realName || userStore.username || '未登录' }}</h2>
                <p class="user-role">VIP会员</p>
                <p class="user-email">{{ userStore.username || '请先登录' }}</p>
              </div>
              
              <div class="user-actions">
                <el-button 
                  v-if="!userStore.isLoggedIn" 
                  type="primary" 
                  size="large"
                  @click="$router.push('/customer/login')"
                  class="login-btn"
                >
                  <el-icon><UserFilled /></el-icon>
                  立即登录
                </el-button>
                <el-button 
                  v-else
                  type="danger" 
                  size="large"
                  @click="handleLogout"
                  class="logout-btn"
                >
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-button>
              </div>
            </div>
            
            <el-divider>
              <span class="divider-text">会员统计</span>
            </el-divider>
            
            <div class="profile-stats">
              <div class="stat-item">
                <div class="stat-icon">
                  <el-icon><Calendar /></el-icon>
                </div>
                <div class="stat-content">
                  <div class="stat-value">5</div>
                  <div class="stat-label">总预约次数</div>
                </div>
              </div>
              <div class="stat-item">
                <div class="stat-icon">
                  <el-icon><ShoppingCart /></el-icon>
                </div>
                <div class="stat-content">
                  <div class="stat-value">12</div>
                  <div class="stat-label">总订单数</div>
                </div>
              </div>
            </div>
          </el-card>
          
          <!-- 会员等级卡片 -->
          <el-card class="vip-card" shadow="hover">
            <div class="vip-header">
              <div class="vip-icon">
                <el-icon><Star /></el-icon>
              </div>
              <div class="vip-info">
                <h3>会员等级</h3>
                <p>黄金会员</p>
              </div>
            </div>
            <div class="vip-progress">
              <div class="progress-label">距离下一级还需 500 积分</div>
              <el-progress :percentage="65" :stroke-width="8" />
            </div>
            <div class="vip-benefits">
              <h4>会员权益</h4>
              <div class="benefit-list">
                <div class="benefit-item">
                  <el-icon><Discount /></el-icon>
                  <span>专享折扣</span>
                </div>
                <div class="benefit-item">
                  <el-icon><Present /></el-icon>
                  <span>生日礼遇</span>
                </div>
                <div class="benefit-item">
                  <el-icon><Service /></el-icon>
                  <span>优先服务</span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :sm="24" :md="16" :lg="16" :xl="16">
          <!-- 快捷入口卡片 -->
          <el-card class="quick-access-card" shadow="hover" v-if="userStore.isLoggedIn">
            <template #header>
              <div class="card-header">
                <span><el-icon><Menu /></el-icon> 快捷入口</span>
              </div>
            </template>
            <div class="quick-access-grid">
              <div class="access-item" @click="$router.push('/customer/order/list')">
                <div class="access-icon order-icon">
                  <el-icon><ShoppingCart /></el-icon>
                </div>
                <div class="access-label">我的订单</div>
              </div>
              <div class="access-item" @click="$router.push('/customer/reservation/list')">
                <div class="access-icon reservation-icon">
                  <el-icon><Calendar /></el-icon>
                </div>
                <div class="access-label">我的预约</div>
              </div>
              <div class="access-item" @click="$router.push('/customer/review/my')">
                <div class="access-icon review-icon">
                  <el-icon><Star /></el-icon>
                </div>
                <div class="access-label">我的评价</div>
              </div>
              <div class="access-item" @click="$router.push('/customer/announcement')">
                <div class="access-icon announcement-icon">
                  <el-icon><Bell /></el-icon>
                </div>
                <div class="access-label">公告通知</div>
              </div>
            </div>
          </el-card>
          
          <!-- 账户设置卡片 -->
          <el-card class="settings-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span><el-icon><Setting /></el-icon> 账户设置</span>
              </div>
            </template>
            <el-form
              ref="profileFormRef"
              :model="profileForm"
              :rules="rules"
              label-width="100px"
              class="profile-form"
            >
              <el-form-item label="用户名" prop="username">
                <el-input v-model="profileForm.username" disabled>
                  <template #prefix>
                    <el-icon><User /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="profileForm.realName" placeholder="请输入真实姓名">
                  <template #prefix>
                    <el-icon><UserFilled /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="profileForm.phone" placeholder="请输入手机号">
                  <template #prefix>
                    <el-icon><Phone /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="profileForm.email" placeholder="请输入邮箱">
                  <template #prefix>
                    <el-icon><Message /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item class="form-actions">
                <el-button type="primary" @click="updateProfile" :loading="loading" size="large">
                  <el-icon><Check /></el-icon>
                  保存修改
                </el-button>
                <el-button @click="resetForm" size="large">
                  <el-icon><RefreshLeft /></el-icon>
                  重置
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
          
          <!-- 修改密码卡片 -->
          <el-card class="password-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <span><el-icon><Lock /></el-icon> 修改密码</span>
              </div>
            </template>
            <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-width="100px"
              class="password-form"
            >
              <el-form-item label="当前密码" prop="currentPassword">
                <el-input
                  v-model="passwordForm.currentPassword"
                  type="password"
                  show-password
                  placeholder="请输入当前密码"
                >
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  show-password
                  placeholder="请输入新密码"
                >
                  <template #prefix>
                    <el-icon><Key /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  show-password
                  placeholder="请再次输入新密码"
                >
                  <template #prefix>
                    <el-icon><Key /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              
              <el-form-item class="form-actions">
                <el-button type="primary" @click="updatePassword" :loading="passwordLoading" size="large">
                  <el-icon><Check /></el-icon>
                  修改密码
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowLeft, User, UserFilled, Lock, Key, Check, RefreshLeft, Camera, SwitchButton, Calendar, ShoppingCart, Star, Discount, Present, Service, Setting, Phone, Message, Menu, Bell, Loading } from '@element-plus/icons-vue'
import { updateCustomer, getCustomerById } from '@/api/customer'
import { uploadImage } from '@/api/upload'
import request from '@/utils/request'
import CustomerHeader from '@/components/customer/CustomerHeader.vue'

const router = useRouter()

const userStore = useUserStore()
const profileFormRef = ref(null)
const passwordFormRef = ref(null)
const loading = ref(false)
const passwordLoading = ref(false)
const avatarUploading = ref(false)

const profileForm = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  avatar: ''
})


const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入新密码'))
  } else {
    if (passwordForm.confirmPassword !== '') {
      passwordFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const passwordRules = {
  currentPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, validator: validatePass, trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ]
}

onMounted(async () => {
  // 初始化表单数据
  profileForm.username = userStore.username
  profileForm.realName = userStore.realName || ''
  profileForm.phone = userStore.phone || ''
  profileForm.email = userStore.email || ''
  profileForm.avatar = userStore.avatar || ''
  
  // 加载用户详细信息
  if (userStore.userId) {
    await loadUserInfo()
  }
})

const loadUserInfo = async () => {
  try {
    const res = await getCustomerById(userStore.userId)
    if (res.code === 200 && res.data) {
      const user = res.data
      profileForm.realName = user.realName || ''
      profileForm.phone = user.phone || ''
      profileForm.email = user.email || ''
      profileForm.avatar = user.avatar || ''
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const updateProfile = async () => {
  if (!profileFormRef.value) return
  
  profileFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await updateCustomer(userStore.userId, {
          realName: profileForm.realName,
          phone: profileForm.phone,
          email: profileForm.email,
          avatar: profileForm.avatar
        })
        if (res.code === 200) {
          ElMessage.success('个人信息更新成功')
          // 更新 store 中的信息
          userStore.realName = profileForm.realName
          userStore.phone = profileForm.phone
          userStore.email = profileForm.email
          userStore.avatar = profileForm.avatar
          // 同步更新localStorage
          localStorage.setItem('realName', profileForm.realName)
          localStorage.setItem('phone', profileForm.phone)
          localStorage.setItem('email', profileForm.email)
          localStorage.setItem('avatar', profileForm.avatar)
        } else {
          ElMessage.error(res.message || '更新失败')
        }
      } catch (error) {
        console.error('更新个人信息失败:', error)
        ElMessage.error(error.message || '更新失败，请重试')
      } finally {
        loading.value = false
      }
    }
  })
}

const updatePassword = async () => {
  if (!passwordFormRef.value) return
  
  passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        // 使用认证接口修改密码
        // 后端会从JWT Token中获取用户ID，无需前端传递
        const res = await request({
          url: `/auth/change-password`,
          method: 'post',
          data: {
            currentPassword: passwordForm.currentPassword,
            newPassword: passwordForm.newPassword
          }
        })
        if (res.code === 200) {
          ElMessage.success('密码修改成功，请重新登录')
          resetPasswordForm()
          // 延迟退出登录
          setTimeout(() => {
            userStore.logout()
            router.push('/customer/login')
          }, 1500)
        } else {
          ElMessage.error(res.message || '密码修改失败')
        }
      } catch (error) {
        console.error('修改密码失败:', error)
        // 如果接口不存在（404），提示功能暂未实现
        if (error.response?.status === 404 || error.message?.includes('No static resource')) {
          ElMessage.warning('密码修改功能暂未实现，请联系管理员或使用其他方式修改密码')
        } else {
          ElMessage.error(error.message || '密码修改失败，请重试')
        }
      } finally {
        passwordLoading.value = false
      }
    }
  })
}

const beforeAvatarUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 自定义头像上传方法
const handleAvatarUpload = async (options) => {
  const { file } = options
  avatarUploading.value = true
  try {
    const res = await uploadImage(file, 'avatar')
    if (res.code === 200 && res.data?.url) {
      profileForm.avatar = res.data.url
      ElMessage.success('头像上传成功')
      // 自动保存
      await updateProfile()
    } else {
      ElMessage.error(res.message || '头像上传失败')
    }
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error(error.message || '头像上传失败，请重试')
  } finally {
    avatarUploading.value = false
  }
}

const resetForm = () => {
  if (profileFormRef.value) {
    profileFormRef.value.resetFields()
    profileForm.username = userStore.username
    profileForm.realName = userStore.realName
  }
}

const resetPasswordForm = () => {
  if (passwordFormRef.value) {
    passwordFormRef.value.resetFields()
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/customer/home')
  } catch {
    // 用户取消
  }
}
</script>

<style scoped>
.profile-page {
  background-color: #f8f9fa;
  min-height: 100vh;
  padding-bottom: 20px;
}

.profile-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.profile-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 2px solid #ffe7e7;
  margin-bottom: 20px;
}

.header-left .profile-title {
  font-size: 28px;
  font-weight: 600;
  color: #ff4d4f;
  margin: 0 0 5px 0;
}

.header-left .profile-subtitle {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-actions .back-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.profile-info {
  text-align: center;
  padding: 20px 0;
}

.avatar-container {
  position: relative;
  margin-bottom: 20px;
  
  .avatar-uploader {
    position: relative;
    display: inline-block;
    cursor: pointer;
    
    .avatar-wrapper {
      position: relative;
      display: inline-block;
      
      .el-avatar {
        border: 4px solid #fff;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        transition: all 0.3s ease;
      }
      
      .avatar-edit {
        position: absolute;
        bottom: 5px;
        right: 5px;
        width: 30px;
        height: 30px;
        background-color: #ff4d4f;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
        transition: all 0.3s ease;
        
        .el-icon {
          font-size: 16px;
          color: white;
        }
        
        &.avatar-uploading {
          background-color: #409eff;
        }
      }
    }
    
    &:hover .avatar-wrapper .el-avatar {
      transform: scale(1.05);
    }
    
    &:hover .avatar-wrapper .avatar-edit {
      background-color: #ff3333;
      transform: scale(1.1);
    }
  }
}

.profile-info h2 {
  margin: 10px 0 5px;
  font-size: 20px;
}

.profile-info p {
  margin: 0;
  color: #909399;
}

.profile-stats {
  display: flex;
  justify-content: space-around;
  text-align: center;
}

.stat-item {
  flex: 1;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 5px;
}

.quick-access-card {
  margin-bottom: 20px;
}

.quick-access-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  padding: 10px 0;
}

.access-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.3s;
  background: #f8f9fa;
  
  &:hover {
    background: #ffe7e7;
    transform: translateY(-3px);
    box-shadow: 0 4px 12px rgba(255, 77, 79, 0.15);
  }
}

.access-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-bottom: 10px;
  color: #fff;
  
  &.order-icon {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }
  
  &.reservation-icon {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  }
  
  &.review-icon {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  }
  
  &.announcement-icon {
    background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  }
}

.access-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

@media (max-width: 768px) {
  .profile-container {
    padding: 15px;
  }
  
  .profile-header {
    flex-direction: column;
    align-items: flex-start;
    
    .header-actions {
      width: 100%;
      justify-content: flex-end;
    }
  }
  
  .quick-access-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
  }
}
</style>
