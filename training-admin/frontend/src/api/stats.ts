import request from './request'

// 数据概览
export function getOverview() {
  return request({ url: '/admin/stats/overview', method: 'GET' })
}

// 学员学习统计（分页）
export function getStudentStats(params: {
  pageNum: number
  pageSize: number
  keyword?: string
  orgName?: string
}) {
  return request({ url: '/admin/stats/student', method: 'GET', params })
}

// 考试统计
export function getExamStats(examId?: number) {
  return request({
    url: '/admin/stats/exam',
    method: 'GET',
    params: examId ? { examId } : {}
  })
}

// 课程热度统计（分页）
export function getCourseStats(params: { pageNum: number; pageSize: number; keyword?: string }) {
  return request({ url: '/admin/stats/course', method: 'GET', params })
}

// 机构维度统计（分页）
export function getOrgStats(params: { pageNum: number; pageSize: number }) {
  return request({ url: '/admin/stats/org', method: 'GET', params })
}

// 时间趋势统计
export function getTrend(params: { granularity?: string; recentDays?: number }) {
  return request({ url: '/admin/stats/trend', method: 'GET', params })
}
