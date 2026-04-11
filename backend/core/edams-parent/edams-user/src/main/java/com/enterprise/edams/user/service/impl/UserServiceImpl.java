package com.enterprise.edams.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.user.dto.UserCreateRequest;
import com.enterprise.edams.user.dto.UserUpdateRequest;
import com.enterprise.edams.user.dto.UserVO;
import com.enterprise.edams.user.entity.User;
import com.enterprise.edams.user.repository.UserMapper;
import com.enterprise.edams.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public IPage<UserVO> queryUsers(String keyword, Long departmentId, Integer status,
                                     int pageNum, int pageSize) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getEmail, keyword)
                    .or().like(User::getPhone, keyword));
        }
        if (departmentId != null) {
            wrapper.eq(User::getDepartmentId, departmentId);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreatedTime);

        IPage<User> resultPage = userMapper.selectPage(page, wrapper);

        // 转换为VO
        return resultPage.convert(UserVO::fromEntity);
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }
        return UserVO.fromEntity(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO createUser(UserCreateRequest request, String operator) {
        // 检查用户名唯一性
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new BusinessException("用户名已存在: " + request.getUsername());
        }
        // 检查邮箱唯一性
        if (request.getEmail() != null && !request.getEmail().isEmpty()
                && userMapper.findByEmail(request.getEmail()) != null) {
            throw new BusinessException("邮箱已被注册: " + request.getEmail());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setGender(request.getGender());
        user.setDepartmentId(request.getDepartmentId());
        user.setStatus(request.getStatus());
        user.setMfaEnabled(0);
        user.setLoginFailCount(0);
        user.setTenantId(1L); // 默认租户

        user.setCreatedBy(operator);
        userMapper.insert(user);

        log.info("用户创建成功: {} ({})", request.getUsername(), user.getId());
        return UserVO.fromEntity(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, UserUpdateRequest request, String operator) {
        User existing = userMapper.selectById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }

        // 检查邮箱唯一性（如果修改了邮箱）
        if (request.getEmail() != null && !request.getEmail().equals(existing.getEmail())) {
            if (userMapper.findByEmail(request.getEmail()) != null) {
                throw new BusinessException("邮箱已被使用");
            }
            existing.setEmail(request.getEmail());
        }

        if (request.getRealName() != null) existing.setRealName(request.getRealName());
        if (request.getPhone() != null) existing.setPhone(request.getPhone());
        if (request.getAvatar() != null) existing.setAvatar(request.getAvatar());
        if (request.getGender() != null) existing.setGender(request.getGender());
        if (request.getDepartmentId() != null) existing.setDepartmentId(request.getDepartmentId());
        if (request.getStatus() != null) existing.setStatus(request.getStatus());

        existing.setUpdatedBy(operator);
        userMapper.updateById(existing);

        log.info("用户更新成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id, String operator) {
        User user = userMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在");
        }

        // 禁止删除管理员账号
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("不允许删除系统管理员账号");
        }

        user.setDeleted(1);
        user.setUpdatedBy(operator);
        userMapper.updateById(user);

        log.info("用户已逻辑删除: {} ({})", user.getUsername(), id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, String newPassword, String operator) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLoginFailCount(0);
        user.setLockTime(null);
        user.setUpdatedBy(operator);
        userMapper.updateById(user);

        log.info("用户密码已重置: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status, String operator) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态值无效，只能为0或1");
        }

        user.setStatus(status);
        user.setUpdatedBy(operator);
        userMapper.updateById(user);

        log.info("用户{}状态变更为: {}", id, status == 1 ? "启用" : "禁用");
    }

    @Override
    public List<UserVO> getUsersByDepartment(Long departmentId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDepartmentId, departmentId)
               .eq(User::getStatus, 1)
               .orderByAsc(User::getRealName);

        List<User> users = userMapper.selectList(wrapper);
        return users.stream().map(UserVO::fromEntity).toList();
    }
}
