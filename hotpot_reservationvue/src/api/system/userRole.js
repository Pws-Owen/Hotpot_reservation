import request from '@/utils/request'

/**
 * 分页查询用户角色关联列表
 */
export function getUserRolePage(params) {
  return request({
    url: '/system/user-role/page',
    method: 'get',
    params
  })
}

/**
 * 创建用户角色关联
 */
export function createUserRole(data) {
  return request({
    url: '/system/user-role',
    method: 'post',
    data
  })
}

/**
 * 更新用户角色关联
 */
export function updateUserRole(id, data) {
  return request({
    url: `/system/user-role/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除用户角色关联
 */
export function deleteUserRole(id) {
  return request({
    url: `/system/user-role/${id}`,
    method: 'delete'
  })
}

