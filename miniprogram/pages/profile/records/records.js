// pages/profile/records/records.js
// 学习记录：我的考试记录时间线
const examApi = require('../../../api/exam')

Page({
  data: {
    records: [],   // 考试记录时间线
    loading: false
  },

  onLoad() {
    this.loadData()
  },

  async loadData() {
    this.setData({ loading: true })
    try {
      const list = await examApi.getMyRecords()
      // 按提交时间倒序
      const sorted = (list || []).sort((a, b) => {
        const ta = new Date(a.submitTime).getTime() || 0
        const tb = new Date(b.submitTime).getTime() || 0
        return tb - ta
      })
      this.setData({ records: sorted, loading: false })
    } catch (e) {
      console.error('[records] load error', e)
      this.setData({ loading: false })
      wx.showToast({ title: '加载失败', icon: 'none' })
    }
  },

  // 跳转到考试结果页（exam/detail 不存在，跳 result）
  goRecord(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/pages/exam/result/result?id=${id}&from=records` })
  }
})
