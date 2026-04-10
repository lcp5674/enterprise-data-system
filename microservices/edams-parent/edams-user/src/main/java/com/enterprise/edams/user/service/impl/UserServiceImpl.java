package com.enterprise.edams.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.auth.entity.SysUser;
import com.enterprise.edams.auth.repository.SysUserRepository;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.common.result.ResultCode;
import com.enterprise.edams.user.dto.*;
import com.enterprise.edams.user.entity.SysDepartment;
import com.enterprise.edams.user.feign.PermissionFeignClient;
import com.enterprise.edams.user.repository.SysDepartmentRepository;
import com.enterprise.edams.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户服务实现
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserRepository userRepository;
    private final SysDepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionFeignClient permissionFeignClient;

    @Override
    @Transactional
    public UserVO createUser(UserCreateRequest request) {
        log.info("创建用户: username={}", request.getUsername());

        // 检查用户名是否存在
        if (checkUsernameExists(request.getUsername())) {
            throw new BusinessException(ResultCode.RESOURCE_ALREADY_EXISTS, "用户名已存在");
        }

        // 检查邮箱是否存在
        if (request.getEmail() != null && checkEmailExists(request.getEmail())) {
            throw new BusinessException(ResultCode.RESOURCE_ALREADY_EXISTS, "邮箱已被使用");
        }

        // 构建用户实体
        SysUser user = SysUser.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .email(request.getEmail())
                .phone(request.getPhone())
                .employeeNo(request.getEmployeeNo())
                .avatarUrl(request.getAvatarUrl())
                .departmentId(request.getDepartmentId())
                .position(request.getPosition())
                .managerId(request.getManagerId())
                .status(1)
                .userType(request.getUserType())
                .sourceType("LOCAL")
                .loginFailCount(0)
                .mfaEnabled(0)
                .isFirstLogin(1)
                .isDeleted(0)
                .createdBy(getCurrentUsername())
                .createdTime(LocalDateTime.now())
                .updatedBy(getCurrentUsername())
                .updatedTime(LocalDateTime.now())
                .build();

        userRepository.insert(user);

        return convertToVO(user);
    }

    @Override
    @Transactional
    public UserVO updateUser(String userId, UserUpdateRequest request) {
        log.info("更新用户: userId={}", userId);

        SysUser user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "用户不存在");
        }

        // 检查邮箱是否存在
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (checkEmailExists(request.getEmail())) {
                throw new BusinessException(ResultCode.RESOURCE_ALREADY_EXISTS, "邮箱已被使用");
            }
        }

        // 更新字段
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getDepartmentId() != null) {
            user.setDepartmentId(request.getDepartmentId());
        }
        if (request.getPosition() != null) {
            user.setPosition(request.getPosition());
        }
        if (request.getManagerId() != null) {
            user.setManagerId(request.getManagerId());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        user.setUpdatedBy(getCurrentUsername());
        user.setUpdatedTime(LocalDateTime.now());

        userRepository.updateById(user);

        return convertToVO(user);
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        log.info("删除用户: userId={}", userId);

        SysUser user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "用户不存在");
        }

        user.setIsDeleted(1);
        user.setDeletedBy(getCurrentUsername());
        user.setDeletedTime(LocalDateTime.now());

        userRepository.updateById(user);
    }

    @Override
    public UserVO getUserById(String userId) {
        SysUser user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "用户不存在");
        }
        return convertToVO(user);
    }

    @Override
    public UserVO getUserByUsername(String username) {
        SysUser user = userRepository.findByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "用户不存在");
        }
        return convertToVO(user);
    }

    @Override
    public Page<UserVO> pageUsers(String keyword, String departmentId, Integer status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getIsDeleted, 0);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getNickname, keyword)
                    .or().like(SysUser::getEmail, keyword));
        }

        if (departmentId != null && !departmentId.isEmpty()) {
            wrapper.eq(SysUser::getDepartmentId, departmentId);
        }

        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }

        wrapper.orderByDesc(SysUser::getCreatedTime);

        Page<SysUser> page = new Page<>(pageNum, pageSize);
        Page<SysUser> result = userRepository.selectPage(page, wrapper);

        Page<UserVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::convertToVO).toList());

        return voPage;
    }

    @Override
    @Transactional
    public int batchDeleteUsers(List<String> userIds) {
        log.info("批量删除用户: count={}", userIds.size());

        int count = 0;
        for (String userId : userIds) {
            try {
                deleteUser(userId);
                count++;
            } catch (Exception e) {
                log.error("删除用户失败: userId={}", userId, e);
            }
        }
        return count;
    }

    @Override
    @Transactional
    public void changePassword(String userId, String oldPassword, String newPassword) {
        log.info("修改密码: userId={}", userId);

        SysUser user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.AUTH_PASSWORD_ERROR, "原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordExpireTime(null); // 重置密码过期时间
        user.setUpdatedBy(getCurrentUsername());
        user.setUpdatedTime(LocalDateTime.now());

        userRepository.updateById(user);
    }

    @Override
    @Transactional
    public void resetPassword(String userId, String newPassword) {
        log.info("重置密码: userId={}", userId);

        SysUser user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "用户不存在");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordExpireTime(LocalDateTime.now().plusDays(90)); // 默认90天后过期
        user.setUpdatedBy(getCurrentUsername());
        user.setUpdatedTime(LocalDateTime.now());

        userRepository.updateById(user);
    }

    @Override
    @Transactional
    public void enableUser(String userId) {
        SysUser user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "用户不存在");
        }

        user.setStatus(1);
        user.setUpdatedBy(getCurrentUsername());
        user.setUpdatedTime(LocalDateTime.now());

        userRepository.updateById(user);
    }

    @Override
    @Transactional
    public void disableUser(String userId) {
        SysUser user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "用户不存在");
        }

        user.setStatus(0);
        user.setUpdatedBy(getCurrentUsername());
        user.setUpdatedTime(LocalDateTime.now());

        userRepository.updateById(user);
    }

    @Override
    @Transactional
    public void assignRoles(String userId, List<String> roleIds) {
        log.info("分配角色: userId={}, roleIds={}", userId, roleIds);

        // 验证用户是否存在
        SysUser user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "用户不存在");
        }

        try {
            // 调用权限服务分配角色
            var result = permissionFeignClient.assignRoles(userId, roleIds);
            if (result == null || result.getCode() != 200) {
                throw new BusinessException("角色分配失败: " + (result != null ? result.getMessage() : "服务异常"));
            }
            log.info("角色分配成功: userId={}, roleIds={}", userId, roleIds);
        } catch (Exception e) {
            log.error("调用权限服务分配角色失败: userId={}, error={}", userId, e.getMessage());
            throw new BusinessException("角色分配失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> getUserRoles(String userId) {
        try {
            var result = permissionFeignClient.getUserRoles(userId);
            if (result != null && result.getData() != null) {
                return result.getData().stream()
                        .map(role -> (String) role.get("roleCode"))
                        .filter(code -> code != null)
                        .toList();
            }
        } catch (Exception e) {
            log.warn("从权限服务获取用户角色失败, userId: {}, error: {}", userId, e.getMessage());
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getUserMenus(String userId) {
        try {
            var result = permissionFeignClient.getUserMenus(userId);
            if (result != null && result.getData() != null) {
                return result.getData().stream()
                        .map(menu -> (String) menu.get("path"))
                        .filter(path -> path != null)
                        .toList();
            }
        } catch (Exception e) {
            log.warn("从权限服务获取用户菜单失败, userId: {}, error: {}", userId, e.getMessage());
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getUserPermissions(String userId) {
        try {
            var result = permissionFeignClient.getUserPermissions(userId);
            if (result != null && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.warn("从权限服务获取用户权限失败, userId: {}, error: {}", userId, e.getMessage());
        }
        return Collections.emptyList();
    }

    @Override
    public boolean checkUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean checkPhoneExists(String phone) {
        return userRepository.findByPhone(phone) != null;
    }

    // ========== 私有方法 ==========

    private UserVO convertToVO(SysUser user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);

        // 设置部门名称
        if (user.getDepartmentId() != null) {
            SysDepartment dept = departmentRepository.selectById(user.getDepartmentId());
            if (dept != null) {
                vo.setDepartmentName(dept.getName());
            }
        }

        // 设置上级姓名
        if (user.getManagerId() != null) {
            SysUser manager = userRepository.selectById(user.getManagerId());
            if (manager != null) {
                vo.setManagerName(manager.getNickname());
            }
        }

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
