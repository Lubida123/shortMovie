package com.example.shortmovie.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "管理员登录响应")
public class AdminLoginVO {
    @Schema(description = "JWT令牌")
    private String token;

    @Schema(description = "管理员信息")
    private AdminProfileVO userInfo;
}
