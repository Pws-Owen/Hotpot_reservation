import request from '@/utils/request'

/**
 * 分页查询支付记录
 */
export function getPaymentPage(params) {
  return request({
    url: '/payment/page',
    method: 'get',
    params
  })
}

/**
 * 获取支付详情
 */
export function getPaymentById(id) {
  return request({
    url: `/payment/${id}`,
    method: 'get'
  })
}

/**
 * 创建支付记录
 */
export function createPayment(data) {
  return request({
    url: '/payment',
    method: 'post',
    data
  })
}

/**
 * 退款
 */
export function refundPayment(id, reason) {
  return request({
    url: `/payment/${id}/refund`,
    method: 'post',
    data: { reason }
  })
}

