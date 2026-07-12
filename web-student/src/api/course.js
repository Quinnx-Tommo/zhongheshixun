import request from '@/utils/request'

// 课程列表（分页，仅已发布）
export const getCourseList = (params) =>
  request({ url: '/course/list', method: 'GET', params })

// 课程详情
export const getCourseDetail = (id) =>
  request({ url: `/course/detail/${id}`, method: 'GET' })

// 课程章节列表
export const getChapterList = (courseId) =>
  request({ url: '/course/chapter/list', method: 'GET', params: { courseId } })
