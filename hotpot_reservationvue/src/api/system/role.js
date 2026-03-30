import request from '@/utils/request'

/**
 * 分页查询角色列表
 */
export function getRolePage(params) {
  return request({
    url: '/system/role/page',
    method: 'get',
    params
  })
}

/**
 * 获取所有角色列表
 */
export function getAllRoles() {
  return request({
    url: '/system/role/list',
    method: 'get'
  })
}

/**
 * 获取角色详情
 */
export function getRoleById(id) {
  return request({
    url: `/system/role/${id}`,
    method: 'get'
  })
}

/**
 * 创建角色
 */
export function createRole(data) {
  return request({
    url: '/system/role',
    method: 'post',
    data
  })
}

/**
 * 更新角色
 */
export function updateRole(id, data) {
  return request({
    url: `/system/role/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除角色
 */
export function deleteRole(id) {
  return request({
    url: `/system/role/${id}`,
    method: 'delete'
  })
}

/**
 * 分配权限
 */
export function assignPermissions(id, permissionIds) {
  return request({
    url: `/system/role/${id}/assign-permissions`,
    method: 'post',
    data: permissionIds
  })
}

/**
 * 获取角色的权限ID列表
 */
export function getRolePermissions(id) {
  return request({
    url: `/system/role/${id}/permissions`,
    method: 'get'
  })
}

/**
 * 获取角色的用户ID列表
 */
export function getRoleUsers(id) {
  return request({
    url: `/system/role/${id}/users`,
    method: 'get'
  })
}

