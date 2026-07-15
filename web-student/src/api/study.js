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

// 检查是否已报名某课程（轻量接口，避免拉取整页 my-courses）
export const checkEnrolled = (courseId) =>
  request({ url: '/study/check-enrolled', method: 'GET', params: { courseId } })

// 标记章节完成（等价于上报 progress=100 + completed=true）
export const completeChapter = (data) =>
  request({ url: '/study/complete-chapter', method: 'POST', data })
