import request from '@/utils/request'

/**
 * 创建订单
 */
export function createOrder(data) {
  return request({
    url: '/order',
    method: 'post',
    data
  })
}

/**
 * 分页查询订单
 */
export function getOrderPage(params) {
  return request({
    url: '/order/page',
    method: 'get',
    params
  })
}

/**
 * 获取订单详情
 */
export function getOrderById(id) {
  return request({
    url: `/order/${id}`,
    method: 'get'
  })
}

/**
 * 取消订单
 */
export function cancelOrder(id, reason) {
  return request({
    url: `/order/${id}/cancel`,
    method: 'post',
    data: { reason }
  })
}

/**
 * 支付订单
 */
export function payOrder(id, paymentMethod) {
  return request({
    url: `/order/${id}/pay`,
    method: 'post',
    data: { paymentMethod }
  })
}

/**
 * 获取订单详情列表
 */
export function getOrderDetails(id) {
  return request({
    url: `/order/${id}/details`,
    method: 'get'
  })
}

/**
 * 更新订单状态（管理端）
 */
export function updateOrderStatus(id, status) {
  return request({
    url: `/order/${id}/status`,
    method: 'put',
    data: { status }
  })
}

