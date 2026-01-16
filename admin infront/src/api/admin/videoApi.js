// 视频管理API接口
import adminHttp from './http'

// 获取视频列表
export const getVideoList = (params) => adminHttp.get('/video/list', { params })

// 获取视频详情
export const getVideoDetail = (id) => adminHttp.get(`/video/detail/${id}`)

// 审核视频（通过）
export const approveVideo = (id, remark = '') => adminHttp.post(`/video/approve/${id}`, { remark })

// 审核视频（拒绝）
export const rejectVideo = (id, reason = '') => adminHttp.post(`/video/reject/${id}`, { reason })

// 下架视频
export const takeDownVideo = (id, reason = '') => adminHttp.post(`/video/takeDown/${id}`, { reason })

// 恢复视频
export const restoreVideo = (id) => adminHttp.post(`/video/restore/${id}`)

// 删除视频
export const deleteVideo = (id) => adminHttp.delete(`/video/delete/${id}`)

// 批量操作
export const batchApproveVideos = (ids, remark = '') => adminHttp.post('/video/batch/approve', { ids, remark })
export const batchRejectVideos = (ids, reason = '') => adminHttp.post('/video/batch/reject', { ids, reason })
export const batchDeleteVideos = (ids) => adminHttp.post('/video/batch/delete', { ids })

// 获取视频统计数据
export const getVideoStats = () => adminHttp.get('/video/stats')

// 获取视频分类列表
export const getVideoCategories = () => adminHttp.get('/video/categories')

// 导出视频数据
export const exportVideoData = (params) => adminHttp.get('/video/export', { 
  params,
  responseType: 'blob' // 用于下载文件
})

// 更新视频信息
export const updateVideoInfo = (id, data) => adminHttp.put(`/video/update/${id}`, data)