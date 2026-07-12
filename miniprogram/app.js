// miniprogram/app.js
App({
  onLaunch() {
    // 不再强制跳登录：让 tabBar 首页正常展示，内部页面各自检查登录态
    // 首次启动可清理过期 token（可选）
    const token = wx.getStorageSync('token')
    if (token) {
      // 校验 token 格式是否完整，不完整则清理
      const parts = token.split('.')
      if (parts.length !== 3) {
        wx.removeStorageSync('token')
        wx.removeStorageSync('userInfo')
      }
    }

    // 监听网络恢复，同步离线进度（后续实现）
    wx.onNetworkStatusChange((res) => {
      if (res.isConnected) {
        // TODO: 同步离线进度
        console.log('网络已恢复')
      }
    })
  },

  // 全局数据
  globalData: {
    userInfo: null,
    token: null
  }
})
