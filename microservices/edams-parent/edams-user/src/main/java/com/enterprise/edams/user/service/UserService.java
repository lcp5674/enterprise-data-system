package com.enterprise.edams.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.user.dto.*;
import com.enterprise.edams.user.entity.SysUser;

import java.util.List;

/**
 * 用户服务接口
 *
 * @author Backend Team
 * @version 1.0.0
 */
public interface UserService {

    /**
     * 创建用户
     */
    UserVO createUser(UserCreateRequest request);

    /**
     * 更新用户
     */
    UserVO updateUser(String userId, UserUpdateRequest request);

    /**
     * 删除用户
     */
    void deleteUser(String userId);

    /**
     * 根据ID查询用户
     */
    UserVO getUserById(String userId);

    /**
     * 根据用户名查询用户
     */
    UserVO getUserByUsername(String username);

    /**
     * 分页查询用户
     */
    Page<UserVO> pageUsers(String keyword, String departmentId, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 批量删除用户
     */
    int batchDeleteUsers(List<String> userIds);

    /**
     * 修改密码
     */
    void changePassword(String userId, String oldPassword, String newPassword);

    /**
     * 重置密码
     */
    void resetPassword(String userId, String newPassword);

    /**
     * 启用用户
     */
    void enableUser(String userId);

    /**
     * 禁用用户
     */
    void disableUser(String userId);

    /**
     * 分配角色
     */
    void assignRoles(String userId, List<String> roleIds);

    /**
     * 获取用户角色
     */
    List<String> getUserRoles(String userId);

    /**
     * 获取用户菜单
     */
    List<String> getUserMenus(String userId);

    /**
     * 获取用户权限
     */
    List<String> getUserPermissions(String userId);

    /**
     * 检查用户名是否存在
     */
    boolean checkUsernameExists(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean checkEmailExists(String email);

    /**
     * 检查手机号是否存在
     */
    boolean checkPhoneExists(String phone);
}
