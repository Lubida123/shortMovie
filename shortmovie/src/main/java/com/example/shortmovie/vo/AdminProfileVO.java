package com.example.shortmovie.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理员信息")
public class AdminProfileVO {

    @Schema(description = "管理员ID")
    private Long id;

    @Schema(description = "管理员名")
    private String adminname;

    @Schema(description = "密码（加密）")
    private String password;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "状态：2-总管理员，1-正常，0-冻结")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "逻辑删除：0-未删除，1-已删除")
    private Integer isDeleted;
}
