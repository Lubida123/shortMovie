// src/api/admin/dataApi.js
import adminHttp from './http'

// 获取数据分析统计数据
export const getDataAnalysisStats = (params) => adminHttp.get('/data/analysis/stats', { params })

// 获取播放量趋势数据
export const getViewTrendData = (params) => adminHttp.get('/data/trend/views', { params })

// 获取用户互动数据
export const getInteractionData = (params) => adminHttp.get('/data/interaction', { params })

// 获取用户增长数据
export const getUserGrowthData = (params) => adminHttp.get('/data/user/growth', { params })

// 获取视频分类数据
export const getCategoryData = (params) => adminHttp.get('/data/category', { params })

// 获取热门时段数据
export const getActivePeriodData = (params) => adminHttp.get('/data/active/period', { params })

// 获取热门视频排行榜
export const getHotVideos = (params) => adminHttp.get('/data/hot/videos', { params })

// 获取热门创作者排行榜
export const getHotCreators = (params) => adminHttp.get('/data/hot/creators', { params })

// 导出数据
export const exportAnalysisData = (params) => adminHttp.get('/data/export', { 
  params,
  responseType: 'blob' // 用于下载文件
})