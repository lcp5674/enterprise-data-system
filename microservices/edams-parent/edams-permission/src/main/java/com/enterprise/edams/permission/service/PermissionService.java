package com.enterprise.edams.permission.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.permission.dto.PermissionCreateRequest;
import com.enterprise.edams.permission.dto.PermissionVO;
import com.enterprise.edams.permission.dto.RoleVO;

import java.util.List;

/**
 * 权限服务接口
 *
 * @author Backend Team
 * @version 1.0.0
 */
public interface PermissionService {

    /**
     * 创建权限
     */
    PermissionVO createPermission(PermissionCreateRequest request);

    /**
     * 更新权限
     */
    PermissionVO updatePermission(String permissionId, PermissionCreateRequest request);

    /**
     * 删除权限
     */
    void deletePermission(String permissionId);

    /**
     * 根据ID获取权限
     */
    PermissionVO getPermissionById(String permissionId);

    /**
     * 根据权限编码获取权限
     */
    PermissionVO getPermissionByCode(String code);

    /**
     * 分页查询权限
     */
    Page<PermissionVO> pagePermissions(String keyword, String permissionType, String module, int pageNum, int pageSize);

    /**
     * 查询所有权限
     */
    List<PermissionVO> listAllPermissions();

    /**
     * 根据模块查询权限列表
     */
    List<PermissionVO> listPermissionsByModule(String module);

    /**
     * 根据权限类型查询权限列表
     */
    List<PermissionVO> listPermissionsByType(String permissionType);

    /**
     * 获取权限树
     */
    List<PermissionVO> getPermissionTree();

    /**
     * 检查权限编码是否存在
     */
    boolean checkCodeExists(String code);

    /**
     * 根据角色获取权限列表
     */
    List<PermissionVO> getPermissionsByRoleId(String roleId);

    /**
     * 获取用户的所有权限
     */
    List<PermissionVO> getUserPermissions(String userId);

    /**
     * 获取用户的权限编码列表
     */
    List<String> getUserPermissionCodes(String userId);

    /**
     * 验证用户是否具有指定权限
     */
    boolean hasPermission(String userId, String permissionCode);
}
