package com.enterprise.edams.lifecycle.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.lifecycle.entity.LifecyclePolicy;
import com.enterprise.edams.lifecycle.service.LifecyclePolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 生命周期策略控制器
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/lifecycle/policy")
@RequiredArgsConstructor
@Tag(name = "生命周期策略管理", description = "生命周期策略配置管理接口")
public class LifecyclePolicyController {

    private final LifecyclePolicyService lifecyclePolicyService;

    @PostMapping
    @Operation(summary = "创建生命周期策略", description = "创建新的生命周期策略")
    public Result<LifecyclePolicy> createPolicy(@RequestBody LifecyclePolicy policy) {
        LifecyclePolicy created = lifecyclePolicyService.createPolicy(policy);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新生命周期策略", description = "更新指定的生命周期策略")
    public Result<LifecyclePolicy> updatePolicy(@PathVariable Long id, @RequestBody LifecyclePolicy policy) {
        LifecyclePolicy updated = lifecyclePolicyService.updatePolicy(id, policy);
        return Result.success(updated);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取生命周期策略", description = "根据ID获取生命周期策略")
    public Result<LifecyclePolicy> getPolicy(@PathVariable Long id) {
        LifecyclePolicy policy = lifecyclePolicyService.getPolicy(id);
        return Result.success(policy);
    }

    @GetMapping("/code/{policyCode}")
    @Operation(summary = "根据编码获取生命周期策略", description = "根据策略编码获取生命周期策略")
    public Result<LifecyclePolicy> getPolicyByCode(@PathVariable String policyCode) {
        LifecyclePolicy policy = lifecyclePolicyService.getPolicyByCode(policyCode);
        return Result.success(policy);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除生命周期策略", description = "删除指定的生命周期策略")
    public Result<Void> deletePolicy(@PathVariable Long id) {
        lifecyclePolicyService.deletePolicy(id);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询生命周期策略", description = "分页查询所有生命周期策略")
    public Result<IPage<LifecyclePolicy>> listPolicies(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<LifecyclePolicy> page = lifecyclePolicyService.listPolicies(pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/assetType/{assetType}")
    @Operation(summary = "根据数据资产类型查询策略", description = "根据数据资产类型查询适用的生命周期策略")
    public Result<IPage<LifecyclePolicy>> listPoliciesByAssetType(
            @PathVariable String assetType,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "20") @RequestParam(defaultValue = "20") Integer pageSize) {
        IPage<LifecyclePolicy> page = lifecyclePolicyService.listPoliciesByAssetType(assetType, pageNum, pageSize);
        return Result.success(page);
    }

    @PostMapping("/{id}/enable")
    @Operation(summary = "启用生命周期策略", description = "启用指定的生命周期策略")
    public Result<LifecyclePolicy> enablePolicy(@PathVariable Long id) {
        LifecyclePolicy policy = lifecyclePolicyService.enablePolicy(id);
        return Result.success(policy);
    }

    @PostMapping("/{id}/disable")
    @Operation(summary = "禁用生命周期策略", description = "禁用指定的生命周期策略")
    public Result<LifecyclePolicy> disablePolicy(@PathVariable Long id) {
        LifecyclePolicy policy = lifecyclePolicyService.disablePolicy(id);
        return Result.success(policy);
    }

    @GetMapping("/applicable/{assetType}")
    @Operation(summary = "获取适用的生命周期策略", description = "根据数据资产类型获取适用的生命周期策略")
    public Result<LifecyclePolicy> getApplicablePolicy(@PathVariable String assetType) {
        LifecyclePolicy policy = lifecyclePolicyService.getApplicablePolicy(assetType);
        return Result.success(policy);
    }
}