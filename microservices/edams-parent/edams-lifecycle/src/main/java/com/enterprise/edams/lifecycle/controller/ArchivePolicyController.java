package com.enterprise.edams.lifecycle.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.lifecycle.dto.ArchivePolicyCreateRequest;
import com.enterprise.edams.lifecycle.dto.ArchivePolicyDTO;
import com.enterprise.edams.lifecycle.entity.ArchivePolicy;
import com.enterprise.edams.lifecycle.service.ArchivePolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 归档策略控制器
 *
 * @author EDAMS Team
 */
@RestController
@RequestMapping("/api/v1/archive-policies")
@RequiredArgsConstructor
@Tag(name = "归档策略管理", description = "归档策略相关接口")
public class ArchivePolicyController {

    private final ArchivePolicyService archivePolicyService;

    @PostMapping
    @Operation(summary = "创建归档策略")
    public Result<ArchivePolicyDTO> createPolicy(@Valid @RequestBody ArchivePolicyCreateRequest request) {
        return Result.success(archivePolicyService.createPolicy(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新归档策略")
    public Result<ArchivePolicyDTO> updatePolicy(
            @PathVariable String id,
            @Valid @RequestBody ArchivePolicyCreateRequest request) {
        return Result.success(archivePolicyService.updatePolicy(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除归档策略")
    public Result<Void> deletePolicy(@PathVariable String id) {
        archivePolicyService.deletePolicy(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取归档策略详情")
    public Result<ArchivePolicyDTO> getPolicy(@PathVariable String id) {
        return Result.success(archivePolicyService.getPolicy(id));
    }

    @GetMapping
    @Operation(summary = "分页查询归档策略")
    public Result<Page<ArchivePolicyDTO>> listPolicies(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) Boolean enabled) {
        Page<ArchivePolicy> page = new Page<>(current, size);
        return Result.success(archivePolicyService.listPolicies(page, keyword, businessType, enabled));
    }

    @PostMapping("/{id}/enable")
    @Operation(summary = "启用归档策略")
    public Result<Void> enablePolicy(@PathVariable String id) {
        archivePolicyService.enablePolicy(id);
        return Result.success();
    }

    @PostMapping("/{id}/disable")
    @Operation(summary = "停用归档策略")
    public Result<Void> disablePolicy(@PathVariable String id) {
        archivePolicyService.disablePolicy(id);
        return Result.success();
    }

    @PostMapping("/{id}/execute")
    @Operation(summary = "执行归档策略")
    public Result<Void> executePolicy(@PathVariable String id) {
        archivePolicyService.executePolicy(id);
        return Result.success();
    }

    @GetMapping("/enabled")
    @Operation(summary = "获取启用的策略列表")
    public Result<List<ArchivePolicyDTO>> getEnabledPolicies() {
        return Result.success(archivePolicyService.getEnabledPolicies());
    }
}
