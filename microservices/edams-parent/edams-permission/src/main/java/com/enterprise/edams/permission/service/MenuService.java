package com.enterprise.edams.permission.service;

import com.enterprise.edams.permission.dto.MenuVO;

import java.util.List;

/**
 * 菜单服务接口
 *
 * @author Backend Team
 * @version 1.0.0
 */
public interface MenuService {

    /**
     * 获取用户菜单树
     */
    List<MenuVO> getUserMenus(String userId);

    /**
     * 根据应用获取菜单树
     */
    List<MenuVO> getMenusByApplication(String application);

    /**
     * 获取所有菜单列表
     */
    List<MenuVO> listAllMenus();

    /**
     * 根据角色ID获取菜单
     */
    List<MenuVO> getMenusByRoleId(String roleId);

    /**
     * 分配角色菜单
     */
    void assignMenus(String roleId, List<String> menuIds);
}
