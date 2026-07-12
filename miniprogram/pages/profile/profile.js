// miniprogram/pages/profile/profile.js
const userApi = require('../../api/user')
const courseApi = require('../../api/course')
const examApi = require('../../api/exam')
const consultApi = require('../../api/consult')

Page({
  data: {
    profile: {},
    nicknameFirst: '学',
    stats: {
      courseCount: '-',
      studyHours: '-',
      examCount: '-',
      consultCount: 0
    },
    editVisible: false,
    editForm: {
      realName: '',
      phone: '',
      orgName: '',
      jobType: ''
    },
    jobTypes: [
      { label: '临床', value: '临床' },
      { label: '公卫', value: '公卫' },
      { label: '护理', value: '护理' },
      { label: '医技', value: '医技' },
      { label: '其他', value: '其他' }
    ]
  },

  onLoad() {
    this.loadProfile()
    this.loadStats()
  },

  onShow() {
    // 所有 tabBar 页统一登录态检查
    if (!wx.getStorageSync('token')) {
      wx.reLaunch({ url: '/pages/login/login' })
    }
  },

  // 加载个人信息
  async loadProfile() {
    try {
      const res = await userApi.getProfile()
      const profile = res || {}
      const realName = profile.realName || '学员'
      this.setData({
        profile,
        nicknameFirst: realName.charAt(0)
      })
    } catch (e) {
      // request 已统一 toast；失败时使用本地缓存兜底
      const local = wx.getStorageSync('userInfo') || {}
      this.setData({
        profile: local,
        nicknameFirst: (local.realName || '学').charAt(0)
      })
    }
  },

  // 加载学习数据（并发请求课程/考试/咨询真实接口）
  async loadStats() {
    const results = await Promise.allSettled([
      courseApi.getMyCourses({ pageNum: 1, pageSize: 50 }),
      examApi.getMyRecords(),
      consultApi.myList(1, 1)
    ])

    const courseRes = results[0].status === 'fulfilled' ? results[0].value : null
    const examRes = results[1].status === 'fulfilled' ? results[1].value : null
    const consultRes = results[2].status === 'fulfilled' ? results[2].value : null

    const courseTotal = courseRes ? (courseRes.total || 0) : 0
    const examTotal = Array.isArray(examRes) ? examRes.length : 0
    const consultTotal = consultRes ? (consultRes.total || 0) : 0

    // 学习时长：已报名课程估算（简化：每门课 totalHours 作学时）
    const records = courseRes ? (courseRes.records || []) : []
    const hours = records.reduce((sum, c) => sum + (c.totalHours || c.total_hours || 0), 0)

    this.setData({
      stats: {
        courseCount: courseTotal,
        examCount: examTotal,
        consultCount: consultTotal,
        studyHours: hours
      }
    })
  },

  // 跳转 - 我的课程子页
  onGoMyCourse() {
    wx.navigateTo({ url: '/pages/course/my-courses/my-courses' })
  },

  // 跳转 - 学习记录（考试记录时间线）
  onGoExam() {
    wx.navigateTo({ url: '/pages/profile/records/records' })
  },

  // 跳转 - 我的咨询
  onGoMyConsult() {
    wx.switchTab({ url: '/pages/consult/index/index' })
  },

  // 跳转 - 设置
  onGoSettings() {
    wx.navigateTo({ url: '/pages/profile/settings/settings' })
  },

  // 跳转 - 学习中心首页
  onGoStudy() {
    wx.switchTab({ url: '/pages/index/index' })
  },

  // 编辑资料
  onGoEdit() {
    const p = this.data.profile || {}
    this.setData({
      editVisible: true,
      editForm: {
        realName: p.realName || '',
        phone: p.phone || '',
        orgName: p.orgName || '',
        jobType: p.jobType || ''
      }
    })
  },

  onCloseEdit() {
    this.setData({ editVisible: false })
  },

  onJobChange(e) {
    const idx = e.detail.value
    const job = this.data.jobTypes[idx]
    this.setData({ 'editForm.jobType': job ? job.value : '' })
  },

  // 保存资料
  async onSaveProfile() {
    const form = this.data.editForm
    if (!form.realName || !form.realName.trim()) {
      wx.showToast({ title: '请输入姓名', icon: 'none' })
      return
    }
    try {
      await userApi.updateProfile({
        realName: form.realName.trim(),
        phone: form.phone,
        orgName: form.orgName,
        jobType: form.jobType
      })
      wx.showToast({ title: '保存成功', icon: 'success' })
      this.setData({ editVisible: false })
      // 同步更新本地缓存
      const local = wx.getStorageSync('userInfo') || {}
      local.realName = form.realName.trim()
      wx.setStorageSync('userInfo', local)
      this.loadProfile()
    } catch (e) {
      // 错误由 request 统一 toast
    }
  },

  // 离线学习（待功能）
  onGoOffline() {
    wx.showToast({ title: '离线学习开发中', icon: 'none' })
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
