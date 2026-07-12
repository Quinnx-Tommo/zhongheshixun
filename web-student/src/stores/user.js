import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getProfile, updateProfile } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  // 从 localStorage 初始化
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const realName = computed(() => userInfo.value?.realName || '学员')
  const userId = computed(() => userInfo.value?.id)
  // RBAC: 角色编码（ADMIN/TEACHER/STUDENT），来自 /admin/login 返回的 userInfo.role
  // 兼容字段：后端同步返回 userInfo.roleCode，任一存在即可
  const roleCode = computed(() => userInfo.value?.role || userInfo.value?.roleCode || '')

  // 登录（调 /admin/login，走 8080）
  async function login(loginForm) {
    const data = await loginApi(loginForm)
    // 后端失败响应形如 {code:1002, message:'用户名或密码错误'}，显式抛出，
    // 避免把 undefined 写入 token / localStorage
    if (data.code !== 200) {
      const err = new Error(data.message || '登录失败')
      err.code = data.code
      throw err
    }
    token.value = data.data.token
    userInfo.value = data.data.userInfo
    localStorage.setItem('token', data.data.token)
    localStorage.setItem('userInfo', JSON.stringify(data.data.userInfo))
    return data.data
  }

  // 加载个人资料
  async function loadProfile() {
    const data = await getProfile()
    userInfo.value = data
    localStorage.setItem('userInfo', JSON.stringify(data))
    return data
  }

  // 更新个人资料
  async function update(data) {
    await updateProfile(data)
    await loadProfile()
  }

  // 登出
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    realName,
    userId,
    roleCode,
    login,
    loadProfile,
    update,
    logout,
  }
})
