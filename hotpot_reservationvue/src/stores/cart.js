import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

/**
 * 购物车 Store
 */
export const useCartStore = defineStore('cart', () => {
  // 购物车商品列表
  const items = ref([])

  // 购物车商品总数
  const totalCount = computed(() => {
    return items.value.reduce((total, item) => total + item.quantity, 0)
  })

  // 购物车总价
  const totalPrice = computed(() => {
    return items.value.reduce((total, item) => {
      return total + (item.price * item.quantity)
    }, 0)
  })

  // 添加商品到购物车
  const addItem = (menuItem) => {
    const existingItem = items.value.find(item => item.itemId === menuItem.itemId)
    if (existingItem) {
      existingItem.quantity += menuItem.quantity || 1
    } else {
      items.value.push({
        itemId: menuItem.itemId,
        name: menuItem.name || menuItem.itemName,
        price: Number(menuItem.price) || 0,
        imageUrl: menuItem.imageUrl || '',
        description: menuItem.description || '',
        quantity: menuItem.quantity || 1
      })
    }
  }

  // 更新商品数量
  const updateQuantity = (itemId, quantity) => {
    const item = items.value.find(item => item.itemId === itemId)
    if (item) {
      if (quantity <= 0) {
        removeItem(itemId)
      } else {
        item.quantity = quantity
      }
    }
  }

  // 移除商品
  const removeItem = (itemId) => {
    const index = items.value.findIndex(item => item.itemId === itemId)
    if (index > -1) {
      items.value.splice(index, 1)
    }
  }

  // 清空购物车
  const clearCart = () => {
    items.value = []
  }

  // 从 localStorage 恢复购物车
  const restoreCart = () => {
    try {
      const saved = localStorage.getItem('cart')
      if (saved) {
        items.value = JSON.parse(saved)
      }
    } catch (error) {
      console.error('恢复购物车失败:', error)
    }
  }

  // 保存购物车到 localStorage
  const saveCart = () => {
    try {
      localStorage.setItem('cart', JSON.stringify(items.value))
    } catch (error) {
      console.error('保存购物车失败:', error)
    }
  }

  // 监听购物车变化，自动保存
  const watchCart = () => {
    // 使用 watchEffect 或 watch 来监听 items 的变化
    // 这里简化处理，在每次操作后手动保存
  }

  return {
    items,
    totalCount,
    totalPrice,
    addItem,
    updateQuantity,
    removeItem,
    clearCart,
    restoreCart,
    saveCart
  }
}, {
  persist: {
    key: 'cart',
    storage: localStorage,
    paths: ['items']
  }
})

