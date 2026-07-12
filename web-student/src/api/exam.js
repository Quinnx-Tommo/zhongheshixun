import request from '@/utils/request'

// 考试列表
export const getExamList = (params) =>
  request({ url: '/exam/list', method: 'GET', params })

// 开始考试（获取题目）
export const startExam = (id) =>
  request({ url: `/exam/start/${id}`, method: 'POST' })

// 提交答卷
export const submitExam = (data) =>
  request({ url: '/exam/submit', method: 'POST', data })

// 考试记录详情
export const getExamRecord = (id) =>
  request({ url: `/exam/record/${id}`, method: 'GET' })

// 我的考试记录
export const getMyRecords = (params) =>
  request({ url: '/exam/my-records', method: 'GET', params })

// 查看成绩（按 examId 查最新一条已批阅记录 + 聚合答题详情）
export const getExamResult = (examId) =>
  request({ url: '/exam/result', method: 'GET', params: { examId } })
