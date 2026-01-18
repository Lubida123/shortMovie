import http from './http'

export const uploadVideo = (payload) => {
  const formData = new FormData()
  Object.entries(payload || {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null) {
      formData.append(key, value)
    }
  })
  return http.post('/api/video/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export const getVideoList = (params) => http.get('/api/video/list', { params })

export const getVideoDetail = (videoId) => http.get(`/api/video/${videoId}`)

export const recordPlay = (videoId, playRecord) =>
  http.post(`/api/video/${videoId}/play`, playRecord)
