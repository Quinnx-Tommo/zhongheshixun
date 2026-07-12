// pages/exam/do/do.js
const examApi = require('../../../api/exam')

// 网页端考试地址（与 list.js 保持一致）
// 生产环境请替换为真实域名
const WEB_EXAM_URL = 'http://localhost:5174/exam'

Page({
  data: {
    examId: null,
    exam: null,
    questions: [],
    currentIndex: 0,
    answers: {},            // { questionId: 'A' 或 ['A','C'] 或 '答案文本' }
    timeLeft: 0,            // 剩余秒数
    timer: null,
    formattedTime: '00:00',
    submitting: false
  },

  async onLoad(options) {
    const examId = Number(options.id)
    // 兜底：若用户通过分享链接直链进入答题页，提示并返回列表
    wx.showModal({
      title: '请使用网页端参加考试',
      content: '小程序端暂不支持在线考试，请在电脑或手机浏览器中打开网页端完成考试。',
      showCancel: true,
      cancelText: '返回考试列表',
      confirmText: '复制网页链接',
      success: (res) => {
        if (res.confirm) {
          this.copyWebUrl(examId)
        } else {
          wx.redirectTo({ url: '/pages/exam/list/list' })
        }
      }
    })
    // 不再加载试卷 / 启动倒计时
    return
    // ── 以下代码保留但不会执行，作为网页端考试备用路径的注释参考 ──
    /*
    this.setData({ examId })
    wx.showLoading({ title: '加载试卷中' })
    try {
      const res = await examApi.startExam(examId)
      this.setData({
        exam: { id: res.id, title: res.title, duration: res.duration },
        questions: res.questions,
        timeLeft: res.duration * 60
      })
      this.updateFormattedTime()
      this.startTimer()
    } catch (e) {
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
    */
  },

  // 复制网页端考试链接到剪贴板
  copyWebUrl(examId) {
    const url = `${WEB_EXAM_URL}?examId=${examId}`
    wx.setClipboardData({
      data: url,
      success: () => {
        wx.showToast({ title: '链接已复制，请在浏览器打开', icon: 'none', duration: 2500 })
      },
      fail: () => {
        wx.showToast({ title: '复制失败，请手动访问网页端', icon: 'none', duration: 2500 })
      }
    })
  },

  onUnload() {
    // 页面卸载时清理定时器
    if (this.data.timer) {
      clearInterval(this.data.timer)
    }
  },

  // 启动倒计时
  startTimer() {
    const timer = setInterval(() => {
      if (this.data.timeLeft <= 0) {
        clearInterval(timer)
        this.setData({ timer: null })
        wx.showToast({ title: '考试时间到，自动交卷', icon: 'none' })
        this.submitExam()
        return
      }
      this.setData({ timeLeft: this.data.timeLeft - 1 })
      this.updateFormattedTime()
    }, 1000)
    this.setData({ timer })
  },

  // 更新格式化的时间显示
  updateFormattedTime() {
    const s = this.data.timeLeft
    const m = Math.floor(s / 60)
    const sec = s % 60
    const formatted = `${m.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}`
    this.setData({ formattedTime: formatted })
  },

  // 选择选项（单选/判断）
  onSelectOption(e) {
    const { qid, opt, type } = e.currentTarget.dataset
    if (type === '2') {
      // 多选：toggle
      const cur = this.data.answers[qid] ? [...this.data.answers[qid]] : []
      const idx = cur.indexOf(opt)
      if (idx >= 0) cur.splice(idx, 1)
      else cur.push(opt)
      this.setData({ [`answers.${qid}`]: cur })
    } else {
      // 单选/判断
      this.setData({ [`answers.${qid}`]: opt })
    }
  },

  // 填空题输入
  onFillBlank(e) {
    const qid = e.currentTarget.dataset.qid
    this.setData({ [`answers.${qid}`]: e.detail.value })
  },

  // 判断选项是否已选中
  isSelected(qid, opt) {
    const ans = this.data.answers[qid]
    if (!ans) return false
    if (Array.isArray(ans)) return ans.includes(opt)
    return ans === opt
  },

  // 上一题
  prevQ() {
    if (this.data.currentIndex > 0) {
      this.setData({ currentIndex: this.data.currentIndex - 1 })
    }
  },

  // 下一题
  nextQ() {
    if (this.data.currentIndex < this.data.questions.length - 1) {
      this.setData({ currentIndex: this.data.currentIndex + 1 })
    }
  },

  // 提交考试
  async submitExam() {
    if (this.data.submitting) return

    // 先清理定时器
    if (this.data.timer) {
      clearInterval(this.data.timer)
      this.setData({ timer: null })
    }

    // 确认对话框
    const answersCount = Object.keys(this.data.answers).length
    const total = this.data.questions.length
    const content = answersCount < total
      ? `还有 ${total - answersCount} 题未作答，确定交卷？`
      : '确认提交试卷？'

    wx.showModal({
      title: '交卷',
      content,
      confirmButtonText: '提交',
      cancelButtonText: '继续答题',
      success: async (res) => {
        if (res.confirm) {
          await this.doSubmit()
        } else {
          // 恢复倒计时
          this.startTimer()
        }
      }
    })
  },

  // 实际提交
  async doSubmit() {
    this.setData({ submitting: true })
    wx.showLoading({ title: '提交中', mask: true })
    try {
      const answersArr = this.formatAnswers()
      const result = await examApi.submitExam({
        examId: this.data.examId,
        answers: answersArr
      })
      wx.redirectTo({
        url: `/pages/exam/result/result?id=${this.data.examId}&score=${result.score}&passed=${result.passed}&correct=${result.correctCount}&total=${result.totalCount}&from=do`
      })
    } catch (e) {
      wx.showToast({ title: '提交失败，请重试', icon: 'none' })
      this.setData({ submitting: false })
    } finally {
      wx.hideLoading()
    }
  },

  // 格式化答案
  formatAnswers() {
    const arr = []
    for (const q of this.data.questions) {
      const ans = this.data.answers[q.id]
      if (ans === undefined || ans === null || ans === '') continue
      const answer = Array.isArray(ans) ? ans.join('') : ans
      arr.push({ questionId: q.id, answer })
    }
    return arr
  }
})
