package com.enterprise.edams.user.controller;

import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.user.dto.*;
import com.enterprise.edams.user.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门控制器
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
@Tag(name = "部门管理", description = "部门相关接口")
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @Operation(summary = "创建部门", description = "创建新部门")
    public Result<DepartmentVO> createDepartment(@Valid @RequestBody DepartmentCreateRequest request) {
        DepartmentVO department = departmentService.createDepartment(request);
        return Result.success(department);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新部门", description = "更新部门信息")
    public Result<DepartmentVO> updateDepartment(
            @Parameter(description = "部门ID") @PathVariable String id,
            @Valid @RequestBody DepartmentCreateRequest request) {
        DepartmentVO department = departmentService.updateDepartment(id, request);
        return Result.success(department);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门", description = "删除部门")
    public Result<Void> deleteDepartment(
            @Parameter(description = "部门ID") @PathVariable String id) {
        departmentService.deleteDepartment(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取部门详情", description = "根据ID获取部门详情")
    public Result<DepartmentVO> getDepartmentById(
            @Parameter(description = "部门ID") @PathVariable String id) {
        DepartmentVO department = departmentService.getDepartmentById(id);
        return Result.success(department);
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "根据编码获取部门", description = "根据编码获取部门")
    public Result<DepartmentVO> getDepartmentByCode(
            @Parameter(description = "部门编码") @PathVariable String code) {
        DepartmentVO department = departmentService.getDepartmentByCode(code);
        return Result.success(department);
    }

    @GetMapping("/tree")
    @Operation(summary = "获取部门树", description = "获取所有部门树形结构")
    public Result<List<DepartmentVO>> getDepartmentTree() {
        List<DepartmentVO> tree = departmentService.getDepartmentTree();
        return Result.success(tree);
    }

    @GetMapping("/user/{userId}/tree")
    @Operation(summary = "获取用户所属部门树", description = "获取用户所属部门的树形结构")
    public Result<DepartmentVO> getUserDepartmentTree(
            @Parameter(description = "用户ID") @PathVariable String userId) {
        DepartmentVO tree = departmentService.getUserDepartmentTree(userId);
        return Result.success(tree);
    }

    @GetMapping("/{parentId}/children")
    @Operation(summary = "获取子部门", description = "获取指定部门的子部门列表")
    public Result<List<DepartmentVO>> getChildDepartments(
            @Parameter(description = "上级部门ID") @PathVariable String parentId) {
        List<DepartmentVO> children = departmentService.getChildDepartments(parentId);
        return Result.success(children);
    }

    @PutMapping("/{id}/move")
    @Operation(summary = "移动部门", description = "将部门移动到新的上级部门")
    public Result<Void> moveDepartment(
            @Parameter(description = "部门ID") @PathVariable String id,
            @RequestBody DepartmentMoveRequest request) {
        departmentService.moveDepartment(id, request.getNewParentId());
        return Result.success();
    }

    @PutMapping("/{id}/enable")
    @Operation(summary = "启用部门", description = "启用部门")
    public Result<Void> enableDepartment(
            @Parameter(description = "部门ID") @PathVariable String id) {
        departmentService.enableDepartment(id);
        return Result.success();
    }

    @PutMapping("/{id}/disable")
    @Operation(summary = "禁用部门", description = "禁用部门")
    public Result<Void> disableDepartment(
            @Parameter(description = "部门ID") @PathVariable String id) {
        departmentService.disableDepartment(id);
        return Result.success();
    }

    @GetMapping("/{id}/user-count")
    @Operation(summary = "获取部门用户数", description = "获取部门的用户数量")
    public Result<Integer> getDepartmentUserCount(
            @Parameter(description = "部门ID") @PathVariable String id) {
        int count = departmentService.getDepartmentUserCount(id);
        return Result.success(count);
    }

    @GetMapping("/check/code")
    @Operation(summary = "检查部门编码是否存在", description = "检查部门编码是否可用")
    public Result<Boolean> checkCode(
            @Parameter(description = "部门编码") @RequestParam String code) {
        boolean exists = departmentService.checkCodeExists(code);
        return Result.success(!exists);
    }

    // ========== DTO内部类 ==========

    @lombok.Data
    public static class DepartmentMoveRequest {
        @jakarta.validation.constraints.NotBlank(message = "新上级部门ID不能为空")
        private String newParentId;
    }
}
