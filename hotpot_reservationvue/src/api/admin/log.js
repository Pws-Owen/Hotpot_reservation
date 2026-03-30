import request from '@/utils/request'

/**
 * 分页查询操作日志列表
 */
export function getLogPage(params) {
  return request({
    url: '/log/page',
    method: 'get',
    params
  })
}

/**
 * 获取日志详情
 */
export function getLogById(id) {
  return request({
    url: `/log/${id}`,
    method: 'get'
  })
}

/**
 * 删除日志
 */
export function deleteLog(id) {
  return request({
    url: `/log/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除日志
 */
export function deleteLogs(ids) {
  return request({
    url: '/log/batch',
    method: 'delete',
    data: ids
  })
}

