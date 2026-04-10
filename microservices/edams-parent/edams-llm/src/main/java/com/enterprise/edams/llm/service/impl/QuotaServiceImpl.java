package com.enterprise.edams.llm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.edams.llm.config.LlmConfig;
import com.enterprise.edams.llm.dto.QuotaDTO;
import com.enterprise.edams.llm.entity.LlmQuota;
import com.enterprise.edams.llm.entity.LlmUsageRecord;
import com.enterprise.edams.llm.mapper.LlmQuotaMapper;
import com.enterprise.edams.llm.mapper.LlmUsageRecordMapper;
import com.enterprise.edams.llm.service.QuotaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 配额服务实现类
 *
 * @author LLM Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QuotaServiceImpl implements QuotaService {

    private final LlmQuotaMapper quotaMapper;
    private final LlmUsageRecordMapper usageRecordMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LlmConfig llmConfig;
    private final ObjectMapper objectMapper;

    private static final String QUOTA_CACHE_PREFIX = "llm:quota:";
    private static final String USAGE_TODAY_PREFIX = "llm:usage:today:";

    @Override
    public boolean checkQuota(String userId, String providerId, int estimatedTokens) {
        log.debug("检查配额: userId={}, providerId={}, tokens={}", userId, providerId, estimatedTokens);

        // 首先检查Redis缓存
        String cacheKey = QUOTA_CACHE_PREFIX + userId + ":" + providerId;
        QuotaDTO cachedQuota = getCachedQuota(cacheKey);

        if (cachedQuota != null) {
            if (cachedQuota.getTokenRemaining() < estimatedTokens) {
                log.warn("配额不足: userId={}, remaining={}, required={}",
                        userId, cachedQuota.getTokenRemaining(), estimatedTokens);
                return false;
            }
            return true;
        }

        // 从数据库查询
        QuotaDTO quota = getQuota(userId, providerId);
        if (quota == null) {
            // 创建默认配额
            return createDefaultQuota(userId, providerId, estimatedTokens);
        }

        // 检查配额状态
        if (!"ACTIVE".equals(quota.getStatus())) {
            log.warn("配额状态异常: userId={}, status={}", userId, quota.getStatus());
            return false;
        }

        // 检查是否超限
        if (quota.getTokenRemaining() < estimatedTokens) {
            log.warn("Token配额不足: userId={}, remaining={}, required={}",
                    userId, quota.getTokenRemaining(), estimatedTokens);
            return false;
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consumeQuota(String userId, String providerId, int inputTokens, int outputTokens, BigDecimal cost) {
        log.debug("消耗配额: userId={}, providerId={}, input={}, output={}, cost={}",
                userId, providerId, inputTokens, outputTokens, cost);

        // 更新数据库
        LlmQuota quota = getQuotaEntity(userId, providerId);
        if (quota != null) {
            quota.setTokenUsed(quota.getTokenUsed() + inputTokens + outputTokens);
            quota.setRequestUsed(quota.getRequestUsed() + 1);
            quota.setCostUsed(quota.getCostUsed().add(cost));
            quotaMapper.updateById(quota);

            // 检查是否需要重置配额
            checkAndResetQuota(quota);
        }

        // 更新Redis缓存
        updateQuotaCache(userId, providerId, inputTokens + outputTokens, cost);

        // 记录使用
        recordUsage(userId, providerId, inputTokens, outputTokens, cost);
    }

    @Override
    public QuotaDTO getQuota(String userId, String providerId) {
        LlmQuota quota = getQuotaEntity(userId, providerId);
        if (quota == null) {
            return null;
        }

        return toQuotaDTO(quota);
    }

    @Override
    public List<QuotaDTO> getAllQuotas(String userId) {
        LambdaQueryWrapper<LlmQuota> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LlmQuota::getUserId, userId);

        return quotaMapper.selectList(wrapper).stream()
                .map(this::toQuotaDTO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LlmQuota createQuota(LlmQuota quota) {
        quota.setQuotaId(java.util.UUID.randomUUID().toString().replace("-", ""));
        quota.setTokenUsed(0L);
        quota.setRequestUsed(0L);
        quota.setCostUsed(BigDecimal.ZERO);
        quota.setStatus("ACTIVE");
        quota.setResetTime(calculateResetTime(quota.getQuotaType()));

        quotaMapper.insert(quota);
        return quota;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LlmQuota updateQuota(String quotaId, LlmQuota quota) {
        LlmQuota existing = quotaMapper.selectOne(
                new LambdaQueryWrapper<LlmQuota>().eq(LlmQuota::getQuotaId, quotaId));

        if (existing == null) {
            throw new RuntimeException("配额不存在: " + quotaId);
        }

        if (quota.getTokenLimit() != null) {
            existing.setTokenLimit(quota.getTokenLimit());
        }
        if (quota.getRequestLimit() != null) {
            existing.setRequestLimit(quota.getRequestLimit());
        }
        if (quota.getCostLimit() != null) {
            existing.setCostLimit(quota.getCostLimit());
        }
        if (quota.getStatus() != null) {
            existing.setStatus(quota.getStatus());
        }

        quotaMapper.updateById(existing);

        // 清除缓存
        clearQuotaCache(existing.getUserId(), existing.getProviderId());

        return existing;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetQuota(String quotaId) {
        LlmQuota quota = quotaMapper.selectOne(
                new LambdaQueryWrapper<LlmQuota>().eq(LlmQuota::getQuotaId, quotaId));

        if (quota == null) {
            throw new RuntimeException("配额不存在: " + quotaId);
        }

        quota.setTokenUsed(0L);
        quota.setRequestUsed(0L);
        quota.setCostUsed(BigDecimal.ZERO);
        quota.setResetTime(calculateResetTime(quota.getQuotaType()));

        quotaMapper.updateById(quota);

        // 清除缓存
        clearQuotaCache(quota.getUserId(), quota.getProviderId());

        log.info("配额已重置: quotaId={}", quotaId);
    }

    @Override
    public QuotaDTO.TodayUsage getTodayUsage(String tenantId, String providerId) {
        String cacheKey = USAGE_TODAY_PREFIX + tenantId + ":" + providerId + ":" + LocalDate.now();

        @SuppressWarnings("unchecked")
        QuotaDTO.TodayUsage cached = (QuotaDTO.TodayUsage) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 从数据库统计今日使用
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        LambdaQueryWrapper<LlmUsageRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LlmUsageRecord::getTenantId, tenantId)
                .eq(LlmUsageRecord::getProviderId, providerId)
                .between(LlmUsageRecord::getCreatedTime, startOfDay, endOfDay)
                .eq(LlmUsageRecord::getStatus, "SUCCESS");

        List<LlmUsageRecord> records = usageRecordMapper.selectList(wrapper);

        QuotaDTO.TodayUsage usage = QuotaDTO.TodayUsage.builder()
                .inputTokens(records.stream().mapToLong(r -> r.getInputTokens() != null ? r.getInputTokens() : 0).sum())
                .outputTokens(records.stream().mapToLong(r -> r.getOutputTokens() != null ? r.getOutputTokens() : 0).sum())
                .totalTokens(records.stream().mapToLong(r -> r.getTotalTokens() != null ? r.getTotalTokens() : 0).sum())
                .requestCount((long) records.size())
                .totalCost(records.stream().map(r -> r.getTotalCost() != null ? r.getTotalCost() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .lastUsedTime(records.isEmpty() ? null : records.get(records.size() - 1).getCreatedTime())
                .build();

        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, usage, Duration.ofMinutes(5));

        return usage;
    }

    @Override
    public QuotaDTO.TodayUsage getUserTodayUsage(String userId, String providerId) {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        LambdaQueryWrapper<LlmUsageRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LlmUsageRecord::getUserId, userId)
                .eq(LlmUsageRecord::getProviderId, providerId)
                .between(LlmUsageRecord::getCreatedTime, startOfDay, endOfDay)
                .eq(LlmUsageRecord::getStatus, "SUCCESS");

        List<LlmUsageRecord> records = usageRecordMapper.selectList(wrapper);

        return QuotaDTO.TodayUsage.builder()
                .inputTokens(records.stream().mapToLong(r -> r.getInputTokens() != null ? r.getInputTokens() : 0).sum())
                .outputTokens(records.stream().mapToLong(r -> r.getOutputTokens() != null ? r.getOutputTokens() : 0).sum())
                .totalTokens(records.stream().mapToLong(r -> r.getTotalTokens() != null ? r.getTotalTokens() : 0).sum())
                .requestCount((long) records.size())
                .totalCost(records.stream().map(r -> r.getTotalCost() != null ? r.getTotalCost() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .lastUsedTime(records.isEmpty() ? null : records.get(records.size() - 1).getCreatedTime())
                .build();
    }

    // ==================== 私有方法 ====================

    private LlmQuota getQuotaEntity(String userId, String providerId) {
        return quotaMapper.selectOne(
                new LambdaQueryWrapper<LlmQuota>()
                        .eq(LlmQuota::getUserId, userId)
                        .eq(LlmQuota::getProviderId, providerId)
                        .eq(LlmQuota::getStatus, "ACTIVE"));
    }

    private boolean createDefaultQuota(String userId, String providerId, int estimatedTokens) {
        // 检查是否有默认配额配置
        LlmConfig.DefaultQuotaConfig defaultQuota = llmConfig.getQuota();
        if (defaultQuota == null) {
            // 无配额限制
            return true;
        }

        LlmQuota quota = new LlmQuota();
        quota.setQuotaId(java.util.UUID.randomUUID().toString().replace("-", ""));
        quota.setUserId(userId);
        quota.setTenantId("default");
        quota.setProviderId(providerId);
        quota.setQuotaType("DAILY");
        quota.setTokenLimit((long) defaultQuota.getDefaultDailyTokens());
        quota.setTokenUsed(0L);
        quota.setRequestLimit((long) defaultQuota.getDefaultRpm());
        quota.setRequestUsed(0L);
        quota.setCostLimit(BigDecimal.valueOf(1000)); // 默认1000元限额
        quota.setCostUsed(BigDecimal.ZERO);
        quota.setStatus("ACTIVE");
        quota.setResetTime(LocalDateTime.now().plusDays(1).withHour(0).withMinute(0));
        quota.setCreator("system");

        quotaMapper.insert(quota);

        return quota.getTokenLimit() >= estimatedTokens;
    }

    private QuotaDTO toQuotaDTO(LlmQuota quota) {
        long tokenRemaining = quota.getTokenLimit() - quota.getTokenUsed();
        double tokenUsageRate = quota.getTokenLimit() > 0 ?
                (double) quota.getTokenUsed() / quota.getTokenLimit() * 100 : 0;

        long requestRemaining = quota.getRequestLimit() - quota.getRequestUsed();
        double requestUsageRate = quota.getRequestLimit() > 0 ?
                (double) quota.getRequestUsed() / quota.getRequestLimit() * 100 : 0;

        BigDecimal costRemaining = quota.getCostLimit().subtract(quota.getCostUsed());
        double costUsageRate = quota.getCostLimit().compareTo(BigDecimal.ZERO) > 0 ?
                quota.getCostUsed().divide(quota.getCostLimit(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue() : 0;

        return QuotaDTO.builder()
                .quotaId(quota.getQuotaId())
                .userId(quota.getUserId())
                .tenantId(quota.getTenantId())
                .providerId(quota.getProviderId())
                .quotaType(quota.getQuotaType())
                .tokenLimit(quota.getTokenLimit())
                .tokenUsed(quota.getTokenUsed())
                .tokenRemaining(tokenRemaining)
                .tokenUsageRate(tokenUsageRate)
                .requestLimit(quota.getRequestLimit())
                .requestUsed(quota.getRequestUsed())
                .requestRemaining(requestRemaining)
                .requestUsageRate(requestUsageRate)
                .resetTime(quota.getResetTime())
                .status(quota.getStatus())
                .costLimit(quota.getCostLimit())
                .costUsed(quota.getCostUsed())
                .costRemaining(costRemaining)
                .costUsageRate(costUsageRate)
                .build();
    }

    private QuotaDTO getCachedQuota(String cacheKey) {
        return objectMapper.convertValue(redisTemplate.opsForValue().get(cacheKey), QuotaDTO.class);
    }

    private void updateQuotaCache(String userId, String providerId, int tokens, BigDecimal cost) {
        String cacheKey = QUOTA_CACHE_PREFIX + userId + ":" + providerId;
        QuotaDTO quota = getCachedQuota(cacheKey);

        if (quota != null) {
            quota.setTokenUsed(quota.getTokenUsed() + tokens);
            quota.setTokenRemaining(quota.getTokenRemaining() - tokens);
            quota.setCostUsed(quota.getCostUsed().add(cost));
            quota.setCostRemaining(quota.getCostLimit().subtract(quota.getCostUsed()));

            redisTemplate.opsForValue().set(cacheKey, quota, Duration.ofHours(1));
        }
    }

    private void clearQuotaCache(String userId, String providerId) {
        String cacheKey = QUOTA_CACHE_PREFIX + userId + ":" + providerId;
        redisTemplate.delete(cacheKey);
    }

    private void checkAndResetQuota(LlmQuota quota) {
        if (quota.getResetTime() != null && LocalDateTime.now().isAfter(quota.getResetTime())) {
            quota.setTokenUsed(0L);
            quota.setRequestUsed(0L);
            quota.setCostUsed(BigDecimal.ZERO);
            quota.setResetTime(calculateResetTime(quota.getQuotaType()));
            quotaMapper.updateById(quota);

            log.info("配额已自动重置: quotaId={}", quota.getQuotaId());
        }
    }

    private LocalDateTime calculateResetTime(String quotaType) {
        return switch (quotaType) {
            case "DAILY" -> LocalDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0);
            case "MONTHLY" -> LocalDateTime.now().plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0);
            default -> LocalDateTime.now().plusDays(1).withHour(0).withMinute(0);
        };
    }

    private void recordUsage(String userId, String providerId, int inputTokens, int outputTokens, BigDecimal cost) {
        LlmUsageRecord record = new LlmUsageRecord();
        record.setRecordId(java.util.UUID.randomUUID().toString().replace("-", ""));
        record.setUserId(userId);
        record.setProviderId(providerId);
        record.setProviderName(providerId.toUpperCase());
        record.setInputTokens(inputTokens);
        record.setOutputTokens(outputTokens);
        record.setTotalTokens(inputTokens + outputTokens);
        record.setInputCost(cost.multiply(BigDecimal.valueOf(0.3)));
        record.setOutputCost(cost.multiply(BigDecimal.valueOf(0.7)));
        record.setTotalCost(cost);
        record.setRequestType("CHAT");
        record.setStatus("SUCCESS");
        record.setCreatedTime(LocalDateTime.now());

        usageRecordMapper.insert(record);
    }
}
