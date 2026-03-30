import request from '@/utils/request'

/**
 * 创建预约
 */
export function createReservation(data) {
  return request({
    url: '/reservation',
    method: 'post',
    data
  })
}

/**
 * 分页查询预约
 */
export function getReservationPage(params) {
  return request({
    url: '/reservation/page',
    method: 'get',
    params
  })
}

/**
 * 获取预约详情
 */
export function getReservationById(id) {
  return request({
    url: `/reservation/${id}`,
    method: 'get'
  })
}

/**
 * 确认预约
 */
export function confirmReservation(id) {
  return request({
    url: `/reservation/${id}/confirm`,
    method: 'post'
  })
}

/**
 * 取消预约
 */
export function cancelReservation(id, reason) {
  return request({
    url: `/reservation/${id}/cancel`,
    method: 'post',
    params: { reason }
  })
}

/**
 * 用户取消预约（带请求体）
 */
export function cancelReservationByUser(id, reason) {
  return request({
    url: `/reservation/${id}/cancel`,
    method: 'post',
    data: { reason }
  })
}

