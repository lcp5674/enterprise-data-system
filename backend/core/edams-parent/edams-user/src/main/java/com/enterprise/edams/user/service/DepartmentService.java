package com.enterprise.edams.user.service;

import com.enterprise.edams.user.entity.Department;
import java.util.List;

/**
 * 部门服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface DepartmentService {

    /** 获取部门树形结构 */
    List<Department> getDepartmentTree();

    /** 获取所有部门（扁平列表） */
    List<Department> getAllDepartments();

    /** 根据ID获取部门 */
    Department getById(Long id);

    /** 创建部门 */
    Department create(Department department, String operator);

    /** 更新部门 */
    void update(Long id, Department department, String operator);

    /** 删除部门（检查是否有子部门） */
    void delete(Long id, String operator);

    /** 获取子部门列表 */
    List<Department> getChildren(Long parentId);
}
