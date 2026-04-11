package com.enterprise.edams.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.user.dto.UserCreateRequest;
import com.enterprise.edams.user.dto.UserUpdateRequest;
import com.enterprise.edams.user.dto.UserVO;
import com.enterprise.edams.user.entity.User;
import java.util.List;

/**
 * 用户服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface UserService {

    /** 分页查询用户 */
    IPage<UserVO> queryUsers(String keyword, Long departmentId, Integer status, 
                              int pageNum, int pageSize);

    /** 根据ID获取用户详情 */
    UserVO getUserById(Long id);

    /** 创建用户 */
    UserVO createUser(UserCreateRequest request, String operator);

    /** 更新用户 */
    void updateUser(Long id, UserUpdateRequest request, String operator);

    /** 删除用户（逻辑删除） */
    void deleteUser(Long id, String operator);

    /** 重置密码 */
    void resetPassword(Long id, String newPassword, String operator);

    /** 修改状态（启用/禁用） */
    void changeStatus(Long id, Integer status, String operator);

    /** 获取部门下的所有用户 */
    List<UserVO> getUsersByDepartment(Long departmentId);
}
