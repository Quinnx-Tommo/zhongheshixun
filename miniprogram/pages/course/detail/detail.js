// miniprogram/pages/course/detail/detail.js
const courseApi = require('../../../api/course')

Page({
  data: {
    courseId: null,
    detail: null,
    loading: true,
    enrolled: false,      // 是否已报名
    progress: null        // 章节级学习进度
  },

  onLoad(options) {
    const id = options.id
    if (!id) {
      wx.showToast({ title: '参数错误', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1000)
      return
    }
    this.setData({ courseId: id })
    this.loadDetail(id)
    // 查询报名与进度
    this.refreshEnrollState(id)
  },

  onShow() {
    if (!wx.getStorageSync('token')) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    // 返回时刷新报名/进度
    if (this.data.courseId) {
      this.refreshEnrollState(this.data.courseId)
    }
  },

  async loadDetail(id) {
    wx.showLoading({ title: '加载中' })
    try {
      const detail = await courseApi.getDetail(id)
      this.setData({ detail, loading: false })
    } catch (err) {
      console.error('加载课程详情失败', err)
      wx.showToast({ title: '加载失败', icon: 'none' })
    } finally {
      wx.hideLoading()
    }
  },

  // 查询我是否报名了该课程 + 章节学习进度
  async refreshEnrollState(id) {
    try {
      const progress = await courseApi.getProgress(id)
      // progress: [{ chapterId, progress, studyDuration }] 章节级
      let enrolled = false
      if (progress && progress.length > 0) {
        enrolled = true
      } else {
        // 没进度 → 查我的课程列表判定是否报名
        const my = await courseApi.getMyCourses({ pageNum: 1, pageSize: 50 })
        enrolled = (my.records || []).some(c => String(c.id) === String(id))
      }
      this.setData({ enrolled, progress })
    } catch (e) {
      // 忽略
    }
  },

  // 报名
  onEnroll() {
    if (this.data.enrolled) {
      wx.showToast({ title: '已报名', icon: 'none' })
      return
    }
    wx.showModal({
      title: '提示',
      content: '确定要报名这门课程吗？',
      success: async (res) => {
        if (!res.confirm) return
        try {
          await courseApi.enroll(this.data.courseId)
          wx.showToast({ title: '报名成功', icon: 'success' })
          this.setData({ enrolled: true })
        } catch (e) {
          wx.showToast({ title: '报名失败，请重试', icon: 'none' })
        }
      }
    })
  },

  onStartLearn() {
    const { detail } = this.data
    if (!detail || !detail.chapters || detail.chapters.length === 0) {
      wx.showToast({ title: '暂无章节', icon: 'none' })
      return
    }
    // 跳转到学习页面
    wx.navigateTo({ url: `/pages/course/study/study?id=${this.data.courseId}` })
  }
})
