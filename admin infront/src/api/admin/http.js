import axios from "axios";
import { ElMessage } from "element-plus";
import { getAdminToken, clearAdminStorage } from "@/utils/adminAuth";

// 创建管理员请求实例
const adminHttp = axios.create({
	baseURL: "http://localhost:8080/api/admin",
	timeout: 10000,
	headers: {
		"Content-Type": "application/json",
	},
});

// 模拟数据存储（用于模拟后端数据持久化）
let mockUsers = [
	{
		id: 1,
		username: "测试用户1",
		phone: "13800138000",
		role: "user",
		status: "active",
		createTime: "2026-01-01 10:00:00",
	},
	{
		id: 2,
		username: "管理员",
		phone: "13800138001",
		role: "admin",
		status: "active",
		createTime: "2026-01-02 10:00:00",
	},
];

let mockRoles = [
	{ roleCode: "admin", roleName: "管理员", description: "拥有所有权限" },
	{ roleCode: "user", roleName: "普通用户", description: "基础查看权限" },
];

let mockPermissions = [
	{
		permissionCode: "user:manage",
		permissionName: "用户管理",
		children: [{ permissionCode: "user:add", permissionName: "新增用户" }],
	},
	{
		permissionCode: "video:manage",
		permissionName: "视频管理",
		children: [
			{ permissionCode: "video:approve", permissionName: "审核视频" },
			{ permissionCode: "video:delete", permissionName: "删除视频" },
		],
	},
	{
		permissionCode: "permission:manage",
		permissionName: "权限管理",
		children: [{ permissionCode: "role:add", permissionName: "新增角色" }],
	},
	{
		permissionCode: "data:analysis",
		permissionName: "数据分析",
		children: [
			{ permissionCode: "data:view", permissionName: "查看数据" },
			{ permissionCode: "data:export", permissionName: "导出数据" },
		],
	},
];

// 请求拦截器：添加Token + 模拟数据
adminHttp.interceptors.request.use(
	(config) => {
		const token = getAdminToken();
		if (token) {
			config.headers["Authorization"] = `Bearer ${token}`;
		}

		// 特殊处理：登录和注册接口使用 form-urlencoded 格式
		const formUrlencodedEndpoints = ["/login", "/register"];
		const isFormUrlencoded = formUrlencodedEndpoints.some((endpoint) =>
			config.url.includes(endpoint),
		);

		if (
			isFormUrlencoded &&
			config.method === "post" &&
			config.data &&
			typeof config.data === "object"
		) {
			// 转换为 URLSearchParams 格式
			const params = new URLSearchParams();
			Object.keys(config.data).forEach((key) => {
				if (config.data[key] !== null && config.data[key] !== undefined) {
					params.append(key, config.data[key]);
				}
			});
			config.data = params;
			config.headers["Content-Type"] = "application/x-www-form-urlencoded";
		}
		// 其他接口保持 JSON 格式（用户管理接口等）

		return config;
	},
	(error) => Promise.reject(error),
);

// 响应拦截器：处理模拟数据 + 错误
adminHttp.interceptors.response.use(
	(response) => {
		// 后端返回格式: { code, message, data, timestamp }
		const { code, data, message } = response.data;

		if (code === 200) {
			// 直接返回 data，前端可以直接使用 res.data
			return data;
		} else {
			ElMessage.error(message || "请求失败");
			return Promise.reject(new Error(message || "请求失败"));
		}
	},
	(error) => {
		if (error.response?.status === 401) {
			ElMessage.error("登录过期，请重新登录");
			clearAdminStorage();
			window.location.href = "/admin/login";
		} else if (error.response?.status === 403) {
			ElMessage.error("没有权限访问该资源");
		} else {
			ElMessage.error(error.response?.data?.message || error.message || "网络错误");
		}
		return Promise.reject(error);
	},
);

export default adminHttp;
