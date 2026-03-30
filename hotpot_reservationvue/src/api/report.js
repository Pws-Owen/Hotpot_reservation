import request from '@/utils/request'

/**
 * 获取销售报表
 */
export function getSalesReport(params) {
  return request({
    url: '/report/sales',
    method: 'get',
    params
  })
}

/**
 * 获取客户分析报表
 */
export function getCustomerReport(params) {
  return request({
    url: '/report/customer',
    method: 'get',
    params
  })
}

/**
 * 获取库存报表
 */
export function getInventoryReport() {
  return request({
    url: '/report/inventory',
    method: 'get'
  })
}

