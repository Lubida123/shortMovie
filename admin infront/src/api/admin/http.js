import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getAdminToken, clearAdminStorage } from '@/utils/adminAuth'

// 创建管理员请求实例（修改baseURL避免被拦截）
const adminHttp = axios.create({
  baseURL: '/api/admin', // 本地前缀，避免第三方接口拦截
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// 请求拦截器：添加Token + 模拟数据
adminHttp.interceptors.request.use(
  (config) => {
    // 添加管理员Token
    const token = getAdminToken()
    if (token) {
      config.headers['X-Admin-Token'] = token
    }

    // 模拟用户列表数据
    if (config.url.includes('/user/list')) {
      return Promise.reject({
        mock: true,
        data: {
          list: [
            { id: 1, username: '测试用户1', phone: '13800138000', role: 'user', status: 'active', createTime: '2026-01-01 10:00:00' },
            { id: 2, username: '管理员', phone: '13800138001', role: 'admin', status: 'active', createTime: '2026-01-02 10:00:00' }
          ],
          total: 2
        }
      })
    }

    // 模拟角色列表数据
    if (config.url.includes('/role/list')) {
      return Promise.reject({
        mock: true,
        data: [
          { roleCode: 'admin', roleName: '管理员', description: '拥有所有权限' },
          { roleCode: 'user', roleName: '普通用户', description: '基础查看权限' }
        ]
      })
    }

    // 模拟权限树数据
    if (config.url.includes('/permission/tree')) {
      return Promise.reject({
        mock: true,
        data: [
          {
            permissionCode: 'user:manage',
            permissionName: '用户管理',
            children: [{ permissionCode: 'user:add', permissionName: '新增用户' }]
          },
          {
            permissionCode: 'video:manage',
            permissionName: '视频管理',
            children: [
              { permissionCode: 'video:approve', permissionName: '审核视频' },
              { permissionCode: 'video:delete', permissionName: '删除视频' }
            ]
          },
          {
            permissionCode: 'permission:manage',
            permissionName: '权限管理',
            children: [{ permissionCode: 'role:add', permissionName: '新增角色' }]
          }
        ]
      })
    }

    // 模拟视频列表数据
    if (config.url.includes('/video/list')) {
      // 生成模拟视频数据
      const generateMockVideos = () => {
        const videos = []
        const categories = ['life', 'entertainment', 'knowledge', 'game', 'music']
        const statuses = ['pending', 'approved', 'rejected', 'taken_down']
        const authors = ['张三', '李四', '王五', '赵六', '钱七']
        
        for (let i = 1; i <= 100; i++) {
          videos.push({
            id: i,
            title: `测试视频标题 ${i}`,
            coverUrl: `https://via.placeholder.com/120x80?text=Video${i}`,
            duration: Math.floor(Math.random() * 600) + 60,
            authorName: authors[Math.floor(Math.random() * authors.length)],
            authorAvatar: `https://via.placeholder.com/40?text=User${Math.floor(Math.random() * 5) + 1}`,
            category: categories[Math.floor(Math.random() * categories.length)],
            views: Math.floor(Math.random() * 1000000),
            likes: Math.floor(Math.random() * 100000),
            comments: Math.floor(Math.random() * 10000),
            shares: Math.floor(Math.random() * 5000),
            status: statuses[Math.floor(Math.random() * statuses.length)],
            createTime: `2024-${String(Math.floor(Math.random() * 12) + 1).padStart(2, '0')}-${String(Math.floor(Math.random() * 28) + 1).padStart(2, '0')} ${String(Math.floor(Math.random() * 24)).padStart(2, '0')}:${String(Math.floor(Math.random() * 60)).padStart(2, '0')}:${String(Math.floor(Math.random() * 60)).padStart(2, '0')}`
          })
        }
        return videos
      }
      
      const allVideos = generateMockVideos()
      const params = config.params || {}
      let filteredVideos = [...allVideos]
      
      // 应用筛选
      if (params.keyword) {
        const keyword = params.keyword.toLowerCase()
        filteredVideos = filteredVideos.filter(v => 
          v.title.toLowerCase().includes(keyword) || 
          v.authorName.toLowerCase().includes(keyword)
        )
      }
      
      if (params.status) {
        filteredVideos = filteredVideos.filter(v => v.status === params.status)
      }
      
      if (params.category) {
        filteredVideos = filteredVideos.filter(v => v.category === params.category)
      }
      
      // 分页
      const page = params.pageNum || 1
      const pageSize = params.pageSize || 10
      const startIndex = (page - 1) * pageSize
      const endIndex = startIndex + pageSize
      
      return Promise.reject({
        mock: true,
        data: {
          list: filteredVideos.slice(startIndex, endIndex),
          total: filteredVideos.length
        }
      })
    }

    // 模拟视频统计数据
    if (config.url.includes('/video/stats')) {
      return Promise.reject({
        mock: true,
        data: {
          totalVideos: 1560,
          pendingVideos: 42,
          approvedVideos: 1480,
          rejectedVideos: 38,
          todayUploads: 156,
          yesterdayUploads: 142
        }
      })
    }

    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：处理模拟数据 + 错误
adminHttp.interceptors.response.use(
  (response) => response.data,
  (error) => {
    // 优先处理模拟数据
    if (error.mock) return error.data

    // 登录过期处理
    if (error.response?.status === 401) {
      ElMessage.error('登录过期，请重新登录')
      clearAdminStorage()
      window.location.href = '/admin/login'
    } else {
      ElMessage.error(error.message || '请求失败')
    }
    return Promise.reject(error)
  }
)

export default adminHttp