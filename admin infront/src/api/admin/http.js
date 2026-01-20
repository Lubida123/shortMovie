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
  },
  {
  permissionCode: 'data:analysis',
  permissionName: '数据分析',
  children: [
    { permissionCode: 'data:view', permissionName: '查看数据' },
    { permissionCode: 'data:export', permissionName: '导出数据' }
  ]
}
]

// 请求拦截器：添加Token + 模拟数据
adminHttp.interceptors.request.use(
  (config) => {
    // 添加管理员Token
    const token = getAdminToken()
    if (token) {
      config.headers['X-Admin-Token'] = token
    // src/api/admin/http.js - 在请求拦截器中添加

// 获取用户增长数据
if (config.url.includes('/data/user/growth')) {
  const params = config.params || {}
  const days = params.days || 30
  
  const generateData = (count) => {
    return Array.from({ length: count }, () => Math.floor(Math.random() * 200) + 50)
  }
  
  const labels = Array.from({ length: days }, (_, i) => {
    const date = new Date()
    date.setDate(date.getDate() - (days - 1 - i))
    return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
  })
  
  return Promise.reject({
    mock: true,
    data: {
      labels,
      newUsers: generateData(days),
      activeUsers: generateData(days).map(v => v * 10),
      totalUsers: Array.from({ length: days }, (_, i) => 10000 + i * 100)
    }
  })
}

// 获取视频分类数据
if (config.url.includes('/data/category')) {
  return Promise.reject({
    mock: true,
    data: [
      { name: '生活', value: 1560, views: 156000, color: '#FE2C55' },
      { name: '娱乐', value: 1240, views: 124000, color: '#25F4EE' },
      { name: '知识', value: 980, views: 98000, color: '#FF9500' },
      { name: '游戏', value: 760, views: 76000, color: '#00C864' },
      { name: '音乐', value: 540, views: 54000, color: '#AF52DE' }
    ]
  })
}

// 获取热门时段数据
if (config.url.includes('/data/active/period')) {
  const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`)
  return Promise.reject({
    mock: true,
    data: {
      hours,
      weekday: Array.from({ length: 24 }, () => Math.floor(Math.random() * 4000) + 1000),
      weekend: Array.from({ length: 24 }, () => Math.floor(Math.random() * 6000) + 2000),
      average: Array.from({ length: 24 }, () => Math.floor(Math.random() * 5000) + 1500)
    }
  })
}

// 获取热门视频排行榜
if (config.url.includes('/data/hot/videos')) {
  return Promise.reject({
    mock: true,
    data: [
      {
        id: 1,
        cover: 'https://via.placeholder.com/120x80?text=Video1',
        title: '生活小技巧分享',
        author: '张三',
        views: 154321,
        likes: 12345,
        comments: 2345,
        shares: 456,
        interactionRate: 0.095
      },
      {
        id: 2,
        cover: 'https://via.placeholder.com/120x80?text=Video2',
        title: '搞笑短视频合集',
        author: '李四',
        views: 143210,
        likes: 13456,
        comments: 3456,
        shares: 567,
        interactionRate: 0.118
      },
      {
        id: 3,
        cover: 'https://via.placeholder.com/120x80?text=Video3',
        title: '编程入门教程',
        author: '王五',
        views: 132109,
        likes: 14567,
        comments: 4567,
        shares: 678,
        interactionRate: 0.145
      },
      {
        id: 4,
        cover: 'https://via.placeholder.com/120x80?text=Video4',
        title: '游戏精彩集锦',
        author: '赵六',
        views: 121098,
        likes: 15678,
        comments: 5678,
        shares: 789,
        interactionRate: 0.176
      },
      {
        id: 5,
        cover: 'https://via.placeholder.com/120x80?text=Video5',
        title: '音乐现场录制',
        author: '钱七',
        views: 110987,
        likes: 16789,
        comments: 6789,
        shares: 890,
        interactionRate: 0.213
      }
    ]
  })
}

// 获取热门创作者排行榜
if (config.url.includes('/data/hot/creators')) {
  return Promise.reject({
    mock: true,
    data: [
      {
        id: 1,
        avatar: 'https://via.placeholder.com/40?text=ZS',
        name: '张三',
        category: '生活',
        followers: 154321,
        videos: 56,
        totalViews: 1560000,
        totalLikes: 234567,
        totalComments: 45678
      },
      {
        id: 2,
        avatar: 'https://via.placeholder.com/40?text=LS',
        name: '李四',
        category: '娱乐',
        followers: 143210,
        videos: 43,
        totalViews: 1432100,
        totalLikes: 198765,
        totalComments: 34567
      },
      {
        id: 3,
        avatar: 'https://via.placeholder.com/40?text=WW',
        name: '王五',
        category: '知识',
        followers: 132109,
        videos: 32,
        totalViews: 1321090,
        totalLikes: 176543,
        totalComments: 23456
      },
      {
        id: 4,
        avatar: 'https://via.placeholder.com/40?text=ZL',
        name: '赵六',
        category: '游戏',
        followers: 121098,
        videos: 67,
        totalViews: 1210980,
        totalLikes: 154321,
        totalComments: 12345
      },
      {
        id: 5,
        avatar: 'https://via.placeholder.com/40?text=QQ',
        name: '钱七',
        category: '音乐',
        followers: 110987,
        videos: 45,
        totalViews: 1109870,
        totalLikes: 143210,
        totalComments: 11234
      }
    ]
  })
}

// 导出数据接口
if (config.url.includes('/data/export')) {
  // 模拟文件下载
  const blob = new Blob(['模拟导出的数据'], { type: 'application/vnd.ms-excel' })
  const url = URL.createObjectURL(blob)
  
  return Promise.reject({
    mock: true,
    data: {
      success: true,
      message: '数据导出成功',
      url: url
    }
  })
}
// 数据分析统计接口
if (config.url.includes('/data/analysis/stats')) {
  return Promise.reject({
    mock: true,
    data: {
      totalViews: 1567890,
      viewTrend: 12.5,
      totalLikes: 234567,
      likeTrend: 8.3,
      totalComments: 45678,
      commentTrend: 5.7,
      totalShares: 12345,
      shareTrend: 15.2,
      newUsers: 1234,
      activeUsers: 45678,
      totalUsers: 123456
    }
  })
}

// 播放量趋势数据
if (config.url.includes('/data/trend/views')) {
  const params = config.params || {}
  const days = params.days || 7
  
  const generateData = (count) => {
    return Array.from({ length: count }, () => Math.floor(Math.random() * 50000) + 10000)
  }
  
  const labels = Array.from({ length: days }, (_, i) => {
    const date = new Date()
    date.setDate(date.getDate() - (days - 1 - i))
    return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
  })
  
  return Promise.reject({
    mock: true,
    data: {
      labels,
      data: generateData(days)
    }
  })
}

// 用户互动数据
if (config.url.includes('/data/interaction')) {
  return Promise.reject({
    mock: true,
    data: {
      likes: 234567,
      comments: 45678,
      shares: 12345,
      collects: 3456,
      distribution: [
        { name: '点赞', value: 234567, percentage: 60.5 },
        { name: '评论', value: 45678, percentage: 11.8 },
        { name: '分享', value: 12345, percentage: 3.2 },
        { name: '收藏', value: 3456, percentage: 0.9 }
      ]
    }
  })
}
    }

    // ==================== 用户管理相关接口 ====================
    
    

// 获取管理员列表
if (config.url.includes('/admin/list')) {
  const params = config.params || {}
  const keyword = params.keyword || ''
  
  // 筛选管理员
  let adminUsers = mockUsers.filter(user => user.role === 'admin')
  
  // 应用关键词筛选
  if (keyword) {
    const keywordLower = keyword.toLowerCase()
    adminUsers = adminUsers.filter(user => 
      user.username.toLowerCase().includes(keywordLower) || 
      user.phone.includes(keyword)
    )
  }
  
  // 分页逻辑
  const page = params.pageNum || 1
  const pageSize = params.pageSize || 10
  const startIndex = (page - 1) * pageSize
  const endIndex = startIndex + pageSize
  
  return Promise.reject({
    mock: true,
    data: {
      list: adminUsers.slice(startIndex, endIndex),
      total: adminUsers.length
    }
  })
}

// 获取普通用户列表
if (config.url.includes('/user/list')) {
  const params = config.params || {}
  const keyword = params.keyword || ''
  
  // 筛选普通用户
  let normalUsers = mockUsers.filter(user => user.role === 'user')
  
  // 应用关键词筛选
  if (keyword) {
    const keywordLower = keyword.toLowerCase()
    normalUsers = normalUsers.filter(user => 
      user.username.toLowerCase().includes(keywordLower) || 
      user.phone.includes(keyword)
    )
  }
  
  // 分页逻辑
  const page = params.pageNum || 1
  const pageSize = params.pageSize || 10
  const startIndex = (page - 1) * pageSize
  const endIndex = startIndex + pageSize
  
  return Promise.reject({
    mock: true,
    data: {
      list: normalUsers.slice(startIndex, endIndex),
      total: normalUsers.length
    }
  })
}

// 管理员相关操作接口
if (config.url.includes('/admin/add') && config.method === 'post') {
  const newUser = {
    ...config.data,
    id: mockUsers.length > 0 ? Math.max(...mockUsers.map(u => u.id)) + 1 : 1,
    role: 'admin',
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
      message: '管理员创建成功',
      data: newUser
    }
  })
}

if (config.url.includes('/admin/edit') && config.method === 'put') {
  const userData = config.data
  const index = mockUsers.findIndex(u => u.id === userData.id && u.role === 'admin')
  
  if (index !== -1) {
    mockUsers[index] = { ...mockUsers[index], ...userData }
    
    return Promise.reject({
      mock: true,
      data: {
        success: true,
        message: '管理员信息更新成功',
        data: mockUsers[index]
      }
    })
  }
  
  return Promise.reject({
    mock: true,
    data: {
      success: false,
      message: '管理员不存在'
    }
  })
}

if (config.url.includes('/admin/delete/') && config.method === 'delete') {
  const id = parseInt(config.url.split('/').pop())
  const originalLength = mockUsers.length
  
  mockUsers = mockUsers.filter(user => !(user.id === id && user.role === 'admin'))
  
  return Promise.reject({
    mock: true,
    data: {
      success: originalLength !== mockUsers.length,
      message: originalLength !== mockUsers.length ? '管理员删除成功' : '管理员不存在'
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
              permissions: ['user:manage', 'video:manage', 'permission:manage' ,'data:analysis' ]
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