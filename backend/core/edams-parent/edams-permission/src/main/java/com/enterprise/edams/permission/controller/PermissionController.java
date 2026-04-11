package com.enterprise.edams.permission.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.common.result.PageResult;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.permission.entity.Permission;
import com.enterprise.edams.permission.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
@Tag(name = "权限管理", description = "RBAC权限CRUD接口")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @Operation(summary = "分页查询权限", description = "支持关键词搜索、类型过滤、状态过滤")
    public PageResult<Permission> queryPermissions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        IPage<Permission> page = permissionService.queryPermissions(keyword, type, status, pageNum, pageSize);
        return PageResult.success(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @GetMapping("/tree")
    @Operation(summary = "获取权限树", description = "返回树形结构的权限列表")
    public Result<List<Permission>> getPermissionTree() {
        return Result.success(permissionService.getPermissionTree());
    }

    @GetMapping("/enabled")
    @Operation(summary = "获取所有启用的权限", description="用于分配权限时的选择列表")
    public Result<List<Permission>> getEnabled() {
        return Result.success(permissionService.getEnabledPermissions());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取权限详情")
    public Result<Permission> getById(@PathVariable Long id) {
        return Result.success(permissionService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建权限")
    public Result<Permission> create(@Valid @RequestBody Permission permission) {
        Permission created = permissionService.create(permission, "system");
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新权限信息")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody Permission permission) {
        permissionService.update(id, permission, "system");
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除权限")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.delete(id, "system");
        return Result.success();
    }
}
