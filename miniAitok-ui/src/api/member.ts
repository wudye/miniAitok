import request, { publicApi } from '@/utils/request'

// 登录方法（使用 publicApi，不走认证拦截器）
export function login(username: string, password: string): Promise<any> {

  return publicApi({
    url: '/member/api/v1/login',
    method: 'post',
    data: { username, password },
  })
}

export const testLogin = (): Promise<any> => {
  return request({ url: `/member/api/v1`, method: 'get' })
}


export const userLogin = (username: string, password: string): Promise<any> => {
  return publicApi.post('/member/api/v1/login', { username, password })
}

export const userSmsLogin = (telephone: string, smsCode: string): Promise<any> => {
  return publicApi.post('/member/api/v1/sms-login', { telephone, smsCode })
}

// 注册方法
export function register(data: any): Promise<any> {
  return publicApi({ url: '/member/api/v1/register', method: 'post', data })
}

// 获取用户详细信息（受保护接口，使用带认证的 request）
export function getInfo(): Promise<any> {
  return request({ url: '/member/api/v1/userinfo', method: 'get' })
}

export function getPersonInfo(userId: string | number): Promise<any> {
  return request({ url: `/member/api/v1/${userId}`, method: 'get' })
}

// 退出方法
export function logout(): Promise<any> {
  return request({ url: '/logout', method: 'post' })
}

// 更新用户信息
export function updateUserProfile(data: any): Promise<any> {
  return request({ url: '/member/api/v1/update', method: 'put', data })
}

// 修改用户头像（使用 FormData）
export function avatar(file: File | Blob): Promise<any> {
  const fd = new FormData()
  // 如果传入的是 File，后端字段名为 file；如需不同字段名请调整
  fd.append('file', file)
  return request.post('/user/api/v1/avatar', fd, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// 更新用户详情
export function updateMemberInfo(data: any): Promise<any> {
  return request({ url: '/member/api/v1/info/update', method: 'put', data })
}

export default {
  login,
  userLogin,
  userSmsLogin,
  register,
  getInfo,
  getPersonInfo,
  logout,
  updateUserProfile,
  avatar,
  updateMemberInfo,
}
