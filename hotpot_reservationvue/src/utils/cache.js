/**
 * 请求缓存工具
 * 用于缓存API请求结果，减少重复请求
 */

class RequestCache {
  constructor() {
    this.cache = new Map()
    this.defaultTTL = 5 * 60 * 1000 // 默认5分钟过期
  }

  /**
   * 生成缓存key
   * @param {string} url 请求URL
   * @param {object} params 请求参数
   * @returns {string} 缓存key
   */
  generateKey(url, params = {}) {
    const paramsStr = JSON.stringify(params)
    return `${url}:${paramsStr}`
  }

  /**
   * 获取缓存
   * @param {string} url 请求URL
   * @param {object} params 请求参数
   * @returns {any|null} 缓存的数据，如果不存在或已过期返回null
   */
  get(url, params = {}) {
    const key = this.generateKey(url, params)
    const cached = this.cache.get(key)
    
    if (!cached) {
      return null
    }
    
    // 检查是否过期
    if (Date.now() > cached.expireTime) {
      this.cache.delete(key)
      return null
    }
    
    return cached.data
  }

  /**
   * 设置缓存
   * @param {string} url 请求URL
   * @param {object} params 请求参数
   * @param {any} data 要缓存的数据
   * @param {number} ttl 过期时间（毫秒），默认5分钟
   */
  set(url, params = {}, data, ttl = this.defaultTTL) {
    const key = this.generateKey(url, params)
    const expireTime = Date.now() + ttl
    
    this.cache.set(key, {
      data,
      expireTime,
      createdAt: Date.now()
    })
  }

  /**
   * 删除缓存
   * @param {string} url 请求URL
   * @param {object} params 请求参数
   */
  delete(url, params = {}) {
    const key = this.generateKey(url, params)
    this.cache.delete(key)
  }

  /**
   * 清空所有缓存
   */
  clear() {
    this.cache.clear()
  }

  /**
   * 清空指定URL的所有缓存
   * @param {string} url 请求URL
   */
  clearByUrl(url) {
    const keysToDelete = []
    for (const key of this.cache.keys()) {
      if (key.startsWith(url + ':')) {
        keysToDelete.push(key)
      }
    }
    keysToDelete.forEach(key => this.cache.delete(key))
  }

  /**
   * 清理过期缓存
   */
  cleanExpired() {
    const now = Date.now()
    const keysToDelete = []
    
    for (const [key, value] of this.cache.entries()) {
      if (now > value.expireTime) {
        keysToDelete.push(key)
      }
    }
    
    keysToDelete.forEach(key => this.cache.delete(key))
  }
}

// 创建全局缓存实例
const requestCache = new RequestCache()

// 定期清理过期缓存（每10分钟）
setInterval(() => {
  requestCache.cleanExpired()
}, 10 * 60 * 1000)

export default requestCache

