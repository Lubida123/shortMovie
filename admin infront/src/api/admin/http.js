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
            permissionCode: 'permission:manage',
            permissionName: '权限管理',
            children: [{ permissionCode: 'role:add', permissionName: '新增角色' }]
          }
        ]
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