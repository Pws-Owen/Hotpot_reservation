<template>
  <div class="menu-category">
    <div class="category-header">
      <h2>分类浏览</h2>
    </div>
    
    <el-row :gutter="20">
      <el-col :span="6" v-for="category in categories" :key="category.id">
        <el-card class="category-card" @click="handleCategoryClick(category)">
          <div class="category-content">
            <div class="category-icon">
              <el-icon size="40"><Menu /></el-icon>
            </div>
            <h3>{{ category.name }}</h3>
            <p>{{ category.description }}</p>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-divider />
    
    <div v-if="selectedCategory">
      <h3>{{ selectedCategory.name }} - 菜品列表</h3>
      <el-row :gutter="20">
        <el-col :span="6" v-for="item in categoryItems" :key="item.id">
          <el-card class="menu-item-card" shadow="hover">
            <img :src="item.image" class="menu-image" />
            <div class="menu-info">
              <h4>{{ item.name }}</h4>
              <p class="price">¥{{ item.price }}</p>
              <el-button type="primary" size="small" @click="handleAddToCart(item)">加入购物车</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Menu } from '@element-plus/icons-vue'

const router = useRouter()
const categories = ref([])
const selectedCategory = ref(null)
const categoryItems = ref([])

onMounted(() => {
  loadCategories()
})

import { getCategories } from '@/api/menu'
import { getMenuItemPage } from '@/api/menu'
import { useCartStore } from '@/stores/cart'

const cartStore = useCartStore()

const loadCategories = async () => {
  try {
    const res = await getCategories()
    if (res.code === 200 && res.data) {
      categories.value = res.data.map(cat => ({
        id: cat.categoryId,
        name: cat.categoryName,
        description: cat.description || ''
      }))
    }
  } catch (error) {
    console.error('加载分类失败:', error)
    ElMessage.error('加载分类失败')
  }
}

const handleCategoryClick = (category) => {
  selectedCategory.value = category
  loadCategoryItems(category.id)
}

const loadCategoryItems = async (categoryId) => {
  try {
    const res = await getMenuItemPage({
      current: 1,
      size: 100,
      categoryId: categoryId,
      status: 1
    })
    if (res.code === 200 && res.data) {
      categoryItems.value = (res.data.records || []).map(item => ({
        id: item.itemId,
        name: item.itemName,
        price: Number(item.price) || 0,
        image: item.imageUrl || 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjE1MCIgZmlsbD0iI2YwZjBmMCIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjE0IiBmaWxsPSIjOTk5IiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+55So5oi35Zu+54mHPC90ZXh0Pjwvc3ZnPg=='
      }))
    }
  } catch (error) {
    console.error('加载菜品失败:', error)
    ElMessage.error('加载菜品失败')
  }
}

const handleAddToCart = (item) => {
  cartStore.addItem({
    itemId: item.id,
    name: item.name,
    price: item.price,
    imageUrl: item.image,
    quantity: 1
  })
  cartStore.saveCart()
  ElMessage.success(`已添加 ${item.name} 到购物车`)
}
</script>

<style scoped>
.menu-category {
  padding: 20px;
}

.category-header {
  margin-bottom: 20px;
}

.category-card {
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 20px;
}

.category-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.category-content {
  text-align: center;
}

.category-icon {
  margin-bottom: 10px;
  color: #409EFF;
}

.category-content h3 {
  margin: 10px 0;
  color: #303133;
}

.category-content p {
  color: #909399;
  font-size: 14px;
}

.menu-item-card {
  margin-bottom: 20px;
}

.menu-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 4px;
  margin-bottom: 10px;
}

.menu-info {
  text-align: center;
}

.menu-info h4 {
  margin: 10px 0;
  color: #303133;
}

.price {
  font-size: 18px;
  color: #f56c6c;
  font-weight: bold;
  margin: 10px 0;
}
</style>
