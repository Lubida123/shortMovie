package com.example.shortmovie.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_permission")
@Schema(description = "权限实体")
public class Permission {
    @TableId(type = IdType.AUTO)
    @Schema(description = "权限ID")
    private Long id;

    @Schema(description = "权限编码")
    private String permissionCode;

    @Schema(description = "权限名称")
    private String permissionName;

    @Schema(description = "资源标识")
    private String resource;

    @Schema(description = "操作类型")
    private String action;

    @Schema(description = "所属模块")
    private String module;

    @Schema(description = "状态：1-启用，0-禁用")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
