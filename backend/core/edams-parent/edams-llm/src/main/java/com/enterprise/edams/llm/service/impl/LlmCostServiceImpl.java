package com.enterprise.edams.llm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.edams.llm.dto.LlmCostStatistics;
import com.enterprise.edams.llm.entity.LlmCostRecord;
import com.enterprise.edams.llm.entity.LlmUsageLog;
import com.enterprise.edams.llm.repository.LlmCostRecordMapper;
import com.enterprise.edams.llm.repository.LlmUsageLogMapper;
import com.enterprise.edams.llm.service.LlmCostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 成本分析服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmCostServiceImpl implements LlmCostService {

    private final LlmUsageLogMapper usageLogMapper;
    private final LlmCostRecordMapper costRecordMapper;

    @Override
    public LlmCostStatistics getUserCostStatistics(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        LlmCostStatistics stats = new LlmCostStatistics();
        stats.setUserId(userId);
        
        LambdaQueryWrapper<LlmUsageLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LlmUsageLog::getUserId, userId)
               .between(LlmUsageLog::getRequestTime, startDate, endDate);
        
        List<LlmUsageLog> logs = usageLogMapper.selectList(wrapper);
        calculateStatistics(stats, logs);
        
        return stats;
    }

    @Override
    public LlmCostStatistics getTenantCostStatistics(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        LlmCostStatistics stats = new LlmCostStatistics();
        stats.setTenantId(tenantId);
        
        LambdaQueryWrapper<LlmUsageLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LlmUsageLog::getTenantId, tenantId)
               .between(LlmUsageLog::getRequestTime, startDate, endDate);
        
        List<LlmUsageLog> logs = usageLogMapper.selectList(wrapper);
        calculateStatistics(stats, logs);
        
        return stats;
    }

    @Override
    public LlmCostStatistics getModelCostStatistics(Long modelId, LocalDateTime startDate, LocalDateTime endDate) {
        LlmCostStatistics stats = new LlmCostStatistics();
        stats.setModelId(modelId);
        
        LambdaQueryWrapper<LlmUsageLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LlmUsageLog::getModelId, modelId)
               .between(LlmUsageLog::getRequestTime, startDate, endDate);
        
        List<LlmUsageLog> logs = usageLogMapper.selectList(wrapper);
        calculateStatistics(stats, logs);
        
        return stats;
    }

    @Override
    public List<LlmCostStatistics> getCostTrend(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        LambdaQueryWrapper<LlmCostRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LlmCostRecord::getTenantId, tenantId)
              .between(LlmCostRecord::getStatDate, startDate, endDate)
              .orderByAsc(LlmCostRecord::getStatDate);
        
        List<LlmCostRecord> records = costRecordMapper.selectList(wrapper);
        
        return records.stream().map(record -> {
            LlmCostStatistics stats = new LlmCostStatistics();
            stats.setTenantId(record.getTenantId());
            stats.setModelCode(record.getModelCode());
            stats.setTotalTokens(record.getTotalTokens());
            stats.setInputTokens(record.getInputTokens());
            stats.setOutputTokens(record.getOutputTokens());
            stats.setTotalCost(record.getTotalCost());
            stats.setInputCost(record.getInputCost());
            stats.setOutputCost(record.getOutputCost());
            stats.setRequestCount(record.getRequestCount());
            return stats;
        }).toList();
    }

    @Override
    public BigDecimal getTenantTotalCost(Long tenantId) {
        return costRecordMapper.sumTenantTotalCost(tenantId);
    }

    @Override
    public List<LlmCostStatistics> getModelCostRanking(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        List<LlmCostRecord> rankings = costRecordMapper.selectModelCostRanking(tenantId, startDate, endDate);
        return rankings.stream().map(record -> {
            LlmCostStatistics stats = new LlmCostStatistics();
            stats.setModelCode(record.getModelCode());
            stats.setTotalCost(record.getTotalCost());
            return stats;
        }).toList();
    }

    @Override
    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点执行
    @Transactional
    public void dailyCostAggregation() {
        log.info("Starting daily cost aggregation");
        LocalDateTime yesterday = LocalDate.now().minusDays(1).atStartOfDay();
        // 聚合逻辑将在实际实现中添加
        log.info("Daily cost aggregation completed");
    }

    private void calculateStatistics(LlmCostStatistics stats, List<LlmUsageLog> logs) {
        long totalTokens = 0;
        long inputTokens = 0;
        long outputTokens = 0;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal inputCost = BigDecimal.ZERO;
        BigDecimal outputCost = BigDecimal.ZERO;
        long requestCount = 0;
        
        for (LlmUsageLog log : logs) {
            totalTokens += log.getTotalTokens();
            inputTokens += log.getInputTokens();
            outputTokens += log.getOutputTokens();
            totalCost = totalCost.add(log.getTotalCost());
            inputCost = inputCost.add(log.getInputCost());
            outputCost = outputCost.add(log.getOutputCost());
            requestCount++;
        }
        
        stats.setTotalTokens(totalTokens);
        stats.setInputTokens(inputTokens);
        stats.setOutputTokens(outputTokens);
        stats.setTotalCost(totalCost);
        stats.setInputCost(inputCost);
        stats.setOutputCost(outputCost);
        stats.setRequestCount(requestCount);
        
        if (requestCount > 0) {
            stats.setAvgCostPerRequest(totalCost.divide(BigDecimal.valueOf(requestCount), 4, java.math.RoundingMode.HALF_UP));
        }
        if (totalTokens > 0) {
            stats.setAvgCostPerToken(totalCost.divide(BigDecimal.valueOf(totalTokens), 6, java.math.RoundingMode.HALF_UP));
        }
    }
}
