package com.example.shortmovie.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员登录请求DTO
 */
@Data
@Schema(description = "管理员登录请求")
public class AdminLoginDTO {
    @Schema(description = "登录账号（用户名/邮箱/手机号）", required = true)
    @NotBlank(message = "登录账号不能为空")
    private String account;

    @Schema(description = "密码", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;
}
