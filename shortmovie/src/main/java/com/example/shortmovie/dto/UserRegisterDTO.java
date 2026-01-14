package com.example.shortmovie.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户注册请求DTO
 */
@Data
@Schema(description = "用户注册请求")
public class UserRegisterDTO {
    
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;
    
    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Schema(description = "邮箱验证码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码不能为空")
    private String emailCode;
}
