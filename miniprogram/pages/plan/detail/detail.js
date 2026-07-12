// miniprogram/pages/plan/detail/detail.js
// 培训计划详情页：展示计划整体进度 + 计划下课程列表，点击课程跳转课程详情
const planApi = require('../../../api/plan')

Page({
  data: {
    planId: null,
    plan: null,            // { id, title, description, startTime, endTime, courseList, totalCount, completedCount, progress }
    courseList: [],
    loading: true
  },

  onLoad(options) {
    const id = options.id
    if (!id) {
      wx.showToast({ title: '参数错误', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1000)
      return
    }
    this.setData({ planId: id })
    this.loadDetail(id)
  },

  onShow() {
    // 检查登录态（与 plan.js / course/detail 一致）
    if (!wx.getStorageSync('token')) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    // 返回页面时刷新进度
    if (this.data.planId) {
      this.loadDetail(this.data.planId)
    }
  },

  onPullDownRefresh() {
    this.loadDetail(this.data.planId).then(() => wx.stopPullDownRefresh())
  },

  async loadDetail(id) {
    wx.showLoading({ title: '加载中' })
    try {
      const plan = await planApi.getDetail(id)
      this.setData({
        plan,
        courseList: plan.courseList || [],
        loading: false
      })
    } catch (err) {
      console.error('加载计划详情失败', err)
      wx.showToast({ title: '加载失败', icon: 'none' })
      this.setData({ loading: false })
    } finally {
      wx.hideLoading()
    }
  },

  // 课程卡片点击 → 跳转课程详情（让用户自己报名/学习）
  goCourseDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/course/detail/detail?id=${id}` })
  },

  // "查看课程" 按钮 → 同样跳课程详情（MVP 阶段无 enrolled 字段，统一走 detail）
  onViewCourse(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/course/detail/detail?id=${id}` })
  },

  // 防止按钮事件冒泡触发卡片点击（冗余保护）
  noop() {}
})
