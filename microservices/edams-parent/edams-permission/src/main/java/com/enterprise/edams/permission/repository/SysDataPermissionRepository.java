package com.enterprise.edams.permission.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.permission.entity.SysDataPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 数据权限Mapper
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Mapper
public interface SysDataPermissionRepository extends BaseMapper<SysDataPermission> {

    /**
     * 根据角色ID查询数据权限配置
     */
    @Select("SELECT * FROM sys_data_permission WHERE role_id = #{roleId} LIMIT 1")
    SysDataPermission findByRoleId(@Param("roleId") String roleId);

    /**
     * 根据用户ID查询数据权限配置列表
     */
    @Select("SELECT * FROM sys_data_permission WHERE user_id = #{userId}")
    List<SysDataPermission> findByUserId(@Param("userId") String userId);
}
