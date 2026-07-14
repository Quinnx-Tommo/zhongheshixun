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
