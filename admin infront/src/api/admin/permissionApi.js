import adminHttp from "./http";

// ====================== 角色权限管理接口 ======================
// 获取角色列表
export const getRoleList = () => adminHttp.get("/roles");

// 创建角色
export const addRole = (data) => adminHttp.post("/roles", data);

// 更新角色
export const editRole = (data) => adminHttp.put("/roles", data);

// 删除角色
export const deleteRole = (roleId) =>
	adminHttp.delete(`/roles/${roleId}`);

// 获取权限树
export const getPermissionTree = () => adminHttp.get("/permissions/tree");

// 获取角色权限
export const getRolePermission = (roleId) =>
	adminHttp.get(`/roles/${roleId}/permissions`);

// 保存角色权限
export const saveRolePermission = (data) =>
	adminHttp.post("/roles/permissions", data);
