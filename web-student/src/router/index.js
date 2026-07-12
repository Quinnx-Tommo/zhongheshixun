import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

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
  // P2: TEACHER 我的课程
  {
    path: '/my-courses',
    name: 'MyCourses',
    component: () => import('@/views/teacher/my-courses.vue'),
    meta: {
      requiresAuth: true,
      title: '我的课程',
      roleCode: 'TEACHER',
    },
  },
  // P3: TEACHER 题库管理
  {
    path: '/question-bank',
    name: 'QuestionBank',
    component: () => import('@/views/teacher/question-bank.vue'),
    meta: {
      requiresAuth: true,
      title: '题库管理',
      roleCode: 'TEACHER',
    },
  },
  // TEACHER 咨询回复（占位 + 引导管理后台）
  {
    path: '/consult-manage',
    name: 'ConsultManage',
    component: () => import('@/views/teacher/consult-manage.vue'),
    meta: {
      requiresAuth: true,
      title: '咨询回复',
      roleCode: 'TEACHER',
    },
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
// 3) RBAC：to.meta.roleCode 不为空且不等于当前用户 roleCode → 跳 /home + 警告
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
  // RBAC 角色校验：仅在已登录且路由声明 roleCode 时生效
  const requiredRole = to.meta?.roleCode
  if (token && requiredRole) {
    // 从 store / localStorage 取当前用户 role（store 优先，因为可能已更新）
    const userStore = useUserStore()
    const currentRole = userStore.roleCode || ''
    if (currentRole && currentRole !== requiredRole) {
      ElMessage.warning(`当前账号角色（${currentRole || '未知'}）无权访问"${to.meta.title || to.path}"，已为您返回首页。`)
      next('/home')
      return
    }
  }
  next()
})

export default router
