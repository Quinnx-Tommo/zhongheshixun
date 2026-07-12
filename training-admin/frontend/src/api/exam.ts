import request from './request'

export interface ExamQuery {
  pageNum: number
  pageSize: number
  title?: string
  status?: number
  courseId?: number
}

export function getExamPage(params: ExamQuery) {
  return request({ url: '/admin/exam/page', method: 'GET', params })
}

export function createExam(data: any) {
  return request({ url: '/admin/exam', method: 'POST', data })
}

export function updateExam(data: any) {
  return request({ url: '/admin/exam', method: 'PUT', data })
}

export function deleteExam(id: number) {
  return request({ url: `/admin/exam/${id}`, method: 'DELETE' })
}

export function getExamDetail(id: number) {
  return request({ url: `/admin/exam/${id}`, method: 'GET' })
}

export function generateExamPaper(data: { examId: number; knowledgePointIds: number[] }) {
  return request({ url: '/admin/exam/generate', method: 'POST', data })
}

/**
 * P1-5 修复：发布考试（status 0 草稿 → 1 已发布）
 * 后端 ExamServiceImpl.publish 自动组卷兜底 + 题量校验，前端无需额外组装。
 */
export function publishExam(id: number) {
  return request({ url: `/admin/exam/publish/${id}`, method: 'POST' })
}

/**
 * P1-5 修复：下架考试（status 1 已发布 → 2 已下架）
 */
export function offlineExam(id: number) {
  return request({ url: `/admin/exam/offline/${id}`, method: 'POST' })
}

export function getKnowledgePage(params: { pageNum?: number; pageSize?: number; title?: string }) {
  return request({ url: '/admin/knowledge/page', method: 'GET', params })
}
