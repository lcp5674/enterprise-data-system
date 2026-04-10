package com.enterprise.edams.permission.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.permission.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 角色Mapper
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Mapper
public interface SysRoleRepository extends BaseMapper<SysRole> {

    /**
     * 根据角色编码查询
     */
    @Select("SELECT * FROM sys_role WHERE code = #{code} AND is_deleted = 0 LIMIT 1")
    SysRole findByCode(@Param("code") String code);

    /**
     * 检查角色编码是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM sys_role WHERE code = #{code} AND is_deleted = 0")
    boolean existsByCode(@Param("code") String code);
}
