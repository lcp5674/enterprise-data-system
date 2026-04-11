package com.enterprise.edams.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.user.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 部门Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    @Select("SELECT * FROM sys_department WHERE code = #{code} AND deleted = 0")
    Department findByCode(@Param("code") String code);

    @Select("SELECT * FROM sys_department WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort_order ASC")
    List<Department> findByParentId(@Param("parentId") Long parentId);

    @Select("SELECT COUNT(*) FROM sys_department WHERE parent_id = #{parentId} AND deleted = 0")
    int countByParentId(@Param("parentId") Long parentId);
}
