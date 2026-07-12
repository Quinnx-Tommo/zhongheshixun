import request from './request'

export interface ChapterQuery {
  pageNum: number
  pageSize: number
  courseId: number
  title?: string
}

export function getChapterPage(params: ChapterQuery) {
  return request({ url: '/admin/chapter/page', method: 'GET', params })
}

export function getChapterByCourse(courseId: number) {
  return request({ url: `/admin/chapter/course/${courseId}`, method: 'GET' })
}

export function createChapter(data: any) {
  return request({ url: '/admin/chapter', method: 'POST', data })
}

export function updateChapter(data: any) {
  return request({ url: '/admin/chapter', method: 'PUT', data })
}

export function deleteChapter(id: number) {
  return request({ url: `/admin/chapter/${id}`, method: 'DELETE' })
}

export function sortChapter(ids: number[]) {
  return request({ url: '/admin/chapter/sort', method: 'PUT', data: ids })
}
