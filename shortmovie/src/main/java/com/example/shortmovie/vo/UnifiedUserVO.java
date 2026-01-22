package com.example.shortmovie.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 统一用户视图对象
 */
@Data
@Schema(description = "统一用户视图对象")
public class UnifiedUserVO {
    
    @Schema(description = "用户ID")
    private Long id;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "手机号")
    private String phone;
    
    @Schema(description = "邮箱")
    private String email;
    
    @Schema(description = "用户角色：user-普通用户，admin-管理员")
    private String role;
    
    @Schema(description = "账号状态：active-正常，disabled-禁用")
    private String status;
    
    @Schema(description = "创建时间")
    private String createTime;
    
    @Schema(description = "昵称")
    private String nickname;
}