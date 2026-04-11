package com.enterprise.edams.permission.service;

import java.util.List;

/**
 * 角色-权限绑定服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface RolePermissionService {

    /** 获取角色拥有的权限ID列表 */
    List<Long> getPermissionIdsByRoleId(Long roleId);

    /** 为角色分配权限 */
    void assignPermissions(Long roleId, List<Long> permissionIds, String operator);

    /** 获取角色的所有权限编码列表（用于前端路由守卫） */
    List<String> getPermissionCodes(Long roleId);
}
