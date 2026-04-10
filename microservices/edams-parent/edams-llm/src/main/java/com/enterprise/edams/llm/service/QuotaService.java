package com.enterprise.edams.llm.service;

import com.enterprise.edams.llm.dto.QuotaDTO;
import com.enterprise.edams.llm.entity.LlmQuota;

import java.math.BigDecimal;
import java.util.List;

/**
 * 配额服务接口
 *
 * @author LLM Team
 * @version 1.0.0
 */
public interface QuotaService {

    /**
     * 检查配额是否充足
     *
     * @param userId 用户ID
     * @param providerId 提供商ID
     * @param estimatedTokens 预估Token数
     * @return 是否充足
     */
    boolean checkQuota(String userId, String providerId, int estimatedTokens);

    /**
     * 消耗配额
     *
     * @param userId 用户ID
     * @param providerId 提供商ID
     * @param inputTokens 输入Token数
     * @param outputTokens 输出Token数
     * @param cost 费用
     */
    void consumeQuota(String userId, String providerId, int inputTokens, int outputTokens, BigDecimal cost);

    /**
     * 获取用户配额
     *
     * @param userId 用户ID
     * @param providerId 提供商ID
     * @return 配额信息
     */
    QuotaDTO getQuota(String userId, String providerId);

    /**
     * 获取用户所有配额
     *
     * @param userId 用户ID
     * @return 配额列表
     */
    List<QuotaDTO> getAllQuotas(String userId);

    /**
     * 创建配额
     *
     * @param quota 配额信息
     * @return 创建的配额
     */
    LlmQuota createQuota(LlmQuota quota);

    /**
     * 更新配额
     *
     * @param quotaId 配额ID
     * @param quota 配额信息
     * @return 更新后的配额
     */
    LlmQuota updateQuota(String quotaId, LlmQuota quota);

    /**
     * 重置配额
     *
     * @param quotaId 配额ID
     */
    void resetQuota(String quotaId);

    /**
     * 获取租户今日使用统计
     *
     * @param tenantId 租户ID
     * @param providerId 提供商ID
     * @return 今日使用统计
     */
    QuotaDTO.TodayUsage getTodayUsage(String tenantId, String providerId);

    /**
     * 获取用户今日使用统计
     *
     * @param userId 用户ID
     * @param providerId 提供商ID
     * @return 今日使用统计
     */
    QuotaDTO.TodayUsage getUserTodayUsage(String userId, String providerId);
}
