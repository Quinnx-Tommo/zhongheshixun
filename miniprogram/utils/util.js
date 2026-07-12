// miniprogram/utils/util.js — 通用工具函数

/**
 * 带缓存的请求（弱网优化）
 * @param {string} key 缓存键
 * @param {function} fetchFn 请求函数
 * @param {number} cacheTime 缓存时间（毫秒），默认 5 分钟
 */
const cacheGet = async (key, fetchFn, cacheTime = 5 * 60 * 1000) => {
  const cached = wx.getStorageSync(key)
  if (cached && Date.now() - cached.time < cacheTime) {
    return cached.data
  }
  try {
    const data = await fetchFn()
    wx.setStorageSync(key, { data, time: Date.now() })
    return data
  } catch (e) {
    // 网络失败时降级使用缓存
    if (cached) return cached.data
    throw e
  }
}

/**
 * 格式化时间（yyyy-MM-dd HH:mm:ss）
 */
function formatTime(date = new Date()) {
  const pad = (n) => n.toString().padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

/**
 * 防抖函数
 */
function debounce(fn, delay = 300) {
  let timer = null
  return function (...args) {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => fn.apply(this, args), delay)
  }
}

module.exports = {
  cacheGet,
  formatTime,
  debounce
}
