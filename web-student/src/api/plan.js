import request from '@/utils/request'

// 培训计划列表（仅返回已发布的）
export const getPlanList = (params) =>
  request({ url: '/plan/list', method: 'GET', params })

// 培训计划详情
export const getPlanDetail = (id) =>
  request({ url: `/plan/detail/${id}`, method: 'GET' })

// 我的培训计划
export const getMyPlans = (params) =>
  request({ url: '/plan/my', method: 'GET', params })
