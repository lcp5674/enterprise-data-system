package com.enterprise.edams.llm.service;

import com.enterprise.edams.llm.dto.LlmCostStatistics;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 成本分析服务接口
 */
public interface LlmCostService {

    /**
     * 获取用户成本统计
     */
    LlmCostStatistics getUserCostStatistics(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取租户成本统计
     */
    LlmCostStatistics getTenantCostStatistics(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 获取模型成本统计
     */
    LlmCostStatistics getModelCostStatistics(Long modelId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 查询成本趋势
     */
    List<LlmCostStatistics> getCostTrend(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 查询租户总成本
     */
    BigDecimal getTenantTotalCost(Long tenantId);

    /**
     * 查询模型成本排名
     */
    List<LlmCostStatistics> getModelCostRanking(Long tenantId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 每日成本汇总定时任务
     */
    void dailyCostAggregation();
}
