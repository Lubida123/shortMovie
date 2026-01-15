import http from './http'

const toFormParams = (payload) => {
  const params = new URLSearchParams()
  Object.entries(payload || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      params.append(key, value)
    }
  })
  return params
}

const formHeaders = { 'Content-Type': 'application/x-www-form-urlencoded' }

export const login = (payload) =>
  http.post('/user/login', toFormParams(payload), { headers: formHeaders })

export const register = (payload) =>
  http.post('/user/register', toFormParams(payload), { headers: formHeaders })

export const sendEmailCode = (payload) =>
  http.post('/user/send-code', toFormParams(payload), { headers: formHeaders })

export const getProfile = () => http.get('/user/profile')

export const updateUserInfo = (data) => {
  if (data instanceof FormData) {
    return http.put('/user-info/update', data, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  }
  return http.put('/user-info/update', data)
}

export const updatePassword = (payload) =>
  http.put('/user/password', toFormParams(payload), { headers: formHeaders })

export const logout = () => http.post('/user/logout')

export const userApi = {
  login,
  register,
  sendEmailCode,
  getProfile,
  updateUserInfo,
  updatePassword,
  logout,
}
