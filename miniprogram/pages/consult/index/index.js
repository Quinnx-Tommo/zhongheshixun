// miniprogram/pages/consult/index/index.js
const consultApi = require('../../../api/consult')

Page({
  data: {
    // 输入的问题
    question: '',
    // 咨询记录列表（按时间倒序）
    consults: [],
    // 提交中状态
    submitting: false,
    // 分页
    pageNum: 1,
    pageSize: 10,
    hasMore: true,
    loadingList: false
  },

  onLoad() {
    this.loadList()
  },

  onShow() {
    if (!wx.getStorageSync('token')) {
      wx.reLaunch({ url: '/pages/login/login' })
    }
  },

  onPullDownRefresh() {
    this.setData({ pageNum: 1, consults: [], hasMore: true })
    this.loadList(true)
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loadingList) {
      this.setData({ pageNum: this.data.pageNum + 1 })
      this.loadList()
    }
  },

  // 输入
  onInput(e) {
    this.setData({ question: e.detail.value })
  },

  // 提交问题
  async onSubmit() {
    const q = (this.data.question || '').trim()
    if (!q) {
      wx.showToast({ title: '请输入问题', icon: 'none' })
      return
    }
    if (this.data.submitting) return
    this.setData({ submitting: true })

    try {
      const res = await consultApi.ask(q)
      // 顶部插入新记录
      const newItem = {
        id: res.consultId,
        question: q,
        answer: res.autoReply || null,
        isAuto: res.matched ? 1 : 0,
        createTime: '刚刚',
        replyTime: res.matched ? '刚刚' : null,
        matched: res.matched
      }
      this.setData({
        consults: [newItem, ...this.data.consults],
        question: ''
      })
      if (res.matched) {
        wx.showToast({ title: '已智能回复', icon: 'success' })
      } else {
        wx.showToast({ title: '已提交，请等待人工回复', icon: 'none' })
      }
    } catch (e) {
      // 错误由 request 统一 toast
    } finally {
      this.setData({ submitting: false })
    }
  },

  // 加载咨询列表
  async loadList(refresh = false) {
    if (this.data.loadingList) return
    this.setData({ loadingList: true })
    try {
      const res = await consultApi.myList(this.data.pageNum, this.data.pageSize)
      const list = (res.list || []).map(item => ({
        ...item,
        createTime: this.formatTime(item.createTime),
        replyTime: item.replyTime ? this.formatTime(item.replyTime) : null
      }))
      const consults = refresh ? list : [...this.data.consults, ...list]
      this.setData({
        consults,
        hasMore: list.length >= this.data.pageSize
      })
    } catch (e) {
      // error handled by request
    } finally {
      this.setData({ loadingList: false })
      wx.stopPullDownRefresh()
    }
  },

  formatTime(t) {
    if (!t) return ''
    // 2026-07-08 10:15:30 → 07-08 10:15
    const d = new Date(t.replace(/-/g, '/'))
    if (isNaN(d.getTime())) return t
    const mo = (d.getMonth() + 1).toString().padStart(2, '0')
    const day = d.getDate().toString().padStart(2, '0')
    const h = d.getHours().toString().padStart(2, '0')
    const mi = d.getMinutes().toString().padStart(2, '0')
    return `${mo}-${day} ${h}:${mi}`
  }
})
