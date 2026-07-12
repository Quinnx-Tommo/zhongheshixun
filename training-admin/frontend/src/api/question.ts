import request from './request'

export interface QuestionQuery {
  pageNum: number
  pageSize: number
  courseId?: number
  title?: string
  type?: number
  difficulty?: number
}

export function getQuestionPage(params: QuestionQuery) {
  return request({ url: '/admin/question/page', method: 'GET', params })
}

export function createQuestion(data: any) {
  return request({ url: '/admin/question', method: 'POST', data })
}

export function updateQuestion(data: any) {
  return request({ url: '/admin/question', method: 'PUT', data })
}

export function deleteQuestion(id: number) {
  return request({ url: `/admin/question/${id}`, method: 'DELETE' })
}

export function getQuestionDetail(id: number) {
  return request({ url: `/admin/question/${id}`, method: 'GET' })
}
