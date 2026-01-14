package com.example.shortmovie.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 邮箱验证码实体类
 */
@Data
@TableName("email_verification")
@Schema(description = "邮箱验证码实体")
public class EmailVerification {
    
    @TableId(type = IdType.AUTO)
    @Schema(description = "记录ID")
    private Long id;
    
    @Schema(description = "邮箱")
    private String email;
    
    @Schema(description = "验证码")
    private String code;
    
    @Schema(description = "过期时间")
    private LocalDateTime expireTime;
    
    @Schema(description = "是否已使用：0-未使用，1-已使用")
    private Integer isUsed;
    
    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
