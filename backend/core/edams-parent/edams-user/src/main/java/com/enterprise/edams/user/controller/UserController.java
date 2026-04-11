package com.enterprise.edams.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.common.result.PageResult;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.user.dto.UserCreateRequest;
import com.enterprise.edams.user.dto.UserUpdateRequest;
import com.enterprise.edams.user.dto.UserVO;
import com.enterprise.edams.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户CRUD、状态管理、密码重置等接口")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "分页查询用户", description = "支持关键词搜索、部门筛选、状态过滤")
    public PageResult<UserVO> queryUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        IPage<UserVO> page = userService.queryUsers(keyword, departmentId, status, pageNum, pageSize);
        return PageResult.success(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @PostMapping
    @Operation(summary = "创建用户", description = "管理员创建新用户账号")
    public Result<UserVO> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserVO user = userService.createUser(request, "system");
        return Result.success(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息")
    public Result<Void> updateUser(@PathVariable Long id,
                                  @Valid @RequestBody UserUpdateRequest request) {
        userService.updateUser(id, request, "system");
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "逻辑删除用户（不物理删除）")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id, "system");
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "修改用户状态", description = "启用或禁用用户账号")
    public Result<Void> changeStatus(
            @PathVariable Long id,
            @RequestParam Integer status) {
        userService.changeStatus(id, status, "system");
        return Result.success();
    }

    @PutMapping("/{id}/password/reset")
    @Operation(summary = "重置用户密码", description = "管理员强制重置用户密码")
    public Result<Void> resetPassword(
            @PathVariable Long id,
            @RequestParam String newPassword) {
        if (newPassword == null || newPassword.length() < 8 || newPassword.length() > 100) {
            return Result.fail("密码长度必须在8-100个字符之间");
        }
        userService.resetPassword(id, newPassword, "system");
        return Result.success();
    }

    @GetMapping("/department/{deptId}")
    @Operation(summary = "获取部门下的所有用户")
    public Result<List<UserVO>> getUsersByDepartment(@PathVariable("deptId") Long deptId) {
        List<UserVO> users = userService.getUsersByDepartment(deptId);
        return Result.success(users);
    }
}
