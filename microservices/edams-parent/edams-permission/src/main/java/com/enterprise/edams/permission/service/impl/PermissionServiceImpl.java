package com.enterprise.edams.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.common.result.ResultCode;
import com.enterprise.edams.permission.dto.PermissionCreateRequest;
import com.enterprise.edams.permission.dto.PermissionVO;
import com.enterprise.edams.permission.entity.SysPermission;
import com.enterprise.edams.permission.entity.SysRolePermission;
import com.enterprise.edams.permission.entity.SysUserRole;
import com.enterprise.edams.permission.repository.SysPermissionRepository;
import com.enterprise.edams.permission.repository.SysRolePermissionRepository;
import com.enterprise.edams.permission.repository.SysUserRoleRepository;
import com.enterprise.edams.permission.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 权限服务实现
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final SysPermissionRepository permissionRepository;
    private final SysRolePermissionRepository rolePermissionRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String PERMISSION_CACHE_PREFIX = "edams:permission:";
    private static final long CACHE_EXPIRE_MINUTES = 30;

    @Override
    @Transactional
    public PermissionVO createPermission(PermissionCreateRequest request) {
        log.info("创建权限: name={}, code={}", request.getName(), request.getCode());

        // 检查编码是否存在
        if (checkCodeExists(request.getCode())) {
            throw new BusinessException(ResultCode.RESOURCE_ALREADY_EXISTS, "权限编码已存在");
        }

        SysPermission permission = SysPermission.builder()
                .name(request.getName())
                .code(request.getCode())
                .permissionType(request.getPermissionType())
                .module(request.getModule())
                .resourcePath(request.getResourcePath())
                .httpMethod(request.getHttpMethod())
                .parentId(request.getParentId())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .createdBy(getCurrentUsername())
                .createdTime(LocalDateTime.now())
                .updatedBy(getCurrentUsername())
                .updatedTime(LocalDateTime.now())
                .isDeleted(0)
                .build();

        permissionRepository.insert(permission);

        return convertToVO(permission);
    }

    @Override
    @Transactional
    public PermissionVO updatePermission(String permissionId, PermissionCreateRequest request) {
        log.info("更新权限: permissionId={}", permissionId);

        SysPermission permission = permissionRepository.selectById(permissionId);
        if (permission == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "权限不存在");
        }

        // 更新字段
        if (request.getName() != null) {
            permission.setName(request.getName());
        }
        if (request.getModule() != null) {
            permission.setModule(request.getModule());
        }
        if (request.getResourcePath() != null) {
            permission.setResourcePath(request.getResourcePath());
        }
        if (request.getHttpMethod() != null) {
            permission.setHttpMethod(request.getHttpMethod());
        }
        if (request.getParentId() != null) {
            permission.setParentId(request.getParentId());
        }
        if (request.getDescription() != null) {
            permission.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            permission.setStatus(request.getStatus());
        }
        if (request.getSortOrder() != null) {
            permission.setSortOrder(request.getSortOrder());
        }

        permission.setUpdatedBy(getCurrentUsername());
        permission.setUpdatedTime(LocalDateTime.now());

        permissionRepository.updateById(permission);

        return convertToVO(permission);
    }

    @Override
    @Transactional
    public void deletePermission(String permissionId) {
        log.info("删除权限: permissionId={}", permissionId);

        SysPermission permission = permissionRepository.selectById(permissionId);
        if (permission == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "权限不存在");
        }

        // 删除角色-权限关联
        rolePermissionRepository.deleteByPermissionId(permissionId);

        // 逻辑删除权限
        permission.setIsDeleted(1);
        permission.setDeletedBy(getCurrentUsername());
        permission.setDeletedTime(LocalDateTime.now());
        permissionRepository.updateById(permission);
    }

    @Override
    public PermissionVO getPermissionById(String permissionId) {
        SysPermission permission = permissionRepository.selectById(permissionId);
        if (permission == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "权限不存在");
        }
        return convertToVO(permission);
    }

    @Override
    public PermissionVO getPermissionByCode(String code) {
        SysPermission permission = permissionRepository.findByCode(code);
        if (permission == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "权限不存在");
        }
        return convertToVO(permission);
    }

    @Override
    public Page<PermissionVO> pagePermissions(String keyword, String permissionType, String module, int pageNum, int pageSize) {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getIsDeleted, 0);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(SysPermission::getName, keyword)
                    .or().like(SysPermission::getCode, keyword));
        }

        if (permissionType != null && !permissionType.isEmpty()) {
            wrapper.eq(SysPermission::getPermissionType, permissionType);
        }

        if (module != null && !module.isEmpty()) {
            wrapper.eq(SysPermission::getModule, module);
        }

        wrapper.orderByAsc(SysPermission::getSortOrder)
                .orderByDesc(SysPermission::getCreatedTime);

        Page<SysPermission> page = new Page<>(pageNum, pageSize);
        Page<SysPermission> result = permissionRepository.selectPage(page, wrapper);

        Page<PermissionVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::convertToVO).toList());

        return voPage;
    }

    @Override
    public List<PermissionVO> listAllPermissions() {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysPermission::getIsDeleted, 0)
                .orderByAsc(SysPermission::getSortOrder);
        return permissionRepository.selectList(wrapper).stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<PermissionVO> listPermissionsByModule(String module) {
        return permissionRepository.findByModule(module).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PermissionVO> listPermissionsByType(String permissionType) {
        return permissionRepository.findByPermissionType(permissionType).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PermissionVO> getPermissionTree() {
        List<SysPermission> allPermissions = permissionRepository.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getIsDeleted, 0)
                        .orderByAsc(SysPermission::getSortOrder)
        );

        return buildPermissionTree(allPermissions, null);
    }

    @Override
    public boolean checkCodeExists(String code) {
        return permissionRepository.existsByCode(code);
    }

    @Override
    public List<PermissionVO> getPermissionsByRoleId(String roleId) {
        List<String> permissionIds = rolePermissionRepository.findPermissionIdsByRoleId(roleId);
        if (permissionIds.isEmpty()) {
            return new ArrayList<>();
        }
        return permissionRepository.selectBatchIds(permissionIds).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PermissionVO> getUserPermissions(String userId) {
        // 先从缓存获取
        String cacheKey = PERMISSION_CACHE_PREFIX + "user:" + userId;
        @SuppressWarnings("unchecked")
        List<PermissionVO> cachedPermissions = (List<PermissionVO>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedPermissions != null) {
            return cachedPermissions;
        }

        // 获取用户的所有角色
        List<String> roleIds = userRoleRepository.findRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有角色的权限
        Set<String> permissionIdSet = new HashSet<>();
        for (String roleId : roleIds) {
            permissionIdSet.addAll(rolePermissionRepository.findPermissionIdsByRoleId(roleId));
        }

        if (permissionIdSet.isEmpty()) {
            return new ArrayList<>();
        }

        List<PermissionVO> permissions = permissionRepository.selectBatchIds(permissionIdSet).stream()
                .filter(p -> p.getStatus() == 1)
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, permissions, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return permissions;
    }

    @Override
    public List<String> getUserPermissionCodes(String userId) {
        return getUserPermissions(userId).stream()
                .map(PermissionVO::getCode)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasPermission(String userId, String permissionCode) {
        // 超级管理员拥有所有权限
        if ("SUPER_ADMIN".equals(userId)) {
            return true;
        }

        List<String> permissionCodes = getUserPermissionCodes(userId);
        return permissionCodes.contains(permissionCode);
    }

    // ========== 私有方法 ==========

    private List<PermissionVO> buildPermissionTree(List<SysPermission> permissions, String parentId) {
        return permissions.stream()
                .filter(p -> (parentId == null && p.getParentId() == null) ||
                        (parentId != null && parentId.equals(p.getParentId())))
                .map(p -> {
                    PermissionVO vo = convertToVO(p);
                    List<PermissionVO> children = buildPermissionTree(permissions, p.getId());
                    vo.setParentName(p.getParentId() != null ?
                            permissions.stream()
                                    .filter(parent -> parent.getId().equals(p.getParentId()))
                                    .findFirst()
                                    .map(SysPermission::getName)
                                    .orElse(null) : null);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private PermissionVO convertToVO(SysPermission permission) {
        PermissionVO vo = new PermissionVO();
        BeanUtils.copyProperties(permission, vo);
        return vo;
    }

    private String getCurrentUsername() {
        try {
            return org.springframework.security.core.SecurityContextHolder.getContext()
                    .getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
}
