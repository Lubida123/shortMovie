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
  http.post('/api/user/login', toFormParams(payload), { headers: formHeaders })

export const register = (payload) =>
  http.post('/api/user/register', toFormParams(payload), { headers: formHeaders })

export const sendEmailCode = (payload) =>
  http.post('/api/user/send-code', toFormParams(payload), { headers: formHeaders })

export const getProfile = () => http.get('/api/user/profile')

export const updateUserInfo = (payload) =>
  http.put('/api/user/profile', null, {
    params: payload,
  })

export const updatePassword = (payload) =>
  http.put('/api/user/password', toFormParams(payload), { headers: formHeaders })

export const logout = () => http.post('/api/user/logout')

export const uploadAvatar = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/cos/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export const userApi = {
  login,
  register,
  sendEmailCode,
  getProfile,
  updateUserInfo,
  updatePassword,
  logout,
  uploadAvatar,
}
