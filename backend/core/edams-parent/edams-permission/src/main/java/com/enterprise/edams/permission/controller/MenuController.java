package com.enterprise.edams.permission.controller;

import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.permission.entity.Menu;
import com.enterprise.edams.permission.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
@Tag(name = "菜单管理", description = "前端菜单/路由配置CRUD接口")
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/tree")
    @Operation(summary = "获取菜单树", description="返回树形结构菜单，用于前端渲染导航栏")
    public Result<List<Menu>> getMenuTree() {
        return Result.success(menuService.getMenuTree());
    }

    @GetMapping
    @Operation(summary = "获取所有启用的菜单（扁平列表）")
    public Result<List<Menu>> getAllEnabled() {
        return Result.success(menuService.getAllEnabled());
    }

    @GetMapping("/role/{roleId}")
    @Operation(summary = "根据角色ID获取菜单列表", description="用于登录后动态加载用户可访问的菜单")
    public Result<List<Menu>> getByRoleId(@PathVariable Long roleId) {
        return Result.success(menuService.getByRoleId(roleId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取菜单详情")
    public Result<Menu> getById(@PathVariable Long id) {
        return Result.success(menuService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建菜单")
    public Result<Menu> create(@Valid @RequestBody Menu menu) {
        Menu created = menuService.create(menu, "system");
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新菜单信息")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody Menu menu) {
        menuService.update(id, menu, "system");
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜单")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id, "system");
        return Result.success();
    }
}
