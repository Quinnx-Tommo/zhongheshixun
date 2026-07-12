import request from '@/utils/request'

/**
 * 教师工作台 API
 * 注：/api/course/page、/api/question/page 实际尚未在后端学员端 (9899) 提供。
 * 任务要求字面调用 /api/* 路径以保持 M10 联调期"最小改动"原则；
 * 失败时由页面降级到 /api/course/list，或引导到管理后台 5176。
 */

/**
 * 教师"我的课程"分页
 * @param {Object} params { pageNum, pageSize, teacherId, title, courseType }
 */
export const getTeacherCoursePage = (params) =>
  request({ url: '/course/page', method: 'GET', params })

/**
 * 学员端课程列表（降级 fallback）
 * @param {Object} params { pageNum, pageSize, title, courseType }
 */
export const getCourseListFallback = (params) =>
  request({ url: '/course/list', method: 'GET', params })

/**
 * 题库分页
 * @param {Object} params { pageNum, pageSize, courseId, questionType, difficulty }
 */
export const getQuestionPage = (params) =>
  request({ url: '/question/page', method: 'GET', params })

/**
 * 新增/更新/删除题目
 */
export const createQuestion = (data) =>
  request({ url: '/question', method: 'POST', data })

export const updateQuestion = (data) =>
  request({ url: '/question', method: 'PUT', data })

export const deleteQuestion = (id) =>
  request({ url: `/question/${id}`, method: 'DELETE' })

/**
 * 某课程的学员考试记录（教学分析用）
 * @param {Object} params { pageNum, pageSize, courseId }
 */
export const getExamRecordPage = (params) =>
  request({ url: '/exam/record/page', method: 'GET', params })
