<template>
  <div class="customer-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span></span>
          <el-button type="primary" @click="handleAdd">新增会员</el-button>
        </div>
      </template>
      
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="客户姓名">
          <el-input v-model="searchForm.name" placeholder="请输入客户姓名" clearable />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table 
        :data="customerList" 
        border
        style="width: 100%"
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="userId" label="ID" width="80" />
        <el-table-column prop="realName" label="客户姓名" width="120" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="vipLevel" label="VIP等级" width="100">
          <template #default="{ row }">
            <el-tag :type="getVipLevelType(row.vipLevel)">
              {{ getVipLevelText(row.vipLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalConsumption" label="累计消费" width="120">
          <template #default="{ row }">
            ¥{{ Number(row.totalConsumption || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="totalOrders" label="订单数" width="100" />
        <el-table-column prop="email" label="邮箱" width="180" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="success" size="small" @click="handleViewOrders(row)">消费记录</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 消费记录对话框 -->
    <el-dialog 
      v-model="orderDialogVisible" 
      title="消费记录" 
      width="1200px"
      :close-on-click-modal="false"
    >
      <div v-if="currentCustomer">
        <div class="customer-info">
          <el-descriptions :column="4" border>
            <el-descriptions-item label="客户姓名">{{ currentCustomer.realName || currentCustomer.username }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ currentCustomer.phone }}</el-descriptions-item>
            <el-descriptions-item label="累计消费">¥{{ Number(currentCustomer.totalConsumption || 0).toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item label="订单总数">{{ currentCustomer.totalOrders || 0 }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <el-form :inline="true" :model="orderSearchForm" class="order-search-form" style="margin-top: 20px;">
          <el-form-item label="订单状态">
            <el-select v-model="orderSearchForm.status" placeholder="请选择状态" clearable style="width: 180px;">
              <el-option label="待支付" :value="0" />
              <el-option label="已支付" :value="1" />
              <el-option label="制作中" :value="2" />
              <el-option label="已完成" :value="3" />
              <el-option label="已取消" :value="4" />
              <el-option label="已退款" :value="5" />
            </el-select>
          </el-form-item>
          <el-form-item label="日期">
            <el-date-picker
              v-model="orderSearchForm.date"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              clearable
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadOrderList" :loading="orderLoading">查询</el-button>
            <el-button @click="resetOrderSearch">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table 
          :data="orderList" 
          border
          style="width: 100%; margin-top: 20px;"
          v-loading="orderLoading"
        >
          <el-table-column prop="orderId" label="订单ID" width="100" />
          <el-table-column prop="orderNo" label="订单号" width="150" show-overflow-tooltip />
          <el-table-column prop="totalPrice" label="订单金额" width="120">
            <template #default="{ row }">
              ¥{{ Number(row.totalPrice || 0).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="discountAmount" label="优惠金额" width="120">
            <template #default="{ row }">
              ¥{{ Number(row.discountAmount || 0).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="actualAmount" label="实付金额" width="120">
            <template #default="{ row }">
              <span style="color: #f56c6c; font-weight: bold;">
                ¥{{ Number(row.actualAmount || 0).toFixed(2) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="orderType" label="订单类型" width="100">
            <template #default="{ row }">
              {{ getOrderTypeText(row.orderType) }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="订单状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getOrderStatusType(row.status)">
                {{ getOrderStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDateTime(row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="payTime" label="支付时间" width="180">
            <template #default="{ row }">
              {{ row.payTime ? formatDateTime(row.payTime) : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleViewOrderDetail(row)">查看详情</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="order-pagination" style="margin-top: 20px;">
          <el-pagination
            v-model:current-page="orderPagination.current"
            v-model:page-size="orderPagination.size"
            :total="orderPagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleOrderSizeChange"
            @current-change="handleOrderPageChange"
          />
        </div>
      </div>

      <template #footer>
        <el-button @click="orderDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 订单详情对话框 -->
    <el-dialog 
      v-model="orderDetailDialogVisible" 
      title="订单详情" 
      width="800px"
    >
      <div v-if="currentOrderDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ currentOrderDetail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="订单状态">
            <el-tag :type="getOrderStatusType(currentOrderDetail.status)">
              {{ getOrderStatusText(currentOrderDetail.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="订单金额">¥{{ Number(currentOrderDetail.totalPrice || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="优惠金额">¥{{ Number(currentOrderDetail.discountAmount || 0).toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="实付金额">
            <span style="color: #f56c6c; font-weight: bold;">
              ¥{{ Number(currentOrderDetail.actualAmount || 0).toFixed(2) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="订单类型">{{ getOrderTypeText(currentOrderDetail.orderType) }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDateTime(currentOrderDetail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ currentOrderDetail.payTime ? formatDateTime(currentOrderDetail.payTime) : '-' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间" :span="2">{{ currentOrderDetail.finishTime ? formatDateTime(currentOrderDetail.finishTime) : '-' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ currentOrderDetail.remark || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider>订单明细</el-divider>
        <el-table :data="currentOrderDetail.details || []" border style="width: 100%;">
          <el-table-column prop="itemName" label="菜品名称" width="200" />
          <el-table-column prop="quantity" label="数量" width="100" />
          <el-table-column prop="price" label="单价" width="120">
            <template #default="{ row }">
              ¥{{ Number(row.price || 0).toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="subtotal" label="小计" width="120">
            <template #default="{ row }">
              ¥{{ Number(row.subtotal || 0).toFixed(2) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="orderDetailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新增会员对话框 -->
    <el-dialog 
      v-model="addDialogVisible" 
      title="新增会员" 
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form 
        :model="addForm" 
        :rules="addFormRules" 
        ref="addFormRef"
        label-width="100px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="addForm.username" 
            placeholder="请输入用户名"
            clearable
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="addForm.password" 
            type="password"
            placeholder="请输入密码（至少6位）"
            show-password
            clearable
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="addForm.confirmPassword" 
            type="password"
            placeholder="请再次输入密码"
            show-password
            clearable
          />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input 
            v-model="addForm.realName" 
            placeholder="请输入真实姓名"
            clearable
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input 
            v-model="addForm.phone" 
            placeholder="请输入手机号"
            clearable
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input 
            v-model="addForm.email" 
            placeholder="请输入邮箱（可选）"
            clearable
          />
        </el-form-item>
        <el-form-item label="VIP等级">
          <el-select v-model="addForm.vipLevel" placeholder="请选择VIP等级" style="width: 100%;">
            <el-option label="普通" :value="0" />
            <el-option label="VIP1" :value="1" />
            <el-option label="VIP2" :value="2" />
            <el-option label="VIP3" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAdd" :loading="addLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCustomerPage, createCustomer } from '@/api/customer'
import { getOrderPage, getOrderById } from '@/api/order'
import { formatDateTime } from '@/utils/date'

const searchForm = ref({
  name: '',
  phone: ''
})

const selectedRows = ref([])
const customerList = ref([])
const loading = ref(false)

const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

// 消费记录相关
const orderDialogVisible = ref(false)
const orderDetailDialogVisible = ref(false)
const currentCustomer = ref(null)
const orderList = ref([])
const orderLoading = ref(false)
const currentOrderDetail = ref(null)

// 新增会员相关
const addDialogVisible = ref(false)
const addFormRef = ref(null)
const addLoading = ref(false)
const addForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  email: '',
  vipLevel: 0
})

// 表单验证规则
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== addForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

const addFormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, validator: validatePhone, trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
}

const orderSearchForm = reactive({
  status: null,
  date: null
})

const orderPagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

onMounted(() => {
  loadCustomerList()
})

/**
 * 加载客户列表
 */
const loadCustomerList = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.value.page,
      size: pagination.value.size
    }
    if (searchForm.value.name) {
      params.name = searchForm.value.name
    }
    if (searchForm.value.phone) {
      params.phone = searchForm.value.phone
    }
    
    const res = await getCustomerPage(params)
    if (res.code === 200 && res.data) {
      customerList.value = res.data.records || []
      pagination.value.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载客户列表失败:', error)
    ElMessage.error('加载客户列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  pagination.value.page = 1
  loadCustomerList()
}

/**
 * 重置搜索
 */
const handleReset = () => {
  searchForm.value = {
    name: '',
    phone: ''
  }
  pagination.value.page = 1
  loadCustomerList()
}

/**
 * 新增会员
 */
const handleAdd = () => {
  // 重置表单
  Object.assign(addForm, {
    username: '',
    password: '',
    confirmPassword: '',
    realName: '',
    phone: '',
    email: '',
    vipLevel: 0
  })
  // 清除表单验证
  if (addFormRef.value) {
    addFormRef.value.clearValidate()
  }
  addDialogVisible.value = true
}

/**
 * 提交新增会员
 */
const handleSubmitAdd = async () => {
  if (!addFormRef.value) return
  
  await addFormRef.value.validate(async (valid) => {
    if (valid) {
      addLoading.value = true
      try {
        const data = {
          username: addForm.username.trim(),
          password: addForm.password,
          realName: addForm.realName.trim(),
          phone: addForm.phone.trim(),
          email: addForm.email ? addForm.email.trim() : null,
          vipLevel: addForm.vipLevel
        }
        
        const res = await createCustomer(data)
        if (res.code === 200) {
          ElMessage.success('新增会员成功')
          addDialogVisible.value = false
          loadCustomerList()
        } else {
          ElMessage.error(res.message || '新增会员失败')
        }
      } catch (error) {
        console.error('新增会员失败:', error)
        ElMessage.error('新增会员失败')
      } finally {
        addLoading.value = false
      }
    }
  })
}

/**
 * 查看消费记录
 */
const handleViewOrders = (row) => {
  currentCustomer.value = row
  orderDialogVisible.value = true
  orderPagination.current = 1
  resetOrderSearch()
  loadOrderList()
}

/**
 * 加载订单列表
 */
const loadOrderList = async () => {
  if (!currentCustomer.value) return
  
  orderLoading.value = true
  try {
    const params = {
      current: orderPagination.current,
      size: orderPagination.size,
      userId: currentCustomer.value.userId
    }
    
    if (orderSearchForm.status !== null) {
      params.status = orderSearchForm.status
    }
    if (orderSearchForm.date) {
      params.date = orderSearchForm.date
    }
    
    const res = await getOrderPage(params)
    if (res.code === 200 && res.data) {
      orderList.value = res.data.records || []
      orderPagination.total = res.data.total || 0
    } else {
      ElMessage.error(res.message || '获取订单列表失败')
      orderList.value = []
      orderPagination.total = 0
    }
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('获取订单列表失败')
    orderList.value = []
    orderPagination.total = 0
  } finally {
    orderLoading.value = false
  }
}

/**
 * 重置订单搜索
 */
const resetOrderSearch = () => {
  orderSearchForm.status = null
  orderSearchForm.date = null
  orderPagination.current = 1
  loadOrderList()
}

/**
 * 查看订单详情
 */
const handleViewOrderDetail = async (row) => {
  try {
    const res = await getOrderById(row.orderId)
    if (res.code === 200 && res.data) {
      currentOrderDetail.value = res.data
      orderDetailDialogVisible.value = true
    } else {
      ElMessage.error(res.message || '获取订单详情失败')
    }
  } catch (error) {
    console.error('获取订单详情失败:', error)
    ElMessage.error('获取订单详情失败')
  }
}

/**
 * 订单分页大小改变
 */
const handleOrderSizeChange = (size) => {
  orderPagination.size = size
  orderPagination.current = 1
  loadOrderList()
}

/**
 * 订单当前页改变
 */
const handleOrderPageChange = (current) => {
  orderPagination.current = current
  loadOrderList()
}

/**
 * 分页大小改变
 */
const handleSizeChange = (size) => {
  pagination.value.size = size
  pagination.value.page = 1
  loadCustomerList()
}

/**
 * 当前页改变
 */
const handlePageChange = (page) => {
  pagination.value.page = page
  loadCustomerList()
}

/**
 * 选择改变
 */
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

/**
 * 获取VIP等级文本
 */
const getVipLevelText = (level) => {
  const levelMap = {
    0: '普通',
    1: 'VIP1',
    2: 'VIP2',
    3: 'VIP3'
  }
  return levelMap[level] || '普通'
}

/**
 * 获取VIP等级类型
 */
const getVipLevelType = (level) => {
  const typeMap = {
    0: 'info',
    1: 'success',
    2: 'warning',
    3: 'danger'
  }
  return typeMap[level] || 'info'
}

/**
 * 获取订单类型文本
 */
const getOrderTypeText = (type) => {
  const typeMap = {
    1: '堂食',
    2: '外卖',
    3: '预约点餐'
  }
  return typeMap[type] || '未知'
}

/**
 * 获取订单状态文本
 */
const getOrderStatusText = (status) => {
  const statusMap = {
    0: '待支付',
    1: '已支付',
    2: '制作中',
    3: '已完成',
    4: '已取消',
    5: '已退款'
  }
  return statusMap[status] || '未知'
}

/**
 * 获取订单状态类型
 */
const getOrderStatusType = (status) => {
  const typeMap = {
    0: 'warning',
    1: 'success',
    2: 'info',
    3: 'success',
    4: 'info',
    5: 'danger'
  }
  return typeMap[status] || ''
}

</script>

<style scoped>
.customer-list {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.customer-info {
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
}
</style>

