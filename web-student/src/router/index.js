import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { layout: false },
  },
  {
    path: '/',
    redirect: '/home',
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/home/index.vue'),
    meta: { requiresAuth: true, title: '首页' },
  },
  {
    path: '/courses',
    name: 'CourseList',
    component: () => import('@/views/course/list.vue'),
    meta: { requiresAuth: true, title: '课程中心' },
  },
  {
    path: '/courses/:id',
    name: 'CourseDetail',
    component: () => import('@/views/course/detail.vue'),
    meta: { requiresAuth: true, title: '课程详情' },
  },
  {
    path: '/courses/:id/learn',
    name: 'CourseLearn',
    component: () => import('@/views/course/learn.vue'),
    meta: { requiresAuth: true, title: '课程学习' },
  },
  {
    path: '/exams',
    name: 'ExamList',
    component: () => import('@/views/exam/list.vue'),
    meta: { requiresAuth: true, title: '考试中心' },
  },
  {
    path: '/exams/:id',
    name: 'ExamAnswer',
    component: () => import('@/views/exam/answer.vue'),
    meta: { requiresAuth: true, title: '考试答题' },
  },
  {
    path: '/exams/:id/result',
    name: 'ExamResult',
    component: () => import('@/views/exam/result.vue'),
    meta: { requiresAuth: true, title: '考试成绩' },
  },
  // P0-3 修复：新增培训计划路由
  {
    path: '/plans',
    name: 'PlanList',
    component: () => import('@/views/plan/index.vue'),
    meta: { requiresAuth: true, title: '培训计划' },
  },
  {
    path: '/plans/:id',
    name: 'PlanDetail',
    component: () => import('@/views/plan/detail.vue'),
    meta: { requiresAuth: true, title: '计划详情' },
  },
  // P0-3 修复：新增咨询路由
  {
    path: '/consult',
    name: 'Consult',
    component: () => import('@/views/consult/index.vue'),
    meta: { requiresAuth: true, title: '在线咨询' },
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/profile/index.vue'),
    meta: { requiresAuth: true, title: '个人中心' },
  },
  // 讲师专属页面（TEACHER 角色可见，菜单由 MainLayout 按 roleCode 动态渲染）
  {
    path: '/teacher/my-courses',
    name: 'TeacherMyCourses',
    component: () => import('@/views/teacher/my-courses.vue'),
    meta: { requiresAuth: true, title: '我的课程', roles: ['TEACHER'] },
  },
  {
    path: '/teacher/question-bank',
    name: 'TeacherQuestionBank',
    component: () => import('@/views/teacher/question-bank.vue'),
    meta: { requiresAuth: true, title: '题库管理', roles: ['TEACHER'] },
  },
  {
    path: '/teacher/consult-manage',
    name: 'TeacherConsultManage',
    component: () => import('@/views/teacher/consult-manage.vue'),
    meta: { requiresAuth: true, title: '咨询回复', roles: ['TEACHER'] },
  },
  // 兜底：未匹配路径跳首页
  {
    path: '/:pathMatch(.*)*',
    redirect: '/home',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫：
// 1) 未登录跳 /login
// 2) 已登录访问 /login 自动跳 /home
// 3) 角色权限校验：meta.roles 限制页面可见角色
// 4) ADMIN 角色不进入学员前台，引导去管理后台 5176
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }
  if (to.path === '/login' && token) {
    next('/home')
    return
  }
  // ADMIN 角色引导去管理后台（避免在学员前台显示不相关菜单）
  const userInfoStr = localStorage.getItem('userInfo')
  const userInfo = userInfoStr ? JSON.parse(userInfoStr) : null
  const roleCode = userInfo?.role || userInfo?.roleCode || ''
  if (roleCode === 'ADMIN' && to.path !== '/login') {
    // ADMIN 已登录，跳转到管理后台 5176
    window.location.href = 'http://localhost:5176/'
    return
  }
  // 角色级权限校验：meta.roles 限制页面可见角色
  if (to.meta.roles && to.meta.roles.length > 0) {
    if (!to.meta.roles.includes(roleCode)) {
      // 无权限访问该页面，跳回首页
      next('/home')
      return
    }
  }
  next()
})

export default router
