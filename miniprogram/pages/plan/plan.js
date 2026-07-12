// miniprogram/pages/plan/plan.js
const courseApi = require('../../api/course')
const examApi = require('../../api/exam')

Page({
  data: {
    planList: [],
    stats: {
      totalCourse: 0,
      finished: 0,
      learning: 0,
      hours: 0
    },
    monthProgress: 0
  },

  onLoad() {
    this.loadPlan()
  },

  onShow() {
    if (!wx.getStorageSync('token')) {
      wx.reLaunch({ url: '/pages/login/login' })
    }
  },

  onPullDownRefresh() {
    this.loadPlan().then(() => wx.stopPullDownRefresh())
  },

  async loadPlan() {
    wx.showLoading({ title: '加载中' })
    try {
      // 1. 取我报名的课程（真实）
      const courseRes = await courseApi.getMyCourses({ pageNum: 1, pageSize: 50 })
        .catch(() => ({ records: [], total: 0 }))
      const enrolledRecords = courseRes.records || []

      // 2. 取我的考试记录（真实，用于统计已完成）
      const examRes = await examApi.getMyRecords().catch(() => [])
      const examRecords = Array.isArray(examRes) ? examRes : []
      const passedExams = examRecords.filter(r => r.passed).length

      // 3. 真实学习进度（章节级）或估算
      const planList = await Promise.all(
        enrolledRecords.map(async (c) => {
          // 尝试拉真实进度
          let progress = 0
          try {
            const p = await courseApi.getProgress(c.id)
            if (p && p.length > 0) {
              // 所有章节都完成 100% → 100；否则取平均
              const done = p.filter(ch => (ch.progress || 0) >= 100).length
              progress = done === p.length
                ? 100
                : Math.round(p.reduce((s, ch) => s + (ch.progress || 0), 0) / p.length)
            }
          } catch (e) {
            // 忽略，使用估算
          }

          return {
            id: c.id,
            title: c.title,
            courseType: c.courseType ?? c.course_type,
            teacher: c.teacherName || c.teacher_name || '专家团队',
            description: c.description || '',
            totalHours: c.totalHours ?? (c.total_hours || 0),
            progress
          }
        })
      )

      // 统计：已完成为 progress===100 的课程数 + passedExams
      const finished = planList.filter(p => p.progress >= 100).length + passedExams
      const learning = planList.filter(p => p.progress > 0 && p.progress < 100).length
      const total = planList.length
      const monthProgress = total === 0 ? 0 : Math.round((finished / Math.max(1, total)) * 100)
      const hours = planList.reduce((s, p) => s + (p.totalHours || 0), 0)

      this.setData({
        planList,
        stats: {
          totalCourse: total,
          finished,
          learning,
          hours
        },
        monthProgress
      })
    } catch (e) {
      // request 统一 toast
    } finally {
      wx.hideLoading()
    }
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/course/detail/detail?id=${id}` })
  }
})
