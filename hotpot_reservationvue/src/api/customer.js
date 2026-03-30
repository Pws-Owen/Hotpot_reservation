import request from '@/utils/request'

/**
 * 分页查询客户列表
 */
export function getCustomerPage(params) {
  return request({
    url: '/customer/page',
    method: 'get',
    params
  })
}

/**
 * 获取客户详情
 */
export function getCustomerById(id) {
  return request({
    url: `/customer/${id}`,
    method: 'get'
  })
}

/**
 * 更新客户信息
 */
export function updateCustomer(id, data) {
  return request({
    url: `/customer/${id}`,
    method: 'put',
    data
  })
}

/**
 * 获取客户订单列表
 */
export function getCustomerOrders(customerId, params) {
  return request({
    url: `/customer/${customerId}/orders`,
    method: 'get',
    params
  })
}

/**
 * 创建客户（新增会员）
 */
export function createCustomer(data) {
  return request({
    url: '/customer',
    method: 'post',
    data
  })
}

/**
 * 更新客户标签
 */
export function updateCustomerTag(id, tag) {
  return request({
    url: `/customer/${id}/tag`,
    method: 'put',
    data: { tag }
  })
}

