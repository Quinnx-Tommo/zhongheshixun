// miniprogram/pages/course/study/study.js
// 课程学习页：视频播放 + 章节切换 + 进度上报（节流 10s）+ 断点续播
const studyApi = require('../../../api/study')

// 模块级变量：进度上报节流计时（避免高频写后端）
let _lastReport = 0

Page({
  data: {
    courseId: null,
    course: null,          // 课程信息
    chapters: [],          // 章节列表（含 progress/lastPosition）
    currentIndex: 0,       // 当前播放章节下标
    currentChapter: null,  // 当前播放章节对象
    progressMap: {},       // chapterId → { progress, lastPosition, studyDuration }
    overallProgress: 0,    // 总进度 0-100
    loading: true,         // 骨架屏开关
    videoContext: null     // wx.createVideoContext 实例
  },

  onLoad(options) {
    // 登录态检查
    if (!wx.getStorageSync('token')) {
      wx.reLaunch({ url: '/pages/login/login' })
      return
    }

    const courseId = options.id
    if (!courseId) {
      wx.showToast({ title: '参数错误', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1000)
      return
    }

    this.setData({ courseId })

    // 创建 video 上下文（必须在 onLoad 里，onReady 也行但早一点更稳）
    this.setData({
      videoContext: wx.createVideoContext('courseVideo', this)
    })

    this.loadCourseDetail(courseId)
  },

  onShow() {
    if (!wx.getStorageSync('token')) {
      wx.reLaunch({ url: '/pages/login/login' })
    }
  },

  // 一次性拉取：课程 + 章节 + 进度 + 是否报名
  async loadCourseDetail(courseId) {
    wx.showLoading({ title: '加载中' })
    try {
      const res = await studyApi.getCourseDetail(courseId)
      const { course, chapters, progress, enrolled, overallProgress } = res

      // 未报名 → 提示并返回
      if (!enrolled) {
        wx.showToast({ title: '请先报名', icon: 'none' })
        setTimeout(() => wx.navigateBack(), 1200)
        return
      }

      // 按 chapterId 索引进度
      const progressMap = {}
      ;(progress || []).forEach(p => {
        progressMap[p.chapterId] = {
          progress: p.progress ?? 0,
          lastPosition: p.lastPosition ?? p.last_position ?? 0,
          studyDuration: p.studyDuration ?? p.study_duration ?? 0
        }
      })

      const idx = this._findInitialIndex(chapters, progressMap)
      const currentChapter = chapters[idx] || null

      this.setData({
        course,
        chapters,
        progressMap,
        overallProgress: overallProgress || 0,
        currentIndex: idx,
        currentChapter,
        loading: false
      })

      // 首章节断点续播：等 video 渲染后 seek
      if (currentChapter) {
        const pos = progressMap[currentChapter.id]?.lastPosition || 0
        if (pos > 0) {
          // 延迟 300ms 等 video 组件就绪
          setTimeout(() => {
            if (this.data.videoContext) {
              this.data.videoContext.seek(pos)
            }
          }, 300)
        }
      }
    } catch (err) {
      console.error('加载课程详情失败', err)
      wx.showToast({ title: '加载失败，请重试', icon: 'none' })
      this.setData({ loading: false })
    } finally {
      wx.hideLoading()
    }
  },

  // 找初始播放章节：优先第一个 progress<100 的章节，否则第 0 章
  _findInitialIndex(chapters, progressMap) {
    if (!chapters || chapters.length === 0) return 0
    for (let i = 0; i < chapters.length; i++) {
      const p = progressMap[chapters[i].id]
      if (!p || p.progress < 100) return i
    }
    return 0
  },

  // 章节切换
  onSwitchChapter(e) {
    const index = Number(e.currentTarget.dataset.index)
    if (index === this.data.currentIndex) return
    const chapter = this.data.chapters[index]
    if (!chapter) return

    this.setData({
      currentIndex: index,
      currentChapter: chapter
    })

    // 断点续播：切到该章节 lastPosition
    const pos = this.data.progressMap[chapter.id]?.lastPosition || 0
    if (pos > 0 && this.data.videoContext) {
      setTimeout(() => {
        this.data.videoContext.seek(pos)
      }, 200)
    }
  },

  // 视频播放进度更新 — 节流 10s 上报一次
  onTimeUpdate(e) {
    const { currentTime, duration } = e.detail
    if (!duration || duration <= 0) return
    if (!this.data.currentChapter) return

    const now = Date.now()
    // 节流：距上次上报不足 10s 则跳过
    if (now - _lastReport < 10000) return

    const progress = Math.min(100, Math.floor((currentTime / duration) * 100))
    const chapterId = this.data.currentChapter.id
    const lastPosition = Math.floor(currentTime)

    // 更新本地 progressMap
    const progressMap = { ...this.data.progressMap }
    progressMap[chapterId] = {
      ...(progressMap[chapterId] || {}),
      progress,
      lastPosition,
      studyDuration: (progressMap[chapterId]?.studyDuration || 0) + 10
    }

    // 重新计算总进度
    const overallProgress = this._calcOverall(progressMap)

    this.setData({ progressMap, overallProgress })

    // 标记上报时间
    _lastReport = now

    // 上报后端（fire-and-forget，失败仅 console）
    studyApi.reportProgress({
      courseId: this.data.courseId,
      chapterId,
      progress,
      studyDuration: 10,
      lastPosition
    }).catch(err => {
      console.warn('进度上报失败', err)
    })
  },

  // 计算总进度 = 各章节 progress 平均
  _calcOverall(progressMap) {
    const chapters = this.data.chapters
    if (!chapters || chapters.length === 0) return 0
    const sum = chapters.reduce((s, ch) => s + (progressMap[ch.id]?.progress || 0), 0)
    return Math.round(sum / chapters.length)
  },

  // "标记学完"按钮：把当前章节进度设为 100 并上报
  onMarkDone() {
    const chapter = this.data.currentChapter
    if (!chapter) return
    const progressMap = { ...this.data.progressMap }
    progressMap[chapter.id] = {
      ...(progressMap[chapter.id] || {}),
      progress: 100,
      lastPosition: 0,
      studyDuration: progressMap[chapter.id]?.studyDuration || 0
    }
    const overallProgress = this._calcOverall(progressMap)
    this.setData({ progressMap, overallProgress })

    // 立即上报（重置节流，让这次必上报）
    _lastReport = 0
    studyApi.reportProgress({
      courseId: this.data.courseId,
      chapterId: chapter.id,
      progress: 100,
      studyDuration: 0,
      lastPosition: 0
    }).then(() => {
      wx.showToast({ title: '已完成本章节', icon: 'success' })
    }).catch(err => {
      console.warn('标记学完上报失败', err)
      wx.showToast({ title: '标记失败', icon: 'none' })
    })
  },

  // 视频加载失败兜底
  onVideoError(e) {
    console.error('视频加载失败', e)
    wx.showToast({ title: '视频加载失败', icon: 'none' })
  },

  // 页面卸载时上报最终进度（清零节流，确保最后一次能上报）
  onUnload() {
    if (!this.data.currentChapter) return
    const chapterId = this.data.currentChapter.id
    const p = this.data.progressMap[chapterId] || {}
    // fire-and-forget，不用 await
    studyApi.reportProgress({
      courseId: this.data.courseId,
      chapterId,
      progress: p.progress || 0,
      studyDuration: 10,
      lastPosition: p.lastPosition || 0
    }).catch(err => {
      console.warn('最终进度上报失败', err)
    })
  }
})
