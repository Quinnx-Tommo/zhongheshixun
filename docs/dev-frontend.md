# 四川省基层卫生人员网络培训平台 - 后台管理前端开发文档

> **版本**：1.0.0
> **更新日期**：2026-07-07
> **技术栈**：Vue 3.4 + TypeScript + Element Plus + Pinia + Vue Router 4 + Axios + ECharts
> **性质**：后台管理系统（管理员/讲师使用）实现手册
> **配套文档导航**：
> - 项目总览/架构/数据库/分工：`docs/设计文档.md`
> - 后端实现手册：`docs/开发文档.md`
> - API 接口清单：`docs/dev-api.md`
> - 小程序前端手册：`docs/dev-miniapp.md`
> - 部署手册：`docs/deploy.md`

---

## 一、项目搭建与目录结构

### 1.1 创建命令

```bash
# 在 training-admin 目录下创建 Vue3 + TS 项目
cd training-admin
npm create vite@latest frontend -- --template vue-ts
cd frontend

# 安装核心依赖
npm install

# 安装项目所需第三方库
npm install element-plus @element-plus/icons-vue
npm install axios vue-router@4 pinia
npm install echarts vue-echarts   # 统计图表

# 类型声明（如需要）
npm install -D @types/node
```

> 说明：`element-plus` 按需引入亦可，为简化开发本示例采用**全局注册**。

### 1.2 完整目录树

```
frontend/
├── index.html
├── package.json
├── tsconfig.json
├── tsconfig.node.json
├── vite.config.ts              ← Vite 配置（端口 5176，代理 → 后端 9898）
└── src/
    ├── api/                    ← 接口封装
    │   ├── request.ts          ← axios 实例（拦截器核心）
    │   ├── user.ts             ← 用户/认证接口
    │   ├── teacher.ts          ← 讲师接口
    │   ├── course.ts          ← 课程/章节/知识点接口
    │   ├── question.ts        ← 试题接口
    │   ├── exam.ts            ← 考试接口
    │   ├── plan.ts            ← 培训计划接口
    │   ├── consult.ts         ← 咨询/知识库接口
    │   └── stats.ts           ← 统计接口
    ├── components/             ← 公共组件
    │   ├── Pagination.vue      ← 分页组件
    │   └── Charts/
    │       ├── BarChart.vue   ← 柱状图封装
    │       ├── PieChart.vue   ← 饼图封装
    │       └── LineChart.vue  ← 折线图封装
    ├── views/                  ← 页面
    │   ├── login/index.vue     ← 登录页
    │   ├── layout/index.vue   ← 布局框架（侧边栏+顶部栏）
    │   ├── course/
    │   │   ├── list.vue        ← 课程列表
    │   │   └── edit.vue        ← 课程编辑（含 offline_flag 开关）
    │   ├── chapter/index.vue   ← 章节管理（视频上传+排序）
    │   ├── knowledge-point/index.vue ← 知识点管理
    │   ├── question/index.vue  ← 试题管理（5 种题型动态表单）
    │   ├── exam/index.vue     ← 考试管理（自动组卷）
    │   ├── plan/index.vue     ← 培训计划
    │   ├── user/index.vue     ← 用户管理
    │   ├── teacher/index.vue  ← 讲师管理
    │   ├── stats/index.vue    ← 统计报表（ECharts）
    │   └── consult/index.vue  ← 咨询管理（SLA 标红）
    ├── router/
    │   └── index.ts            ← 路由配置 + 守卫
    ├── store/
    │   └── user.ts             ← 用户状态（Pinia）
    ├── utils/
    │   └── auth.ts             ← Token 读写封装
    ├── styles/
    │   └── index.css           ← 全局样式 + CSS 变量
    ├── App.vue
    └── main.ts                 ← 入口（注册 Element Plus / Pinia / Router）
```

### 1.3 vite.config.ts

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    host: '0.0.0.0',
    port: 5176,  // 原 5173 已弃用，改用 5176 避开 web-student 的 5174
    open: false,
    proxy: {
      // 后台管理 API → 后端 9898（避开 8080 僵尸死锁）
      '/admin': {
        target: 'http://localhost:9898',
        changeOrigin: true
      },
      // 文件上传/视频等静态资源
      '/upload': {
        target: 'http://localhost:9898',
        changeOrigin: true
      }
    }
  }
})
```

### 1.4 main.ts

```typescript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import App from './App.vue'
import router from './router'
import './styles/index.css'

const app = createApp(App)

// 全局注册 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })   // 中文本地化
app.mount('#app')
```

---

## 二、核心封装（完整代码）

### 2.1 src/api/request.ts — axios 实例

```typescript
import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '@/router'

// 从环境变量读取 baseURL（默认走 Vite 代理，可省略）
const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// ========== 请求拦截器：自动携带 Bearer Token ==========
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ========== 响应拦截器：200 解包 / 401 跳登录 / 其他报错 ==========
request.interceptors.response.use(
  (response) => {
    const res = response.data

    // 文件流（下载）直接返回
    if (response.config.responseType === 'blob') {
      return response.data
    }

    // 成功：code === 200
    if (res.code === 200) {
      return res
    }

    // 业务错误：弹窗提示
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || 'Error'))
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message || '网络错误'

    if (status === 401) {
      ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
        confirmButtonText: '重新登录',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      })
    } else if (status === 403) {
      ElMessage.error('无权限访问')
    } else if (status >= 500) {
      ElMessage.error('服务器繁忙，请稍后重试')
    } else {
      ElMessage.error(message)
    }
    return Promise.reject(error)
  }
)

export default request

// 通用 GET/POST 便捷封装（可选）
export const http = {
  get<T = any>(url: string, params?: object, config?: AxiosRequestConfig) {
    return request.get<{ code: number; message: string; data: T }>(url, { params, ...config })
  },
  post<T = any>(url: string, data?: object, config?: AxiosRequestConfig) {
    return request.post<{ code: number; message: string; data: T }>(url, data, config)
  },
  put<T = any>(url: string, data?: object) {
    return request.put<{ code: number; message: string; data: T }>(url, data)
  },
  del<T = any>(url: string) {
    return request.delete<{ code: number; message: string; data: T }>(url)
  }
}
```

### 2.2 src/utils/auth.ts — Token 读写封装

```typescript
const TOKEN_KEY = 'token'
const USER_INFO_KEY = 'userInfo'

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

export function getUserInfo(): any {
  const info = localStorage.getItem(USER_INFO_KEY)
  return info ? JSON.parse(info) : null
}

export function setUserInfo(userInfo: any): void {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
}

export function removeUserInfo(): void {
  localStorage.removeItem(USER_INFO_KEY)
}
```

### 2.3 src/store/user.ts — Pinia Store

```typescript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { loginApi, getUserInfoApi } from '@/api/user'
import {
  getToken, setToken, removeToken,
  getUserInfo, setUserInfo, removeUserInfo
} from '@/utils/auth'

export interface UserInfo {
  id: number
  username: string
  realName: string
  role: 'admin' | 'teacher' | 'student'
  avatar?: string
  orgName?: string
  jobType?: string
}

export const useUserStore = defineStore('user', () => {
  // ========== State ==========
  const token = ref<string>(getToken() || '')
  const userInfo = ref<UserInfo | null>(getUserInfo())

  // ========== Actions ==========

  /**
   * 登录：调 API → 存 token + userInfo
   */
  async function login(loginForm: { username: string; password: string }) {
    const res = await loginApi(loginForm)
    const { token: tk, userInfo: info } = res.data
    token.value = tk
    userInfo.value = info
    setToken(tk)
    setUserInfo(info)
    return res.data
  }

  /**
   * 获取当前用户信息（用于刷新页面后恢复）
   */
  async function fetchUserInfo() {
    const res = await getUserInfoApi()
    userInfo.value = res.data
    setUserInfo(res.data)
    return res.data
  }

  /**
   * 退出登录：清空状态 + 跳转登录页
   */
  function logout() {
    token.value = ''
    userInfo.value = null
    removeToken()
    removeUserInfo()
  }

  return { token, userInfo, login, fetchUserInfo, logout }
})
```

### 2.4 src/router/index.ts — 路由 + 守卫

```typescript
import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes: RouteRecordRaw[] = [
  // 登录页（无需登录）
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  // 布局框架（需要登录）
  {
    path: '/',
    component: () => import('@/views/layout/index.vue'),
    redirect: '/course',
    children: [
      { path: 'course', name: 'CourseList',
        component: () => import('@/views/course/list.vue'),
        meta: { title: '课程管理', icon: 'VideoCamera' } },
      { path: 'course/edit/:id?', name: 'CourseEdit',
        component: () => import('@/views/course/edit.vue'),
        meta: { title: '课程编辑', hidden: true } },
      { path: 'chapter/:courseId', name: 'ChapterList',
        component: () => import('@/views/chapter/index.vue'),
        meta: { title: '章节管理', hidden: true } },
      { path: 'knowledge-point', name: 'KnowledgePointList',
        component: () => import('@/views/knowledge-point/index.vue'),
        meta: { title: '知识点管理', icon: 'Collection' } },
      { path: 'question', name: 'QuestionList',
        component: () => import('@/views/question/index.vue'),
        meta: { title: '试题管理', icon: 'Document' } },
      { path: 'exam', name: 'ExamList',
        component: () => import('@/views/exam/index.vue'),
        meta: { title: '考试管理', icon: 'Edit' } },
      { path: 'plan', name: 'PlanList',
        component: () => import('@/views/plan/index.vue'),
        meta: { title: '培训计划', icon: 'Calendar' } },
      { path: 'user', name: 'UserList',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' } },
      { path: 'teacher', name: 'TeacherList',
        component: () => import('@/views/teacher/index.vue'),
        meta: { title: '讲师管理', icon: 'UserFilled' } },
      { path: 'stats', name: 'Stats',
        component: () => import('@/views/stats/index.vue'),
        meta: { title: '统计报表', icon: 'DataAnalysis' } },
      { path: 'consult', name: 'ConsultList',
        component: () => import('@/views/consult/index.vue'),
        meta: { title: '咨询管理', icon: 'ChatDotRound' } }
    ]
  },
  // 兜底：404
  { path: '/:pathMatch(.*)*', redirect: '/course' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// ========== beforeEach 守卫：未登录跳转登录页 ==========
router.beforeEach((to, _from, next) => {
  const token = getToken()
  if (to.path === '/login') {
    next()
    return
  }
  if (!token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

// 设置页面标题
router.afterEach((to) => {
  document.title = `${(to.meta?.title as string) || ''} - 基层卫生培训平台`
})

export default router
```

---

## 三、登录页面（完整可运行代码）

> **路由**：`/login`

### src/views/login/index.vue

```vue
<template>
  <div class="login-container">
    <el-card class="login-card" shadow="always">
      <div class="login-header">
        <h2>四川省基层卫生人员网络培训平台</h2>
        <p class="sub-title">后台管理系统</p>
      </div>

      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="用户名"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>

        <!-- 验证码占位（作业简化版可跳过，生产环境接入图形/短信验证码） -->
        <el-form-item prop="captcha">
          <div class="captcha-row">
            <el-input
              v-model="loginForm.captcha"
              placeholder="验证码"
              :prefix-icon="Key"
            />
            <span class="captcha-placeholder">ABCD</span>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>

        <div class="login-tips">
          <p>默认账号：admin / 123456（管理员）</p>
          <p>teacher01 / 123456（讲师）</p>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { User, Lock, Key } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: '123456',
  captcha: ''
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

async function handleLogin() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login({
      username: loginForm.username,
      password: loginForm.password
    })
    ElMessage.success('登录成功')
    // 登录成功后跳转：优先跳 redirect，否则跳首页
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (err: any) {
    ElMessage.error(err.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #409eff 0%, #1a3e72 100%);
}
.login-card {
  width: 420px;
  padding: 20px;
  border-radius: 8px;
}
.login-header {
  text-align: center;
  margin-bottom: 24px;
}
.login-header h2 {
  font-size: 20px;
  color: #303133;
  margin: 0 0 8px;
}
.sub-title {
  font-size: 14px;
  color: #909399;
  margin: 0;
}
.login-btn {
  width: 100%;
}
.captcha-row {
  display: flex;
  gap: 12px;
  width: 100%;
}
.captcha-placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 100px;
  background: #f4f4f5;
  border-radius: 4px;
  color: #409eff;
  font-weight: bold;
  letter-spacing: 4px;
  user-select: none;
}
.login-tips {
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
  text-align: center;
}
.login-tips p { margin: 4px 0; }
</style>
```

---

## 四、布局框架（完整代码）

> **路由**：`/`（layout 容器，子路由挂载到 `router-view`）

### src/views/layout/index.vue

```vue
<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="logo">
        <span v-if="!isCollapse">基层卫生培训平台</span>
        <span v-else>培</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="#001529"
        text-color="#fff"
        active-text-color="#409eff"
        router
      >
        <template v-for="item in menuList" :key="item.path">
          <el-menu-item
            v-if="!item.meta?.hidden"
            :index="'/' + item.path"
          >
            <el-icon><component :is="item.meta?.icon" /></el-icon>
            <template #title>{{ item.meta?.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部栏 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :icon="UserFilled" />
              <span class="user-name">{{ userStore.userInfo?.realName || '管理员' }}</span>
              <el-tag size="small" type="primary" effect="dark">
                {{ roleLabel }}
              </el-tag>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { Fold, Expand, UserFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

// 菜单项与路由映射表（与 router/index.ts 的 children 对应）
const menuList = computed(() => {
  const root = router.options.routes.find((r) => r.path === '/')
  return (root?.children || []).filter((c) => !c.meta?.hidden)
})

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title as string)

const roleLabel = computed(() => {
  const map: Record<string, string> = { admin: '管理员', teacher: '讲师', student: '学员' }
  return map[userStore.userInfo?.role || ''] || '用户'
})

async function handleCommand(cmd: string) {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' })
    userStore.logout()
    router.push('/login')
  } else if (cmd === 'profile') {
    // 预留：跳转个人信息页
  }
}
</script>

<style scoped>
.layout-container { height: 100vh; }
.layout-aside {
  background-color: #001529;
  transition: width 0.3s;
  overflow: hidden;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  background-color: #002140;
  white-space: nowrap;
  overflow: hidden;
}
.layout-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.collapse-btn {
  font-size: 20px;
  cursor: pointer;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.user-name { font-size: 14px; color: #303133; }
.layout-main {
  background: #f0f2f5;
  padding: 16px;
  overflow-y: auto;
}
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
```

---

## 五、业务页面清单

> 每节给出：路由路径 + 功能要点 + 关键代码片段（搜索栏/表格列/操作按钮/分页）。

### 5.1 课程管理

> **路由**：`/course`（列表）、`/course/edit/:id?`（编辑）

| 功能点 | 说明 |
|--------|------|
| 列表 | 表格展示课程，支持按标题/类型/状态搜索 |
| 新增/编辑 | 表单：标题、描述、封面上传、类型、学时、讲师选择 |
| **offline_flag 开关** | 勾选"允许离线学习"后，系统异步打包 ZIP 并生成下载链接 |
| 发布/下架 | 状态流转：草稿(0) → 已发布(1) → 已下架(2) |

**关键代码片段 — 搜索栏 + 表格列 + 操作按钮 + 分页：**

```vue
<template>
  <div>
    <!-- 搜索栏 -->
    <el-form :inline="true" :model="queryForm" class="search-form">
      <el-form-item label="课程名称">
        <el-input v-model="queryForm.title" placeholder="请输入" clearable />
      </el-form-item>
      <el-form-item label="类型">
        <el-select v-model="queryForm.courseType" placeholder="全部" clearable>
          <el-option label="公开课" :value="1" />
          <el-option label="必修课" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryForm.status" placeholder="全部" clearable>
          <el-option label="草稿" :value="0" />
          <el-option label="已发布" :value="1" />
          <el-option label="已下架" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="onSearch">查询</el-button>
        <el-button @click="onReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-button type="primary" @click="onAdd">新增课程</el-button>

    <!-- 表格 -->
    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="课程名称" />
      <el-table-column label="类型">
        <template #default="{ row }>
          <el-tag :type="row.courseType === 1 ? 'success' : 'warning'">
            {{ row.courseType === 1 ? '公开课' : '必修课' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="teacherName" label="讲师" />
      <el-table-column label="离线学习">
        <template #default="{ row }">
          <el-tag :type="row.offlineFlag === 1 ? 'success' : 'info'">
            {{ row.offlineFlag === 1 ? '可离线' : '不可离线' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button link type="primary" @click="onEdit(row)">编辑</el-button>
          <el-button link type="success" @click="onPublish(row, 1)" v-if="row.status !== 1">发布</el-button>
          <el-button link type="warning" @click="onPublish(row, 0)" v-if="row.status === 1">下架</el-button>
          <el-button link type="danger" @click="onDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="queryForm.pageNum"
      v-model:page-size="queryForm.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="loadList"
      @current-change="loadList"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCourseList, publishCourse, deleteCourse } from '@/api/course'

const router = useRouter()
const list = ref([])
const total = ref(0)
const queryForm = reactive({ title: '', courseType: null, status: null, pageNum: 1, pageSize: 10 })

async function loadList() {
  const res = await getCourseList(queryForm)
  list.value = res.data.records
  total.value = res.data.total
}
function onSearch() { queryForm.pageNum = 1; loadList() }
function onReset() { Object.assign(queryForm, { title: '', courseType: null, status: null, pageNum: 1 }); loadList() }
function onAdd() { router.push('/course/edit') }
function onEdit(row: any) { router.push(`/course/edit/${row.id}`) }
async function onPublish(row: any, status: number) {
  await publishCourse({ id: row.id, status })
  ElMessage.success(status === 1 ? '已发布' : '已下架')
  loadList()
}
async function onDelete(row: any) {
  await ElMessageBox.confirm(`确定删除课程"${row.title}"？`, '提示', { type: 'warning' })
  await deleteCourse(row.id)
  ElMessage.success('删除成功')
  loadList()
}
function statusText(s: number) { return ['草稿', '已发布', '已下架'][s] }
function statusType(s: number) { return ['info', 'success', 'warning'][s] as any }
onMounted(loadList)
</script>
```

**课程编辑页 offline_flag 开关片段：**

```vue
<el-form-item label="允许离线学习">
  <el-switch
    v-model="form.offlineFlag"
    :active-value="1"
    :inactive-value="0"
    active-text="是"
    inactive-text="否"
  />
  <span class="form-tip">开启后系统将异步打包 ZIP 离线包，学员可下载学习</span>
</el-form-item>
```

---

### 5.2 章节管理

> **路由**：`/chapter/:courseId`

| 功能点 | 说明 |
|--------|------|
| 章节列表 | 按 `sort_order` 排序展示 |
| 新增/编辑 | 标题、排序、视频上传、时长 |
| 视频上传 | 使用 `el-upload`，限制 MP4，最大 500MB |
| 排序 | 上移/下移按钮调整播放顺序 |

**关键代码片段 — 视频上传 + 排序：**

```vue
<el-form-item label="视频">
  <el-upload
    action="/admin/chapter/upload"
    :headers="{ Authorization: 'Bearer ' + token }"
    accept="video/mp4"
    :limit="1"
    :on-success="onVideoSuccess"
    :before-upload="beforeVideoUpload"
  >
    <el-button type="primary">选择视频</el-button>
  </el-upload>
  <span v-if="form.videoUrl" class="video-name">{{ form.videoUrl }}</span>
</el-form-item>

<script setup>
function beforeVideoUpload(file: File) {
  const isMp4 = file.type === 'video/mp4'
  const isLt500M = file.size / 1024 / 1024 < 500
  if (!isMp4) ElMessage.error('仅支持 MP4 格式')
  if (!isLt500M) ElMessage.error('视频大小不能超过 500MB')
  return isMp4 && isLt500M
}
function onVideoSuccess(res: any) { form.videoUrl = form.videoUrl = res.data.url }
</script>
```

---

### 5.3 知识点管理

> **路由**：`/knowledge-point`

| 功能点 | 说明 |
|--------|------|
| 列表 | 按章节/关键词筛选 |
| 新增/编辑 | 标题、内容、关联章节 |
| 关联试题 | 知识点下挂试题，支撑"知识点下管理试题" |

**关键代码片段 — 搜索栏 + 表格：**

```vue
<el-form :inline="true" :model="queryForm">
  <el-form-item label="章节">
    <el-select v-model="queryForm.chapterId" placeholder="全部" clearable filterable>
      <el-option v-for="c in chapterList" :key="c.id" :label="c.title" :value="c.id" />
    </el-select>
  </el-form-item>
  <el-form-item label="关键词">
    <el-input v-model="queryForm.keyword" placeholder="请输入" clearable />
  </el-form-item>
  <el-form-item>
    <el-button type="primary" @click="loadList">查询</el-button>
  </el-form-item>
</el-form>

<el-table :data="list" border>
  <el-table-column prop="id" label="ID" width="60" />
  <el-table-column prop="title" label="知识点" />
  <el-table-column prop="chapterTitle" label="所属章节" />
  <el-table-column prop="content" label="内容" show-overflow-tooltip />
  <el-table-column label="操作" width="160">
    <template #default="{ row }">
      <el-button link type="primary" @click="onEdit(row)">编辑</el-button>
      <el-button link type="danger" @click="onDelete(row)">删除</el-button>
    </template>
  </el-table-column>
</el-table>
```

---

### 5.4 试题管理（5 种题型动态表单）

> **路由**：`/question`

| 功能点 | 说明 |
|--------|------|
| 列表 | 按知识点/题型/难度筛选 |
| 新增 | 5 种题型动态表单：单选/多选给 options 输入、判断给 radio、填空/问答给 text |
| 批量导入 | Excel 批量导入 |

**完整题型切换代码片段（核心）：**

```vue
<template>
  <el-dialog :title="form.id ? '编辑试题' : '新增试题'" v-model="visible" width="700px">
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="题型" prop="questionType">
        <el-radio-group v-model="form.questionType">
          <el-radio :value="1">单选题</el-radio>
          <el-radio :value="2">多选题</el-radio>
          <el-radio :value="3">判断题</el-radio>
          <el-radio :value="4">填空题</el-radio>
          <el-radio :value="5">问答题</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="题目内容" prop="title">
        <el-input v-model="form.title" type="textarea" :rows="3" />
      </el-form-item>

      <el-form-item label="知识点" prop="knowledgePointId">
        <el-select v-model="form.knowledgePointId" placeholder="请选择" filterable>
          <el-option v-for="kp in kpList" :key="kp.id" :label="kp.title" :value="kp.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="难度" prop="difficulty">
        <el-radio-group v-model="form.difficulty">
          <el-radio :value="1">简单</el-radio>
          <el-radio :value="2">普通</el-radio>
          <el-radio :value="3">困难</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- ========== 动态表单：根据题型切换 ========== -->

      <!-- 单选/多选：options 输入（A/B/C/D 选项） -->
      <template v-if="[1, 2].includes(form.questionType)">
        <el-form-item
          v-for="(opt, idx) in form.options"
          :key="idx"
          :label="`选项 ${optionLabels[idx]}`"
          :prop="`options.${idx}`"
          :rules="{ required: true, message: `请输入选项 ${optionLabels[idx]}`, trigger: 'blur' }"
        >
          <el-input v-model="form.options[idx]" placeholder="如：A. ≥140/90" />
        </el-form-item>
        <el-form-item>
          <el-button size="small" @click="addOption" v-if="form.options.length < 6">+ 添加选项</el-button>
        </el-form-item>
        <el-form-item label="正确答案" prop="answer">
          <el-select v-model="form.answer" placeholder="请选择">
            <el-option
              v-for="(opt, idx) in form.options"
              :key="idx"
              :label="`${optionLabels[idx]}. ${opt}`"
              :value="optionLabels[idx]"
              v-if="opt"
            />
          </el-select>
          <span class="form-tip" v-if="form.questionType === 2">多选可多选（如 AB）</span>
        </el-form-item>
      </template>

      <!-- 判断：radio -->
      <template v-else-if="form.questionType === 3">
        <el-form-item label="正确答案" prop="answer">
          <el-radio-group v-model="form.answer">
            <el-radio value="正确">正确</el-radio>
            <el-radio value="错误">错误</el-radio>
          </el-radio-group>
        </el-form-item>
      </template>

      <!-- 填空/问答：text -->
      <template v-else>
        <el-form-item label="参考答案" prop="answer">
          <el-input v-model="form.answer" type="textarea" :rows="3" />
        </el-form-item>
      </template>

      <el-form-item label="分值" prop="score">
        <el-input-number v-model="form.score" :min="1" :max="20" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="onSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { addQuestion, updateQuestion } from '@/api/question'

const visible = defineModel<boolean>()
const optionLabels = ['A', 'B', 'C', 'D', 'E', 'F']
const kpList = ref<any[]>([])

const form = reactive<any>({
  id: null,
  questionType: 1,
  title: '',
  knowledgePointId: null,
  difficulty: 2,
  options: ['', '', '', ''],
  answer: '',
  score: 5
})

// 题型切换时重置答案与选项
watch(() => form.questionType, (t) => {
  form.answer = t === 2 ? [] : ''
  if ([1, 2].includes(t)) {
    form.options = ['', '', '', '']
  } else {
    form.options = []
  }
})

function addOption() { if (form.options.length < 6) form.options.push('') }

async function onSubmit() {
  if (form.id) await updateQuestion(form)
  else await addQuestion(form)
  ElMessage.success('保存成功')
  visible.value = false
  emit('success')
}
</script>
```

---

### 5.5 考试管理（自动组卷 + 进度显示）

> **路由**：`/exam`

| 功能点 | 说明 |
|--------|------|
| 列表 | 按考试类型/标题筛选 |
| 创建考试 | 标题、类型、关联课程/计划、总分、及格分、时长、重考次数 |
| **自动组卷** | 点击按钮 → 调用 `/admin/exam/generate` → 显示进度 |
| 进度显示 | 组卷过程显示 loading + 进度条 |

**关键代码片段 — 自动组卷按钮 + 进度：**

```vue
<el-button
  type="success"
  :loading="generating"
  @click="onGenerate(row)"
>
  自动组卷
</el-button>

<!-- 组卷进度 -->
<el-dialog v-model="generateVisible" title="自动组卷" width="400px" :close-on-click-modal="false">
  <el-progress :percentage="progress" :status="progressStatus" />
  <p class="generate-tip">{{ progressTip }}</p>
</el-dialog>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { generatePaper } from '@/api/exam'

const generating = ref(false)
const generateVisible = ref(false)
const progress = ref(0)
const progressStatus = ref('')
const progressTip = ref('正在准备题库...')

async function onGenerate(exam: any) {
  generating.value = true
  generateVisible.value = true
  progress.value = 0
  progressTip.value = '正在准备题库...'

  const timer = setInterval(() => {
    if (progress.value < 90) progress.value += Math.random() * 15
  }, 300)

  try {
    await generatePaper({ examId: exam.id })
    progress.value = 100
    progressStatus.value = 'success'
    progressTip.value = '组卷完成！'
    ElMessage.success('自动组卷成功')
  } catch {
    progressStatus.value = 'exception'
    progressTip.value = '组卷失败，请重试'
  } finally {
    clearInterval(timer)
    generating.value = false
    setTimeout(() => (generateVisible.value = false), 1500)
  }
}
</script>
```

---

### 5.6 用户 + 讲师管理（角色/岗位下拉）

> **路由**：`/user`、`/teacher`

| 功能点 | 说明 |
|--------|------|
| 用户列表 | 按角色/姓名搜索 |
| 新增/编辑 | 用户名、姓名、角色、机构、岗位类型、状态 |
| 讲师列表 | 按姓名搜索 |
| 新增/编辑 | 关联用户、职称、机构、简介 |

**关键代码片段 — 角色/岗位下拉：**

```vue
<el-form-item label="角色" prop="role">
  <el-select v-model="form.role" placeholder="请选择">
    <el-option label="管理员" value="admin" />
    <el-option label="讲师" value="teacher" />
    <el-option label="学员" value="student" />
  </el-select>
</el-form-item>

<el-form-item label="岗位类型" prop="jobType">
  <el-select v-model="form.jobType" placeholder="请选择" clearable>
    <el-option label="临床医学" value="临床医学" />
    <el-option label="公共卫生" value="公共卫生" />
    <el-option label="护理" value="护理" />
    <el-option label="医技（B超/心电/检验/影像）" value="医技" />
  </el-select>
</el-form-item>
```

---

### 5.7 培训计划（课程关联多选 + 拖拽排序占位）

> **路由**：`/plan`

| 功能点 | 说明 |
|--------|------|
| 计划列表 | 按标题/状态筛选 |
| 创建计划 | 标题、描述、时间范围、参与学员 |
| **课程关联** | 多选课程 + 拖拽排序（占位） |
| 流程控制 | `sort_order` 控制学习顺序 |

**关键代码片段 — 课程关联多选 + 拖拽排序占位：**

```vue
<el-form-item label="关联课程" prop="courseIds">
  <el-select v-model="form.courseIds" multiple filterable placeholder="请选择课程" style="width: 100%">
    <el-option v-for="c in courseList" :key="c.id" :label="c.title" :value="c.id" />
  </el-select>
</el-form-item>

<!-- 拖拽排序占位（生产环境可接入 vuedraggable） -->
<el-table :data="planCourses" border row-key="id">
  <el-table-column label="排序" width="80">
    <template #default="{ $index }">
      <el-icon class="drag-handle"><Rank /></el-icon>
      {{ $index + 1 }}
    </template>
  </el-table-column>
  <el-table-column prop="title" label="课程名称" />
  <el-table-column label="必修/选修">
    <template #default="{ row }">
      <el-switch v-model="row.isRequired" :active-value="1" :inactive-value="0" />
    </template>
  </el-table-column>
  <el-table-column label="操作" width="120">
    <template #default="{ row }">
      <el-button link type="primary" @click="moveUp(row)">上移</el-button>
      <el-button link type="danger" @click="removeCourse(row)">移除</el-button>
    </template>
  </el-table-column>
</el-table>
```

---

### 5.8 咨询管理（SLA 超时标红 + 人工回复弹窗）

> **路由**：`/consult`

| 功能点 | 说明 |
|--------|------|
| 咨询列表 | 按状态/关键词筛选 |
| **SLA 超时标红** | 超过 slaHours（默认 24h）未回复的行标红显示 |
| 人工回复 | 弹窗填写回复内容，提交后状态变"已回复" |
| 知识库管理 | 关键词 + 内容，支撑智能匹配 |

**关键代码片段 — SLA 超时标红 + 人工回复弹窗：**

```vue
<!-- 表格：超时行标红 -->
<el-table :data="list" border :row-class-name="rowClassName">
  <el-table-column prop="id" label="ID" width="60" />
  <el-table-column prop="title" label="问题" />
  <el-table-column prop="studentName" label="学员" />
  <el-table-column prop="createTime" label="提问时间" />
  <el-table-column label="状态">
    <template #default="{ row }">
      <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
      <el-tag v-if="row.overdueHours > 0" type="danger" size="small" effect="dark">
        超时 {{ row.overdueHours }}h
      </el-tag>
    </template>
  </el-table-column>
  <el-table-column label="操作" width="120">
    <template #default="{ row }">
      <el-button link type="primary" @click="onReply(row)" v-if="row.status === 0">回复</el-button>
      <el-button link @click="onView(row)">查看</el-button>
    </template>
  </el-table-column>
</el-table>

<!-- 人工回复弹窗 -->
<el-dialog v-model="replyVisible" title="人工回复" width="600px">
  <div class="question-content">
    <h4>学员问题：</h4>
    <p>{{ currentRow?.title }}</p>
    <p class="content">{{ currentRow?.content }}</p>
  </div>
  <el-form :model="replyForm" label-width="80px">
    <el-form-item label="回复内容" prop="reply">
      <el-input v-model="replyForm.reply" type="textarea" :rows="5" placeholder="请输入回复内容" />
    </el-form-item>
  </el-form>
  <template #footer>
    <el-button @click="replyVisible = false">取消</el-button>
    <el-button type="primary" @click="submitReply">提交回复</el-button>
  </template>
</el-dialog>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getConsultList, replyConsult } from '@/api/consult'

const list = ref<any[]>([])
const replyVisible = ref(false)
const currentRow = ref<any>(null)
const replyForm = reactive({ id: null, reply: '' })

// 超时行标红
function rowClassName({ row }: { row: any }) {
  if (row.overdueHours > 0) return 'row-overdue'
  return ''
}

function onReply(row: any) {
  currentRow.value = row
  replyForm.id = row.id
  replyForm.reply = ''
  replyVisible.value = true
}

async function submitReply() {
  await replyConsult(replyForm)
  ElMessage.success('回复成功')
  replyVisible.value = false
  loadList()
}

async function loadList() {
  const res = await getConsultList({ pageNum: 1, pageSize: 10 })
  list.value = res.data.records
}
</script>

<style>
/* 超时行红色背景 */
.row-overdue { background-color: #fef0f0 !important; }
</style>
```

---

### 5.9 统计报表 ⭐（ECharts 图文并茂）

> **路由**：`/stats`

| 图表 | 类型 | 数据来源 |
|------|------|---------|
| 学员学习时长 Top10 | 柱状图（Bar） | `/admin/stats/student` |
| 考试通过率 | 饼图（Pie） | `/admin/stats/exam` |
| 近 7 天学习趋势 | 折线图（Line） | `/admin/stats/trend` |
| 平台数据概览 | 数字卡片 | `/admin/stats/overview` |

**完整 Vue + vue-echarts 组件代码片段：**

```vue
<template>
  <div class="stats-page">
    <!-- 数据概览卡片 -->
    <el-row :gutter="16" class="overview-row">
      <el-col :span="6" v-for="item in overview" :key="item.label">
        <el-card shadow="hover" class="overview-card">
          <div class="overview-label">{{ item.label }}</div>
          <div class="overview-value" :style="{ color: item.color }">
            {{ item.value }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card>
          <template #header>学员学习时长 Top10（小时）</template>
          <v-chart class="chart" :option="barOption" autoresize />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>考试通过率</template>
          <v-chart class="chart" :option="pieOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="24">
        <el-card>
          <template #header>近 7 天学习趋势</template>
          <v-chart class="chart line-chart" :option="lineOption" autoresize />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, PieChart, LineChart } from 'echarts/charts'
import {
  TitleComponent, TooltipComponent,
  LegendComponent, GridComponent
} from 'echarts/components'
import { getStatsOverview, getStatsStudent, getStatsExam, getStatsTrend } from '@/api/stats'

// 注册 ECharts 组件
use([
  CanvasRenderer, BarChart, PieChart, LineChart,
  TitleComponent, TooltipComponent, LegendComponent, GridComponent
])

// ========== 概览数据 ==========
const overview = ref([
  { label: '总用户数', value: 0, color: '#409eff' },
  { label: '总课程数', value: 0, color: '#67c23a' },
  { label: '总考试数', value: 0, color: '#e6a23c' },
  { label: '今日活跃', value: 0, color: '#f56c6c' }
])

// ========== 柱状图：学员学习时长 Top10 ==========
const barOption = ref({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: [], axisLabel: { rotate: 20 } },
  yAxis: { type: 'value', name: '小时' },
  series: [{
    name: '学习时长',
    type: 'bar',
    data: [],
    itemStyle: { color: '#409eff', borderRadius: [4, 4, 0, 0] },
    barWidth: '50%'
  }]
})

// ========== 饼图：考试通过率 ==========
const pieOption = ref({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { bottom: '5%', left: 'center' },
  series: [{
    name: '考试结果',
    type: 'pie',
    radius: ['40%', '70%'],
    avoidLabelOverlap: true,
    itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
    label: { show: true, formatter: '{b}: {d}%' },
    data: [
      { value: 0, name: '已通过', itemStyle: { color: '#67c23a' } },
      { value: 0, name: '未通过', itemStyle: { color: '#f56c6c' } }
    ]
  }]
})

// ========== 折线图：近 7 天学习趋势 ==========
const lineOption = ref({
  tooltip: { trigger: 'axis' },
  legend: { data: ['学习人数', '考试人数'] },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: [] },
  yAxis: { type: 'value' },
  series: [
    {
      name: '学习人数', type: 'line', smooth: true, data: [],
      itemStyle: { color: '#409eff' },
      areaStyle: { opacity: 0.1 }
    },
    {
      name: '考试人数', type: 'line', smooth: true, data: [],
      itemStyle: { color: '#67c23a' },
      areaStyle: { opacity: 0.1 }
    }
  ]
})

// ========== 加载数据 ==========
async function loadStats() {
  // 概览
  const ov = await getStatsOverview()
  overview.value[0].value = ov.data.userCount
  overview.value[1].value = ov.data.courseCount
  overview.value[2].value = ov.data.examCount
  overview.value[3].value = ov.data.todayActive

  // 柱状图
  const stu = await getStatsStudent({ pageNum: 1, pageSize: 10 })
  barOption.value.xAxis.data = stu.data.records.map((r: any) => r.realName)
  barOption.value.series[0].data = stu.data.records.map((r: any) => r.totalStudyHours)

  // 饼图
  const exam = await getStatsExam()
  pieOption.value.series[0].data[0].value = exam.data.totalCount * exam.data.passRate
  pieOption.value.series[0].data[1].value = exam.data.totalCount * (1 - exam.data.passRate)

  // 折线图
  const trend = await getStatsTrend({ days: 7 })
  lineOption.value.xAxis.data = trend.data.dates
  lineOption.value.series[0].data = trend.data.studyCounts
  lineOption.value.series[1].data = trend.data.examCounts
}

onMounted(loadStats)
</script>

<style scoped>
.overview-row { margin-bottom: 16px; }
.overview-card { text-align: center; }
.overview-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.overview-value { font-size: 28px; font-weight: bold; }
.chart-row { margin-bottom: 16px; }
.chart { height: 320px; }
.line-chart { height: 360px; }
</style>
```

---

## 六、接口 api 模块清单

> 所有接口返回 `Promise<Result<T>>`，其中 `Result` 结构：`{ code: number; message: string; data: T }`

### src/api/user.ts

```typescript
import { http } from './request'

export interface LoginDTO { username: string; password: string }
export interface LoginVO { token: string; userInfo: UserInfo }
export interface UserInfo {
  id: number; username: string; realName: string;
  role: 'admin' | 'teacher' | 'student'; avatar?: string;
  orgName?: string; jobType?: string;
}

export const loginApi = (data: LoginDTO) => http.post<LoginVO>('/admin/login', data)
export const getUserInfoApi = () => http.get<UserInfo>('/admin/user/info')
export const getUserList = (params: any) => http.get('/admin/user/list', params)
export const addUser = (data: any) => http.post('/admin/user/add', data)
export const updateUser = (data: any) => http.put('/admin/user/edit', data)
export const deleteUser = (id: number) => http.del(`/admin/user/${id}`)
```

### src/api/course.ts

```typescript
import { http } from './request'

export const getCourseList = (params: any) => http.get('/admin/course/list', params)
export const getCourseDetail = (id: number) => http.get(`/admin/course/detail/${id}`)
export const addCourse = (data: any) => http.post('/admin/course/add', data)
export const updateCourse = (data: any) => http.put('/admin/course/edit', data)
export const publishCourse = (data: { id: number; status: number }) =>
  http.put('/admin/course/publish', data)
export const deleteCourse = (id: number) => http.del(`/admin/course/${id}`)
export const enableOffline = (data: { id: number }) =>
  http.post('/admin/course/offline/enable', data)

// 章节
export const getChapterList = (courseId: number) => http.get(`/admin/chapter/list/${courseId}`)
export const addChapter = (data: any) => http.post('/admin/chapter/add', data)
export const updateChapter = (data: any) => http.put('/admin/chapter/edit', data)

// 知识点
export const getKnowledgePointList = (params: any) => http.get('/admin/knowledge-point/list', params)
export const addKnowledgePoint = (data: any) => http.post('/admin/knowledge-point/add', data)
```

### src/api/question.ts

```typescript
import { http } from './request'

export const getQuestionList = (params: any) => http.get('/admin/question/list', params)
export const addQuestion = (data: any) => http.post('/admin/question/add', data)
export const updateQuestion = (data: any) => http.put('/admin/question/edit', data)
export const importQuestions = (formData: FormData) =>
  http.post('/admin/question/import', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
```

### src/api/exam.ts

```typescript
import { http } from './request'

export const getExamList = (params: any) => http.get('/admin/exam/list', params)
export const addExam = (data: any) => http.post('/admin/exam/add', data)
export const generatePaper = (data: { examId: number }) =>
  http.post('/admin/exam/generate', data)
```

### src/api/plan.ts

```typescript
import { http } from './request'

export const getPlanList = (params: any) => http.get('/admin/plan/list', params)
export const addPlan = (data: any) => http.post('/admin/plan/add', data)
export const linkPlanCourses = (data: { planId: number; courses: any[] }) =>
  http.post('/admin/plan/course', data)
```

### src/api/consult.ts

```typescript
import { http } from './request'

export const getConsultList = (params: any) => http.get('/admin/consult/list', params)
export const replyConsult = (data: { id: number; reply: string }) =>
  http.post('/admin/consult/reply', data)
export const getSlaList = (params: any) => http.get('/admin/consult/sla-list', params)
export const getKnowledgeBaseList = (params: any) => http.get('/admin/knowledge-base/list', params)
export const addKnowledgeBase = (data: any) => http.post('/admin/knowledge-base/add', data)
```

### src/api/stats.ts

```typescript
import { http } from './request'

export const getStatsOverview = () => http.get('/admin/stats/overview')
export const getStatsStudent = (params: any) => http.get('/admin/stats/student', params)
export const getStatsExam = (params?: any) => http.get('/admin/stats/exam', params)
export const getStatsCourse = (params: any) => http.get('/admin/stats/course', params)
export const getStatsOrg = (params: any) => http.get('/admin/stats/org', params)
export const getStatsTrend = (params: { days?: number }) => http.get('/admin/stats/trend', params)
```

### src/api/teacher.ts

```typescript
import { http } from './request'

export const getTeacherList = (params: any) => http.get('/admin/teacher/list', params)
export const addTeacher = (data: any) => http.post('/admin/teacher/add', data)
export const updateTeacher = (data: any) => http.put('/admin/teacher/edit', data)
```

---

## 七、样式与主题

### 7.1 全局 CSS 变量（src/styles/index.css）

```css
:root {
  /* 主题色（与 Element Plus 默认主色一致） */
  --color-primary: #409eff;
  --color-success: #67c23a;
  --color-warning: #e6a23c;
  --color-danger: #f56c6c;
  --color-info: #909399;

  /* 布局 */
  --sidebar-width: 220px;
  --sidebar-width-collapse: 64px;
  --header-height: 60px;
  --sidebar-bg: #001529;

  /* 文字 */
  --text-primary: #303133;
  --text-regular: #606266;
  --text-secondary: #909399;
  --text-placeholder: #c0c4cc;

  /* 圆角与阴影 */
  --border-radius: 8px;
  --box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

/* 全局重置 */
html, body, #app { height: 100%; margin: 0; }
body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC',
               'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  color: var(--text-primary);
  background: #f0f2f5;
}

/* 搜索表单统一样式 */
.search-form {
  background: #fff;
  padding: 16px 16px 0;
  border-radius: var(--border-radius);
  margin-bottom: 16px;
}

/* 表格操作按钮间距 */
.el-table .el-button + .el-button { margin-left: 4px; }

/* 表单提示文字 */
.form-tip { font-size: 12px; color: var(--text-secondary); margin-left: 8px; }
```

### 7.2 Element Plus 主题色覆盖（可选）

```css
/* 覆盖 Element Plus CSS 变量（在 main.ts 中引入） */
:root {
  --el-color-primary: #409eff;
  --el-color-primary-light-3: #79bbff;
  --el-color-primary-light-5: #a0cfff;
  --el-color-primary-light-7: #c6e2ff;
  --el-color-primary-light-9: #ecf5ff;
  --el-color-primary-dark-2: #337ecc;
}
```

### 7.3 响应式断点

```css
/* 后台管理系统以 PC 为主，仅做最小响应式适配 */
@media screen and (max-width: 1200px) {
  .chart-row .el-col { width: 100%; margin-bottom: 16px; }
}
@media screen and (max-width: 768px) {
  .layout-aside { position: fixed; z-index: 1000; height: 100vh; }
  .search-form .el-form-item { display: block; width: 100%; }
}
```

---

## 八、开发顺序建议（成员C 参考）

| 阶段 | 产出 | 依赖 |
|------|------|------|
| **第1天** | 项目搭建、目录结构、request/router/store、登录页、布局框架 | 无 |
| **第2天** | 课程管理（列表+编辑+offline_flag）、章节管理、知识点管理 | 成员A 完成课程/章节接口 |
| **第3天** | 试题管理（5 种题型动态表单）、考试管理（自动组卷） | 成员B 完成试题/考试接口 |
| **第4天** | 用户管理、讲师管理、培训计划 | 成员A/B 完成接口 |
| **第5天** | 统计报表（ECharts 三图）、咨询管理（SLA 标红） | 成员B 完成统计/咨询接口 |
| **第6天** | 联调、样式优化、Bug 修复 | 全员 |

---

## 九、注意事项

1. **接口联调**：开发期通过 Vite 代理（`/admin → localhost:9898`）跨域，生产构建后放入 Spring Boot `static/` 目录。
2. **Token 过期**：`request.ts` 响应拦截器已统一处理 401，弹出确认框后跳转登录页。
3. **文件上传**：视频上传需携带 `Authorization` 头，`el-upload` 的 `:headers` 属性动态绑定。
4. **ECharts**：使用 `vue-echarts` 的 `use()` 按需注册组件，减小打包体积。
5. **题型表单**：`questionType` 切换时通过 `watch` 重置 `answer` 与 `options`，避免脏数据。
6. **SLA 标红**：咨询列表的 `row-class-name` 根据 `overdueHours > 0` 动态返回类名。
7. **角色权限**：讲师角色部分接口只读，可通过 `v-if="userStore.userInfo?.role === 'admin'"` 控制按钮显示。

---

> 📌 **使用建议**：
> - 本手册与 `docs/dev-api.md` 接口清单一一对应，联调时请对照接口路径与字段。
> - 所有 Vue 3 代码均使用 `<script setup lang="ts">` 风格，可直接复制到项目运行。
> - 统计报表章节（5.9）为答辩重点，ECharts 三图务必完整实现。
