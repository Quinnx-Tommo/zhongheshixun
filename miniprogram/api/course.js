// miniprogram/api/course.js
const { request } = require('./request')

// ============= 字段映射 =============
// 后端 course_type: 1公开课 / 2必修课  →  前端 type: '公开课' / '必修课'
const TYPE_MAP = { 1: '公开课', 2: '必修课' }

// PageResult → list.js 期望的结构
function wrapPage(res, mapper) {
  // res 是 PageResult: { records, total, pageNum, pageSize }
  const records = (res.records || []).map(mapper)
  return {
    records,
    total: res.total || 0,
    pageNum: res.pageNum,
    pageSize: res.pageSize,
    hasMore: records.length >= (res.pageSize || 10)
  }
}

// 单个课程实体 → list 卡片使用的字段
function mapCourse(c) {
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

// 后端课程详情VO: { course: {...}, chapters: [...] }
function mapDetail(data) {
  return { ...mapCourse(data.course || data), chapters: data.chapters || [] }
}

// chapter 字段映射
function mapChapter(ch) {
  return {
    id: ch.id,
    title: ch.title,
    sortOrder: ch.sortOrder ?? ch.sort_order ?? 0,
    videoUrl: ch.videoUrl || ch.video_url || '',
    duration: ch.duration || 0,
    progress: ch.progress ?? null   // 学习进度接口返回时填充
  }
}

module.exports = {
  // 课程列表（pageNum, pageSize, title?, courseType?）
  getList: (params) => {
    const query = {
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 10
    }
    if (params.title) query.title = params.title
    if (params.courseType !== '' && params.courseType != null) {
      query.courseType = params.courseType
    }
    // GET /api/course/list 返回 PageResult
    return request('/course/list', 'GET', query).then(res => wrapPage(res, mapCourse))
  },

  // 我的课程（已报名）
  // study 接口返回 PageResult<Course>，已包含当前学员报名的课程
  getMyCourses: (params) => {
    const query = {
      pageNum: params.pageNum || 1,
      pageSize: params.pageSize || 10
    }
    return request('/study/my-courses', 'GET', query).then(res => wrapPage(res, mapCourse))
  },

  // 课程详情
  getDetail: (id) => {
    return request(`/course/detail/${id}`, 'GET').then(mapDetail)
  },

  // 章节列表
  getChapters: (courseId) => {
    return request('/course/chapter/list', 'GET', { courseId })
      .then(list => (list || []).map(mapChapter))
  },

  // 报名课程
  enroll: (courseId) => request('/study/enroll', 'POST', { courseId }),

  // 上报学习进度
  reportProgress: (data) => request('/study/progress', 'POST', data),

  // 查询某课程学习进度（返回章节级）
  getProgress: (courseId) => request(`/study/progress/${courseId}`, 'GET')
}
