package com.example.shortmovie.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 用户状态更新请求DTO
 */
@Data
@Schema(description = "用户状态更新请求")
public class UserStatusUpdateRequest {
    
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", required = true)
    private Long id;
    
    @NotBlank(message = "用户角色不能为空")
    @Pattern(regexp = "^(user|admin)$", message = "用户角色只能是user或admin")
    @Schema(description = "用户角色：user-普通用户，admin-管理员", required = true)
    private String role;
    
    @NotBlank(message = "账号状态不能为空")
    @Pattern(regexp = "^(active|disabled)$", message = "账号状态只能是active或disabled")
    @Schema(description = "新状态：active-正常，disabled-禁用", required = true)
    private String status;
    
    @Schema(description = "状态变更原因")
    private String reason;
}