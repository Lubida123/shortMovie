package com.example.shortmovie.utils;

import com.example.shortmovie.entity.Admin;
import com.example.shortmovie.entity.User;
import com.example.shortmovie.vo.UnifiedUserVO;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户数据转换工具类
 */
public class UserConverter {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 转换User列表为统一VO
     */
    public static List<UnifiedUserVO> convertUsersToVO(List<User> users) {
        return users.stream().map(user -> {
            UnifiedUserVO vo = new UnifiedUserVO();
            vo.setId(user.getId());
            vo.setUsername(user.getUsername());
            vo.setPhone(user.getPhone());
            vo.setEmail(user.getEmail());
            vo.setRole("user");
            vo.setStatus(user.getStatus() == 1 ? "active" : "disabled");
            vo.setCreateTime(user.getCreateTime() != null ? user.getCreateTime().format(FORMATTER) : "");
            vo.setNickname(user.getNickname());
            return vo;
        }).collect(Collectors.toList());
    }
    
    /**
     * 转换Admin列表为统一VO
     */
    public static List<UnifiedUserVO> convertAdminsToVO(List<Admin> admins) {
        return admins.stream().map(admin -> {
            UnifiedUserVO vo = new UnifiedUserVO();
            vo.setId(admin.getId());
            vo.setUsername(admin.getAdminname());
            vo.setPhone(admin.getPhone());
            vo.setEmail(admin.getEmail());
            vo.setRole("admin");
            vo.setStatus((admin.getStatus() == 1 || admin.getStatus() == 2) ? "active" : "disabled");
            vo.setCreateTime(admin.getCreateTime() != null ? admin.getCreateTime().format(FORMATTER) : "");
            vo.setNickname(admin.getNickname());
            return vo;
        }).collect(Collectors.toList());
    }
    
    /**
     * 转换单个User为统一VO
     */
    public static UnifiedUserVO convertUserToVO(User user) {
        UnifiedUserVO vo = new UnifiedUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setRole("user");
        vo.setStatus(user.getStatus() == 1 ? "active" : "disabled");
        vo.setCreateTime(user.getCreateTime() != null ? user.getCreateTime().format(FORMATTER) : "");
        vo.setNickname(user.getNickname());
        return vo;
    }
    
    /**
     * 转换单个Admin为统一VO
     */
    public static UnifiedUserVO convertAdminToVO(Admin admin) {
        UnifiedUserVO vo = new UnifiedUserVO();
        vo.setId(admin.getId());
        vo.setUsername(admin.getAdminname());
        vo.setPhone(admin.getPhone());
        vo.setEmail(admin.getEmail());
        vo.setRole("admin");
        vo.setStatus((admin.getStatus() == 1 || admin.getStatus() == 2) ? "active" : "disabled");
        vo.setCreateTime(admin.getCreateTime() != null ? admin.getCreateTime().format(FORMATTER) : "");
        vo.setNickname(admin.getNickname());
        return vo;
    }
}