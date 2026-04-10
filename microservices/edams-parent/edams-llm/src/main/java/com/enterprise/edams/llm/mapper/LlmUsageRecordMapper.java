package com.enterprise.edams.llm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.llm.entity.LlmUsageRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * LLM使用记录Mapper接口
 *
 * @author LLM Team
 * @version 1.0.0
 */
@Mapper
public interface LlmUsageRecordMapper extends BaseMapper<LlmUsageRecord> {

    /**
     * 查询用户在时间范围内的使用记录
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 使用记录列表
     */
    List<LlmUsageRecord> selectByUserAndTimeRange(@Param("userId") String userId,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 查询租户在时间范围内的使用统计
     *
     * @param tenantId 租户ID
     * @param providerId 提供商ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 使用记录列表
     */
    List<LlmUsageRecord> selectByTenantAndProviderAndTimeRange(@Param("tenantId") String tenantId,
                                                                 @Param("providerId") String providerId,
                                                                 @Param("startTime") LocalDateTime startTime,
                                                                 @Param("endTime") LocalDateTime endTime);
}
