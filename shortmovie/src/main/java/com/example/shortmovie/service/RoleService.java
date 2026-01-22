package com.example.shortmovie.service;

import com.example.shortmovie.dto.RoleCreateDTO;
import com.example.shortmovie.dto.RolePermissionDTO;
import com.example.shortmovie.dto.RoleUpdateDTO;
import com.example.shortmovie.vo.PermissionTreeVO;
import com.example.shortmovie.vo.RoleVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RoleService {
    List<RoleVO> getRoleList();

    @Transactional(rollbackFor = Exception.class)
    void createRole(RoleCreateDTO dto);

    @Transactional(rollbackFor = Exception.class)
    void updateRole(RoleUpdateDTO dto);

    @Transactional(rollbackFor = Exception.class)
    void deleteRole(Long roleId);

    List<PermissionTreeVO> getPermissionTree();

    List<String> getRolePermissions(Long roleId);

    @Transactional(rollbackFor = Exception.class)
    void saveRolePermissions(RolePermissionDTO dto);

    int getRoleUserCount(Long roleId);

    List<RoleVO> getUserRoles(Long userId);
}
