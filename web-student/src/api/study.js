import request from '@/utils/request'

// 报名课程
export const enrollCourse = (courseId) =>
  request({ url: '/study/enroll', method: 'POST', data: { courseId } })

// 上报学习进度
export const reportProgress = (data) =>
  request({ url: '/study/progress', method: 'POST', data })

// 获取某课程学习进度
export const getProgress = (courseId) =>
  request({ url: `/study/progress/${courseId}`, method: 'GET' })

// 我的课程（分页）
export const getMyCourses = (params) =>
  request({ url: '/study/my-courses', method: 'GET', params })
