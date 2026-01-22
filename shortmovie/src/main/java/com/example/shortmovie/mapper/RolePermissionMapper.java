package com.example.shortmovie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.shortmovie.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;

@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
    
    /**
     * 删除角色的所有权限
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(Long roleId);
}
