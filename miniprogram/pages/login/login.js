// miniprogram/pages/login/login.js
const userApi = require('../../api/user')

Page({
  data: {
    loading: false,
    agreed: false  // 是否同意用户协议
  },

  // 用户协议勾选
  onAgree(e) {
    this.setData({ agreed: e.detail.value.length > 0 })
  },

  // 微信一键登录
  onLogin() {
    // 1. 检查是否同意用户协议
    if (!this.data.agreed) {
      wx.showToast({ title: '请先同意用户协议', icon: 'none' })
      return
    }

    if (this.data.loading) return
    this.setData({ loading: true })

    wx.showLoading({ title: '登录中...' })

    // 2. 获取微信临时 code
    wx.login({
      success: async (res) => {
        if (!res.code) {
          wx.hideLoading()
          wx.showToast({ title: '获取 code 失败', icon: 'none' })
          this.setData({ loading: false })
          return
        }
        try {
          // 3. 调用后端登录接口
          const result = await userApi.login({
            code: res.code,
            nickname: '学员',
            avatar: ''
          })
          // 4. 存储 token + userInfo
          wx.setStorageSync('token', result.token)
          wx.setStorageSync('userInfo', result.userInfo)
          wx.hideLoading()
          wx.showToast({ title: '登录成功', icon: 'success' })
          // 5. 跳转首页
          setTimeout(() => {
            wx.reLaunch({ url: '/pages/index/index' })
          }, 1500)
        } catch (err) {
          wx.hideLoading()
          console.error('登录失败', err)
          wx.showToast({ title: '登录失败，请重试', icon: 'none' })
        } finally {
          this.setData({ loading: false })
        }
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '微信登录失败', icon: 'none' })
        this.setData({ loading: false })
      }
    })
  }
})
