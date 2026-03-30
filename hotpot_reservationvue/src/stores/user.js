/**
 * 用户状态管理Store
 * 使用Pinia管理用户登录状态、用户信息、角色权限等
 */
import { defineStore } from 'pinia'
import { login, register } from '@/api/admin/auth'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', {
  /**
   * 状态定义
   * 从localStorage初始化用户信息，实现页面刷新后保持登录状态
   */
  state: () => ({
    token: localStorage.getItem('token') || '',
    userId: localStorage.getItem('userId') || '',
    username: localStorage.getItem('username') || '',
    realName: localStorage.getItem('realName') || '',
    avatar: localStorage.getItem('avatar') || '',
    phone: localStorage.getItem('phone') || '',
    email: localStorage.getItem('email') || '',
    roles: JSON.parse(localStorage.getItem('roles') || '[]'),
    permissions: JSON.parse(localStorage.getItem('permissions') || '[]')
  }),

  /**
   * 计算属性（Getters）
   * 提供派生状态，方便组件中使用
   */
  getters: {
    /**
     * 判断用户是否已登录
     */
    isLoggedIn: (state) => !!state.token,
    /**
     * 判断用户是否为管理员或前台
     */
    isAdmin: (state) => state.roles.includes('ADMIN') || state.roles.includes('RECEPTIONIST'),
    /**
     * 判断用户是否为普通客户
     */
    isCustomer: (state) => state.roles.includes('USER') || state.roles.length === 0
  },

  /**
   * 动作（Actions）
   * 定义可修改状态的方法
   */
  actions: {
    /**
     * 用户登录
     * 调用登录API，成功后保存用户信息和Token到store和localStorage
     * 
     * @param {Object} loginForm 登录表单数据（包含username、password等）
     * @param {boolean} showSuccessMessage 是否显示登录成功提示，默认为true
     * @returns {Promise<boolean>} 登录是否成功
     */
    async login(loginForm, showSuccessMessage = true) {
      try {
        const res = await login(loginForm)
        if (res.code === 200 && res.data) {
          this.token = res.data.token
          this.userId = res.data.userId
          this.username = res.data.username
          this.realName = res.data.realName || ''
          this.avatar = res.data.avatar || ''
          this.phone = res.data.phone || ''
          this.email = res.data.email || ''
          this.roles = res.data.roles || []
          this.permissions = res.data.permissions || []
          
          // 保存到localStorage
          localStorage.setItem('token', this.token)
          localStorage.setItem('userId', this.userId)
          localStorage.setItem('username', this.username)
          localStorage.setItem('realName', this.realName)
          localStorage.setItem('avatar', this.avatar)
          localStorage.setItem('phone', this.phone)
          localStorage.setItem('email', this.email)
          localStorage.setItem('roles', JSON.stringify(this.roles))
          localStorage.setItem('permissions', JSON.stringify(this.permissions))
          
          // 只有在需要显示成功消息时才显示
          if (showSuccessMessage) {
            ElMessage.success('登录成功')
          }
          return true
        }
        return false
      } catch (error) {
        ElMessage.error(error.message || '登录失败')
        return false
      }
    },

    /**
     * 用户注册
     * 调用注册API，注册成功后提示用户登录
     * 
     * @param {Object} registerForm 注册表单数据
     * @returns {Promise<boolean>} 注册是否成功
     */
    async register(registerForm) {
      try {
        const res = await register(registerForm)
        if (res.code === 200) {
          ElMessage.success('注册成功，请登录')
          return true
        }
        return false
      } catch (error) {
        ElMessage.error(error.message || '注册失败')
        return false
      }
    },

    /**
     * 用户登出
     * 清空store中的用户信息和localStorage中的数据
     */
    logout() {
      this.token = ''
      this.userId = ''
      this.username = ''
      this.realName = ''
      this.avatar = ''
      this.phone = ''
      this.email = ''
      this.roles = []
      this.permissions = []
      
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      localStorage.removeItem('realName')
      localStorage.removeItem('avatar')
      localStorage.removeItem('phone')
      localStorage.removeItem('email')
      localStorage.removeItem('roles')
      localStorage.removeItem('permissions')
    }
  }
})

