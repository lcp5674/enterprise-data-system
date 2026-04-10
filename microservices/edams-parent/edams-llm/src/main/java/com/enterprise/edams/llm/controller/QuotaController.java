package com.enterprise.edams.llm.controller;

import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.llm.dto.QuotaDTO;
import com.enterprise.edams.llm.entity.LlmQuota;
import com.enterprise.edams.llm.service.QuotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 配额管理接口
 *
 * @author LLM Team
 * @version 1.0.0
 */
@Tag(name = "配额管理", description = "LLM配额查询和管理接口")
@RestController
@RequestMapping("/api/v1/llm/quota")
@RequiredArgsConstructor
public class QuotaController {

    private final QuotaService quotaService;

    @Operation(summary = "获取配额", description = "获取用户的LLM配额信息")
    @GetMapping
    public Result<QuotaDTO> getQuota(
            @Parameter(description = "用户ID") @RequestParam String userId,
            @Parameter(description = "提供商ID") @RequestParam(required = false) String providerId) {
        QuotaDTO quota;
        if (providerId != null) {
            quota = quotaService.getQuota(userId, providerId);
        } else {
            List<QuotaDTO> quotas = quotaService.getAllQuotas(userId);
            // 返回第一个或创建默认
            quota = quotas.isEmpty() ? null : quotas.get(0);
        }
        return Result.success(quota);
    }

    @Operation(summary = "获取所有配额", description = "获取用户的所有LLM配额列表")
    @GetMapping("/all")
    public Result<List<QuotaDTO>> getAllQuotas(
            @Parameter(description = "用户ID") @RequestParam String userId) {
        List<QuotaDTO> quotas = quotaService.getAllQuotas(userId);
        return Result.success(quotas);
    }

    @Operation(summary = "获取今日使用统计", description = "获取用户今日的LLM使用统计")
    @GetMapping("/today")
    public Result<QuotaDTO.TodayUsage> getTodayUsage(
            @Parameter(description = "用户ID") @RequestParam String userId,
            @Parameter(description = "提供商ID") @RequestParam(required = false) String providerId) {
        QuotaDTO.TodayUsage usage;
        if (providerId != null) {
            usage = quotaService.getUserTodayUsage(userId, providerId);
        } else {
            usage = quotaService.getUserTodayUsage(userId, "openai");
        }
        return Result.success(usage);
    }

    @Operation(summary = "获取租户今日使用统计", description = "获取租户今日的LLM使用统计")
    @GetMapping("/tenant/today")
    public Result<QuotaDTO.TodayUsage> getTenantTodayUsage(
            @Parameter(description = "租户ID") @RequestParam String tenantId,
            @Parameter(description = "提供商ID") @RequestParam(required = false) String providerId) {
        QuotaDTO.TodayUsage usage;
        if (providerId != null) {
            usage = quotaService.getTodayUsage(tenantId, providerId);
        } else {
            usage = quotaService.getTodayUsage(tenantId, "openai");
        }
        return Result.success(usage);
    }

    @Operation(summary = "创建配额", description = "创建新的LLM配额")
    @PostMapping
    public Result<LlmQuota> createQuota(@RequestBody LlmQuota quota) {
        LlmQuota created = quotaService.createQuota(quota);
        return Result.success("配额创建成功", created);
    }

    @Operation(summary = "更新配额", description = "更新指定的LLM配额")
    @PutMapping("/{quotaId}")
    public Result<LlmQuota> updateQuota(
            @Parameter(description = "配额ID") @PathVariable String quotaId,
            @RequestBody LlmQuota quota) {
        LlmQuota updated = quotaService.updateQuota(quotaId, quota);
        return Result.success("配额更新成功", updated);
    }

    @Operation(summary = "重置配额", description = "重置指定的LLM配额")
    @PostMapping("/{quotaId}/reset")
    public Result<Void> resetQuota(
            @Parameter(description = "配额ID") @PathVariable String quotaId) {
        quotaService.resetQuota(quotaId);
        return Result.success("配额重置成功", null);
    }

    @Operation(summary = "检查配额", description = "检查用户配额是否充足")
    @GetMapping("/check")
    public Result<Boolean> checkQuota(
            @Parameter(description = "用户ID") @RequestParam String userId,
            @Parameter(description = "提供商ID") @RequestParam String providerId,
            @Parameter(description = "预估Token数") @RequestParam int estimatedTokens) {
        boolean sufficient = quotaService.checkQuota(userId, providerId, estimatedTokens);
        return Result.success(sufficient);
    }
}
