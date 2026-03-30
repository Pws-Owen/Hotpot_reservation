import request from '@/utils/request'

/**
 * 获取所有餐桌
 */
export function getAllTables() {
  return request({
    url: '/table',
    method: 'get'
  })
}

/**
 * 分页查询餐桌
 */
export function getTablePage(params) {
  return request({
    url: '/table/page',
    method: 'get',
    params
  })
}

/**
 * 获取餐桌详情
 */
export function getTableById(id) {
  return request({
    url: `/table/${id}`,
    method: 'get'
  })
}

/**
 * 创建餐桌
 */
export function createTable(data) {
  return request({
    url: '/table',
    method: 'post',
    data
  })
}

/**
 * 更新餐桌
 */
export function updateTable(id, data) {
  return request({
    url: `/table/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除餐桌
 */
export function deleteTable(id) {
  return request({
    url: `/table/${id}`,
    method: 'delete'
  })
}

/**
 * 查询可用餐桌
 */
export function getAvailableTables(date, time) {
  return request({
    url: '/table/available',
    method: 'get',
    params: { date, time }
  })
}

