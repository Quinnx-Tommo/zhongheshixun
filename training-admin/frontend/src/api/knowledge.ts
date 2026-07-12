import request from './request'

export interface KnowledgeQuery {
  pageNum: number
  pageSize: number
  courseId: number
  name?: string
}

export function getKnowledgePage(params: KnowledgeQuery) {
  return request({ url: '/admin/knowledge/page', method: 'GET', params })
}

export function createKnowledge(data: any) {
  return request({ url: '/admin/knowledge', method: 'POST', data })
}

export function updateKnowledge(data: any) {
  return request({ url: '/admin/knowledge', method: 'PUT', data })
}

export function deleteKnowledge(id: number) {
  return request({ url: `/admin/knowledge/${id}`, method: 'DELETE' })
}
