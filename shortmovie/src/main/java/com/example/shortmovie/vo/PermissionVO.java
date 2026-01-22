package com.example.shortmovie.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "权限信息")
public class PermissionVO {
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

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}