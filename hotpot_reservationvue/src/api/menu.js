import request from '@/utils/request'

// ==================== 分类管理接口 ====================

/**
 * 获取所有分类
 */
export function getCategories() {
  return request({
    url: '/menu/categories',
    method: 'get'
  })
}

/**
 * 获取分类详情
 */
export function getCategoryById(id) {
  return request({
    url: `/menu/categories/${id}`,
    method: 'get'
  })
}

/**
 * 创建分类
 */
export function createCategory(data) {
  return request({
    url: '/menu/categories',
    method: 'post',
    data
  })
}

/**
 * 更新分类
 */
export function updateCategory(id, data) {
  return request({
    url: `/menu/categories/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除分类
 */
export function deleteCategory(id) {
  return request({
    url: `/menu/categories/${id}`,
    method: 'delete'
  })
}

// ==================== 菜品管理接口 ====================

/**
 * 分页查询菜品
 */
export function getMenuItemPage(params) {
  return request({
    url: '/menu/items/page',
    method: 'get',
    params
  })
}

/**
 * 获取菜品详情
 */
export function getMenuItemById(id) {
  return request({
    url: `/menu/items/${id}`,
    method: 'get'
  })
}

/**
 * 创建菜品
 */
export function createMenuItem(data) {
  return request({
    url: '/menu/items',
    method: 'post',
    data
  })
}

/**
 * 更新菜品
 */
export function updateMenuItem(id, data) {
  return request({
    url: `/menu/items/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除菜品
 */
export function deleteMenuItem(id) {
  return request({
    url: `/menu/items/${id}`,
    method: 'delete'
  })
}

