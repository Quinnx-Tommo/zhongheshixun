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

// SLA 超时告警列表（默认 1 分钟阈值，与教师要求 SLA < 1min 一致）
export function getSlaAlert(slaMinutes = 1) {
  return request({ url: '/admin/consult/sla-alert', method: 'GET', params: { slaMinutes } })
}
