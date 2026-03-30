import request from '@/utils/request'

/**
 * 获取仪表盘统计数据
 * @param {number} days - 预约趋势天数，可选值：7或30，默认7
 */
export function getDashboardStats(days = 7) {
  return request({
    url: '/dashboard/stats',
    method: 'get',
    params: { days }
  })
}

