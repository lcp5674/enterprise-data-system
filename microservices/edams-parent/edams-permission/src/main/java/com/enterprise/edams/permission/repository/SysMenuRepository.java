package com.enterprise.edams.permission.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.permission.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单Mapper
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Mapper
public interface SysMenuRepository extends BaseMapper<SysMenu> {

    /**
     * 根据父ID查询子菜单
     */
    @Select("SELECT * FROM sys_menu WHERE parent_id = #{parentId} AND is_deleted = 0 ORDER BY sort_order")
    List<SysMenu> findByParentId(@Param("parentId") String parentId);

    /**
     * 根据应用查询菜单列表
     */
    @Select("SELECT * FROM sys_menu WHERE application = #{application} AND is_deleted = 0 ORDER BY sort_order")
    List<SysMenu> findByApplication(@Param("application") String application);

    /**
     * 根据路由路径查询菜单
     */
    @Select("SELECT * FROM sys_menu WHERE path = #{path} AND is_deleted = 0 LIMIT 1")
    SysMenu findByPath(@Param("path") String path);

    /**
     * 根据用户ID查询菜单列表
     */
    @Select("""
        SELECT DISTINCT m.* FROM sys_menu m
        INNER JOIN sys_role_permission rmp ON m.id = rmp.permission_id
        INNER JOIN sys_user_role ur ON rmp.role_id = ur.role_id
        WHERE ur.user_id = #{userId}
        AND m.is_deleted = 0
        AND m.status = 1
        ORDER BY m.sort_order
        """)
    List<SysMenu> findMenusByUserId(@Param("userId") String userId);
}
