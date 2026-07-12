import request from '@/utils/request'

// 我的学习统计
// P2-11 修复：后端 /api/stats/my 不接受 studentId 参数（统一从 request attribute 取 userId），
// 这里签名保留可选参数仅为兼容旧调用，调用方无需再传 studentId。
export const getMyStats = () =>
  request({ url: '/stats/my', method: 'GET' })
