package com.enterprise.edams.llm.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.llm.entity.LlmUsageLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 使用日志Mapper
 */
@Mapper
public interface LlmUsageLogMapper extends BaseMapper<LlmUsageLog> {

    /**
     * 分页查询用户使用日志
     */
    @Select("SELECT * FROM llm_usage_log WHERE user_id = #{userId} ORDER BY request_time DESC")
    IPage<LlmUsageLog> selectPageByUser(Page<LlmUsageLog> page, @Param("userId") Long userId);

    /**
     * 分页查询租户使用日志
     */
    @Select("SELECT * FROM llm_usage_log WHERE tenant_id = #{tenantId} ORDER BY request_time DESC")
    IPage<LlmUsageLog> selectPageByTenant(Page<LlmUsageLog> page, @Param("tenantId") Long tenantId);

    /**
     * 查询模型的日使用统计
     */
    @Select("SELECT SUM(total_tokens) as totalTokens, SUM(total_cost) as totalCost, COUNT(*) as count " +
           "FROM llm_usage_log WHERE model_code = #{modelCode} AND DATE(request_time) = DATE(#{date})")
    LlmUsageLog selectDailyStats(@Param("modelCode") String modelCode, @Param("date") LocalDateTime date);

    /**
     * 查询用户日使用量
     */
    @Select("SELECT COALESCE(SUM(total_tokens), 0) FROM llm_usage_log " +
           "WHERE user_id = #{userId} AND DATE(request_time) = CURDATE()")
    Long sumUserDailyTokens(@Param("userId") Long userId);

    /**
     * 查询用户日费用
     */
    @Select("SELECT COALESCE(SUM(total_cost), 0) FROM llm_usage_log " +
           "WHERE user_id = #{userId} AND DATE(request_time) = CURDATE()")
    java.math.BigDecimal sumUserDailyCost(@Param("userId") Long userId);

    /**
     * 查询最近的失败日志
     */
    @Select("SELECT * FROM llm_usage_log WHERE status = 'FAILED' ORDER BY request_time DESC LIMIT #{limit}")
    List<LlmUsageLog> selectRecentFailures(@Param("limit") int limit);
}
