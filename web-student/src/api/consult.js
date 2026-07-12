import request from '@/utils/request'

// 提问
export const askConsult = (data) =>
  request({ url: '/consult/ask', method: 'POST', data })

// 我的咨询列表
export const getMyConsults = (params) =>
  request({ url: '/consult/my', method: 'GET', params })
