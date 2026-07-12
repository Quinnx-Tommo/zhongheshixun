import request from './request'

export interface CourseQuery {
  pageNum: number
  pageSize: number
  title?: string
  courseType?: number
  status?: number
}

export function getCoursePage(params: CourseQuery) {
  return request({ url: '/admin/course/page', method: 'GET', params })
}

export function createCourse(data: any) {
  return request({ url: '/admin/course', method: 'POST', data })
}

export function updateCourse(data: any) {
  return request({ url: '/admin/course', method: 'PUT', data })
}

export function publishCourse(data: { id: number; status: number }) {
  return request({ url: '/admin/course/publish', method: 'PUT', data })
}

export function deleteCourse(id: number) {
  return request({ url: `/admin/course/${id}`, method: 'DELETE' })
}

export function getCourseDetail(id: number) {
  return request({ url: `/admin/course/${id}`, method: 'GET' })
}
