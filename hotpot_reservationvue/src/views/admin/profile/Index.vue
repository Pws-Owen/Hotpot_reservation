<template>
  <div class="profile-index">
    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="个人信息" name="info">
          <el-form :model="userInfo" label-width="100px" style="max-width: 600px">
            <el-form-item label="头像">
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
                    :src="userInfo.avatar || userStore.avatar || 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iI2YwZjBmMCIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjE0IiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+55So5oi3PC90ZXh0Pjwvc3ZnPg=='"
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
              <div class="avatar-tip">点击头像上传新头像，支持 JPG、PNG 格式，大小不超过 2MB</div>
            </el-form-item>
            <el-form-item label="用户名">
              <el-input v-model="userInfo.username" disabled />
            </el-form-item>
            <el-form-item label="真实姓名">
              <el-input v-model="userInfo.realName" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="userInfo.phone" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="userInfo.email" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateInfo" :loading="loading">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="修改密码" name="password">
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px" style="max-width: 600px">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Camera, Loading } from '@element-plus/icons-vue'
import { getUserById, updateUser } from '@/api/system/user'
import { uploadImage } from '@/api/upload'
import request from '@/utils/request'

const userStore = useUserStore()
const activeTab = ref('info')
const passwordFormRef = ref(null)
const loading = ref(false)
const avatarUploading = ref(false)

const userInfo = reactive({
  username: '',
  realName: '',
  phone: '',
  email: '',
  avatar: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

onMounted(async () => {
  // 从store获取基本信息
  userInfo.username = userStore.username
  userInfo.realName = userStore.realName || ''
  userInfo.phone = userStore.phone || ''
  userInfo.email = userStore.email || ''
  userInfo.avatar = userStore.avatar || ''
  
  // 从API获取完整用户信息
  if (userStore.userId) {
    await loadUserInfo()
  }
})

const loadUserInfo = async () => {
  try {
    const res = await getUserById(userStore.userId)
    if (res.code === 200 && res.data) {
      const user = res.data
      userInfo.realName = user.realName || ''
      userInfo.phone = user.phone || ''
      userInfo.email = user.email || ''
      userInfo.avatar = user.avatar || ''
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

const handleUpdateInfo = async () => {
  loading.value = true
  try {
    const res = await updateUser(userStore.userId, {
      realName: userInfo.realName,
      phone: userInfo.phone,
      email: userInfo.email,
      avatar: userInfo.avatar
    })
    if (res.code === 200) {
      ElMessage.success('保存成功')
      // 更新 store 中的信息
      userStore.realName = userInfo.realName
      userStore.phone = userInfo.phone
      userStore.email = userInfo.email
      userStore.avatar = userInfo.avatar
      // 同步更新localStorage
      localStorage.setItem('realName', userInfo.realName)
      localStorage.setItem('phone', userInfo.phone)
      localStorage.setItem('email', userInfo.email)
      localStorage.setItem('avatar', userInfo.avatar)
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    console.error('更新用户信息失败:', error)
    ElMessage.error(error.message || '保存失败，请重试')
  } finally {
    loading.value = false
  }
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await request({
          url: '/auth/change-password',
          method: 'post',
          data: {
            currentPassword: passwordForm.oldPassword,
            newPassword: passwordForm.newPassword
          }
        })
        if (res.code === 200) {
          ElMessage.success('密码修改成功')
          passwordFormRef.value.resetFields()
        } else {
          ElMessage.error(res.message || '密码修改失败')
        }
      } catch (error) {
        console.error('修改密码失败:', error)
        ElMessage.error(error.message || '密码修改失败，请重试')
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

const handleAvatarUpload = async (options) => {
  const { file } = options
  avatarUploading.value = true
  try {
    const res = await uploadImage(file, 'avatar')
    if (res.code === 200 && res.data?.url) {
      userInfo.avatar = res.data.url
      ElMessage.success('头像上传成功')
      // 自动保存
      await handleUpdateInfo()
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
</script>

<style scoped>
.profile-index {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;
}

.avatar-uploader {
  position: relative;
  display: inline-block;
  cursor: pointer;
}

.avatar-wrapper {
  position: relative;
  display: inline-block;
}

.avatar-wrapper .el-avatar {
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
  background-color: #409eff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

.avatar-edit .el-icon {
  font-size: 16px;
  color: white;
}

.avatar-edit.avatar-uploading {
  background-color: #67c23a;
}

.avatar-uploader:hover .avatar-wrapper .el-avatar {
  transform: scale(1.05);
}

.avatar-uploader:hover .avatar-wrapper .avatar-edit {
  background-color: #66b1ff;
  transform: scale(1.1);
}

.avatar-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}
</style>
