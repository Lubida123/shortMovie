package com.example.shortmovie.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user")
@Schema(description = "用户实体")
public class User {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "用户ID")
    private Long id;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "密码（加密）")
    private String password;
    
    @Schema(description = "手机号")
    private String phone;
    
    @Schema(description = "邮箱")
    private String email;
    
    @Schema(description = "昵称")
    private String nickname;
    
    @Schema(description = "头像URL")
    private String avatar;
    
    @Schema(description = "状态：1-正常，0-冻结")
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @TableLogic
    @Schema(description = "逻辑删除：0-未删除，1-已删除")
    private Integer isDeleted;
}
