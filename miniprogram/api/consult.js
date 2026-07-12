// miniprogram/api/consult.js
const { request } = require('./request')

module.exports = {
  /** 发起咨询（智能问答 / 人工工单） */
  ask(question) {
    return request('/consult/ask', 'POST', { question })
  },

  /** 我的咨询列表 */
  myList(pageNum = 1, pageSize = 10) {
    return request(`/consult/my?pageNum=${pageNum}&pageSize=${pageSize}`)
  }
}
