package com.enterprise.edams.llm.controller;

import com.enterprise.edams.llm.dto.*;
import com.enterprise.edams.llm.service.LlmCostService;
import com.enterprise.edams.llm.service.LlmInvokeService;
import com.enterprise.edams.llm.service.LlmUsageService;
import com.enterprise.edams.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * LLM调用控制器
 */
@Tag(name = "LLM调用", description = "大模型调用和成本分析")
@RestController
@RequestMapping("/api/llm")
@RequiredArgsConstructor
public class LlmInvokeController {

    private final LlmInvokeService invokeService;
    private final LlmUsageService usageService;
    private final LlmCostService costService;

    @PostMapping("/invoke")
    @Operation(summary = "调用大模型")
    public Result<LlmInvokeResponse> invoke(@RequestBody LlmInvokeRequest request) {
        LlmInvokeResponse response = invokeService.invoke(request);
        return Result.success(response);
    }

    @PostMapping("/invoke/batch")
    @Operation(summary = "批量调用大模型")
    public Result<LlmInvokeResponse[]> batchInvoke(@RequestBody LlmInvokeRequest[] requests) {
        LlmInvokeResponse[] responses = invokeService.batchInvoke(requests);
        return Result.success(responses);
    }

    @GetMapping("/estimate")
    @Operation(summary = "估算调用成本")
    public Result<BigDecimal> estimateCost(@RequestBody LlmInvokeRequest request) {
        BigDecimal cost = invokeService.estimateCost(request);
        return Result.success(cost);
    }

    @GetMapping("/user/{userId}/daily-tokens")
    @Operation(summary = "获取用户日使用量")
    public Result<Long> getUserDailyTokens(@PathVariable Long userId) {
        Long tokens = usageService.getUserDailyTokens(userId);
        return Result.success(tokens);
    }

    @GetMapping("/user/{userId}/daily-cost")
    @Operation(summary = "获取用户日费用")
    public Result<BigDecimal> getUserDailyCost(@PathVariable Long userId) {
        BigDecimal cost = usageService.getUserDailyCost(userId);
        return Result.success(cost);
    }

    @GetMapping("/failures")
    @Operation(summary = "获取最近的失败调用")
    public Result<List<LlmUsageLogDTO>> getRecentFailures(
            @RequestParam(defaultValue = "10") @Parameter(description = "返回数量") int limit) {
        List<LlmUsageLogDTO> failures = usageService.getRecentFailures(limit);
        return Result.success(failures);
    }

    @GetMapping("/statistics/user/{userId}")
    @Operation(summary = "获取用户成本统计")
    public Result<LlmCostStatistics> getUserCostStatistics(
            @PathVariable @Parameter(description = "用户ID") Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        LlmCostStatistics statistics = costService.getUserCostStatistics(userId, startDate, endDate);
        return Result.success(statistics);
    }

    @GetMapping("/statistics/tenant/{tenantId}")
    @Operation(summary = "获取租户成本统计")
    public Result<LlmCostStatistics> getTenantCostStatistics(
            @PathVariable @Parameter(description = "租户ID") Long tenantId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        LlmCostStatistics statistics = costService.getTenantCostStatistics(tenantId, startDate, endDate);
        return Result.success(statistics);
    }

    @GetMapping("/statistics/tenant/{tenantId}/trend")
    @Operation(summary = "获取租户成本趋势")
    public Result<List<LlmCostStatistics>> getCostTrend(
            @PathVariable @Parameter(description = "租户ID") Long tenantId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        List<LlmCostStatistics> trend = costService.getCostTrend(tenantId, startDate, endDate);
        return Result.success(trend);
    }

    @GetMapping("/statistics/tenant/{tenantId}/total-cost")
    @Operation(summary = "获取租户总成本")
    public Result<BigDecimal> getTenantTotalCost(@PathVariable Long tenantId) {
        BigDecimal totalCost = costService.getTenantTotalCost(tenantId);
        return Result.success(totalCost);
    }

    @GetMapping("/statistics/tenant/{tenantId}/ranking")
    @Operation(summary = "获取模型成本排名")
    public Result<List<LlmCostStatistics>> getModelCostRanking(
            @PathVariable @Parameter(description = "租户ID") Long tenantId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {
        List<LlmCostStatistics> ranking = costService.getModelCostRanking(tenantId, startDate, endDate);
        return Result.success(ranking);
    }
}
