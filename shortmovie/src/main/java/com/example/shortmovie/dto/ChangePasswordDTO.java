package com.example.shortmovie.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改密码 DTO
 */
@Data
@Schema(description = "修改密码请求")
public class ChangePasswordDTO {
    
    @Schema(description = "旧密码")
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;
    
    @Schema(description = "新密码")
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
