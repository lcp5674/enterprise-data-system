package com.enterprise.edams.llm.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.llm.entity.LlmQuota;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 配额Mapper
 */
@Mapper
public interface LlmQuotaMapper extends BaseMapper<LlmQuota> {

    /**
     * 查询用户的有效配额
     */
    @Select("SELECT * FROM llm_quota WHERE user_id = #{userId} AND model_id = #{modelId} " +
           "AND status = 'ACTIVE' AND enabled = true AND deleted = 0 " +
           "AND (period_end IS NULL OR period_end > NOW()) LIMIT 1")
    LlmQuota selectActiveQuota(@Param("userId") Long userId, @Param("modelId") Long modelId);

    /**
     * 查询租户的配额
     */
    @Select("SELECT * FROM llm_quota WHERE tenant_id = #{tenantId} AND user_id IS NULL " +
           "AND model_id = #{modelId} AND status = 'ACTIVE' AND enabled = true LIMIT 1")
    LlmQuota selectTenantQuota(@Param("tenantId") Long tenantId, @Param("modelId") Long modelId);

    /**
     * 查询所有需要重置的配额
     */
    @Select("SELECT * FROM llm_quota WHERE quota_type = #{quotaType} AND period_end < NOW() AND deleted = 0")
    java.util.List<LlmQuota> selectExpiredQuotas(@Param("quotaType") String quotaType);

    /**
     * 更新已使用量
     */
    @Update("UPDATE llm_quota SET quota_used = quota_used + #{tokens}, cost_used = cost_used + #{cost}, " +
            "request_used = request_used + 1, update_time = NOW() WHERE id = #{id}")
    int updateUsedQuota(@Param("id") Long id, @Param("tokens") Long tokens, @Param("cost") BigDecimal cost);
}
