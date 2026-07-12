// miniprogram/api/user.js — 用户/登录
const { request } = require('./request')
module.exports = {
  // 微信登录：wx.login 获取 code → 后端换取 token
  login: (data) => request('/wx/login', 'POST', data),
  // 获取当前用户信息
  getProfile: () => request('/user/profile', 'GET'),
  // 更新昵称头像
  updateProfile: (data) => request('/user/profile', 'PUT', data)
}
