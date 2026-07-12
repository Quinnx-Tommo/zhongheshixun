// miniprogram/api/request.js
const { baseURL, timeout } = require('./config')

// 请求重试次数（弱网优化）
const MAX_RETRY = 3

function request(url, method = 'GET', data = {}, retryCount = 0) {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token')
    wx.request({
      url: baseURL + url,
      method,
      data,
      timeout,
      header: {
        'Authorization': token ? `Bearer ${token}` : '',
        'Content-Type': 'application/json'
      },
      success(res) {
        const { code, message, data } = res.data
        if (code === 200) {
          resolve(data)
        } else if (code === 401) {
          // Token 失效，清除并跳转登录
          wx.removeStorageSync('token')
          wx.removeStorageSync('userInfo')
          wx.reLaunch({ url: '/pages/login/login' })
          reject(new Error('未登录'))
        } else {
          wx.showToast({ title: message || '请求失败', icon: 'none' })
          reject(res.data)
        }
      },
      fail(err) {
        // 弱网重试
        if (retryCount < MAX_RETRY) {
          setTimeout(() => {
            request(url, method, data, retryCount + 1).then(resolve, reject)
          }, 1000 * (retryCount + 1))
        } else {
          wx.showToast({ title: '网络错误，请检查网络', icon: 'none' })
          reject(err)
        }
      }
    })
  })
}

module.exports = { request }
