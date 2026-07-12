// pages/exam/result/result.js
const examApi = require('../../../api/exam')

Page({
  data: {
    id: null,            // 记录 id（优先）
    examId: null,        // 考试 id（兜底）
    score: 0,
    passed: false,
    correctCount: 0,
    totalCount: 0,
    from: 'do'
  },

  onLoad(options) {
    this.setData({
      id: options.id ? Number(options.id) : null,
      examId: options.examId ? Number(options.examId) : null,
      score: Number(options.score) || 0,
      passed: options.passed === 'true',
      correctCount: Number(options.correct) || 0,
      totalCount: Number(options.total) || 0,
      from: options.from || 'do'
    })

    // 如果有 examId 但无 score，拉服务器详情
    if (options.examId && !options.score) {
      this.loadRecord(Number(options.examId))
    }
  },

  // 从 recordId 拉详情
  async loadRecord(examId) {
    try {
      const list = await examApi.getMyRecords()
      const found = (list || []).find(r => r.examId === examId)
      if (found) {
        this.setData({
          score: found.score || 0,
          passed: !!found.passed,
          correctCount: found.correctCount || 0,
          totalCount: found.totalCount || 0
        })
      }
    } catch (e) {
      // 忽略
    }
  },

  // 返回考试列表
  goBack() {
    wx.redirectTo({ url: '/pages/exam/list/list' })
  },

  // 再考一次
  retryExam() {
    const id = this.data.id
    wx.redirectTo({ url: `/pages/exam/do/do?id=${id}` })
  },

  // 返回首页
  goHome() {
    wx.switchTab({ url: '/pages/index/index' })
  },

  // 分享
  onShareAppMessage() {
    return {
      title: `我考了 ${this.data.score} 分，一起来学习吧！`,
      path: '/pages/index/index'
    }
  }
})
