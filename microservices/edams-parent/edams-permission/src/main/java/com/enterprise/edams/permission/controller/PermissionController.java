package com.enterprise.edams.permission.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.result.PageResult;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.permission.dto.PermissionCreateRequest;
import com.enterprise.edams.permission.dto.PermissionVO;
import com.enterprise.edams.permission.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限控制器
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Tag(name = "权限管理", description = "权限相关接口")
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @Operation(summary = "创建权限", description = "创建新权限")
    public Result<PermissionVO> createPermission(@Valid @RequestBody PermissionCreateRequest request) {
        PermissionVO permission = permissionService.createPermission(request);
        return Result.success(permission);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新权限", description = "更新权限信息")
    public Result<PermissionVO> updatePermission(
            @Parameter(description = "权限ID") @PathVariable String id,
            @Valid @RequestBody PermissionCreateRequest request) {
        PermissionVO permission = permissionService.updatePermission(id, request);
        return Result.success(permission);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除权限", description = "删除权限")
    public Result<Void> deletePermission(
            @Parameter(description = "权限ID") @PathVariable String id) {
        permissionService.deletePermission(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取权限详情", description = "根据ID获取权限详情")
    public Result<PermissionVO> getPermissionById(
            @Parameter(description = "权限ID") @PathVariable String id) {
        PermissionVO permission = permissionService.getPermissionById(id);
        return Result.success(permission);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "根据编码获取权限", description = "根据权限编码获取权限信息")
    public Result<PermissionVO> getPermissionByCode(
            @Parameter(description = "权限编码") @PathVariable String code) {
        PermissionVO permission = permissionService.getPermissionByCode(code);
        return Result.success(permission);
    }

    @GetMapping
    @Operation(summary = "分页查询权限", description = "分页查询权限列表")
    public Result<PageResult<PermissionVO>> pagePermissions(
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "权限类型") @RequestParam(required = false) String permissionType,
            @Parameter(description = "模块") @RequestParam(required = false) String module,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int pageSize) {
        Page<PermissionVO> page = permissionService.pagePermissions(keyword, permissionType, module, pageNum, pageSize);
        return Result.success(PageResult.of(page));
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有权限", description = "获取所有权限列表")
    public Result<List<PermissionVO>> listAllPermissions() {
        List<PermissionVO> permissions = permissionService.listAllPermissions();
        return Result.success(permissions);
    }

    @GetMapping("/tree")
    @Operation(summary = "获取权限树", description = "获取权限树形结构")
    public Result<List<PermissionVO>> getPermissionTree() {
        List<PermissionVO> tree = permissionService.getPermissionTree();
        return Result.success(tree);
    }

    @GetMapping("/module/{module}")
    @Operation(summary = "根据模块获取权限", description = "根据模块获取权限列表")
    public Result<List<PermissionVO>> listByModule(
            @Parameter(description = "模块名称") @PathVariable String module) {
        List<PermissionVO> permissions = permissionService.listPermissionsByModule(module);
        return Result.success(permissions);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "根据类型获取权限", description = "根据权限类型获取权限列表")
    public Result<List<PermissionVO>> listByType(
            @Parameter(description = "权限类型") @PathVariable String type) {
        List<PermissionVO> permissions = permissionService.listPermissionsByType(type);
        return Result.success(permissions);
    }

    @GetMapping("/check/code")
    @Operation(summary = "检查权限编码是否存在", description = "检查权限编码是否可用")
    public Result<Boolean> checkCodeExists(
            @Parameter(description = "权限编码") @RequestParam String code) {
        boolean exists = permissionService.checkCodeExists(code);
        return Result.success(!exists);
    }

    @GetMapping("/role/{roleId}")
    @Operation(summary = "获取角色的权限", description = "获取指定角色的权限列表")
    public Result<List<PermissionVO>> getByRole(
            @Parameter(description = "角色ID") @PathVariable String roleId) {
        List<PermissionVO> permissions = permissionService.getPermissionsByRoleId(roleId);
        return Result.success(permissions);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户的权限", description = "获取指定用户的所有权限")
    public Result<List<PermissionVO>> getUserPermissions(
            @Parameter(description = "用户ID") @PathVariable String userId) {
        List<PermissionVO> permissions = permissionService.getUserPermissions(userId);
        return Result.success(permissions);
    }

    @GetMapping("/user/{userId}/codes")
    @Operation(summary = "获取用户的权限编码", description = "获取用户的所有权限编码")
    public Result<List<String>> getUserPermissionCodes(
            @Parameter(description = "用户ID") @PathVariable String userId) {
        List<String> codes = permissionService.getUserPermissionCodes(userId);
        return Result.success(codes);
    }

    @GetMapping("/user/{userId}/check/{code}")
    @Operation(summary = "检查用户权限", description = "检查用户是否具有指定权限")
    public Result<Boolean> checkUserPermission(
            @Parameter(description = "用户ID") @PathVariable String userId,
            @Parameter(description = "权限编码") @PathVariable String code) {
        boolean hasPermission = permissionService.hasPermission(userId, code);
        return Result.success(hasPermission);
    }
}
