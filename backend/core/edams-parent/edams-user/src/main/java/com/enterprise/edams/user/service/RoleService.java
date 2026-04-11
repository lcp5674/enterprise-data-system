package com.enterprise.edams.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.user.entity.Role;
import java.util.List;

/**
 * 角色服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface RoleService {

    /** 分页查询角色 */
    IPage<Role> queryRoles(String keyword, Integer status, int pageNum, int pageSize);

    /** 获取所有启用的角色（下拉列表用） */
    List<Role> getEnabledRoles();

    /** 根据ID获取角色 */
    Role getById(Long id);

    /** 创建角色 */
    Role create(Role role, String operator);

    /** 更新角色 */
    void update(Long id, Role role, String operator);

    /** 删除角色 */
    void delete(Long id, String operator);
}
