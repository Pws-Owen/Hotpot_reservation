import request from '@/utils/request'

/**
 * 上传图片
 * @param {File} file 文件对象
 * @param {String} type 文件类型：menu-菜品图片, avatar-头像, review-评论图片, common-通用
 * @returns {Promise} 返回包含url的对象
 */
export function uploadImage(file, type = 'common') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', type)
  
  return request({
    url: '/upload/image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除图片
 * @param {String} url 图片URL
 * @returns {Promise}
 */
export function deleteImage(url) {
  return request({
    url: '/upload/image',
    method: 'delete',
    params: { url }
  })
}

