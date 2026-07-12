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

/**
 * 获取用户信息（localStorage → 对象）
 *
 * 防御性设计：
 * - null / '' / 'null' / 'undefined' 字符串均视为空
 * - JSON 解析失败返回 null，避免白屏崩溃
 */
export function getUserInfo(): any {
  const info = localStorage.getItem(USER_INFO_KEY)
  if (!info || info === 'null' || info === 'undefined') {
    return null
  }
  try {
    return JSON.parse(info)
  } catch {
    // 脏数据（如损坏的 JSON、非法值）清理并返回 null
    localStorage.removeItem(USER_INFO_KEY)
    return null
  }
}

export function setUserInfo(userInfo: any): void {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
}

export function removeUserInfo(): void {
  localStorage.removeItem(USER_INFO_KEY)
}
