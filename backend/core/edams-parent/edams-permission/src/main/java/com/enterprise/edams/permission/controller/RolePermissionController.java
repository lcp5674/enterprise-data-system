package com.enterprise.edams.permission.controller;

import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.permission.service.RolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色-权限绑定控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/role-permissions")
@RequiredArgsConstructor
@Tag(name = "角色权限管理", description = "角色与权限的分配/解绑接口")
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    @GetMapping("/role/{roleId}")
    @Operation(summary = "获取角色的权限ID列表")
    public Result<List<Long>> getPermissionIdsByRoleId(@PathVariable Long roleId) {
        return Result.success(rolePermissionService.getPermissionIdsByRoleId(roleId));
    }

    @GetMapping("/role/{roleId}/codes")
    @Operation(summary = "获取角色的权限编码列表", description="用于前端路由权限守卫")
    public Result<List<String>> getPermissionCodes(@PathVariable Long roleId) {
        return Result.success(rolePermissionService.getPermissionCodes(roleId));
    }

    @PutMapping("/role/{roleId}")
    @Operation(summary = "为角色分配权限", description="批量设置角色拥有的权限（全量替换）")
    public Result<Void> assignPermissions(
            @PathVariable Long roleId,
            @RequestBody List<Long> permissionIds) {
        rolePermissionService.assignPermissions(roleId, permissionIds, "system");
        return Result.success();
    }
}
