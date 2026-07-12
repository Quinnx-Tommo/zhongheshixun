// pages/course/my-courses/my-courses.js
// 我的学习：已报名课程列表（含学习进度）
const courseApi = require('../../../api/course')

Page({
  data: {
    list: [],         // 课程列表（含 progress 字段）
    pageNum: 1,
    pageSize: 10,
    hasMore: true,
    loading: false,   // 首次加载显示骨架屏
    appendLoading: false // 底部上拉加载更多
  },

  onLoad() {
    this.loadData(false)
  },

  onShow() {
    // 检查登录态
    const token = wx.getStorageSync('token')
    if (!token) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      setTimeout(() => {
        wx.reLaunch({ url: '/pages/login/login' })
      }, 800)
    }
  },

  // 加载数据；append=true 表示上拉追加
  async loadData(append = false) {
    if (append) {
      if (!this.data.hasMore) return
      this.setData({ appendLoading: true })
    } else {
      this.setData({ loading: true })
    }

    try {
      const pageNum = append ? this.data.pageNum + 1 : 1
      const res = await courseApi.getMyCourses({ pageNum, pageSize: this.data.pageSize })

      // 并发拉取每门课的学习进度
      const withProgress = await Promise.all((res.records || []).map(async (c) => {
        try {
          const prog = await courseApi.getProgress(c.id)
          // prog 可能是章节数组或对象；这里取 progress 百分比
          const progress = (prog && typeof prog === 'object' && 'progress' in prog)
            ? prog.progress
            : (Array.isArray(prog) ? this.calcProgress(prog) : 0)
          return { ...c, progress: progress || 0 }
        } catch (e) {
          return { ...c, progress: 0 }
        }
      }))

      const list = append ? this.data.list.concat(withProgress) : withProgress
      this.setData({
        list,
        pageNum,
        hasMore: res.hasMore,
        loading: false,
        appendLoading: false
      })
    } catch (e) {
      console.error('[my-courses] load error', e)
      this.setData({ loading: false, appendLoading: false })
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  },

  // 章节数组 → 完成百分比
  calcProgress(chapters) {
    if (!chapters || chapters.length === 0) return 0
    const done = chapters.filter(ch => ch.completed || ch.progress >= 100).length
    return Math.round((done / chapters.length) * 100)
  },

  // 上拉触底 → 加载下一页
  onReachBottom() {
    if (this.data.hasMore && !this.data.appendLoading) {
      this.loadData(true)
    }
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.loadData(false).then(() => {
      wx.stopPullDownRefresh()
    }).catch(() => {
      wx.stopPullDownRefresh()
    })
  },

  // 跳转到学习页
  goStudy(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/pages/course/study/study?id=${id}` })
  },

  // 跳转到课程详情
  goDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/pages/course/detail/detail?id=${id}` })
  }
})
