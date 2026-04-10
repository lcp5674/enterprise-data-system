package com.enterprise.edams.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.edams.auth.repository.SysUserRepository;
import com.enterprise.edams.auth.entity.SysUser;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.common.result.ResultCode;
import com.enterprise.edams.user.dto.*;
import com.enterprise.edams.user.entity.SysDepartment;
import com.enterprise.edams.user.repository.SysDepartmentRepository;
import com.enterprise.edams.user.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 部门服务实现
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final SysDepartmentRepository departmentRepository;
    private final SysUserRepository userRepository;

    @Override
    @Transactional
    public DepartmentVO createDepartment(DepartmentCreateRequest request) {
        log.info("创建部门: name={}, code={}", request.getName(), request.getCode());

        // 检查编码是否存在
        if (checkCodeExists(request.getCode())) {
            throw new BusinessException(ResultCode.RESOURCE_ALREADY_EXISTS, "部门编码已存在");
        }

        // 计算层级和路径
        int level = 1;
        String parentPath = "";
        String treePath = "";

        if (request.getParentId() != null && !request.getParentId().isEmpty()) {
            SysDepartment parent = departmentRepository.selectById(request.getParentId());
            if (parent == null) {
                throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "上级部门不存在");
            }
            level = parent.getLevel() + 1;
            parentPath = parent.getPath();
            treePath = parent.getTreePath();
        }

        String path = parentPath + "/" + request.getName();
        treePath = treePath + "/" + request.getId();

        // 构建部门实体
        SysDepartment department = SysDepartment.builder()
                .name(request.getName())
                .code(request.getCode())
                .parentId(request.getParentId())
                .level(level)
                .path(path)
                .treePath(treePath)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .leaderId(request.getLeaderId())
                .leaderName(request.getLeaderName())
                .contactPerson(request.getContactPerson())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .description(request.getDescription())
                .status(1)
                .isDeleted(0)
                .createdBy(getCurrentUsername())
                .createdTime(LocalDateTime.now())
                .updatedBy(getCurrentUsername())
                .updatedTime(LocalDateTime.now())
                .build();

        departmentRepository.insert(department);

        // 更新树形路径
        department.setTreePath(treePath + "/" + department.getId());
        departmentRepository.updateById(department);

        return convertToVO(department);
    }

    @Override
    @Transactional
    public DepartmentVO updateDepartment(String departmentId, DepartmentCreateRequest request) {
        log.info("更新部门: departmentId={}", departmentId);

        SysDepartment department = departmentRepository.selectById(departmentId);
        if (department == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "部门不存在");
        }

        // 检查编码是否存在
        if (!department.getCode().equals(request.getCode()) && checkCodeExists(request.getCode())) {
            throw new BusinessException(ResultCode.RESOURCE_ALREADY_EXISTS, "部门编码已存在");
        }

        // 更新字段
        department.setName(request.getName());
        department.setCode(request.getCode());
        if (request.getSortOrder() != null) {
            department.setSortOrder(request.getSortOrder());
        }
        department.setLeaderId(request.getLeaderId());
        department.setLeaderName(request.getLeaderName());
        department.setContactPerson(request.getContactPerson());
        department.setContactPhone(request.getContactPhone());
        department.setContactEmail(request.getContactEmail());
        department.setDescription(request.getDescription());
        department.setUpdatedBy(getCurrentUsername());
        department.setUpdatedTime(LocalDateTime.now());

        departmentRepository.updateById(department);

        return convertToVO(department);
    }

    @Override
    @Transactional
    public void deleteDepartment(String departmentId) {
        log.info("删除部门: departmentId={}", departmentId);

        SysDepartment department = departmentRepository.selectById(departmentId);
        if (department == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "部门不存在");
        }

        // 检查是否有子部门
        List<SysDepartment> children = departmentRepository.findByParentId(departmentId);
        if (!children.isEmpty()) {
            throw new BusinessException(ResultCode.DATA_INVALID, "请先删除子部门");
        }

        // 检查是否有用户
        if (getDepartmentUserCount(departmentId) > 0) {
            throw new BusinessException(ResultCode.DATA_INVALID, "请先转移部门用户");
        }

        department.setIsDeleted(1);
        department.setDeletedBy(getCurrentUsername());
        department.setDeletedTime(LocalDateTime.now());

        departmentRepository.updateById(department);
    }

    @Override
    public DepartmentVO getDepartmentById(String departmentId) {
        SysDepartment department = departmentRepository.selectById(departmentId);
        if (department == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "部门不存在");
        }
        return convertToVO(department);
    }

    @Override
    public DepartmentVO getDepartmentByCode(String code) {
        SysDepartment department = departmentRepository.findByCode(code);
        if (department == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "部门不存在");
        }
        return convertToVO(department);
    }

    @Override
    public List<DepartmentVO> getDepartmentTree() {
        List<SysDepartment> allDepartments = departmentRepository.selectList(
                new LambdaQueryWrapper<SysDepartment>()
                        .eq(SysDepartment::getIsDeleted, 0)
                        .orderByAsc(SysDepartment::getSortOrder)
        );

        return buildDepartmentTree(allDepartments, null);
    }

    @Override
    public DepartmentVO getUserDepartmentTree(String userId) {
        SysUser user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "用户不存在");
        }

        if (user.getDepartmentId() == null) {
            return null;
        }

        return getDepartmentById(user.getDepartmentId());
    }

    @Override
    public List<DepartmentVO> getChildDepartments(String parentId) {
        List<SysDepartment> children = departmentRepository.findByParentId(parentId);
        return children.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void moveDepartment(String departmentId, String newParentId) {
        log.info("移动部门: departmentId={}, newParentId={}", departmentId, newParentId);

        SysDepartment department = departmentRepository.selectById(departmentId);
        if (department == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "部门不存在");
        }

        // 计算新的层级和路径
        int newLevel = 1;
        String newParentPath = "";
        String newTreePath = "";

        if (newParentId != null && !newParentId.isEmpty()) {
            SysDepartment newParent = departmentRepository.selectById(newParentId);
            if (newParent == null) {
                throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "目标上级部门不存在");
            }

            // 检查是否移动到自己的子部门
            if (newParent.getTreePath().contains(departmentId)) {
                throw new BusinessException(ResultCode.DATA_INVALID, "不能移动到子部门");
            }

            newLevel = newParent.getLevel() + 1;
            newParentPath = newParent.getPath();
            newTreePath = newParent.getTreePath();
        }

        // 更新部门
        department.setParentId(newParentId);
        department.setLevel(newLevel);
        department.setPath(newParentPath + "/" + department.getName());
        department.setTreePath(newTreePath + "/" + department.getId());
        department.setUpdatedBy(getCurrentUsername());
        department.setUpdatedTime(LocalDateTime.now());

        departmentRepository.updateById(department);

        // 更新子部门的层级和路径
        updateChildDepartmentPaths(departmentId, department.getPath(), department.getTreePath(), newLevel);
    }

    @Override
    @Transactional
    public void enableDepartment(String departmentId) {
        SysDepartment department = departmentRepository.selectById(departmentId);
        if (department == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "部门不存在");
        }

        department.setStatus(1);
        department.setUpdatedBy(getCurrentUsername());
        department.setUpdatedTime(LocalDateTime.now());

        departmentRepository.updateById(department);
    }

    @Override
    @Transactional
    public void disableDepartment(String departmentId) {
        SysDepartment department = departmentRepository.selectById(departmentId);
        if (department == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "部门不存在");
        }

        department.setStatus(0);
        department.setUpdatedBy(getCurrentUsername());
        department.setUpdatedTime(LocalDateTime.now());

        departmentRepository.updateById(department);
    }

    @Override
    public int getDepartmentUserCount(String departmentId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getDepartmentId, departmentId);
        wrapper.eq(SysUser::getIsDeleted, 0);
        return userRepository.selectCount(wrapper).intValue();
    }

    @Override
    public boolean checkCodeExists(String code) {
        return departmentRepository.existsByCode(code);
    }

    // ========== 私有方法 ==========

    private List<DepartmentVO> buildDepartmentTree(List<SysDepartment> allDepartments, String parentId) {
        return allDepartments.stream()
                .filter(d -> Objects.equals(d.getParentId(), parentId))
                .map(d -> {
                    DepartmentVO vo = convertToVO(d);
                    vo.setChildren(buildDepartmentTree(allDepartments, d.getId()));
                    vo.setUserCount(getDepartmentUserCount(d.getId()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    private void updateChildDepartmentPaths(String parentId, String parentPath, String parentTreePath, int parentLevel) {
        List<SysDepartment> children = departmentRepository.findByParentId(parentId);
        for (SysDepartment child : children) {
            child.setLevel(parentLevel + 1);
            child.setPath(parentPath + "/" + child.getName());
            child.setTreePath(parentTreePath + "/" + child.getId());
            child.setUpdatedBy(getCurrentUsername());
            child.setUpdatedTime(LocalDateTime.now());
            departmentRepository.updateById(child);

            updateChildDepartmentPaths(child.getId(), child.getPath(), child.getTreePath(), child.getLevel());
        }
    }

    private DepartmentVO convertToVO(SysDepartment department) {
        DepartmentVO vo = new DepartmentVO();
        BeanUtils.copyProperties(department, vo);
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
