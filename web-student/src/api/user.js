import axios from 'axios'
import request from '@/utils/request'

// 登录接口：走 /admin 代理到 8080（不走 /api，不需要 JWT）
export const login = (data) =>
  axios.post('/admin/login', data).then((res) => res.data)

// 获取个人资料：走 /api 代理到 8081（需要 JWT，用 request 实例）
export const getProfile = () =>
  request({ url: '/user/profile', method: 'GET' })

// 更新个人资料：走 /api 代理到 8081（需要 JWT，用 request 实例）
export const updateProfile = (data) =>
  request({ url: '/user/profile', method: 'PUT', data })
