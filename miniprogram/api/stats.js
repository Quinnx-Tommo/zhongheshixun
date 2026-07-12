// miniprogram/api/stats.js
// 学习统计相关接口（个人学习数据总览）
const { request } = require('./request')

module.exports = {
  // 我的学习统计总览
  // 返回 { totalStudyHours, completedCourses, examCount, avgScore, enrolledCount, passedCount }
  getMyStats: () => request('/stats/my', 'GET')
}
