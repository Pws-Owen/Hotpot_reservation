import request from '@/utils/request'

/**
 * 分页查询评论列表
 */
export function getReviewPage(params) {
  return request({
    url: '/review/page',
    method: 'get',
    params
  })
}

/**
 * 获取评论详情
 */
export function getReviewById(id) {
  return request({
    url: `/review/${id}`,
    method: 'get'
  })
}

/**
 * 创建评论
 */
export function createReview(data) {
  return request({
    url: '/review',
    method: 'post',
    data
  })
}

/**
 * 更新评论
 */
export function updateReview(id, data) {
  return request({
    url: `/review/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除评论
 */
export function deleteReview(id) {
  return request({
    url: `/review/${id}`,
    method: 'delete'
  })
}

/**
 * 回复评论
 */
export function replyReview(id, reply) {
  return request({
    url: `/review/${id}/reply`,
    method: 'post',
    data: { reply }
  })
}

