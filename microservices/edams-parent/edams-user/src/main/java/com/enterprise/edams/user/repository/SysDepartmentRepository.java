package com.enterprise.edams.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.user.entity.SysDepartment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 部门Mapper
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Mapper
public interface SysDepartmentRepository extends BaseMapper<SysDepartment> {

    /**
     * 根据部门编码查询
     */
    @Select("SELECT * FROM sys_department WHERE code = #{code} AND is_deleted = 0 LIMIT 1")
    SysDepartment findByCode(@Param("code") String code);

    /**
     * 查询子部门
     */
    @Select("SELECT * FROM sys_department WHERE parent_id = #{parentId} AND is_deleted = 0 ORDER BY sort_order")
    List<SysDepartment> findByParentId(@Param("parentId") String parentId);

    /**
     * 查询顶级部门
     */
    @Select("SELECT * FROM sys_department WHERE parent_id IS NULL AND is_deleted = 0 ORDER BY sort_order")
    List<SysDepartment> findRootDepartments();

    /**
     * 检查部门编码是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM sys_department WHERE code = #{code} AND is_deleted = 0")
    boolean existsByCode(@Param("code") String code);

    /**
     * 根据层级查询部门
     */
    @Select("SELECT * FROM sys_department WHERE level = #{level} AND is_deleted = 0 ORDER BY sort_order")
    List<SysDepartment> findByLevel(@Param("level") Integer level);
}
