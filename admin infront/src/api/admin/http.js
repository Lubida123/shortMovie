import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getAdminToken, clearAdminStorage } from '@/utils/adminAuth'

// 创建管理员请求实例
const adminHttp = axios.create({
  baseURL: '/api/admin',
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// 模拟数据存储（用于模拟后端数据持久化）
let mockUsers = [
  {
    id: 1,
    username: '测试用户1',
    phone: '13800138000',
    role: 'user',
    status: 'active',
    createTime: '2026-01-01 10:00:00'
  },
  {
    id: 2,
    username: '管理员',
    phone: '13800138001',
    role: 'admin',
    status: 'active',
    createTime: '2026-01-02 10:00:00'
  }
]

let mockRoles = [
  { roleCode: 'admin', roleName: '管理员', description: '拥有所有权限' },
  { roleCode: 'user', roleName: '普通用户', description: '基础查看权限' }
]

let mockPermissions = [
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

// 请求拦截器：添加Token + 模拟数据
adminHttp.interceptors.request.use(
  (config) => {
    // 添加管理员Token
    const token = getAdminToken()
    if (token) {
      config.headers['X-Admin-Token'] = token
    }

    // ==================== 用户管理相关接口 ====================
    
    // 获取用户列表
    if (config.url.includes('/user/list')) {
      return Promise.reject({
        mock: true,
        data: {
          list: mockUsers,
          total: mockUsers.length
        }
      })
    }

    // 新增用户
    if (config.url.includes('/user/add') && config.method === 'post') {
      const newUser = {
        ...config.data,
        id: mockUsers.length > 0 ? Math.max(...mockUsers.map(u => u.id)) + 1 : 1,
        createTime: new Date().toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        }).replace(/\//g, '-')
      }
      
      mockUsers.unshift(newUser)
      
      return Promise.reject({
        mock: true,
        data: {
          success: true,
          message: '用户创建成功',
          data: newUser
        }
      })
    }

    // 编辑用户
    if (config.url.includes('/user/edit') && config.method === 'put') {
      const userData = config.data
      const index = mockUsers.findIndex(u => u.id === userData.id)
      
      if (index !== -1) {
        // 保留原有ID和创建时间
        const updatedUser = {
          ...mockUsers[index],
          ...userData
        }
        mockUsers[index] = updatedUser
        
        return Promise.reject({
          mock: true,
          data: {
            success: true,
            message: '用户信息更新成功',
            data: updatedUser
          }
        })
      }
      
      return Promise.reject({
        mock: true,
        data: {
          success: false,
          message: '用户不存在'
        }
      })
    }

    // 删除用户
    if (config.url.includes('/user/delete/') && config.method === 'delete') {
      const id = parseInt(config.url.split('/').pop())
      const originalLength = mockUsers.length
      
      mockUsers = mockUsers.filter(user => user.id !== id)
      
      return Promise.reject({
        mock: true,
        data: {
          success: originalLength !== mockUsers.length,
          message: originalLength !== mockUsers.length ? '用户删除成功' : '用户不存在'
        }
      })
    }

    // 变更用户状态
    if (config.url.includes('/user/changeStatus') && config.method === 'put') {
      const { id, status } = config.data
      const user = mockUsers.find(u => u.id === id)
      
      if (user) {
        user.status = status
        
        return Promise.reject({
          mock: true,
          data: {
            success: true,
            message: `用户状态已${status === 'active' ? '启用' : '禁用'}`,
            data: user
          }
        })
      }
      
      return Promise.reject({
        mock: true,
        data: {
          success: false,
          message: '用户不存在'
        }
      })
    }

    // ==================== 角色管理相关接口 ====================
    
    // 获取角色列表
    if (config.url.includes('/role/list')) {
      return Promise.reject({
        mock: true,
        data: mockRoles
      })
    }

    // 新增角色
    if (config.url.includes('/role/add') && config.method === 'post') {
      const newRole = config.data
      
      // 检查角色编码是否已存在
      if (mockRoles.some(role => role.roleCode === newRole.roleCode)) {
        return Promise.reject({
          mock: true,
          data: {
            success: false,
            message: '角色编码已存在'
          }
        })
      }
      
      mockRoles.push(newRole)
      
      return Promise.reject({
        mock: true,
        data: {
          success: true,
          message: '角色创建成功',
          data: newRole
        }
      })
    }

    // 编辑角色
    if (config.url.includes('/role/edit') && config.method === 'put') {
      const roleData = config.data
      const index = mockRoles.findIndex(r => r.roleCode === roleData.roleCode)
      
      if (index !== -1) {
        mockRoles[index] = { ...mockRoles[index], ...roleData }
        
        return Promise.reject({
          mock: true,
          data: {
            success: true,
            message: '角色信息更新成功',
            data: mockRoles[index]
          }
        })
      }
      
      return Promise.reject({
        mock: true,
        data: {
          success: false,
          message: '角色不存在'
        }
      })
    }

    // 删除角色
    if (config.url.includes('/role/delete/') && config.method === 'delete') {
      const roleCode = config.url.split('/').pop()
      
      // 系统角色不能删除
      if (roleCode === 'admin') {
        return Promise.reject({
          mock: true,
          data: {
            success: false,
            message: '系统角色不能删除'
          }
        })
      }
      
      const originalLength = mockRoles.length
      mockRoles = mockRoles.filter(role => role.roleCode !== roleCode)
      
      return Promise.reject({
        mock: true,
        data: {
          success: originalLength !== mockRoles.length,
          message: originalLength !== mockRoles.length ? '角色删除成功' : '角色不存在'
        }
      })
    }

    // ==================== 权限管理相关接口 ====================
    
    // 获取权限树形结构
    if (config.url.includes('/permission/tree')) {
      return Promise.reject({
        mock: true,
        data: mockPermissions
      })
    }

    // 获取角色权限
    if (config.url.includes('/role/permission/') && config.method === 'get') {
      const roleCode = config.url.split('/').pop()
      
      // 管理员默认拥有所有权限
      if (roleCode === 'admin') {
        const allPermissions = []
        const extractPermissions = (nodes) => {
          nodes.forEach(node => {
            allPermissions.push(node.permissionCode)
            if (node.children) {
              extractPermissions(node.children)
            }
          })
        }
        extractPermissions(mockPermissions)
        
        return Promise.reject({
          mock: true,
          data: allPermissions
        })
      }
      
      // 其他角色默认没有权限（可以根据需要修改）
      return Promise.reject({
        mock: true,
        data: []
      })
    }

    // 保存角色权限
    if (config.url.includes('/role/savePermission') && config.method === 'post') {
      const { roleCode, permissionCodes } = config.data
      
      // 这里只是模拟保存，实际应该存储到"数据库"中
      console.log(`保存角色 ${roleCode} 的权限:`, permissionCodes)
      
      return Promise.reject({
        mock: true,
        data: {
          success: true,
          message: '权限保存成功'
        }
      })
    }

    // ==================== 视频管理相关接口 ====================
    
    // 模拟视频列表数据
    if (config.url.includes('/video/list')) {
      // ... 原有的视频列表模拟代码保持不变 ...
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

    // ==================== 登录接口 ====================
    
    // 管理员登录
    if (config.url.includes('/login') && config.method === 'post') {
      const { username, password } = config.data
      
      // 模拟登录验证
      if (username === 'admin' && password === '123456') {
        return Promise.reject({
          mock: true,
          data: {
            success: true,
            message: '登录成功',
            token: 'mock-admin-token-123456',
            userInfo: {
              username: '管理员',
              role: 'admin',
              permissions: ['user:manage', 'video:manage', 'permission:manage']
            }
          }
        })
      }
      
      return Promise.reject({
        mock: true,
        data: {
          success: false,
          message: '用户名或密码错误'
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
    if (error.mock) {
      // 如果是失败响应，抛出错误
      if (error.data && error.data.success === false) {
        return Promise.reject(new Error(error.data.message || '操作失败'))
      }
      return error.data
    }

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