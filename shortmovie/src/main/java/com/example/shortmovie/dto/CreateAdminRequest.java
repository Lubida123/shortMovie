package com.example.shortmovie.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 创建管理员请求DTO
 */
@Data
@Schema(description = "创建管理员请求")
public class CreateAdminRequest {
    
    @NotBlank(message = "管理员名不能为空")
    @Size(min = 2, max = 20, message = "管理员名长度在2-20个字符之间")
    @Schema(description = "管理员名", required = true)
    private String adminname;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度在6-20个字符之间")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{6,20}$", message = "密码必须包含字母和数字")
    @Schema(description = "密码", required = true)
    private String password;
    
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", required = true)
    private String phone;
    
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;
    
    @NotBlank(message = "账号状态不能为空")
    @Pattern(regexp = "^(active|disabled)$", message = "账号状态只能是active或disabled")
    @Schema(description = "账号状态：active-正常，disabled-禁用", required = true)
    private String status = "active";
    
    @Schema(description = "昵称")
    private String nickname;
}