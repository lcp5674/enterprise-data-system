package com.enterprise.edams.permission.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.permission.entity.SysRolePermission;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色-权限关联Mapper
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Mapper
public interface SysRolePermissionRepository extends BaseMapper<SysRolePermission> {

    /**
     * 根据角色ID查询权限ID列表
     */
    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id = #{roleId}")
    List<String> findPermissionIdsByRoleId(@Param("roleId") String roleId);

    /**
     * 根据角色ID删除所有权限关联
     */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") String roleId);

    /**
     * 根据权限ID删除所有角色关联
     */
    @Delete("DELETE FROM sys_role_permission WHERE permission_id = #{permissionId}")
    int deleteByPermissionId(@Param("permissionId") String permissionId);

    /**
     * 检查角色-权限关联是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM sys_role_permission WHERE role_id = #{roleId} AND permission_id = #{permissionId}")
    boolean existsByRoleIdAndPermissionId(@Param("roleId") String roleId, @Param("permissionId") String permissionId);
}
