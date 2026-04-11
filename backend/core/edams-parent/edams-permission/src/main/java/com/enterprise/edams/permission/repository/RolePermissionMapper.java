package com.enterprise.edams.permission.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.permission.entity.RolePermission;
import org.apache.ibatis.annotations.*;

/**
 * 角色-权限关联Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    /** 删除角色的所有权限 */
    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

    /** 查询角色拥有的所有权限ID列表 */
    @Select("SELECT permission_id FROM sys_role_permission WHERE role_id = #{roleId}")
    List<Long> findPermissionIdsByRoleId(@Param("roleId") Long roleId);
}
