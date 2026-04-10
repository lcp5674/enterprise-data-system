package com.enterprise.edams.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.result.PageResult;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.user.dto.*;
import com.enterprise.edams.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "创建用户", description = "创建新用户")
    public Result<UserVO> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserVO user = userService.createUser(request);
        return Result.success(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "更新用户信息")
    public Result<UserVO> updateUser(
            @Parameter(description = "用户ID") @PathVariable String id,
            @Valid @RequestBody UserUpdateRequest request) {
        UserVO user = userService.updateUser(id, request);
        return Result.success(user);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "删除用户")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID") @PathVariable String id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情", description = "根据ID获取用户详情")
    public Result<UserVO> getUserById(
            @Parameter(description = "用户ID") @PathVariable String id) {
        UserVO user = userService.getUserById(id);
        return Result.success(user);
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "根据用户名获取用户", description = "根据用户名获取用户信息")
    public Result<UserVO> getUserByUsername(
            @Parameter(description = "用户名") @PathVariable String username) {
        UserVO user = userService.getUserByUsername(username);
        return Result.success(user);
    }

    @GetMapping
    @Operation(summary = "分页查询用户", description = "分页查询用户列表")
    public Result<PageResult<UserVO>> pageUsers(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "部门ID") @RequestParam(required = false) String departmentId,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int pageSize) {
        Page<UserVO> page = userService.pageUsers(keyword, departmentId, status, pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除用户", description = "批量删除用户")
    public Result<Integer> batchDeleteUsers(@RequestBody List<String> userIds) {
        int count = userService.batchDeleteUsers(userIds);
        return Result.success(count);
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "修改密码", description = "用户修改自己的密码")
    public Result<Void> changePassword(
            @Parameter(description = "用户ID") @PathVariable String id,
            @RequestBody PasswordChangeRequest request) {
        userService.changePassword(id, request.getOldPassword(), request.getNewPassword());
        return Result.success();
    }

    @PutMapping("/{id}/reset-password")
    @Operation(summary = "重置密码", description = "管理员重置用户密码")
    public Result<Void> resetPassword(
            @Parameter(description = "用户ID") @PathVariable String id,
            @RequestBody PasswordResetRequest request) {
        userService.resetPassword(id, request.getNewPassword());
        return Result.success();
    }

    @PutMapping("/{id}/enable")
    @Operation(summary = "启用用户", description = "启用用户账号")
    public Result<Void> enableUser(
            @Parameter(description = "用户ID") @PathVariable String id) {
        userService.enableUser(id);
        return Result.success();
    }

    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用用户", description = "禁用用户账号")
    public Result<Void> disableUser(
            @Parameter(description = "用户ID") @PathVariable String id) {
        userService.disableUser(id);
        return Result.success();
    }

    @PutMapping("/{id}/roles")
    @Operation(summary = "分配角色", description = "给用户分配角色")
    public Result<Void> assignRoles(
            @Parameter(description = "用户ID") @PathVariable String id,
            @RequestBody List<String> roleIds) {
        userService.assignRoles(id, roleIds);
        return Result.success();
    }

    @GetMapping("/{id}/roles")
    @Operation(summary = "获取用户角色", description = "获取用户的角色列表")
    public Result<List<String>> getUserRoles(
            @Parameter(description = "用户ID") @PathVariable String id) {
        List<String> roles = userService.getUserRoles(id);
        return Result.success(roles);
    }

    @GetMapping("/{id}/menus")
    @Operation(summary = "获取用户菜单", description = "获取用户的菜单权限")
    public Result<List<String>> getUserMenus(
            @Parameter(description = "用户ID") @PathVariable String id) {
        List<String> menus = userService.getUserMenus(id);
        return Result.success(menus);
    }

    @GetMapping("/{id}/permissions")
    @Operation(summary = "获取用户权限", description = "获取用户的权限列表")
    public Result<List<String>> getUserPermissions(
            @Parameter(description = "用户ID") @PathVariable String id) {
        List<String> permissions = userService.getUserPermissions(id);
        return Result.success(permissions);
    }

    @GetMapping("/check/username")
    @Operation(summary = "检查用户名是否存在", description = "检查用户名是否可用")
    public Result<Boolean> checkUsername(
            @Parameter(description = "用户名") @RequestParam String username) {
        boolean exists = userService.checkUsernameExists(username);
        return Result.success(!exists);
    }

    @GetMapping("/check/email")
    @Operation(summary = "检查邮箱是否存在", description = "检查邮箱是否可用")
    public Result<Boolean> checkEmail(
            @Parameter(description = "邮箱") @RequestParam String email) {
        boolean exists = userService.checkEmailExists(email);
        return Result.success(!exists);
    }

    // ========== DTO内部类 ==========

    @lombok.Data
    public static class PasswordChangeRequest {
        @jakarta.validation.constraints.NotBlank(message = "原密码不能为空")
        private String oldPassword;
        @jakarta.validation.constraints.NotBlank(message = "新密码不能为空")
        @jakarta.validation.constraints.Size(min = 8, max = 100, message = "密码长度必须在8-100个字符之间")
        private String newPassword;
    }

    @lombok.Data
    public static class PasswordResetRequest {
        @jakarta.validation.constraints.NotBlank(message = "新密码不能为空")
        private String newPassword;
    }
}
