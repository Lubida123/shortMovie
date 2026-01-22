package com.example.shortmovie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.shortmovie.dto.RoleCreateDTO;
import com.example.shortmovie.dto.RolePermissionDTO;
import com.example.shortmovie.dto.RoleUpdateDTO;
import com.example.shortmovie.entity.Role;
import com.example.shortmovie.entity.Permission;
import com.example.shortmovie.entity.RolePermission;
import com.example.shortmovie.exception.ValidationException;
import com.example.shortmovie.mapper.RoleMapper;
import com.example.shortmovie.mapper.PermissionMapper;
import com.example.shortmovie.mapper.RolePermissionMapper;
import com.example.shortmovie.mapper.UserRoleMapper;
import com.example.shortmovie.service.RoleService;
import com.example.shortmovie.vo.RoleVO;
import com.example.shortmovie.vo.PermissionVO;
import com.example.shortmovie.vo.PermissionTreeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public List<RoleVO> getRoleList() {
        List<Role> roles = roleMapper.selectList(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getStatus, 1)
                        .orderByAsc(Role::getCreateTime)
        );

        return roles.stream().map(this::convertToRoleVO).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void createRole(RoleCreateDTO dto) {
        // 检查角色编码是否已存在
        LambdaQueryWrapper<Role> query = new LambdaQueryWrapper<>();
        query.eq(Role::getRoleCode, dto.getRoleCode());
        if (roleMapper.selectCount(query) > 0) {
            throw new ValidationException("角色编码已存在");
        }

        Role role = new Role();
        BeanUtils.copyProperties(dto, role);
        role.setStatus(1);

        roleMapper.insert(role);
        log.info("创建角色成功: {}", dto.getRoleCode());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateRole(RoleUpdateDTO dto) {
        Role role = roleMapper.selectById(dto.getId());
        if (role == null) {
            throw new ValidationException("角色不存在");
        }

        role.setRoleName(dto.getRoleName());
        role.setDescription(dto.getDescription());

        roleMapper.updateById(role);
        log.info("更新角色成功: {}", role.getRoleCode());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteRole(Long roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new ValidationException("角色不存在");
        }

        // 检查是否为系统角色
        if ("SUPER_ADMIN".equals(role.getRoleCode())) {
            throw new ValidationException("系统角色不能删除");
        }

        // 检查是否有用户正在使用该角色
        int userCount = userRoleMapper.countUsersByRoleId(roleId);
        if (userCount > 0) {
            throw new ValidationException("该角色正在被 " + userCount + " 个用户使用，无法删除");
        }

        // 删除角色权限关联
        rolePermissionMapper.deleteByRoleId(roleId);

        // 删除角色
        roleMapper.deleteById(roleId);
        log.info("删除角色成功: {}", role.getRoleCode());
    }

    @Override
    public List<PermissionTreeVO> getPermissionTree() {
        List<Permission> permissions = permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>()
                        .eq(Permission::getStatus, 1)
                        .orderByAsc(Permission::getModule, Permission::getResource)
        );

        return buildPermissionTree(permissions);
    }

    @Override
    public List<String> getRolePermissions(Long roleId) {
        List<Permission> permissions = permissionMapper.selectPermissionsByRoleId(roleId);
        return permissions.stream()
                .map(Permission::getPermissionCode)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveRolePermissions(RolePermissionDTO dto) {
        // 删除原有权限
        rolePermissionMapper.deleteByRoleId(dto.getRoleId());

        // 添加新权限
        if (!dto.getPermissionIds().isEmpty()) {
            List<RolePermission> rolePermissions = dto.getPermissionIds().stream()
                    .map(permissionId -> {
                        RolePermission rp = new RolePermission();
                        rp.setRoleId(dto.getRoleId());
                        rp.setPermissionId(permissionId);
                        return rp;
                    })
                    .collect(Collectors.toList());

            rolePermissions.forEach(rolePermissionMapper::insert);
        }

        log.info("保存角色权限成功: roleId={}, permissionCount={}",
                dto.getRoleId(), dto.getPermissionIds().size());
    }

    /**
     * 构建权限树
     */
    private List<PermissionTreeVO> buildPermissionTree(List<Permission> permissions) {
        Map<String, PermissionTreeVO> moduleMap = new LinkedHashMap<>();

        for (Permission permission : permissions) {
            String module = permission.getModule();

            // 创建模块节点
            if (!moduleMap.containsKey(module)) {
                PermissionTreeVO moduleNode = new PermissionTreeVO();
                moduleNode.setId(null); // 模块节点没有ID
                moduleNode.setKey(module);
                moduleNode.setLabel(getModuleName(module));
                moduleNode.setPermissionCode(module);
                moduleNode.setPermissionName(getModuleName(module));
                moduleNode.setDescription("模块: " + getModuleName(module));
                moduleNode.setIsLeaf(false);
                moduleNode.setChildren(new ArrayList<>());
                moduleMap.put(module, moduleNode);
            }

            // 创建权限节点
            PermissionTreeVO permissionNode = new PermissionTreeVO();
            permissionNode.setId(permission.getId()); // 设置权限ID
            permissionNode.setKey(permission.getPermissionCode());
            permissionNode.setLabel(permission.getPermissionName());
            permissionNode.setPermissionCode(permission.getPermissionCode());
            permissionNode.setPermissionName(permission.getPermissionName());
            permissionNode.setDescription(permission.getResource() + "的" + getActionName(permission.getAction()) + "权限");
            permissionNode.setIsLeaf(true);
            permissionNode.setChildren(null);

            moduleMap.get(module).getChildren().add(permissionNode);
        }

        return new ArrayList<>(moduleMap.values());
    }

    /**
     * 获取模块名称
     */
    private String getModuleName(String module) {
        Map<String, String> moduleNames = Map.of(
                "USER_MANAGEMENT", "用户管理",
                "VIDEO_MANAGEMENT", "视频管理",
                "PERMISSION_MANAGEMENT", "权限管理",
                "DATA_ANALYSIS", "数据分析"
        );
        return moduleNames.getOrDefault(module, module);
    }

    /**
     * 获取操作名称
     */
    private String getActionName(String action) {
        Map<String, String> actionNames = Map.of(
                "READ", "查看",
                "ADD", "新增",
                "EDIT", "编辑",
                "DELETE", "删除",
                "APPROVE", "审核",
                "ASSIGN", "分配",
                "STATUS", "状态变更",
                "BATCH", "批量操作"
        );
        return actionNames.getOrDefault(action, action);
    }

    /**
     * 转换为RoleVO
     */
    private RoleVO convertToRoleVO(Role role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);

        // 获取角色权限
        List<Permission> permissions = permissionMapper.selectPermissionsByRoleId(role.getId());
        List<PermissionVO> permissionVOs = permissions.stream()
                .map(this::convertToPermissionVO)
                .collect(Collectors.toList());
        vo.setPermissions(permissionVOs);

        // 获取用户数量
        vo.setUserCount(userRoleMapper.countUsersByRoleId(role.getId()));

        return vo;
    }

    /**
     * 转换为PermissionVO
     */
    private PermissionVO convertToPermissionVO(Permission permission) {
        PermissionVO vo = new PermissionVO();
        BeanUtils.copyProperties(permission, vo);
        return vo;
    }

    @Override
    public int getRoleUserCount(Long roleId) {
        return userRoleMapper.countUsersByRoleId(roleId);
    }

    @Override
    public List<RoleVO> getUserRoles(Long userId) {
        List<Role> roles = userRoleMapper.selectRolesByUserId(userId);
        return roles.stream().map(this::convertToRoleVO).collect(Collectors.toList());
    }
}