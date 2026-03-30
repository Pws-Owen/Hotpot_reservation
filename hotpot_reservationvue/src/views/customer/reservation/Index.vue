<template>
  <div class="reservation-page">
    <!-- 通用头部：顶部栏 + 导航，这里不需要搜索栏 -->
    <CustomerHeader :show-search="false" />
    
    <div class="reservation-container">
      <!-- 头部区域：标题+返回按钮 -->
      <div class="reservation-header">
        <div class="header-left">
          <h1 class="reservation-title">位置预订</h1>
          <p class="reservation-subtitle">新鲜食材 · 地道川味 · 预约座位</p>
        </div>
        <div class="header-actions">
          <!-- 返回菜单按钮 -->
          <el-button 
            type="primary" 
            @click="$router.push('/customer/menu')"
            class="back-btn"
          >
            <el-icon><ArrowLeft /></el-icon>
            返回菜单
          </el-button>
        </div>
      </div>
      
      <!-- 预订表单卡片 -->
        <div class="reservation-card">
          <el-form
          ref="reservationFormRef"
          :model="reservationForm"
          :rules="rules"
          label-width="120px"
          class="reservation-form"
        >
          <div class="form-row">
            <el-form-item label="预约日期" prop="reservationDate">
              <el-date-picker
                v-model="reservationForm.reservationDate"
                type="date"
                placeholder="选择日期"
                :disabled-date="disabledDate"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                size="large"
              />
            </el-form-item>
            
            <el-form-item label="预约时间" prop="reservationTime">
              <el-time-picker
                v-model="reservationForm.reservationTime"
                placeholder="选择时间"
                format="HH:mm"
                value-format="HH:mm"
                style="width: 100%"
                size="large"
              />
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="用餐人数" prop="guestCount">
              <el-input-number
                v-model="reservationForm.guestCount"
                :min="1"
                :max="20"
                style="width: 100%"
                size="large"
              />
            </el-form-item>
            
            <el-form-item label="选择餐桌">
              <el-select
                v-model="reservationForm.tableId"
                :placeholder="tableSelectPlaceholder"
                clearable
                style="width: 100%"
                size="large"
                filterable
              >
                <el-option-group
                  v-if="recommendedTables.length > 0"
                  label="推荐餐桌（适合您的人数）"
                >
                  <el-option
                    v-for="table in recommendedTables"
                    :key="table.tableId"
                    :label="`${table.tableName || table.tableNumber} (容纳${table.capacity}人)`"
                    :value="table.tableId"
                  >
                    <span style="color: #67c23a; font-weight: 500;">
                      {{ table.tableName || table.tableNumber }} (容纳{{ table.capacity }}人)
                    </span>
                  </el-option>
                </el-option-group>
                <el-option-group
                  v-if="otherTables.length > 0"
                  label="其他可用餐桌"
                >
                  <el-option
                    v-for="table in otherTables"
                    :key="table.tableId"
                    :label="`${table.tableName || table.tableNumber} (容纳${table.capacity}人)`"
                    :value="table.tableId"
                  />
                </el-option-group>
                <el-option
                  v-if="filteredTables.length === 0"
                  disabled
                  value=""
                  label="暂无合适的餐桌"
                />
              </el-select>
              <div class="table-tip" v-if="reservationForm.guestCount > 0">
                <el-icon><InfoFilled /></el-icon>
                <span>已为您筛选出适合{{ reservationForm.guestCount }}人的餐桌</span>
              </div>
            </el-form-item>
          </div>
          
          <div class="form-row">
            <el-form-item label="联系人姓名" prop="contactName">
              <el-input
                v-model="reservationForm.contactName"
                placeholder="请输入联系人姓名"
                size="large"
                prefix-icon="User"
              />
            </el-form-item>
            
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input
                v-model="reservationForm.contactPhone"
                placeholder="请输入手机号"
                size="large"
                prefix-icon="Phone"
              />
            </el-form-item>
          </div>
          
          <el-form-item label="特殊要求">
            <el-input
              v-model="reservationForm.specialRequest"
              type="textarea"
              placeholder="如有特殊要求请在此填写，如：靠窗位置、儿童座椅等"
              :rows="3"
              resize="none"
            />
          </el-form-item>
          
          <div class="form-actions">
            <el-button 
              type="primary" 
              @click="submitReservation" 
              :loading="loading"
              size="large"
              class="submit-btn"
            >
              <el-icon v-if="!loading"><Check /></el-icon>
              提交预约
            </el-button>
            <el-button @click="resetForm" size="large" class="reset-btn">
              <el-icon><RefreshLeft /></el-icon>
              重置
            </el-button>
          </div>
        </el-form>
      </div>
      
      <!-- 预订提示卡片 -->
      <div class="tips-card">
        <div class="tips-header">
          <el-icon class="tips-icon"><Bell /></el-icon>
          <h3>预订小贴士</h3>
        </div>
        <ul class="tips-list">
          <li>预订时间请至少提前2小时</li>
          <li>如需取消预订，请提前1小时通知</li>
          <li>高峰时段可能需要排队等候</li>
          <li>如有特殊饮食需求，请在特殊要求中注明</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Calendar, EditPen, Check, RefreshLeft, Bell, User, Phone, InfoFilled } from '@element-plus/icons-vue'
import { createReservation } from '@/api/reservation'
import { getTablePage } from '@/api/table'
import { useUserStore } from '@/stores/user'
import CustomerHeader from '@/components/customer/CustomerHeader.vue'

const router = useRouter()
const reservationFormRef = ref(null)
const loading = ref(false)
const allTables = ref([]) // 所有可用餐桌
const userStore = useUserStore() // 获取用户store

const reservationForm = reactive({
  reservationDate: '',
  reservationTime: '',
  guestCount: 2,
  contactPhone: '',
  contactName: '',
  tableId: null,
  specialRequest: '',
  duration: 120 // 默认2小时
})

const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

const rules = {
  reservationDate: [
    { required: true, message: '请选择预约日期', trigger: 'change' }
  ],
  reservationTime: [
    { required: true, message: '请选择预约时间', trigger: 'change' }
  ],
  guestCount: [
    { required: true, message: '请选择用餐人数', trigger: 'change' }
  ],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  contactName: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' }
  ]
}

/**
 * 自动填充用户信息
 * 从userStore中获取登录用户的姓名和电话，自动填入表单
 */
const fillUserInfo = () => {
  if (userStore.isLoggedIn) {
    // 自动填充用户姓名（优先使用realName，如果没有则使用username）
    if (userStore.realName) {
      reservationForm.contactName = userStore.realName
    } else if (userStore.username) {
      reservationForm.contactName = userStore.username
    }
    
    // 自动填充用户电话
    if (userStore.phone) {
      reservationForm.contactPhone = userStore.phone
    }
  }
}

onMounted(() => {
  // 检查登录状态
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/customer/login')
    return
  }
  
  // 自动填充用户信息
  fillUserInfo()
  
  // 加载餐桌列表
  loadTables()
})

/**
 * 加载可用餐桌列表
 * 只加载状态为可用(status=1)的餐桌
 * 加载完成后自动匹配最合适的桌子
 */
const loadTables = async () => {
  try {
    const res = await getTablePage({ current: 1, size: 100, status: 1 })
    if (res.code === 200 && res.data) {
      allTables.value = res.data.records || []
      // 加载后自动匹配一次
      autoMatchTable()
    }
  } catch (error) {
    console.error('加载餐桌列表失败:', error)
  }
}

/**
 * 根据用餐人数筛选合适的桌子
 * 只返回容量大于等于用餐人数的桌子
 */
const filteredTables = computed(() => {
  if (!reservationForm.guestCount || reservationForm.guestCount <= 0) {
    return allTables.value
  }
  
  const guestCount = reservationForm.guestCount
  // 筛选出容量>=人数的桌子
  return allTables.value.filter(table => {
    const capacity = Number(table.capacity) || 0
    return capacity >= guestCount
  })
})

/**
 * 推荐餐桌：容量刚好或略大于人数（最多不超过2人）
 * 优先推荐容量最接近用餐人数的桌子，避免浪费
 */
const recommendedTables = computed(() => {
  if (!reservationForm.guestCount || reservationForm.guestCount <= 0) {
    return []
  }
  
  const guestCount = reservationForm.guestCount
  return filteredTables.value
    .filter(table => {
      const capacity = Number(table.capacity) || 0
      // 推荐：容量刚好或略大于人数（最多不超过2人）
      return capacity >= guestCount && capacity <= guestCount + 2
    })
    .sort((a, b) => {
      // 优先显示容量最接近人数的
      const diffA = Math.abs(Number(a.capacity) - guestCount)
      const diffB = Math.abs(Number(b.capacity) - guestCount)
      return diffA - diffB
    })
})

// 其他可用餐桌：容量大于推荐范围的
const otherTables = computed(() => {
  if (!reservationForm.guestCount || reservationForm.guestCount <= 0) {
    return allTables.value
  }
  
  const guestCount = reservationForm.guestCount
  return filteredTables.value
    .filter(table => {
      const capacity = Number(table.capacity) || 0
      // 其他：容量大于推荐范围的
      return capacity > guestCount + 2
    })
    .sort((a, b) => {
      // 按容量从小到大排序
      return Number(a.capacity) - Number(b.capacity)
    })
})

// 桌子选择框的提示文字
const tableSelectPlaceholder = computed(() => {
  if (!reservationForm.guestCount || reservationForm.guestCount <= 0) {
    return '请先选择用餐人数'
  }
  if (recommendedTables.value.length > 0) {
    return `已为您推荐${recommendedTables.value.length}个合适的餐桌`
  }
  if (filteredTables.value.length > 0) {
    return `已筛选出${filteredTables.value.length}个可用餐桌`
  }
  return '暂无合适的餐桌，请调整用餐人数'
})

/**
 * 自动匹配最合适的桌子
 * 当用户选择用餐人数时，自动选择容量最接近的桌子
 * 如果当前已选桌子仍然合适，则不自动更改
 */
const autoMatchTable = () => {
  if (!reservationForm.guestCount || reservationForm.guestCount <= 0) {
    return
  }
  
  // 如果已经有选中的桌子，检查是否还合适
  if (reservationForm.tableId) {
    const selectedTable = allTables.value.find(t => t.tableId === reservationForm.tableId)
    if (selectedTable) {
      const capacity = Number(selectedTable.capacity) || 0
      if (capacity >= reservationForm.guestCount) {
        // 当前选中的桌子仍然合适，不自动更改
        return
      }
    }
  }
  
  // 自动选择最合适的桌子（容量最接近人数的）
  if (recommendedTables.value.length > 0) {
    reservationForm.tableId = recommendedTables.value[0].tableId
  } else if (filteredTables.value.length > 0) {
    reservationForm.tableId = filteredTables.value[0].tableId
  } else {
    reservationForm.tableId = null
  }
}

// 监听用餐人数变化，自动匹配桌子
watch(() => reservationForm.guestCount, (newVal, oldVal) => {
  if (newVal && newVal > 0 && newVal !== oldVal) {
    // 延迟一下，确保computed已经更新
    setTimeout(() => {
      autoMatchTable()
    }, 100)
  }
})

const submitReservation = async () => {
  if (!reservationFormRef.value) return
  
  try {
    // 表单验证
    await reservationFormRef.value.validate()
    
    loading.value = true
    try {
      // 格式化日期
      const dateStr = reservationForm.reservationDate
      const timeStr = reservationForm.reservationTime
      
      // 验证必填字段
      if (!dateStr || !timeStr || !reservationForm.guestCount || !reservationForm.contactPhone || !reservationForm.contactName) {
        ElMessage.warning('请填写完整的预约信息')
        loading.value = false
        return
      }
      
      const data = {
        reservationDate: dateStr,
        reservationTime: timeStr,
        guestCount: reservationForm.guestCount,
        contactPhone: reservationForm.contactPhone,
        contactName: reservationForm.contactName,
        tableId: reservationForm.tableId || null,
        specialRequest: reservationForm.specialRequest || '',
        duration: reservationForm.duration || 120
      }
      
      console.log('提交预约数据:', data)
      
      const res = await createReservation(data)
      console.log('预约响应:', res)
      
      if (res.code === 200) {
        // 跳转到预约成功页面，传递预约信息
        const reservationData = {
          date: reservationForm.reservationDate,
          time: reservationForm.reservationTime,
          peopleCount: reservationForm.guestCount,
          phone: reservationForm.contactPhone,
          reservationNo: res.data?.reservationNo || ''
        }
        router.push({
          path: '/customer/reservation/success',
          query: {
            data: encodeURIComponent(JSON.stringify(reservationData))
          }
        })
      } else {
        // 跳转到预约失败页面
        router.push({
          path: '/customer/reservation/fail',
          query: {
            message: encodeURIComponent(res.message || '预约失败，请稍后重试')
          }
        })
      }
    } catch (error) {
      console.error('提交预约失败:', error)
      // 跳转到预约失败页面
      const errorMessage = error.response?.data?.message || error.message || '网络错误，请稍后重试'
      router.push({
        path: '/customer/reservation/fail',
        query: {
          message: encodeURIComponent(errorMessage)
        }
      })
    } finally {
      loading.value = false
    }
  } catch (error) {
    // 表单验证失败
    console.error('表单验证失败:', error)
    // 不显示错误消息，因为 Element Plus 会自动显示验证错误
  }
}

const resetForm = () => {
  if (reservationFormRef.value) {
    reservationFormRef.value.resetFields()
  }
  Object.assign(reservationForm, {
    reservationDate: '',
    reservationTime: '',
    guestCount: 2,
    contactPhone: '',
    contactName: '',
    tableId: null,
    specialRequest: '',
    duration: 120
  })
  
  // 重置后重新填充用户信息
  fillUserInfo()
}
</script>

<style scoped lang="scss">
.reservation-page {
  background: #f5f5f5;
}

.reservation-container {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
  background-color: #f8f8f8;
  min-height: 100vh;
  overflow-y: auto;
  padding-top: 20px;
  box-sizing: border-box;
}

// 头部样式
.reservation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 2px solid #ffe7e7;
  margin-bottom: 20px;

  .header-left {
    .reservation-title {
      font-size: 28px;
      font-weight: bold;
      color: #ff4d4f;
      margin: 0 0 5px 0;
    }

    .reservation-subtitle {
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

// 预订表单卡片
.reservation-card {
  // background: transparent;
  border-radius: 0;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.06);
  padding: 0;
  margin-bottom: 30px;
  overflow: hidden;
  min-height: 500px;

  .card-header {
    background: linear-gradient(135deg, #fff8f8 0%, #fff 100%);
    padding: 15px 30px;
    border-bottom: 1px solid #f0f0f0;

    .card-title {
      font-size: 22px;
      font-weight: 600;
      color: #333;
      margin: 0;
      display: flex;
      align-items: center;
      gap: 10px;
      color: #ff4d4f;
    }
  }

  .reservation-form {
    padding: 40px 40px 0 40px;
    min-height: 570px;
    display: flex;
    flex-direction: column;

    .form-row {
      display: flex;
      gap: 30px;
      margin-bottom: 45px;

      .el-form-item {
        flex: 1;
        margin-bottom: 0;
      }
    }

    .form-actions {
      display: flex;
      justify-content: center;
      gap: 20px;
      margin-top: auto;
      padding-top: 30px;
      padding-bottom: 40px;

      .submit-btn {
        padding: 14px 40px;
        font-weight: 500;
        border-radius: 8px;
        background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
        border: none;
        box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3);
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 6px 16px rgba(255, 77, 79, 0.4);
        }
      }

      .reset-btn {
        padding: 14px 40px;
        font-weight: 500;
        border-radius: 8px;
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-2px);
        }
      }
    }
  }
}

// 预订提示卡片
.tips-card {
  background: transparent;
  border-radius: 0;
  box-shadow: none;
  padding: 25px 30px;
  margin-bottom: 30px;

  .tips-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 15px;

    .tips-icon {
      font-size: 24px;
      color: #ff4d4f;
      transition: none;
    }

    h3 {
      font-size: 18px;
      font-weight: 600;
      color: #333;
      margin: 0;
    }
  }

  .tips-list {
    margin: 0;
    padding-left: 20px;

    li {
      margin-bottom: 8px;
      color: #666;
      line-height: 1.5;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

// 表单样式增强
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #333;
  padding-bottom: 12px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  height: 48px;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(255, 77, 79, 0.2);
}

:deep(.el-select .el-input__wrapper) {
  cursor: pointer;
}

:deep(.el-date-editor.el-input) {
  width: 100%;
}

:deep(.el-textarea__inner) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  padding: 12px 15px;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  &:focus {
    box-shadow: 0 0 0 2px rgba(255, 77, 79, 0.2);
  }
}

:deep(.el-input-number) {
  width: 100%;

  .el-input__wrapper {
    width: 100%;
    height: 48px;
  }
}

// 餐桌选择提示
.table-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  padding: 8px 12px;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 6px;
  font-size: 13px;
  color: #0369a1;
  
  .el-icon {
    color: #0ea5e9;
    font-size: 16px;
  }
}

// 推荐餐桌样式
:deep(.el-select-dropdown) {
  .el-select-group__title {
    font-weight: 600;
    color: #67c23a;
    padding: 8px 12px;
    background: #f0f9ff;
  }
  
  .el-select-group__wrap {
    &:not(:last-of-type) {
      border-bottom: 1px solid #e4e7ed;
      padding-bottom: 8px;
      margin-bottom: 8px;
    }
  }
}

// 响应式适配
@media (max-width: 768px) {
  .reservation-header {
    flex-direction: column;
    align-items: flex-start;

    .header-actions {
      width: 100%;
      justify-content: flex-end;
    }
  }

  .reservation-card {
    .reservation-form {
      padding: 20px;

      .form-row {
        flex-direction: column;
        gap: 0;
      }
    }
  }

  .form-actions {
    flex-direction: column;

    .submit-btn, .reset-btn {
      width: 100%;
    }
  }
}
</style>

