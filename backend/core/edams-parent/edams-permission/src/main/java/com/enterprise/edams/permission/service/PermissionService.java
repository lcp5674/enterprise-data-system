package com.enterprise.edams.permission.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.permission.entity.Permission;
import java.util.List;

/**
 * 权限服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface PermissionService {

    /** 分页查询权限 */
    IPage<Permission> queryPermissions(String keyword, Integer type, Integer status,
                                        int pageNum, int pageSize);

    /** 获取权限树 */
    List<Permission> getPermissionTree();

    /** 获取所有启用的权限 */
    List<Permission> getEnabledPermissions();

    /** 根据ID获取权限详情 */
    Permission getById(Long id);

    /** 创建权限 */
    Permission create(Permission permission, String operator);

    /** 更新权限 */
    void update(Long id, Permission permission, String operator);

    /** 删除权限 */
    void delete(Long id, String operator);

    /** 获取角色拥有的所有权限 */
    List<Permission> getByRoleId(Long roleId);
}
