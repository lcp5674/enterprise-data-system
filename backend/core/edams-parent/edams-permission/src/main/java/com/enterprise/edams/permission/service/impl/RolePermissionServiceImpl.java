package com.enterprise.edams.permission.service.impl;

import com.enterprise.edams.permission.entity.Permission;
import com.enterprise.edams.permission.entity.RolePermission;
import com.enterprise.edams.permission.repository.PermissionMapper;
import com.enterprise.edams.permission.repository.RolePermissionMapper;
import com.enterprise.edams.permission.service.RolePermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色-权限绑定服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;

    @Override
    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        return rolePermissionMapper.findPermissionIdsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds, String operator) {
        // 1. 先删除该角色的所有现有权限
        rolePermissionMapper.deleteByRoleId(roleId);
        
        // 2. 批量插入新的权限关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                RolePermission rp = new RolePermission();
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                rp.setCreatedBy(operator);
                rolePermissionMapper.insert(rp);
            }
        }
        log.info("角色{}权限分配完成，共分配{}个权限", roleId,
                permissionIds != null ? permissionIds.size() : 0);
    }

    @Override
    public List<String> getPermissionCodes(Long roleId) {
        // 获取角色拥有的所有权限实体
        List<Permission> permissions = permissionMapper.findByRoleId(roleId);
        return permissions.stream()
                .map(p -> p.getCode() != null ? p.getCode() : "")
                .filter(code -> !code.isEmpty())
                .collect(Collectors.toList());
    }
}
