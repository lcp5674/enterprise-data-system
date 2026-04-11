package com.enterprise.edams.user.controller;

import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.user.entity.Department;
import com.enterprise.edams.user.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
@Tag(name = "部门管理", description = "部门树形结构CRUD接口")
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping("/tree")
    @Operation(summary = "获取部门树", description = "返回树形结构的部门列表（用于前端树形组件）")
    public Result<List<Department>> getDepartmentTree() {
        return Result.success(departmentService.getDepartmentTree());
    }

    @GetMapping
    @Operation(summary = "获取所有部门", description = "返回扁平化的部门列表")
    public Result<List<Department>> getAllDepartments() {
        return Result.success(departmentService.getAllDepartments());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取部门详情")
    public Result<Department> getById(@PathVariable Long id) {
        return Result.success(departmentService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建部门")
    public Result<Department> create(@Valid @RequestBody Department department) {
        Department created = departmentService.create(department, "system");
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新部门信息")
    public Result<Void> update(@PathVariable Long id,
                               @Valid @RequestBody Department department) {
        departmentService.update(id, department, "system");
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门", description = "逻辑删除，需先确保无子部门和关联用户")
    public Result<Void> delete(@PathVariable Long id) {
        departmentService.delete(id, "system");
        return Result.success();
    }

    @GetMapping("/{parentId}/children")
    @Operation(summary = "获取子部门列表")
    public Result<List<Department>> getChildren(@PathVariable Long parentId) {
        return Result.success(departmentService.getChildren(parentId));
    }
}
