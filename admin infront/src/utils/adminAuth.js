// 管理员本地存储KEY
const ADMIN_TOKEN_KEY = 'admin_token'
const ADMIN_USER_KEY = 'admin_user_info'

// 权限映射：将数据库的细粒度权限映射到前端路由所需的模块权限
const PERMISSION_MAPPING = {
  'user:manage': ['USER:READ', 'USER:ADD', 'USER:EDIT', 'USER:DELETE', 'USER:STATUS'],
  'permission:manage': ['ROLE:READ', 'ROLE:ADD', 'ROLE:EDIT', 'ROLE:DELETE', 'PERMISSION:ASSIGN'],
  'video:manage': ['VIDEO:READ', 'VIDEO:APPROVE', 'VIDEO:REJECT', 'VIDEO:DELETE', 'VIDEO:BATCH'],
  'data:analysis': ['DATA:VIEW', 'DATA:STATS', 'DATA:EXPORT']
}

// 存储Token
export const setAdminToken = (token) => {
  try {
    localStorage.setItem(ADMIN_TOKEN_KEY, token)
    console.log('[adminAuth] Token存储成功')
    return true
  } catch (error) {
    console.error('[adminAuth] Token存储失败:', error)
    return false
  }
}

// 获取Token
export const getAdminToken = () => localStorage.getItem(ADMIN_TOKEN_KEY) || null

// 存储用户信息
export const setAdminUserInfo = (info) => {
  try {
    const serialized = JSON.stringify(info)
    localStorage.setItem(ADMIN_USER_KEY, serialized)
    console.log('[adminAuth] 用户信息存储成功:', {
      role: info.role,
      permissions: info.permissions
    })
    return true
  } catch (error) {
    console.error('[adminAuth] 用户信息存储失败:', error)
    return false
  }
}

// 获取用户信息
export const getAdminUserInfo = () => {
  try {
    const info = localStorage.getItem(ADMIN_USER_KEY)
    if (!info) {
      console.warn('[adminAuth] localStorage中没有用户信息')
      return null
    }
    
    const parsed = JSON.parse(info)
    
    // 数据完整性验证
    if (!parsed.role || !Array.isArray(parsed.permissions)) {
      console.error('[adminAuth] 用户信息格式无效:', parsed)
      clearAdminStorage()
      return null
    }
    
    console.log('[adminAuth] 用户信息读取成功:', {
      role: parsed.role,
      permissions: parsed.permissions
    })
    
    return parsed
  } catch (error) {
    console.error('[adminAuth] 用户信息解析失败:', error)
    clearAdminStorage()
    return null
  }
}

// 校验是否登录
export const isAdminLogin = () => !!getAdminToken()

// 清除所有管理员存储（退出登录）
export const clearAdminStorage = () => {
  localStorage.removeItem(ADMIN_TOKEN_KEY)
  localStorage.removeItem(ADMIN_USER_KEY)
  console.log('[adminAuth] 已清除所有管理员存储')
}

/**
 * 检查用户是否拥有指定的模块权限
 * @param {string} modulePermission - 模块权限代码，如 'user:manage'
 * @returns {boolean} 是否拥有权限
 */
export const hasAdminPermission = (modulePermission) => {
  const userInfo = getAdminUserInfo()
  
  if (!userInfo) {
    console.warn('[adminAuth] 权限检查失败: 没有用户信息')
    return false
  }
  
  if (!userInfo.permissions) {
    console.error('[adminAuth] 权限检查失败: 用户信息缺少permissions字段')
    return false
  }
  
  // SUPER_ADMIN和admin角色拥有所有权限
  if (userInfo.role === 'admin' || userInfo.role === 'SUPER_ADMIN') {
    console.log(`[adminAuth] 权限检查通过: ${modulePermission} (${userInfo.role}角色)`)
    return true
  }
  
  // 获取该模块权限所需的细粒度权限列表
  const requiredPermissions = PERMISSION_MAPPING[modulePermission]
  
  if (!requiredPermissions) {
    console.warn(`[adminAuth] 未找到权限映射: ${modulePermission}`)
    // 如果没有映射，尝试直接匹配
    const hasPermission = userInfo.permissions.includes(modulePermission)
    console.log(`[adminAuth] 直接匹配权限: ${modulePermission} = ${hasPermission}`)
    return hasPermission
  }
  
  // 检查用户是否拥有所需权限中的至少一个（OR逻辑）
  // 如果需要用户拥有所有权限，改为 every
  const hasPermission = requiredPermissions.some(perm => 
    userInfo.permissions.includes(perm)
  )
  
  console.log(`[adminAuth] 权限检查: ${modulePermission}`, {
    required: requiredPermissions,
    userPermissions: userInfo.permissions,
    result: hasPermission
  })
  
  return hasPermission
}

// 诊断工具
export const diagnoseAdminAuth = () => {
  console.group('=== 管理员认证诊断 ===')
  
  const token = getAdminToken()
  console.log('Token存在:', !!token)
  if (token) {
    console.log('Token长度:', token.length)
  }
  
  const rawUserInfo = localStorage.getItem(ADMIN_USER_KEY)
  console.log('原始用户信息存在:', !!rawUserInfo)
  if (rawUserInfo) {
    console.log('原始用户信息:', rawUserInfo)
  }
  
  const userInfo = getAdminUserInfo()
  console.log('解析后的用户信息:', userInfo)
  
  if (userInfo) {
    console.log('角色:', userInfo.role)
    console.log('权限列表:', userInfo.permissions)
    
    // 测试每个模块权限
    const modulePermissions = [
      'user:manage',
      'permission:manage',
      'video:manage',
      'data:analysis'
    ]
    
    console.group('模块权限测试:')
    modulePermissions.forEach(perm => {
      const result = hasAdminPermission(perm)
      const mapping = PERMISSION_MAPPING[perm]
      console.log(`${perm}:`, result, '(需要:', mapping, ')')
    })
    console.groupEnd()
  }
  
  console.groupEnd()
}

// 将诊断工具挂载到window对象（仅开发环境）
if (import.meta.env.DEV) {
  window.diagnoseAdminAuth = diagnoseAdminAuth
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