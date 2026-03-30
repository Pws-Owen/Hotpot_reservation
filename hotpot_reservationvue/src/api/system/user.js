import request from '@/utils/request'

/**
 * 分页查询系统用户列表
 */
export function getUserPage(params) {
  return request({
    url: '/system/user/page',
    method: 'get',
    params
  })
}

/**
 * 获取用户详情
 */
export function getUserById(id) {
  return request({
    url: `/system/user/${id}`,
    method: 'get'
  })
}

/**
 * 创建用户
 */
export function createUser(data) {
  return request({
    url: '/system/user',
    method: 'post',
    data
  })
}

/**
 * 更新用户
 */
export function updateUser(id, data) {
  return request({
    url: `/system/user/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除用户
 */
export function deleteUser(id) {
  return request({
    url: `/system/user/${id}`,
    method: 'delete'
  })
}

/**
 * 重置密码
 */
export function resetPassword(id, data) {
  return request({
    url: `/system/user/${id}/reset-password`,
    method: 'post',
    data
  })
}

/**
 * 分配角色
 */
export function assignRoles(id, roleIds) {
  return request({
    url: `/system/user/${id}/assign-roles`,
    method: 'post',
    data: roleIds
  })
}

/**
 * 获取用户的角色ID列表
 */
export function getUserRoles(id) {
  return request({
    url: `/system/user/${id}/roles`,
    method: 'get'
  })
}

