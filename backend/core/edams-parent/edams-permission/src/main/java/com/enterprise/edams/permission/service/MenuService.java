package com.enterprise.edams.permission.service;

import com.enterprise.edams.permission.entity.Menu;
import java.util.List;

/**
 * 菜单服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface MenuService {

    /** 获取菜单树 */
    List<Menu> getMenuTree();

    /** 根据角色ID获取菜单列表 */
    List<Menu> getByRoleId(Long roleId);

    /** 获取所有启用的菜单（扁平） */
    List<Menu> getAllEnabled();

    /** 根据ID获取菜单详情 */
    Menu getById(Long id);

    /** 创建菜单 */
    Menu create(Menu menu, String operator);

    /** 更新菜单 */
    void update(Long id, Menu menu, String operator);

    /** 删除菜单 */
    void delete(Long id, String operator);
}
