import request from '@/utils/request'

/**
 * 获取所有权限列表（树形结构）
 */
export function getAllPermissions() {
  return request({
    url: '/system/permission/list',
    method: 'get'
  })
}

