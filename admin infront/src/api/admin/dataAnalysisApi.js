import adminHttp from "./http";

// ==================== 辅助函数 ====================

/**
 * 格式化日期为后端需要的格式 (YYYY-MM-DD)
 * @param {Date|string|null} date - 日期对象或字符串
 * @returns {string|null} 格式化后的日期字符串
 */
export const formatDateForBackend = (date) => {
	if (!date) return null;
	if (typeof date === "string") return date;
	if (date instanceof Date) {
		return date.toISOString().split("T")[0];
	}
	return null;
};

/**
 * 构建时间范围参数
 * @param {Object} options - 选项对象
 * @param {string} options.timeRange - 时间范围 (today/week/month/custom)
 * @param {Date|string} [options.startDate] - 开始日期
 * @param {Date|string} [options.endDate] - 结束日期
 * @returns {Object} 格式化后的参数对象
 */
export const buildTimeRangeParams = ({ timeRange, startDate, endDate }) => {
	const params = { timeRange };

	if (timeRange === "custom" && startDate && endDate) {
		params.startDate = formatDateForBackend(startDate);
		params.endDate = formatDateForBackend(endDate);
	}

	return params;
};

/**
 * 解析后端返回的日期字符串
 * @param {string} dateStr - 日期字符串
 * @returns {Date|null} Date 对象
 */
export const parseDateFromBackend = (dateStr) => {
	if (!dateStr) return null;
	return new Date(dateStr);
};

// ==================== 数据转换函数 ====================

/**
 * 转换热门视频数据
 * @param {Object} backendData - 后端返回的数据
 * @returns {Array} 转换后的视频数组
 */
export const transformHotVideos = (backendData) => {
	return backendData?.videos || [];
};

/**
 * 转换分类分布数据
 * @param {Object} backendData - 后端返回的数据
 * @returns {Array} 转换后的分类数组
 */
export const transformCategoryData = (backendData) => {
	return backendData?.categories || [];
};

// ==================== API 函数 ====================

/**
 * 获取核心指标数据
 * @param {Object} params - 查询参数
 * @param {string} params.timeRange - 时间范围 (today/week/month/custom)
 * @param {string} [params.startDate] - 开始日期 (YYYY-MM-DD)
 * @param {string} [params.endDate] - 结束日期 (YYYY-MM-DD)
 * @returns {Promise<Object>} 核心指标数据
 */
export const getCoreMetrics = (params) => {
	return adminHttp.get("/analysis/core-metrics", { params });
};

/**
 * 获取播放量趋势
 * @param {Object} params - 查询参数
 * @param {string} params.timeRange - 时间范围
 * @param {string} params.chartType - 图表类型 (daily/weekly/monthly)
 * @param {string} [params.startDate] - 开始日期
 * @param {string} [params.endDate] - 结束日期
 * @returns {Promise<Object>} 播放量趋势数据
 */
export const getViewTrend = (params) => {
	return adminHttp.get("/analysis/view-trend", { params });
};

/**
 * 获取用户互动分析
 * @param {Object} params - 查询参数
 * @param {string} params.timeRange - 时间范围
 * @param {string} params.type - 类型 (distribution/comparison)
 * @param {string} [params.startDate] - 开始日期
 * @param {string} [params.endDate] - 结束日期
 * @returns {Promise<Object>} 用户互动分析数据
 */
export const getInteraction = (params) => {
	return adminHttp.get("/analysis/interaction", { params });
};

/**
 * 获取用户增长趋势
 * @param {Object} params - 查询参数
 * @param {string} params.timeRange - 时间范围
 * @param {string} params.type - 类型 (new/active/total)
 * @param {string} [params.startDate] - 开始日期
 * @param {string} [params.endDate] - 结束日期
 * @returns {Promise<Object>} 用户增长趋势数据
 */
export const getUserGrowth = (params) => {
	return adminHttp.get("/analysis/user-growth", { params });
};

/**
 * 获取视频分类分布
 * @param {Object} params - 查询参数
 * @param {string} params.timeRange - 时间范围
 * @param {string} params.sortBy - 排序方式 (count/views/interaction)
 * @param {string} [params.startDate] - 开始日期
 * @param {string} [params.endDate] - 结束日期
 * @returns {Promise<Object>} 视频分类分布数据
 */
export const getCategoryDistribution = (params) => {
	return adminHttp.get("/analysis/category-distribution", { params });
};

/**
 * 获取用户活跃时段
 * @param {Object} params - 查询参数
 * @param {string} params.timeRange - 时间范围
 * @param {string} params.period - 时段类型 (weekday/weekend/all)
 * @param {string} [params.startDate] - 开始日期
 * @param {string} [params.endDate] - 结束日期
 * @returns {Promise<Object>} 用户活跃时段数据
 */
export const getActivePeriod = (params) => {
	return adminHttp.get("/analysis/active-period", { params });
};

/**
 * 获取热门视频排行
 * @param {Object} params - 查询参数
 * @param {string} params.timeRange - 时间范围
 * @param {string} params.sortBy - 排序方式 (views/likes/comments/interactionRate)
 * @param {number} [params.limit=10] - 返回数量
 * @param {string} [params.startDate] - 开始日期
 * @param {string} [params.endDate] - 结束日期
 * @returns {Promise<Object>} 热门视频排行数据
 */
export const getHotVideos = (params) => {
	return adminHttp.get("/analysis/hot-videos", { params });
};

/**
 * 导出数据
 * @param {Object} params - 查询参数
 * @param {string} params.timeRange - 时间范围
 * @param {string} [params.dataType='all'] - 数据类型 (core-metrics/hot-videos/user-growth/category/all)
 * @param {string} [params.format='json'] - 导出格式
 * @param {string} [params.startDate] - 开始日期
 * @param {string} [params.endDate] - 结束日期
 * @returns {Promise<Object>} 导出数据
 */
export const exportData = (params) => {
	return adminHttp.get("/analysis/export", { params });
};
