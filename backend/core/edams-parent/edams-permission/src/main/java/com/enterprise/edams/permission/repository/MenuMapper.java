package com.enterprise.edams.permission.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.permission.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    @Select("SELECT * FROM sys_menu WHERE code = #{code} AND deleted = 0")
    Menu findByCode(@Param("code") String code);

    @Select("SELECT m.* FROM sys_menu m " +
             "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
             "WHERE rm.role_id = #{roleId} AND m.deleted = 0 AND m.status = 1 " +
             "ORDER BY m.sort_order ASC")
    List<Menu> findByRoleId(@Param("roleId") Long roleId);

    @Select("SELECT * FROM sys_menu WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort_order ASC")
    List<Menu> findByParentId(@Param("parentId") Long parentId);

    @Select("SELECT * FROM sys_menu WHERE deleted = 0 AND status = 1 ORDER BY parent_id ASC, sort_order ASC")
    List<Menu> findAllEnabled();
}
