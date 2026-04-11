package com.enterprise.edams.llm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.edams.llm.dto.LlmQuotaDTO;
import com.enterprise.edams.llm.entity.LlmQuota;

import java.util.List;

/**
 * 配额服务接口
 */
public interface LlmQuotaService extends IService<LlmQuota> {

    /**
     * 分页查询配额
     */
    Page<LlmQuota> selectPage(Long tenantId, Long userId, int pageNum, int pageSize);

    /**
     * 根据ID查询
     */
    LlmQuotaDTO getById(Long id);

    /**
     * 创建配额
     */
    LlmQuotaDTO create(LlmQuotaDTO dto);

    /**
     * 更新配额
     */
    LlmQuotaDTO update(Long id, LlmQuotaDTO dto);

    /**
     * 删除配额
     */
    void delete(Long id);

    /**
     * 查询用户有效配额
     */
    LlmQuotaDTO getActiveQuota(Long userId, Long modelId);

    /**
     * 查询租户配额
     */
    LlmQuotaDTO getTenantQuota(Long tenantId, Long modelId);

    /**
     * 消耗配额
     */
    void consumeQuota(Long userId, Long modelId, Long tokens, java.math.BigDecimal cost);

    /**
     * 检查配额是否充足
     */
    boolean checkQuota(Long userId, Long modelId, int estimatedTokens);

    /**
     * 重置过期配额
     */
    void resetExpiredQuotas();

    /**
     * 查询用户的配额列表
     */
    List<LlmQuotaDTO> getUserQuotas(Long userId);
}
