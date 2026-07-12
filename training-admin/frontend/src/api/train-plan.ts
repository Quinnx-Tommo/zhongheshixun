import request from './request'

export interface TrainPlanQuery {
  pageNum: number
  pageSize: number
  title?: string
  status?: number
}

export function getTrainPlanPage(params: TrainPlanQuery) {
  return request({ url: '/admin/train-plan/page', method: 'GET', params })
}

export function createTrainPlan(data: any) {
  return request({ url: '/admin/train-plan', method: 'POST', data })
}

export function updateTrainPlan(data: any) {
  return request({ url: '/admin/train-plan', method: 'PUT', data })
}

export function deleteTrainPlan(id: number) {
  return request({ url: `/admin/train-plan/${id}`, method: 'DELETE' })
}

export function getTrainPlanDetail(id: number) {
  return request({ url: `/admin/train-plan/${id}`, method: 'GET' })
}

export function addPlanCourses(data: { planId: number; courseIds: number[] }) {
  return request({ url: '/admin/train-plan/courses', method: 'POST', data })
}

export function removePlanCourse(id: number) {
  return request({ url: `/admin/train-plan/courses/${id}`, method: 'DELETE' })
}
