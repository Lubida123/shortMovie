package com.example.shortmovie.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 用户信息更新请求DTO
 */
@Data
@Schema(description = "用户信息更新请求")
public class UserUpdateDTO {
    
    @Schema(description = "手机号")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;
    
    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Schema(description = "昵称")
    private String nickname;
    
    @Schema(description = "头像URL")
    private String avatar;
}
