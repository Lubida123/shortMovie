package com.example.shortmovie.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user_role")
@Schema(description = "用户角色关联实体")
public class UserRole {
    @TableId(type = IdType.AUTO)
    @Schema(description = "关联ID")
    private Long id;

    @Schema(description = "用户ID（管理员ID）")
    private Long userId;

    @Schema(description = "角色ID")
    private Long roleId;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "分配时间")
    private LocalDateTime assignTime;

    @Schema(description = "分配人ID")
    private Long assignedBy;
}
