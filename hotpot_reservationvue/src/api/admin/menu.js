import request from '@/utils/request'

/**
 * 分页查询菜单分类列表
 */
export function getCategoryPage(params) {
  return request({
    url: '/admin/menu/category/page',
    method: 'get',
    params
  })
}

/**
 * 创建菜单分类
 */
export function createCategory(data) {
  return request({
    url: '/admin/menu/category',
    method: 'post',
    data
  })
}

/**
 * 更新菜单分类
 */
export function updateCategory(id, data) {
  return request({
    url: `/admin/menu/category/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除菜单分类
 */
export function deleteCategory(id) {
  return request({
    url: `/admin/menu/category/${id}`,
    method: 'delete'
  })
}

/**
 * 分页查询菜品列表（管理端）
 */
export function getMenuItemPage(params) {
  return request({
    url: '/admin/menu/item/page',
    method: 'get',
    params
  })
}

/**
 * 创建菜品
 */
export function createMenuItem(data) {
  return request({
    url: '/admin/menu/item',
    method: 'post',
    data
  })
}

/**
 * 更新菜品
 */
export function updateMenuItem(id, data) {
  return request({
    url: `/admin/menu/item/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除菜品
 */
export function deleteMenuItem(id) {
  return request({
    url: `/admin/menu/item/${id}`,
    method: 'delete'
  })
}

/**
 * 更新菜品库存
 */
export function updateMenuItemStock(id, stock) {
  return request({
    url: `/admin/menu/item/${id}/stock`,
    method: 'put',
    data: { stock }
  })
}

