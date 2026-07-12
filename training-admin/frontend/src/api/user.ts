import request from './request'

export interface UserQuery {
  pageNum: number
  pageSize: number
  role?: string
  keyword?: string
  status?: number
}

export function getUserPage(params: UserQuery) {
  return request({ url: '/admin/user/page', method: 'GET', params })
}

export function createUser(data: any) {
  return request({ url: '/admin/user', method: 'POST', data })
}

export function updateUser(data: any) {
  return request({ url: '/admin/user', method: 'PUT', data })
}

export function updateUserStatus(id: number, status: number) {
  return request({ url: `/admin/user/${id}/status`, method: 'PUT', data: { status } })
}

export function deleteUser(id: number) {
  return request({ url: `/admin/user/${id}`, method: 'DELETE' })
}
