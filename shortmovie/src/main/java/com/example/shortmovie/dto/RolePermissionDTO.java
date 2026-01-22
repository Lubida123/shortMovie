package com.example.shortmovie.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "角色权限分配请求")
public class RolePermissionDTO {
    @NotNull(message = "角色ID不能为空")
    @Schema(description = "角色ID")
    private Long roleId;

    @NotEmpty(message = "权限ID列表不能为空")
    @Schema(description = "权限ID列表")
    private List<Long> permissionIds;
}