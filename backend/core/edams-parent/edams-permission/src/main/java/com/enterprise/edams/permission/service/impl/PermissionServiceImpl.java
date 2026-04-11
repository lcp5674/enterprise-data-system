package com.enterprise.edams.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.permission.entity.Permission;
import com.enterprise.edams.permission.repository.PermissionMapper;
import com.enterprise.edams.permission.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionMapper permissionMapper;

    @Override
    public IPage<Permission> queryPermissions(String keyword, Integer type, Integer status,
                                              int pageNum, int pageSize) {
        Page<Permission> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Permission::getName, keyword)
                    .or().like(Permission::getCode, keyword)
                    .or().like(Permission::getDescription, keyword));
        }
        if (type != null) wrapper.eq(Permission::getType, type);
        if (status != null) wrapper.eq(Permission::getStatus, status);

        wrapper.orderByAsc(Permission::getParentId).orderByAsc(Permission::getSortOrder);
        return permissionMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Permission> getPermissionTree() {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getStatus, 1)
               .orderByAsc(Permission::getParentId)
               .orderByAsc(Permission::getSortOrder);
        
        List<Permission> all = permissionMapper.selectList(wrapper);
        return buildTree(all);
    }

    private List<Permission> buildTree(List<Permission> allPermissions) {
        List<Permission> tree = new ArrayList<>();
        for (Permission perm : allPermissions) {
            if (perm.getParentId() == null || perm.getParentId() == 0L) {
                tree.add(buildChildren(perm, allPermissions));
            }
        }
        return tree;
    }

    private Permission buildChildren(Permission parent, List<Permission> all) {
        for (Permission p : all) {
            if (parent.getId().equals(p.getParentId())) {
                parent.getChildren().add(buildChildren(p, all));
            }
        }
        return parent;
    }

    @Override
    public List<Permission> getEnabledPermissions() {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getStatus, 1).orderByAsc(Permission::getSortOrder);
        return permissionMapper.selectList(wrapper);
    }

    @Override
    public Permission getById(Long id) {
        Permission perm = permissionMapper.selectById(id);
        if (perm == null || perm.getDeleted() == 1) {
            throw new BusinessException("权限不存在");
        }
        return perm;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Permission create(Permission permission, String operator) {
        if (permissionMapper.findByCode(permission.getCode()) != null) {
            throw new BusinessException("权限编码已存在: " + permission.getCode());
        }

        permission.setStatus(permission.getStatus() != null ? permission.getStatus() : 1);
        permission.setCreatedBy(operator);
        permissionMapper.insert(permission);

        log.info("权限创建成功: {} ({})", permission.getName(), permission.getId());
        return permission;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, Permission permission, String operator) {
        Permission existing = permissionMapper.selectById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("权限不存在");
        }

        existing.setName(permission.getName());
        existing.setCode(permission.getCode());
        existing.setType(permission.getType());
        existing.setParentId(permission.getParentId());
        existing.setPath(permission.getPath());
        existing.setMethod(permission.getMethod());
        existing.setSortOrder(permission.getSortOrder());
        existing.setStatus(permission.getStatus());
        existing.setDescription(permission.getDescription());

        existing.setUpdatedBy(operator);
        permissionMapper.updateById(existing);
        log.info("权限更新成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, String operator) {
        Permission perm = permissionMapper.selectById(id);
        if (perm == null || perm.getDeleted() == 1) {
            throw new BusinessException("权限不存在");
        }

        // 检查是否有子权限
        long childCount = permissionMapper.selectCount(
                new LambdaQueryWrapper<Permission>().eq(Permission::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException("该权限下还有子权限，无法删除");
        }

        perm.setDeleted(1);
        perm.setUpdatedBy(operator);
        permissionMapper.updateById(perm);
        log.info("权限已删除: {}", id);
    }

    @Override
    public List<Permission> getByRoleId(Long roleId) {
        return permissionMapper.findByRoleId(roleId);
    }
}
