import http from './http'

export const uploadVideo = (payload) => {
  const formData = new FormData()
  Object.entries(payload || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      formData.append(key, value)
    }
  })
  return http.post('/video/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export const getVideoList = (params) => http.get('/video/list', { params })

export const recordPlay = (videoId, playRecord) =>
  http.post(`/video/${videoId}/play`, playRecord)
