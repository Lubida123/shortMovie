// src/api/admin/userApi.js
import adminHttp from './http'

// 管理员登录
export const adminLogin = (params) => adminHttp.post('/login', params)

// ====================== 用户管理接口 ======================
// 获取用户列表
export const getUserList = (params) => adminHttp.get('/user/list', { params })

// 新增用户
export const addUser = (params) => adminHttp.post('/user/add', params)

// 编辑用户
export const editUser = (params) => adminHttp.put('/user/edit', params)

// 删除用户
export const deleteUser = (id) => adminHttp.delete(`/user/delete/${id}`)

// 变更用户状态
export const changeUserStatus = (params) => adminHttp.put('/user/changeStatus', params)

// ====================== 视频管理接口 ======================
// 获取视频列表
export const getVideoList = (params) => adminHttp.get('/video/list', { params })

// 获取视频统计数据
export const getVideoStats = () => adminHttp.get('/video/stats')

// 审核视频（通过）
export const approveVideo = (id, remark = '') => adminHttp.post(`/video/approve/${id}`, { remark })

// 审核视频（拒绝）
export const rejectVideo = (id, reason = '') => adminHttp.post(`/video/reject/${id}`, { reason })

// 下架视频
export const takeDownVideo = (id, reason = '') => adminHttp.post(`/video/takeDown/${id}`, { reason })

// 批量操作视频
export const batchApproveVideos = (ids, remark = '') => adminHttp.post('/video/batch/approve', { ids, remark })
export const batchRejectVideos = (ids, reason = '') => adminHttp.post('/video/batch/reject', { ids, reason })
export const batchDeleteVideos = (ids) => adminHttp.post('/video/batch/delete', { ids })

// ====================== 权限管理接口 ======================
// 获取角色列表
export const getRoleList = () => adminHttp.get('/role/list')

// 新增角色
export const addRole = (params) => adminHttp.post('/role/add', params)

// 编辑角色
export const editRole = (params) => adminHttp.put('/role/edit', params)

// 删除角色
export const deleteRole = (roleCode) => adminHttp.delete(`/role/delete/${roleCode}`)

// 获取权限树形结构
export const getPermissionTree = () => adminHttp.get('/permission/tree')

// 获取角色权限
export const getRolePermission = (roleCode) => adminHttp.get(`/role/permission/${roleCode}`)

// 保存角色权限
export const saveRolePermission = (params) => adminHttp.post('/role/savePermission', params)