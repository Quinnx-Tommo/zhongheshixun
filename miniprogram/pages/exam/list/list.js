// pages/exam/list/list.js
const examApi = require('../../../api/exam')

// 网页端考试地址（生产环境请替换为真实域名）
// 开发环境: http://localhost:5174/exam
// 生产环境: https://你的域名/exam
const WEB_EXAM_URL = 'http://localhost:5174/exam'

Page({
  data: {
    allExams: [],         // exam/list
    myRecords: [],        // exam/my-records
    list: [],             // 前端 UI 用（已合并字段）
    tab: 'all',
    loading: false
  },

  onLoad() {
    this.loadData()
  },

  onShow() {
    // 已登录才拉真实数据
    if (wx.getStorageSync('token')) {
      this.loadData()
    } else {
      wx.reLaunch({ url: '/pages/login/login' })
    }
  },

  // Tab 切换
  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    if (tab === this.data.tab) return
    this.setData({ tab })
  },

  // 构建 myRecordMap: examId → record
  buildRecordMap(records) {
    const map = new Map()
    for (const r of records || []) {
      map.set(r.examId, r)
    }
    return map
  },

  // 将后端 exam 转为 list UI 项（依赖 record）
  mapExamItem(e, recordMap) {
    const record = recordMap.get(e.id)
    const maxRetry = e.maxRetry ?? 1
    const timesUsed = record ? (record.times || 1) : 0
    return {
      id: e.id,
      title: e.title,
      duration: e.duration || 0,
      questionCount: e.questionCount || 0,
      maxRetry,
      examType: e.examType,
      courseId: e.courseId,
      status: record ? 1 : 0,
      score: record ? record.score : null,
      recordId: record ? record.id : null,
      passed: record ? record.passed : null,
      retryLeft: Math.max(0, maxRetry - timesUsed)
    }
  },

  // 从本地 storage 兜底 record（后端异常时使用）
  fallbackRecords() {
    try {
      const raw = wx.getStorageSync('examRecords') || []
      return raw
    } catch (e) {
      return []
    }
  },

  async loadData() {
    if (this.data.loading) return
    this.setData({ loading: true })

    wx.showLoading({ title: '加载中' })
    try {
      const [exams, records] = await Promise.all([
        examApi.getList().catch(() => []),
        examApi.getMyRecords().catch(() => this.fallbackRecords())
      ])

      const recordMap = this.buildRecordMap(records)
      const list = exams.map(e => this.mapExamItem(e, recordMap))

      this.setData({
        allExams: exams,
        myRecords: records,
        list
      })
    } catch (e) {
      // 错误由 request 统一 toast
    } finally {
      wx.hideLoading()
      this.setData({ loading: false })
    }
  },

  // WXML 中调用，按 tab 过滤
  filteredList() {
    const { list, tab } = this.data
    if (tab === 'pending') return list.filter(i => i.status === 0)
    if (tab === 'done') return list.filter(i => i.status === 1)
    return list
  },

  goExam(e) {
    const { id, status, score, recordId, title } = e.currentTarget.dataset
    // 已考：保留"查看成绩"能力（学员可在小程序查看历史成绩）
    if (status === 1 && recordId) {
      wx.navigateTo({
        url: `/pages/exam/result/result?id=${recordId}&score=${score}&from=list`
      })
      return
    }
    if (status === 1) {
      // 兜底：没记录 id 用 examId
      wx.navigateTo({
        url: `/pages/exam/result/result?id=${id}&score=${score}&from=list`
      })
      return
    }
    // 待考：弹框引导至网页端考试
    this.showWebTip(id, title)
  },

  // 弹框提示：请使用网页端参加考试
  showWebTip(examId, examTitle) {
    wx.showModal({
      title: '请使用网页端参加考试',
      content: '小程序端暂不支持在线考试，请在电脑或手机浏览器中打开网页端完成考试。',
      showCancel: true,
      cancelText: '取消',
      confirmText: '复制网页链接',
      success: (res) => {
        if (res.confirm) {
          this.copyWebUrl(examId, examTitle)
        }
      }
    })
  },

  // 复制网页端考试链接到剪贴板
  copyWebUrl(examId, examTitle) {
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

  onPullDownRefresh() {
    this.loadData().then(() => wx.stopPullDownRefresh())
  }
})
