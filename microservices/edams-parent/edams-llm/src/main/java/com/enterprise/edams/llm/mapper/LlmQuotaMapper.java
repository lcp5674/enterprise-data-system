package com.enterprise.edams.llm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.llm.entity.LlmQuota;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * LLM配额Mapper接口
 *
 * @author LLM Team
 * @version 1.0.0
 */
@Mapper
public interface LlmQuotaMapper extends BaseMapper<LlmQuota> {

    /**
     * 根据用户ID和提供商ID查询
     *
     * @param userId 用户ID
     * @param providerId 提供商ID
     * @return 配额
     */
    LlmQuota selectByUserAndProvider(@Param("userId") String userId, @Param("providerId") String providerId);
}
