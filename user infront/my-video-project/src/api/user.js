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

export const login = (payload) =>
  http.post('/user/login', toFormParams(payload), {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })

export const register = (payload) =>
  http.post('/user/register', toFormParams(payload), {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })

export const sendEmailCode = (payload) =>
  http.post('/user/send-code', toFormParams(payload), {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })

export const getProfile = () => http.get('/user/profile')
