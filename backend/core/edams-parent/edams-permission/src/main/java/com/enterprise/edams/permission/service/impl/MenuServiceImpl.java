package com.enterprise.edams.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.permission.entity.Menu;
import com.enterprise.edams.permission.repository.MenuMapper;
import com.enterprise.edams.permission.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;

    @Override
    public List<Menu> getMenuTree() {
        List<Menu> all = menuMapper.findAllEnabled();
        return buildMenuTree(all);
    }

    private List<Menu> buildMenuTree(List<Menu> allMenus) {
        List<Menu> tree = new ArrayList<>();
        for (Menu menu : allMenus) {
            if (menu.getParentId() == null || menu.getParentId() == 0L) {
                tree.add(buildChildren(menu, allMenus));
            }
        }
        return tree;
    }

    private Menu buildChildren(Menu parent, List<Menu> all) {
        for (Menu m : all) {
            if (parent.getId().equals(m.getParentId())) {
                parent.getChildren().add(buildChildren(m, all));
            }
        }
        return parent;
    }

    @Override
    public List<Menu> getByRoleId(Long roleId) {
        return menuMapper.findByRoleId(roleId);
    }

    @Override
    public List<Menu> getAllEnabled() {
        return menuMapper.findAllEnabled();
    }

    @Override
    public Menu getById(Long id) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null || menu.getDeleted() == 1) {
            throw new BusinessException("菜单不存在");
        }
        return menu;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Menu create(Menu menu, String operator) {
        if (menuMapper.findByCode(menu.getCode()) != null) {
            throw new BusinessException("菜单编码已存在: " + menu.getCode());
        }

        menu.setStatus(1);
        menu.setTenantId(1L);
        menu.setCreatedBy(operator);

        // 设置默认值
        if (menu.getType() == null) menu.setType(1);
        if (menu.getExternal() == null) menu.setExternal(0);
        if (menu.getCache() == null) menu.setCache(0);
        if (menu.getHidden() == null) menu.setHidden(0);

        menuMapper.insert(menu);
        log.info("菜单创建成功: {} ({})", menu.getName(), menu.getId());
        return menu;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, Menu menu, String operator) {
        Menu existing = menuMapper.selectById(id);
        if (existing == null || existing.getDeleted() == 1) {
            throw new BusinessException("菜单不存在");
        }

        existing.setName(menu.getName());
        existing.setCode(menu.getCode());
        existing.setParentId(menu.getParentId());
        existing.setSortOrder(menu.getSortOrder());
        existing.setType(menu.getType());
        existing.setPath(menu.getPath());
        existing.setComponent(menu.getComponent());
        existing.setIcon(menu.getIcon());
        existing.setPermission(menu.getPermission());
        existing.setExternal(menu.getExternal());
        existing.setCache(menu.getCache());
        existing.setHidden(menu.getHidden());
        existing.setStatus(menu.getStatus());
        existing.setRemark(menu.getRemark());

        existing.setUpdatedBy(operator);
        menuMapper.updateById(existing);
        log.info("菜单更新成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, String operator) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null || menu.getDeleted() == 1) {
            throw new BusinessException("菜单不存在");
        }

        // 检查子菜单
        long childCount = menuMapper.selectCount(
                new LambdaQueryWrapper<Menu>().eq(Menu::getParentId, id).eq(Menu::getDeleted, 0));
        if (childCount > 0) {
            throw new BusinessException("该菜单下还有" + childCount + "个子菜单，无法删除。请先删除子菜单或移动到其他位置。");
        }

        menu.setDeleted(1);
        menu.setUpdatedBy(operator);
        menuMapper.updateById(menu);
        log.info("菜单已删除: {}", id);
    }
}
