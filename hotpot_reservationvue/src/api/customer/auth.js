import request from '@/utils/request'

/**
 * 用户端密码登录
 */
export function customerLogin(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/**
 * 用户端短信登录
 */
export function smsLogin(data) {
  return request({
    url: '/auth/sms-login',
    method: 'post',
    data
  })
}

/**
 * 发送验证码
 */
export function sendCode(data) {
  return request({
    url: '/auth/send-code',
    method: 'post',
    data
  })
}

/**
 * 微信登录
 */
export function wechatLogin() {
  return request({
    url: '/auth/wechat',
    method: 'get'
  })
}

/**
 * 支付宝登录
 */
export function alipayLogin() {
  return request({
    url: '/auth/alipay',
    method: 'get'
  })
}

