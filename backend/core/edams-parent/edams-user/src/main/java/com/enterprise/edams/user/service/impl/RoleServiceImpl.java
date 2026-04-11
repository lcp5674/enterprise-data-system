package com.enterprise.edams.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.user.entity.Role;
import com.enterprise.edams.user.repository.RoleMapper;
import com.enterprise.edams.user.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;

    @Override
    public IPage<Role> queryRoles(String keyword, Integer status, int pageNum, int pageSize) {
        Page<Role> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Role::getName, keyword)
                    .or().like(Role::getCode, keyword)
                    .or().like(Role::getDescription, keyword));
        }
        if (status != null) {
            wrapper.eq(Role::getStatus, status);
        }
        wrapper.orderByAsc(Role::getSortOrder);

        return roleMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Role> getEnabledRoles() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getStatus, 1).orderByAsc(Role::getSortOrder);
        return roleMapper.selectList(wrapper);
    }

    @Override
    public Role getById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException("角色不存在");
        }
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Role create(Role role, String operator) {
        // 检查编码唯一性
        if (roleMapper.findByCode(role.getCode()) != null) {
            throw new BusinessException("角色编码已存在: " + role.getCode());
        }

        role.setStatus(role.getStatus() != null ? role.getStatus() : 1);
        role.setDataScope(role.getDataScope() != null ? role.getDataScope() : 3); // 默认本部门数据权限
        role.setTenantId(1L);
        role.setCreatedBy(operator);

        roleMapper.insert(role);
        log.info("角色创建成功: {} ({})", role.getName(), role.getId());
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, Role role, String operator) {
        Role existing = roleMapper.selectById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("角色不存在");
        }

        existing.setName(role.getName());
        existing.setCode(role.getCode());
        existing.setDescription(role.getDescription());
        existing.setSortOrder(role.getSortOrder());
        existing.setStatus(role.getStatus());
        existing.setDataScope(role.getDataScope());

        existing.setUpdatedBy(operator);
        roleMapper.updateById(existing);
        log.info("角色更新成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, String operator) {
        Role role = roleMapper.selectById(id);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException("角色不存在");
        }

        // 禁止删除内置角色
        if ("ROLE_ADMIN".equals(role.getCode()) || "ROLE_USER".equals(role.getCode())) {
            throw new BusinessException("不允许删除系统内置角色");
        }

        role.setDeleted(1);
        role.setUpdatedBy(operator);
        roleMapper.updateById(role);

        log.info("角色已删除: {}", id);
    }
}
