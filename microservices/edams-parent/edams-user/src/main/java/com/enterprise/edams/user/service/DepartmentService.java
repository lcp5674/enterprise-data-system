package com.enterprise.edams.user.service;

import com.enterprise.edams.user.dto.*;

import java.util.List;

/**
 * 部门服务接口
 *
 * @author Backend Team
 * @version 1.0.0
 */
public interface DepartmentService {

    /**
     * 创建部门
     */
    DepartmentVO createDepartment(DepartmentCreateRequest request);

    /**
     * 更新部门
     */
    DepartmentVO updateDepartment(String departmentId, DepartmentCreateRequest request);

    /**
     * 删除部门
     */
    void deleteDepartment(String departmentId);

    /**
     * 根据ID查询部门
     */
    DepartmentVO getDepartmentById(String departmentId);

    /**
     * 根据编码查询部门
     */
    DepartmentVO getDepartmentByCode(String code);

    /**
     * 查询所有部门（树形结构）
     */
    List<DepartmentVO> getDepartmentTree();

    /**
     * 查询用户所属部门树
     */
    DepartmentVO getUserDepartmentTree(String userId);

    /**
     * 获取子部门
     */
    List<DepartmentVO> getChildDepartments(String parentId);

    /**
     * 移动部门
     */
    void moveDepartment(String departmentId, String newParentId);

    /**
     * 启用部门
     */
    void enableDepartment(String departmentId);

    /**
     * 禁用部门
     */
    void disableDepartment(String departmentId);

    /**
     * 获取部门用户数
     */
    int getDepartmentUserCount(String departmentId);

    /**
     * 检查部门编码是否存在
     */
    boolean checkCodeExists(String code);
}
