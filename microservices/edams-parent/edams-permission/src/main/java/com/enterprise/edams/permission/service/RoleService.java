package com.enterprise.edams.permission.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.permission.dto.*;

import java.util.List;

/**
 * 角色服务接口
 *
 * @author Backend Team
 * @version 1.0.0
 */
public interface RoleService {

    /**
     * 创建角色
     */
    RoleVO createRole(RoleCreateRequest request);

    /**
     * 更新角色
     */
    RoleVO updateRole(String roleId, RoleUpdateRequest request);

    /**
     * 删除角色
     */
    void deleteRole(String roleId);

    /**
     * 根据ID获取角色
     */
    RoleVO getRoleById(String roleId);

    /**
     * 根据角色编码获取角色
     */
    RoleVO getRoleByCode(String code);

    /**
     * 分页查询角色
     */
    Page<RoleVO> pageRoles(String keyword, String roleType, Integer status, int pageNum, int pageSize);

    /**
     * 查询所有角色
     */
    List<RoleVO> listAllRoles();

    /**
     * 检查角色编码是否存在
     */
    boolean checkCodeExists(String code);

    /**
     * 分配角色权限
     */
    void assignPermissions(String roleId, List<String> permissionIds);

    /**
     * 获取角色的权限列表
     */
    List<PermissionVO> getRolePermissions(String roleId);

    /**
     * 分配用户角色
     */
    void assignUsers(String roleId, List<String> userIds);

    /**
     * 获取角色的用户数量
     */
    long getUserCount(String roleId);

    /**
     * 启用角色
     */
    void enableRole(String roleId);

    /**
     * 禁用角色
     */
    void disableRole(String roleId);
}
