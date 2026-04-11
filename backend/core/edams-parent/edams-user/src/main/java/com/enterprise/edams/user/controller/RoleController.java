package com.enterprise.edams.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.common.result.PageResult;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.user.entity.Role;
import com.enterprise.edams.user.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "角色CRUD接口")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @Operation(summary = "分页查询角色", description = "支持关键词搜索和状态过滤")
    public PageResult<Role> queryRoles(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        IPage<Role> page = roleService.queryRoles(keyword, status, pageNum, pageSize);
        return PageResult.success(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @GetMapping("/enabled")
    @Operation(summary = "获取所有启用的角色", description = "用于下拉选择框等场景")
    public Result<List<Role>> getEnabledRoles() {
        return Result.success(roleService.getEnabledRoles());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取角色详情")
    public Result<Role> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建角色")
    public Result<Role> create(@Valid @RequestBody Role role) {
        Role created = roleService.create(role, "system");
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色信息")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody Role role) {
        roleService.update(id, role, "system");
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id, "system");
        return Result.success();
    }
}
