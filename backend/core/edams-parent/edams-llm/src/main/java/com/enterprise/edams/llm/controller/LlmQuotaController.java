package com.enterprise.edams.llm.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.llm.dto.LlmQuotaDTO;
import com.enterprise.edams.llm.entity.LlmQuota;
import com.enterprise.edams.llm.service.LlmQuotaService;
import com.enterprise.edams.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 配额控制器
 */
@Tag(name = "配额管理", description = "配额的CRUD操作")
@RestController
@RequestMapping("/api/llm/quota")
@RequiredArgsConstructor
public class LlmQuotaController {

    private final LlmQuotaService quotaService;

    @GetMapping
    @Operation(summary = "分页查询配额")
    public Result<Page<LlmQuota>> page(
            @Parameter(description = "租户ID") @RequestParam(required = false) Long tenantId,
            @Parameter(description = "用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int pageSize) {
        Page<LlmQuota> page = quotaService.selectPage(tenantId, userId, pageNum, pageSize);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询配额")
    public Result<LlmQuotaDTO> getById(@PathVariable Long id) {
        LlmQuotaDTO quota = quotaService.getById(id);
        return Result.success(quota);
    }

    @GetMapping("/user/{userId}/model/{modelId}")
    @Operation(summary = "查询用户有效配额")
    public Result<LlmQuotaDTO> getActiveQuota(
            @PathVariable @Parameter(description = "用户ID") Long userId,
            @PathVariable @Parameter(description = "模型ID") Long modelId) {
        LlmQuotaDTO quota = quotaService.getActiveQuota(userId, modelId);
        return Result.success(quota);
    }

    @GetMapping("/tenant/{tenantId}/model/{modelId}")
    @Operation(summary = "查询租户配额")
    public Result<LlmQuotaDTO> getTenantQuota(
            @PathVariable @Parameter(description = "租户ID") Long tenantId,
            @PathVariable @Parameter(description = "模型ID") Long modelId) {
        LlmQuotaDTO quota = quotaService.getTenantQuota(tenantId, modelId);
        return Result.success(quota);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户的所有配额")
    public Result<List<LlmQuotaDTO>> getUserQuotas(@PathVariable Long userId) {
        List<LlmQuotaDTO> quotas = quotaService.getUserQuotas(userId);
        return Result.success(quotas);
    }

    @GetMapping("/check")
    @Operation(summary = "检查配额是否充足")
    public Result<Boolean> checkQuota(
            @RequestParam @Parameter(description = "用户ID") Long userId,
            @RequestParam @Parameter(description = "模型ID") Long modelId,
            @RequestParam @Parameter(description = "预估token数") int estimatedTokens) {
        boolean sufficient = quotaService.checkQuota(userId, modelId, estimatedTokens);
        return Result.success(sufficient);
    }

    @PostMapping
    @Operation(summary = "创建配额")
    public Result<LlmQuotaDTO> create(@RequestBody LlmQuotaDTO dto) {
        LlmQuotaDTO created = quotaService.create(dto);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新配额")
    public Result<LlmQuotaDTO> update(@PathVariable Long id, @RequestBody LlmQuotaDTO dto) {
        LlmQuotaDTO updated = quotaService.update(id, dto);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除配额")
    public Result<Void> delete(@PathVariable Long id) {
        quotaService.delete(id);
        return Result.success();
    }

    @PostMapping("/reset-expired")
    @Operation(summary = "重置过期配额")
    public Result<Void> resetExpired() {
        quotaService.resetExpiredQuotas();
        return Result.success();
    }
}
