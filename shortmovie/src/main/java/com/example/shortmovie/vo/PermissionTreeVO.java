package com.example.shortmovie.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "权限树节点")
public class PermissionTreeVO {
    @Schema(description = "权限ID")
    private Long id;

    @Schema(description = "节点标识")
    private String key;

    @Schema(description = "节点名称")
    private String label;

    @Schema(description = "权限编码")
    private String permissionCode;

    @Schema(description = "权限名称")
    private String permissionName;

    @Schema(description = "节点描述")
    private String description;

    @Schema(description = "是否为叶子节点")
    private Boolean isLeaf;

    @Schema(description = "子节点")
    private List<PermissionTreeVO> children;
}
