package com.enterprise.edams.permission.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.permission.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    @Select("SELECT * FROM sys_permission WHERE code = #{code} AND deleted = 0")
    Permission findByCode(@Param("code") String code);

    @Select("SELECT p.* FROM sys_permission p " +
             "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
             "WHERE rp.role_id = #{roleId} AND p.deleted = 0 AND p.status = 1")
    List<Permission> findByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT * FROM sys_permission WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort_order ASC")
    List<Permission> findByParentId(@Param("parentId") Long parentId);
}
