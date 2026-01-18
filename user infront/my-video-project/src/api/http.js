import axios from 'axios'

const http = axios.create({
  baseURL: '',
  timeout: 10000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('shortmovie_user')
  if (token) {
    try {
      const parsed = JSON.parse(token)
      if (parsed?.token) {
        config.headers.Authorization = `Bearer ${parsed.token}`
      }
    } catch (error) {
      // ignore malformed storage
    }
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => Promise.reject(error)
)

export default http
