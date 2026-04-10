package com.enterprise.edams.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.edams.permission.dto.MenuVO;
import com.enterprise.edams.permission.dto.PermissionVO;
import com.enterprise.edams.permission.entity.SysMenu;
import com.enterprise.edams.permission.entity.SysRolePermission;
import com.enterprise.edams.permission.entity.SysUserRole;
import com.enterprise.edams.permission.repository.SysMenuRepository;
import com.enterprise.edams.permission.repository.SysPermissionRepository;
import com.enterprise.edams.permission.repository.SysRolePermissionRepository;
import com.enterprise.edams.permission.repository.SysUserRoleRepository;
import com.enterprise.edams.permission.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final SysMenuRepository menuRepository;
    private final SysPermissionRepository permissionRepository;
    private final SysRolePermissionRepository rolePermissionRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String MENU_CACHE_PREFIX = "edams:menu:";
    private static final long CACHE_EXPIRE_MINUTES = 30;

    @Override
    public List<MenuVO> getUserMenus(String userId) {
        // 超级管理员拥有所有菜单
        if ("SUPER_ADMIN".equals(userId)) {
            return getMenusByApplication("WEB");
        }

        // 先从缓存获取
        String cacheKey = MENU_CACHE_PREFIX + "user:" + userId;
        @SuppressWarnings("unchecked")
        List<MenuVO> cachedMenus = (List<MenuVO>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedMenus != null) {
            return cachedMenus;
        }

        // 获取用户的所有菜单
        List<SysMenu> menus = menuRepository.findMenusByUserId(userId);
        List<MenuVO> menuTree = buildMenuTree(menus);

        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, menuTree, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return menuTree;
    }

    @Override
    public List<MenuVO> getMenusByApplication(String application) {
        List<SysMenu> menus = menuRepository.findByApplication(application);
        return buildMenuTree(menus);
    }

    @Override
    public List<MenuVO> listAllMenus() {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getIsDeleted, 0)
                .orderByAsc(SysMenu::getSortOrder);
        return menuRepository.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MenuVO> getMenusByRoleId(String roleId) {
        // 获取角色关联的权限
        List<String> permissionIds = rolePermissionRepository.findPermissionIdsByRoleId(roleId);
        if (permissionIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取权限关联的菜单
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getIsDeleted, 0)
                .eq(SysMenu::getStatus, 1)
                .in(SysMenu::getId, permissionIds)
                .orderByAsc(SysMenu::getSortOrder);

        return menuRepository.selectList(wrapper).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignMenus(String roleId, List<String> menuIds) {
        log.info("分配角色菜单: roleId={}, menuIds={}", roleId, menuIds);

        // 获取现有菜单关联的权限
        List<String> existingPermissionIds = rolePermissionRepository.findPermissionIdsByRoleId(roleId);
        
        // 找出需要删除和添加的权限
        Set<String> newMenuIdSet = new HashSet<>(menuIds != null ? menuIds : Collections.emptyList());
        Set<String> existingPermissionIdSet = new HashSet<>(existingPermissionIds);

        // 删除不在新列表中的权限
        for (String permissionId : existingPermissionIds) {
            if (!newMenuIdSet.contains(permissionId)) {
                // 这里需要检查是否是菜单类型的权限再删除
                SysMenu menu = menuRepository.selectById(permissionId);
                if (menu != null) {
                    rolePermissionRepository.delete(
                            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysRolePermission>()
                                    .eq(SysRolePermission::getRoleId, roleId)
                                    .eq(SysRolePermission::getPermissionId, permissionId)
                    );
                }
            }
        }

        // 添加新菜单的权限
        if (menuIds != null) {
            for (String menuId : menuIds) {
                if (!existingPermissionIdSet.contains(menuId)) {
                    SysRolePermission rp = SysRolePermission.builder()
                            .id(UUID.randomUUID().toString())
                            .roleId(roleId)
                            .permissionId(menuId)
                            .createdBy(getCurrentUsername())
                            .createdTime(java.time.LocalDateTime.now())
                            .build();
                    rolePermissionRepository.insert(rp);
                }
            }
        }

        // 清除用户菜单缓存
        clearUserMenuCache(roleId);
    }

    // ========== 私有方法 ==========

    private List<MenuVO> buildMenuTree(List<SysMenu> menus) {
        if (menus == null || menus.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, List<SysMenu>> childrenMap = menus.stream()
                .filter(m -> m.getParentId() != null)
                .collect(Collectors.groupingBy(SysMenu::getParentId));

        List<SysMenu> rootMenus = menus.stream()
                .filter(m -> m.getParentId() == null || "0".equals(m.getParentId()))
                .collect(Collectors.toList());

        return rootMenus.stream()
                .map(menu -> buildMenuNode(menu, childrenMap))
                .collect(Collectors.toList());
    }

    private MenuVO buildMenuNode(SysMenu menu, Map<String, List<SysMenu>> childrenMap) {
        MenuVO vo = convertToVO(menu);
        List<SysMenu> children = childrenMap.get(menu.getId());
        if (children != null && !children.isEmpty()) {
            vo.setChildren(children.stream()
                    .map(child -> buildMenuNode(child, childrenMap))
                    .collect(Collectors.toList()));
        }
        return vo;
    }

    private MenuVO convertToVO(SysMenu menu) {
        MenuVO vo = new MenuVO();
        BeanUtils.copyProperties(menu, vo);
        return vo;
    }

    private void clearUserMenuCache(String roleId) {
        // 获取角色关联的所有用户
        List<String> userIds = userRoleRepository.findUserIdsByRoleId(roleId);
        for (String userId : userIds) {
            String cacheKey = MENU_CACHE_PREFIX + "user:" + userId;
            redisTemplate.delete(cacheKey);
        }
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
