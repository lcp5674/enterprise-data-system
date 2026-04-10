package com.enterprise.edams.permission.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.permission.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 权限Mapper
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Mapper
public interface SysPermissionRepository extends BaseMapper<SysPermission> {

    /**
     * 根据权限编码查询
     */
    @Select("SELECT * FROM sys_permission WHERE code = #{code} AND is_deleted = 0 LIMIT 1")
    SysPermission findByCode(@Param("code") String code);

    /**
     * 检查权限编码是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM sys_permission WHERE code = #{code} AND is_deleted = 0")
    boolean existsByCode(@Param("code") String code);

    /**
     * 根据模块查询权限列表
     */
    @Select("SELECT * FROM sys_permission WHERE module = #{module} AND is_deleted = 0 ORDER BY sort_order")
    List<SysPermission> findByModule(@Param("module") String module);

    /**
     * 根据权限类型查询权限列表
     */
    @Select("SELECT * FROM sys_permission WHERE permission_type = #{permissionType} AND is_deleted = 0 ORDER BY sort_order")
    List<SysPermission> findByPermissionType(@Param("permissionType") String permissionType);
}
