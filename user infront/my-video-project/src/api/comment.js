import http from './http'

export const getVideoComments = (videoId, params) =>
  http.get(`/api/comment/video/${videoId}`, { params })

export const createComment = (data) => http.post('/api/comment', data)

export const deleteComment = (commentId) =>
  http.delete(`/api/comment/${commentId}`)

export const getMyComments = (params) => http.get('/api/comment/user', { params })

export const getCommentDetail = (commentId) =>
  http.get(`/api/comment/${commentId}`)

export const commentApi = {
  getVideoComments,
  createComment,
  deleteComment,
  getMyComments,
  getCommentDetail,
}
