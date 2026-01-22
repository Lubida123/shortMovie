/**
 * 获取管理员显示名称
 * 优先级：adminname > nickname > 默认值
 */
export const getDisplayName = (userInfo) => {
	// 容错处理
	if (!userInfo) {
		console.warn("管理员信息为空，使用默认显示名称");
		return "管理员";
	}

	// 优先使用 adminname
	if (userInfo.adminname && userInfo.adminname.trim()) {
		return formatDisplayName(userInfo.adminname);
	}

	// 备用 nickname
	if (userInfo.nickname && userInfo.nickname.trim()) {
		return formatDisplayName(userInfo.nickname);
	}

	// 默认值
	console.warn("管理员姓名字段为空，使用默认显示名称");
	return "管理员";
};

/**
 * 格式化显示名称
 */
export const formatDisplayName = (name) => {
	if (!name) return "管理员";

	const trimmedName = name.trim();
	const maxLength = 20;

	if (trimmedName.length > maxLength) {
		return trimmedName.substring(0, maxLength) + "...";
	}

	return trimmedName;
};
