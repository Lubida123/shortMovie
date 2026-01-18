// 管理员本地存储KEY
const ADMIN_TOKEN_KEY = 'admin_token'
const ADMIN_USER_KEY = 'admin_user_info'

// 存储Token
export const setAdminToken = (token) => localStorage.setItem(ADMIN_TOKEN_KEY, token)

// 获取Token
export const getAdminToken = () => localStorage.getItem(ADMIN_TOKEN_KEY) || null

// 存储用户信息
export const setAdminUserInfo = (info) => localStorage.setItem(ADMIN_USER_KEY, JSON.stringify(info))

// 获取用户信息
export const getAdminUserInfo = () => {
  const info = localStorage.getItem(ADMIN_USER_KEY)
  return info ? JSON.parse(info) : null
}

// 校验是否登录
export const isAdminLogin = () => !!getAdminToken()

// 清除所有管理员存储（退出登录）
export const clearAdminStorage = () => {
  localStorage.removeItem(ADMIN_TOKEN_KEY)
  localStorage.removeItem(ADMIN_USER_KEY)
}

// 修改：取消超级管理员，admin角色拥有所有权限
export const hasAdminPermission = (permissionCode) => {
  const userInfo = getAdminUserInfo()
  if (!userInfo || !userInfo.permissions) return false
  // 移除super_admin判断，admin角色直接拥有所有权限
  if (userInfo.role === 'admin') return true
  return userInfo.permissions.includes(permissionCode)
}

// 模拟获取管理员信息（用于开发）
export const mockAdminUserInfo = () => {
  return {
    username: '管理员',
    role: 'admin',
    permissions: [
      'user:manage',
      'permission:manage',
      'video:manage',
      'data:analysis'
    ]
  }
}