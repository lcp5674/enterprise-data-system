package com.enterprise.edams.permission.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.result.PageResult;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.permission.dto.*;
import com.enterprise.edams.permission.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色控制器
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "角色相关接口")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @Operation(summary = "创建角色", description = "创建新角色")
    public Result<RoleVO> createRole(@Valid @RequestBody RoleCreateRequest request) {
        RoleVO role = roleService.createRole(request);
        return Result.success(role);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色", description = "更新角色信息")
    public Result<RoleVO> updateRole(
            @Parameter(description = "角色ID") @PathVariable String id,
            @Valid @RequestBody RoleUpdateRequest request) {
        RoleVO role = roleService.updateRole(id, request);
        return Result.success(role);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色", description = "删除角色")
    public Result<Void> deleteRole(
            @Parameter(description = "角色ID") @PathVariable String id) {
        roleService.deleteRole(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取角色详情", description = "根据ID获取角色详情")
    public Result<RoleVO> getRoleById(
            @Parameter(description = "角色ID") @PathVariable String id) {
        RoleVO role = roleService.getRoleById(id);
        return Result.success(role);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "根据编码获取角色", description = "根据角色编码获取角色信息")
    public Result<RoleVO> getRoleByCode(
            @Parameter(description = "角色编码") @PathVariable String code) {
        RoleVO role = roleService.getRoleByCode(code);
        return Result.success(role);
    }

    @GetMapping
    @Operation(summary = "分页查询角色", description = "分页查询角色列表")
    public Result<PageResult<RoleVO>> pageRoles(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "角色类型") @RequestParam(required = false) String roleType,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int pageSize) {
        Page<RoleVO> page = roleService.pageRoles(keyword, roleType, status, pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有角色", description = "获取所有启用的角色列表")
    public Result<List<RoleVO>> listAllRoles() {
        List<RoleVO> roles = roleService.listAllRoles();
        return Result.success(roles);
    }

    @GetMapping("/check/code")
    @Operation(summary = "检查角色编码是否存在", description = "检查角色编码是否可用")
    public Result<Boolean> checkCodeExists(
            @Parameter(description = "角色编码") @RequestParam String code) {
        boolean exists = roleService.checkCodeExists(code);
        return Result.success(!exists);
    }

    @PutMapping("/{id}/permissions")
    @Operation(summary = "分配角色权限", description = "给角色分配权限")
    public Result<Void> assignPermissions(
            @Parameter(description = "角色ID") @PathVariable String id,
            @RequestBody List<String> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return Result.success();
    }

    @GetMapping("/{id}/permissions")
    @Operation(summary = "获取角色权限", description = "获取角色的权限列表")
    public Result<List<PermissionVO>> getRolePermissions(
            @Parameter(description = "角色ID") @PathVariable String id) {
        List<PermissionVO> permissions = roleService.getRolePermissions(id);
        return Result.success(permissions);
    }

    @PutMapping("/{id}/users")
    @Operation(summary = "分配角色用户", description = "给角色分配用户")
    public Result<Void> assignUsers(
            @Parameter(description = "角色ID") @PathVariable String id,
            @RequestBody List<String> userIds) {
        roleService.assignUsers(id, userIds);
        return Result.success();
    }

    @GetMapping("/{id}/user-count")
    @Operation(summary = "获取角色用户数量", description = "获取角色关联的用户数量")
    public Result<Long> getUserCount(
            @Parameter(description = "角色ID") @PathVariable String id) {
        long count = roleService.getUserCount(id);
        return Result.success(count);
    }

    @PutMapping("/{id}/enable")
    @Operation(summary = "启用角色", description = "启用角色")
    public Result<Void> enableRole(
            @Parameter(description = "角色ID") @PathVariable String id) {
        roleService.enableRole(id);
        return Result.success();
    }

    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用角色", description = "禁用角色")
    public Result<Void> disableRole(
            @Parameter(description = "角色ID") @PathVariable String id) {
        roleService.disableRole(id);
        return Result.success();
    }
}
