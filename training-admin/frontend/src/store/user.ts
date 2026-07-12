import { defineStore } from 'pinia'
import { ref } from 'vue'
import { setToken, removeToken, setUserInfo, removeUserInfo, getToken, getUserInfo } from '@/utils/auth'

export interface UserInfo {
  id: number
  username: string
  realName: string
  role: 'admin' | 'teacher' | 'student'
  avatar?: string
  orgName?: string
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken() || '')
  const userInfo = ref<any>(getUserInfo())

  function login(userData: { token: string; userInfo: any }) {
    token.value = userData.token
    userInfo.value = userData.userInfo
    setToken(userData.token)
    setUserInfo(userData.userInfo)
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    removeToken()
    removeUserInfo()
  }

  return { token, userInfo, login, logout }
})
