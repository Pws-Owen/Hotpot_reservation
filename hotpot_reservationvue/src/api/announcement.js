import request from '@/utils/request'

/**
 * 分页查询公告列表
 */
export function getAnnouncementPage(params) {
  return request({
    url: '/announcement/page',
    method: 'get',
    params
  })
}

/**
 * 获取公告详情
 */
export function getAnnouncementById(id) {
  return request({
    url: `/announcement/${id}`,
    method: 'get'
  })
}

/**
 * 创建公告
 */
export function createAnnouncement(data) {
  return request({
    url: '/announcement',
    method: 'post',
    data
  })
}

/**
 * 更新公告
 */
export function updateAnnouncement(id, data) {
  return request({
    url: `/announcement/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除公告
 */
export function deleteAnnouncement(id) {
  return request({
    url: `/announcement/${id}`,
    method: 'delete'
  })
}

