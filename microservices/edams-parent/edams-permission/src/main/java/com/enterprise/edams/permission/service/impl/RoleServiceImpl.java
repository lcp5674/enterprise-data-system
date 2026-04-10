package com.enterprise.edams.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.common.result.ResultCode;
import com.enterprise.edams.permission.dto.*;
import com.enterprise.edams.permission.entity.SysPermission;
import com.enterprise.edams.permission.entity.SysRole;
import com.enterprise.edams.permission.entity.SysRolePermission;
import com.enterprise.edams.permission.entity.SysUserRole;
import com.enterprise.edams.permission.repository.*;
import com.enterprise.edams.permission.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final SysRoleRepository roleRepository;
    private final SysPermissionRepository permissionRepository;
    private final SysRolePermissionRepository rolePermissionRepository;
    private final SysUserRoleRepository userRoleRepository;

    @Override
    @Transactional
    public RoleVO createRole(RoleCreateRequest request) {
        log.info("创建角色: name={}, code={}", request.getName(), request.getCode());

        // 检查编码是否存在
        if (checkCodeExists(request.getCode())) {
            throw new BusinessException(ResultCode.RESOURCE_ALREADY_EXISTS, "角色编码已存在");
        }

        // 构建角色实体
        SysRole role = SysRole.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .roleType(request.getRoleType())
                .dataScope(request.getDataScope() != null ? request.getDataScope() : "SELF")
                .status(request.getStatus() != null ? request.getStatus() : 1)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : 0)
                .createdBy(getCurrentUsername())
                .createdTime(LocalDateTime.now())
                .updatedBy(getCurrentUsername())
                .updatedTime(LocalDateTime.now())
                .isDeleted(0)
                .build();

        roleRepository.insert(role);

        // 分配权限
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            assignPermissions(role.getId(), request.getPermissionIds());
        }

        return convertToVO(role);
    }

    @Override
    @Transactional
    public RoleVO updateRole(String roleId, RoleUpdateRequest request) {
        log.info("更新角色: roleId={}", roleId);

        SysRole role = roleRepository.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "角色不存在");
        }

        // 更新字段
        if (request.getName() != null) {
            role.setName(request.getName());
        }
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        if (request.getDataScope() != null) {
            role.setDataScope(request.getDataScope());
        }
        if (request.getStatus() != null) {
            role.setStatus(request.getStatus());
        }
        if (request.getSortOrder() != null) {
            role.setSortOrder(request.getSortOrder());
        }
        if (request.getIsDefault() != null) {
            role.setIsDefault(request.getIsDefault());
        }

        role.setUpdatedBy(getCurrentUsername());
        role.setUpdatedTime(LocalDateTime.now());

        roleRepository.updateById(role);

        // 更新权限
        if (request.getPermissionIds() != null) {
            assignPermissions(roleId, request.getPermissionIds());
        }

        return convertToVO(role);
    }

    @Override
    @Transactional
    public void deleteRole(String roleId) {
        log.info("删除角色: roleId={}", roleId);

        SysRole role = roleRepository.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "角色不存在");
        }

        // 检查是否有用户关联
        long userCount = userRoleRepository.countByRoleId(roleId);
        if (userCount > 0) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR, "该角色已分配给用户，无法删除");
        }

        // 删除角色-权限关联
        rolePermissionRepository.deleteByRoleId(roleId);

        // 逻辑删除角色
        role.setIsDeleted(1);
        role.setDeletedBy(getCurrentUsername());
        role.setDeletedTime(LocalDateTime.now());
        roleRepository.updateById(role);
    }

    @Override
    public RoleVO getRoleById(String roleId) {
        SysRole role = roleRepository.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "角色不存在");
        }
        return convertToVO(role);
    }

    @Override
    public RoleVO getRoleByCode(String code) {
        SysRole role = roleRepository.findByCode(code);
        if (role == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "角色不存在");
        }
        return convertToVO(role);
    }

    @Override
    public Page<RoleVO> pageRoles(String keyword, String roleType, Integer status, int pageNum, int pageSize) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getIsDeleted, 0);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(SysRole::getName, keyword)
                    .or().like(SysRole::getCode, keyword));
        }

        if (roleType != null && !roleType.isEmpty()) {
            wrapper.eq(SysRole::getRoleType, roleType);
        }

        if (status != null) {
            wrapper.eq(SysRole::getStatus, status);
        }

        wrapper.orderByAsc(SysRole::getSortOrder)
                .orderByDesc(SysRole::getCreatedTime);

        Page<SysRole> page = new Page<>(pageNum, pageSize);
        Page<SysRole> result = roleRepository.selectPage(page, wrapper);

        Page<RoleVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::convertToVO).toList());

        return voPage;
    }

    @Override
    public List<RoleVO> listAllRoles() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getIsDeleted, 0)
                .eq(SysRole::getStatus, 1)
                .orderByAsc(SysRole::getSortOrder);
        return roleRepository.selectList(wrapper).stream().map(this::convertToVO).toList();
    }

    @Override
    public boolean checkCodeExists(String code) {
        return roleRepository.existsByCode(code);
    }

    @Override
    @Transactional
    public void assignPermissions(String roleId, List<String> permissionIds) {
        log.info("分配角色权限: roleId={}, permissionIds={}", roleId, permissionIds);

        // 删除现有权限关联
        rolePermissionRepository.deleteByRoleId(roleId);

        // 添加新权限关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (String permissionId : permissionIds) {
                SysRolePermission rp = SysRolePermission.builder()
                        .id(java.util.UUID.randomUUID().toString())
                        .roleId(roleId)
                        .permissionId(permissionId)
                        .createdBy(getCurrentUsername())
                        .createdTime(LocalDateTime.now())
                        .build();
                rolePermissionRepository.insert(rp);
            }
        }
    }

    @Override
    public List<PermissionVO> getRolePermissions(String roleId) {
        List<String> permissionIds = rolePermissionRepository.findPermissionIdsByRoleId(roleId);
        if (permissionIds.isEmpty()) {
            return new ArrayList<>();
        }

        return permissionRepository.selectBatchIds(permissionIds).stream()
                .map(this::convertPermissionToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignUsers(String roleId, List<String> userIds) {
        log.info("分配角色用户: roleId={}, userIds={}", roleId, userIds);

        // 删除现有用户关联
        userRoleRepository.deleteByRoleId(roleId);

        // 添加新用户关联
        if (userIds != null && !userIds.isEmpty()) {
            for (String userId : userIds) {
                SysUserRole ur = SysUserRole.builder()
                        .id(java.util.UUID.randomUUID().toString())
                        .userId(userId)
                        .roleId(roleId)
                        .createdBy(getCurrentUsername())
                        .createdTime(LocalDateTime.now())
                        .build();
                userRoleRepository.insert(ur);
            }
        }
    }

    @Override
    public long getUserCount(String roleId) {
        return userRoleRepository.countByRoleId(roleId);
    }

    @Override
    @Transactional
    public void enableRole(String roleId) {
        SysRole role = roleRepository.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "角色不存在");
        }
        role.setStatus(1);
        role.setUpdatedBy(getCurrentUsername());
        role.setUpdatedTime(LocalDateTime.now());
        roleRepository.updateById(role);
    }

    @Override
    @Transactional
    public void disableRole(String roleId) {
        SysRole role = roleRepository.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "角色不存在");
        }
        role.setStatus(0);
        role.setUpdatedBy(getCurrentUsername());
        role.setUpdatedTime(LocalDateTime.now());
        roleRepository.updateById(role);
    }

    // ========== 私有方法 ==========

    private RoleVO convertToVO(SysRole role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);

        // 设置权限ID列表
        List<String> permissionIds = rolePermissionRepository.findPermissionIdsByRoleId(role.getId());
        vo.setPermissionIds(permissionIds);

        // 设置用户数量
        vo.setUserCount(getUserCount(role.getId()));

        return vo;
    }

    private PermissionVO convertPermissionToVO(SysPermission permission) {
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
