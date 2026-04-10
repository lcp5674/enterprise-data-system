package com.enterprise.edams.permission.controller;

import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.permission.dto.MenuVO;
import com.enterprise.edams.permission.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
@Tag(name = "菜单管理", description = "菜单相关接口")
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户菜单", description = "获取用户可访问的菜单树")
    public Result<List<MenuVO>> getUserMenus(
            @Parameter(description = "用户ID") @PathVariable String userId) {
        List<MenuVO> menus = menuService.getUserMenus(userId);
        return Result.success(menus);
    }

    @GetMapping("/application/{application}")
    @Operation(summary = "根据应用获取菜单", description = "根据应用类型获取菜单树")
    public Result<List<MenuVO>> getMenusByApplication(
            @Parameter(description = "应用类型: WEB, MOBILE") @PathVariable String application) {
        List<MenuVO> menus = menuService.getMenusByApplication(application);
        return Result.success(menus);
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有菜单", description = "获取所有菜单列表")
    public Result<List<MenuVO>> listAllMenus() {
        List<MenuVO> menus = menuService.listAllMenus();
        return Result.success(menus);
    }

    @GetMapping("/role/{roleId}")
    @Operation(summary = "获取角色菜单", description = "获取指定角色的菜单列表")
    public Result<List<MenuVO>> getMenusByRole(
            @Parameter(description = "角色ID") @PathVariable String roleId) {
        List<MenuVO> menus = menuService.getMenusByRoleId(roleId);
        return Result.success(menus);
    }

    @PutMapping("/role/{roleId}")
    @Operation(summary = "分配角色菜单", description = "给角色分配菜单")
    public Result<Void> assignMenus(
            @Parameter(description = "角色ID") @PathVariable String roleId,
            @RequestBody List<String> menuIds) {
        menuService.assignMenus(roleId, menuIds);
        return Result.success();
    }
}
