import adminHttp from "./http";

// ====================== 管理员登录 ======================
export const adminLogin = (params) => adminHttp.post("/login", params);

// ====================== 用户管理接口（区分管理员和普通用户）======================
// 获取管理员列表（独立接口）
export const getAdminList = (params) =>
	adminHttp.get("/admin/list", { params });

// 获取普通用户列表（独立接口）
export const getUserList = (params) => adminHttp.get("/user/list", { params });

// 为了兼容现有代码，创建一个辅助函数来获取合并的用户列表
export const getMergedUserList = async (params) => {
	// 这里不实际调用，仅作为接口定义，实际合并逻辑在前端组件中
	throw new Error("请在前端组件中实现合并逻辑");
};

// ====================== 用户操作接口（需要区分管理员和用户）======================
// 新增用户（需要指定类型）
export const addUser = (params) => {
	// 根据角色决定调用哪个接口
	if (params.role === "admin") {
		return adminHttp.post("/admin/add", params);
	} else {
		return adminHttp.post("/user/add", params);
	}
};

// 编辑用户（需要区分类型）
export const editUser = (params) => {
	// 根据角色决定调用哪个接口
	if (params.role === "admin") {
		return adminHttp.put("/admin/edit", params);
	} else {
		return adminHttp.put("/user/edit", params);
	}
};

// 删除用户（需要区分类型）
export const deleteUser = (id, role) => {
	if (role === "admin") {
		return adminHttp.delete(`/admin/delete/${id}`);
	} else {
		return adminHttp.delete(`/user/delete/${id}`);
	}
};

// 变更用户状态（需要区分类型）
export const changeUserStatus = (params) => {
	const { id, role, status } = params;
	if (role === "admin") {
		return adminHttp.put("/admin/changeStatus", { id, status });
	} else {
		return adminHttp.put("/user/changeStatus", { id, status });
	}
};

// ====================== 视频管理接口 ======================
// 获取视频列表
export const getVideoList = (params) =>
	adminHttp.get("/video/list", { params });

// 获取视频统计数据
export const getVideoStats = () => adminHttp.get("/video/stats");

// 审核视频（通过）
export const approveVideo = (id, remark = "") =>
	adminHttp.post(`/video/approve/${id}`, { remark });

// 审核视频（拒绝）
export const rejectVideo = (id, reason = "") =>
	adminHttp.post(`/video/reject/${id}`, { reason });

// 下架视频
export const takeDownVideo = (id, reason = "") =>
	adminHttp.post(`/video/takeDown/${id}`, { reason });

// 批量操作视频
export const batchApproveVideos = (ids, remark = "") =>
	adminHttp.post("/video/batch/approve", { ids, remark });
export const batchRejectVideos = (ids, reason = "") =>
	adminHttp.post("/video/batch/reject", { ids, reason });
export const batchDeleteVideos = (ids) =>
	adminHttp.post("/video/batch/delete", { ids });
