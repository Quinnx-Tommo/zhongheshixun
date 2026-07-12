// miniprogram/api/plan.js
// 培训计划相关接口（计划列表、详情、我的计划）
const { request } = require('./request')

// ============= 字段映射 =============
const TYPE_MAP = { 1: '公开课', 2: '必修课' }

// 计划下的课程字段映射（复用 mapCourse 逻辑）
function mapPlanCourse(c) {
  return {
    id: c.id,
    title: c.title,
    coverUrl: c.coverUrl || c.cover_url || '',
    description: c.description || '',
    courseType: c.courseType ?? c.course_type ?? 1,
    courseTypeText: TYPE_MAP[c.courseType ?? c.course_type] || '公开课',
    teacherName: c.teacherName || c.teacher_name || '',
    totalHours: c.totalHours ?? c.total_hours ?? 0,
    offlineFlag: c.offlineFlag ?? c.offline_flag ?? 0
  }
}

// PageResult → list 期望结构
function wrapPage(res, mapper) {
  const records = (res.records || []).map(mapper)
  return {
    records,
    total: res.total || 0,
    pageNum: res.pageNum,
    pageSize: res.pageSize,
    hasMore: records.length >= (res.pageSize || 10)
  }
}

module.exports = {
  // 培训计划列表（pageNum, pageSize）
  getList: (params) => {
    const query = {
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 10
    }
    return request('/plan/list', 'GET', query).then(res => wrapPage(res))
  },

  // 培训计划详情
  // 返回 { id, title, description, startTime, endTime, courseList, totalCount, completedCount, progress }
  getDetail: (id) => request(`/plan/detail/${id}`, 'GET').then(data => ({
    id: data.id,
    title: data.title,
    description: data.description || '',
    startTime: data.startTime ?? data.start_time ?? '',
    endTime: data.endTime ?? data.end_time ?? '',
    courseList: (data.courseList || data.course_list || []).map(mapPlanCourse),
    totalCount: data.totalCount ?? data.total_count ?? 0,
    completedCount: data.completedCount ?? data.completed_count ?? 0,
    progress: data.progress ?? 0
  })),

  // 我参与的计划
  getMyPlans: (params) => {
    const query = {
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 10
    }
    return request('/plan/my', 'GET', query).then(res => wrapPage(res))
  }
}
