import request from './request'

// 咨询分页
export function getConsultPage(params: {
  pageNum: number
  pageSize: number
  keyword?: string
  isAuto?: number
}) {
  return request({ url: '/admin/consult/page', method: 'GET', params })
}

// 人工回复
export function replyConsult(data: { id: number; reply: string }) {
  return request({ url: '/admin/consult/reply', method: 'POST', data })
}

// SLA 超时告警列表
export function getSlaAlert(slaHours = 24) {
  return request({ url: '/admin/consult/sla-alert', method: 'GET', params: { slaHours } })
}

// 知识库分页
export function getKnowledgePage(params: {
  pageNum: number
  pageSize: number
  keyword?: string
  category?: string
}) {
  return request({ url: '/admin/consult/knowledge-base/list', method: 'GET', params })
}

// 新增知识库
export function createKnowledge(data: any) {
  return request({ url: '/admin/consult/knowledge-base', method: 'POST', data })
}

// 编辑知识库
export function updateKnowledge(data: any) {
  return request({ url: '/admin/consult/knowledge-base', method: 'PUT', data })
}

// 删除知识库
export function deleteKnowledge(id: number) {
  return request({ url: `/admin/consult/knowledge-base/${id}`, method: 'DELETE' })
}
