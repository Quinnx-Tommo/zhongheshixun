// miniprogram/pages/index/index.js
const courseApi = require('../../api/course')
const statsApi = require('../../api/stats')
const examApi = require('../../api/exam')

// ============= 工具函数 =============

// 计算某课程的整体进度（章节级），失败返回 0
async function fetchOverallProgress(courseId) {
  try {
    const p = await courseApi.getProgress(courseId)
    if (!p || !p.length) return 0
    const sum = p.reduce((s, ch) => s + (ch.progress || 0), 0)
    return Math.round(sum / p.length)
  } catch (e) {
    return 0
  }
}

Page({
  data: {
    userInfo: null,
    banners: [
      { id: 1, image: '/images/banner1.jpg' },
      { id: 2, image: '/images/banner2.jpg' }
    ],
    entries: [
      { icon: '/images/icon-course.png', text: '课程', url: '/pages/course/list/list' },
      { icon: '/images/icon-exam.png', text: '考试', url: '/pages/exam/list/list' },
      { icon: '/images/icon-my.png', text: '我的', url: '/pages/profile/profile' },
      { icon: '/images/icon-consult.png', text: '咨询', url: '/pages/consult/index/index' }
    ],
    recommendList: [],     // 推荐课程（前 6 条）
    recentCourses: [],     // 最近学习的课程（前 2 条，带 overallProgress）
    stats: {
      courseCount: 0,      // 已报名课程数
      studyHours: 0,       // 总学时
      completedCourses: 0, // 已完成课程数
      examCount: 0,        // 考试次数
      passedCount: 0,      // 通过次数
      avgScore: 0,         // 平均分
      passRate: 0          // 考试通过率（整数 %，预计算避免模板做除法）
    },
    loading: true
  },

  onLoad() {
    // 1. 同步本地用户信息（未登录也可显示首页骨架）
    this.setData({ userInfo: wx.getStorageSync('userInfo') })
    // 2. 并发加载推荐 + 统计 + 最近学习
    this.loadAll()
  },

  onShow() {
    // 对齐 profile.js：tabBar 首页也做登录态检查
    if (!wx.getStorageSync('token')) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }
    // 已登录则刷新数据
    this.setData({ userInfo: wx.getStorageSync('userInfo') })
    this.loadAll()
  },

  // 并发拉取推荐课程 / 我的统计 / 最近学习
  async loadAll() {
    this.setData({ loading: true })

    const [recRes, statsRes, myRes] = await Promise.allSettled([
      courseApi.getList({ pageNum: 1, pageSize: 6 }),
      statsApi.getMyStats(),
      courseApi.getMyCourses({ pageNum: 1, pageSize: 50 })
    ])

    // ---- 推荐课程 ----
    const recommendList = (recRes.status === 'fulfilled' && recRes.value)
      ? (recRes.value.records || []).slice(0, 6)
      : []

    // ---- 我的课程（最近学习前 2 条） ----
    const myRecords = (myRes.status === 'fulfilled' && myRes.value)
      ? (myRes.value.records || []).slice(0, 2)
      : []

    // 为最近学习的 2 门课程并发查询章节级进度 → overallProgress
    const recentCourses = await Promise.all(myRecords.map(async (c) => {
      const overallProgress = await fetchOverallProgress(c.id)
      return { ...c, overallProgress }
    }))

    // ---- 统计字段 ----
    let stats = {
      courseCount: 0,
      studyHours: 0,
      completedCourses: 0,
      examCount: 0,
      passedCount: 0,
      avgScore: 0,
      passRate: 0
    }

    if (statsRes.status === 'fulfilled' && statsRes.value) {
      const s = statsRes.value
      const examCount = s.examCount ?? 0
      const passedCount = s.passedCount ?? 0
      stats = {
        courseCount: s.enrolledCount ?? s.courseCount ?? 0,
        studyHours: s.totalStudyHours ?? s.studyHours ?? 0,
        completedCourses: s.completedCourses ?? 0,
        examCount,
        passedCount,
        avgScore: s.avgScore ?? 0,
        passRate: examCount > 0 ? Math.round(passedCount * 100 / examCount) : 0
      }
    } else {
      // 降级：从 my-courses 凑 courseCount / studyHours
      const myForFallback = (myRes.status === 'fulfilled' && myRes.value)
        ? (myRes.value.records || []) : []
      stats.courseCount = myForFallback.length
      stats.studyHours = myForFallback.reduce(
        (sum, c) => sum + (c.totalHours || c.total_hours || 0), 0
      )
      // 平均分从 examApi 补算
      try {
        const records = await examApi.getMyRecords()
        if (Array.isArray(records) && records.length) {
          const examCount = records.length
          const passedCount = records.filter(r => r.passed).length
          stats.examCount = examCount
          stats.passedCount = passedCount
          stats.passRate = Math.round(passedCount * 100 / examCount)
          const total = records.reduce((s, r) => s + (r.score || 0), 0)
          stats.avgScore = Math.round(total / records.length * 10) / 10
        }
      } catch (e) {
        // 降级失败保持默认 0
      }
    }

    this.setData({
      recommendList,
      recentCourses,
      stats,
      loading: false
    })
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.loadAll().then(() => {
      wx.stopPullDownRefresh()
    })
  },

  // 跳转 - 课程详情
  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/course/detail/detail?id=${id}` })
  },

  // 跳转 - 学习页面
  goStudy(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/course/study/study?id=${id}` })
  },

  // 跳转 - 我的课程列表（最近学习"全部"入口）
  onGoMyCourses() {
    wx.navigateTo({ url: '/pages/course/my-courses/my-courses' })
  },

  // 跳转 - 课程中心（推荐课程"更多"/空态引导入口）
  onGoCourseList() {
    wx.navigateTo({ url: '/pages/course/list/list' })
  },

  // 跳转 - 快捷入口（区分 switchTab / navigateTo）
  onGoTo(e) {
    const url = e.currentTarget.dataset.url
    const tabBarPages = [
      '/pages/index/index',
      '/pages/plan/plan',
      '/pages/course/my-courses/my-courses',
      '/pages/consult/index/index',
      '/pages/profile/profile'
    ]
    if (tabBarPages.includes(url)) {
      wx.switchTab({ url })
    } else {
      wx.navigateTo({ url })
    }
  },

  // 退出登录
  onLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('token')
          wx.removeStorageSync('userInfo')
          wx.reLaunch({ url: '/pages/login/login' })
        }
      }
    })
  }
})
