<template>
  <div class="menu-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span></span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            新增菜品
          </el-button>
        </div>
      </template>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="菜品名称">
          <el-input 
            v-model="searchForm.itemName" 
            placeholder="请输入菜品名称" 
            clearable 
            style="width: 200px;"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="searchForm.categoryId" placeholder="选择分类" clearable style="width: 200px;">
            <el-option 
              v-for="cat in categories" 
              :key="cat.categoryId" 
              :label="cat.categoryName" 
              :value="cat.categoryId" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 150px;">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <el-table 
        :data="menuItems" 
        style="width: 100%"
        border
        v-loading="loading"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="itemId" label="ID" width="80" />
        <el-table-column prop="itemName" label="菜品名称" width="150" show-overflow-tooltip />
        <el-table-column label="图片" width="100">
          <template #default="scope">
            <el-image
              v-if="scope.row.imageUrl"
              :src="scope.row.imageUrl"
              :preview-src-list="[scope.row.imageUrl]"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 4px;"
              :preview-teleported="true"
            />
            <span v-else style="color: #909399; font-size: 12px;">暂无图片</span>
          </template>
        </el-table-column>
        <el-table-column prop="categoryId" label="分类ID" width="100" />
        <el-table-column prop="price" label="价格" width="100">
          <template #default="scope">
            ¥{{ scope.row.price }}
          </template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="stock" label="库存" width="100" />
        <el-table-column prop="salesCount" label="销量" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 新增/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogTitle" 
      width="800px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="菜品名称" prop="itemName">
              <el-input v-model="form.itemName" placeholder="请输入菜品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%;">
                <el-option 
                  v-for="cat in categories" 
                  :key="cat.categoryId" 
                  :label="cat.categoryName" 
                  :value="cat.categoryId" 
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="单位" prop="unit">
              <el-input v-model="form.unit" placeholder="如：份、盘、杯" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="库存" prop="stock">
              <el-input-number v-model="form.stock" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="菜品图片" prop="imageUrl">
          <el-upload
            class="image-uploader"
            :http-request="handleImageUpload"
            :show-file-list="false"
            :before-upload="beforeImageUpload"
            accept="image/*"
          >
            <el-image
              v-if="form.imageUrl"
              :src="form.imageUrl"
              class="uploaded-image"
              fit="cover"
            />
            <el-icon v-else class="image-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">
            <div style="font-size: 12px; color: #909399; margin-top: 10px;">
              支持 JPG、PNG、GIF、WEBP 格式，建议尺寸 300x300，大小不超过 5MB
            </div>
          </div>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="辣度">
              <el-radio-group v-model="form.spicyLevel">
                <el-radio :label="0">不辣</el-radio>
                <el-radio :label="1">微辣</el-radio>
                <el-radio :label="2">中辣</el-radio>
                <el-radio :label="3">重辣</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">上架</el-radio>
                <el-radio :label="0">下架</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="是否推荐">
              <el-radio-group v-model="form.isRecommend">
                <el-radio :label="1">是</el-radio>
                <el-radio :label="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="是否热门">
              <el-radio-group v-model="form.isHot">
                <el-radio :label="1">是</el-radio>
                <el-radio :label="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入菜品描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { 
  getMenuItemPage, 
  getMenuItemById, 
  createMenuItem, 
  updateMenuItem, 
  deleteMenuItem,
  getCategories 
} from '@/api/menu'
import { uploadImage } from '@/api/upload'

const userStore = useUserStore()
const menuItems = ref([])
const categories = ref([])
const selectedRows = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const currentId = ref(null)
const loading = ref(false)

const searchForm = reactive({
  itemName: '',
  categoryId: null,
  status: null
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const form = reactive({
  itemName: '',
  categoryId: null,
  price: 0,
  unit: '份',
  stock: 0,
  description: '',
  imageUrl: '',
  spicyLevel: 0,
  isRecommend: 0,
  isHot: 0,
  status: 1,
  sortOrder: 0
})

const rules = {
  itemName: [{ required: true, message: '请输入菜品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const dialogTitle = computed(() => isEdit.value ? '编辑菜品' : '新增菜品')

// 图片上传前验证
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 自定义图片上传
const handleImageUpload = async (options) => {
  const { file } = options
  try {
    const res = await uploadImage(file, 'menu')
    if (res.code === 200 && res.data && res.data.url) {
      form.imageUrl = res.data.url
      ElMessage.success('图片上传成功')
    } else {
      ElMessage.error('图片上传失败')
    }
  } catch (error) {
    console.error('图片上传失败:', error)
    ElMessage.error(error.message || '图片上传失败，请重试')
  }
}

onMounted(() => {
  loadCategories()
  loadData()
})

const loadCategories = async () => {
  try {
    const res = await getCategories()
    if (res.code === 200 && res.data) {
      categories.value = res.data || []
    }
  } catch (error) {
    console.error('加载分类列表失败:', error)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.currentPage,
      size: pagination.pageSize
    }
    if (searchForm.itemName && searchForm.itemName.trim()) {
      params.itemName = searchForm.itemName.trim()
    }
    if (searchForm.categoryId !== null) {
      params.categoryId = searchForm.categoryId
    }
    if (searchForm.status !== null) {
      params.status = searchForm.status
    }
    
    const res = await getMenuItemPage(params)
    if (res.code === 200 && res.data) {
      menuItems.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载菜品列表失败:', error)
    ElMessage.error('加载菜品列表失败')
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  currentId.value = null
  resetForm()
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  try {
    loading.value = true
    const res = await getMenuItemById(row.itemId)
    if (res.code === 200 && res.data) {
      isEdit.value = true
      currentId.value = row.itemId
      Object.assign(form, {
        itemName: res.data.itemName || '',
        categoryId: res.data.categoryId || null,
        price: res.data.price ? Number(res.data.price) : 0,
        unit: res.data.unit || '份',
        stock: res.data.stock || 0,
        description: res.data.description || '',
        imageUrl: res.data.imageUrl || '',
        spicyLevel: res.data.spicyLevel != null ? res.data.spicyLevel : 0,
        isRecommend: res.data.isRecommend != null ? res.data.isRecommend : 0,
        isHot: res.data.isHot != null ? res.data.isHot : 0,
        status: res.data.status != null ? res.data.status : 1,
        sortOrder: res.data.sortOrder || 0
      })
      dialogVisible.value = true
    }
  } catch (error) {
    console.error('获取菜品详情失败:', error)
    ElMessage.error('获取菜品详情失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await updateMenuItem(currentId.value, form)
          ElMessage.success('保存成功')
        } else {
          await createMenuItem(form)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error(error.message || '保存失败')
      }
    }
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确认要删除此菜品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    try {
      await deleteMenuItem(row.itemId)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error(error.message || '删除失败')
    }
  } catch {
    // 用户取消
  }
}

const handleSearch = () => {
  pagination.currentPage = 1
  loadData()
}

const resetSearch = () => {
  searchForm.itemName = ''
  searchForm.categoryId = null
  searchForm.status = null
  handleSearch()
}

const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  loadData()
}

const handleCurrentChange = (val) => {
  pagination.currentPage = val
  loadData()
}

const resetForm = () => {
  Object.assign(form, {
    itemName: '',
    categoryId: null,
    price: 0,
    unit: '份',
    stock: 0,
    description: '',
    imageUrl: '',
    spicyLevel: 0,
    isRecommend: 0,
    isHot: 0,
    status: 1,
    sortOrder: 0
  })
  formRef.value?.clearValidate()
}

const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}
</script>

<style scoped>
.menu-list {
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

.image-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 178px;
  height: 178px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #fafafa;
  transition: all 0.3s;
}

.image-uploader:hover {
  border-color: #409EFF;
}

.image-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.uploaded-image {
  width: 178px;
  height: 178px;
  object-fit: cover;
  border-radius: 6px;
}

.upload-tip {
  margin-top: 10px;
}
</style>