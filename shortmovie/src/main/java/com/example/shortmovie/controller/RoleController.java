package com.example.shortmovie.controller;

import com.example.shortmovie.dto.RoleCreateDTO;
import com.example.shortmovie.dto.RoleUpdateDTO;
import com.example.shortmovie.dto.RolePermissionDTO;
import com.example.shortmovie.service.RoleService;
import com.example.shortmovie.utils.R;
import com.example.shortmovie.vo.RoleVO;
import com.example.shortmovie.vo.PermissionTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@Tag(name = "角色权限管理", description = "角色和权限管理相关接口")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "获取角色列表", description = "获取所有角色列表")
    @GetMapping("/roles")
    public R<List<RoleVO>> getRoleList() {
        List<RoleVO> roles = roleService.getRoleList();
        return R.ok(roles);
    }

    @Operation(summary = "创建角色", description = "创建新角色")
    @PostMapping("/roles")
    public R<Void> createRole(@Valid @RequestBody RoleCreateDTO dto) {
        roleService.createRole(dto);
        return R.ok();
    }

    @Operation(summary = "更新角色", description = "更新角色信息")
    @PutMapping("/roles")
    public R<Void> updateRole(@Valid @RequestBody RoleUpdateDTO dto) {
        roleService.updateRole(dto);
        return R.ok();
    }

    @Operation(summary = "删除角色", description = "逻辑删除指定角色")
    @DeleteMapping("/roles/{roleId}")
    public R<String> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return R.ok("角色已删除");
    }

    @Operation(summary = "获取权限树", description = "获取系统权限树形结构")
    @GetMapping("/permissions/tree")
    public R<List<PermissionTreeVO>> getPermissionTree() {
        List<PermissionTreeVO> tree = roleService.getPermissionTree();
        return R.ok(tree);
    }

    @Operation(summary = "获取角色权限", description = "获取指定角色的权限列表")
    @GetMapping("/roles/{roleId}/permissions")
    public R<List<String>> getRolePermissions(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long roleId) {
        List<String> permissions = roleService.getRolePermissions(roleId);
        return R.ok(permissions);
    }

    @Operation(summary = "保存角色权限", description = "为角色分配权限")
    @PostMapping("/roles/permissions")
    public R<Void> saveRolePermissions(@Valid @RequestBody RolePermissionDTO dto) {
        roleService.saveRolePermissions(dto);
        return R.ok();
    }
}

