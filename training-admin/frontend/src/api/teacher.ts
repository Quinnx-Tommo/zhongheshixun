import request from './request'

export interface TeacherQuery {
  pageNum: number
  pageSize: number
  realName?: string
  direction?: string
  keyword?: string
}

export interface TeacherDTO {
  id?: number
  userId?: number
  realName: string
  title?: string
  education?: string
  direction?: string
  intro?: string
  createTime?: string
}

export function getTeacherPage(params: TeacherQuery) {
  return request({ url: '/admin/teacher/page', method: 'GET', params })
}

export function createTeacher(data: TeacherDTO) {
  return request({ url: '/admin/teacher', method: 'POST', data })
}

export function updateTeacher(data: TeacherDTO) {
  return request({ url: '/admin/teacher', method: 'PUT', data })
}

export function deleteTeacher(id: number) {
  return request({ url: `/admin/teacher/${id}`, method: 'DELETE' })
}
