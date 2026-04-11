package com.enterprise.edams.llm.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.edams.llm.dto.LlmUsageLogDTO;
import com.enterprise.edams.llm.entity.LlmUsageLog;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 使用日志服务接口
 */
public interface LlmUsageService extends IService<LlmUsageLog> {

    /**
     * 分页查询用户日志
     */
    Page<LlmUsageLog> selectUserPage(Long userId, int pageNum, int pageSize);

    /**
     * 分页查询租户日志
     */
    Page<LlmUsageLog> selectTenantPage(Long tenantId, int pageNum, int pageSize);

    /**
     * 根据ID查询
     */
    LlmUsageLogDTO getById(Long id);

    /**
     * 记录使用日志
     */
    LlmUsageLogDTO recordUsage(LlmUsageLogDTO dto);

    /**
     * 查询日使用统计
     */
    LlmUsageLog getDailyStats(String modelCode, LocalDateTime date);

    /**
     * 查询用户日使用量
     */
    Long getUserDailyTokens(Long userId);

    /**
     * 查询用户日费用
     */
    BigDecimal getUserDailyCost(Long userId);

    /**
     * 查询最近的失败日志
     */
    List<LlmUsageLogDTO> getRecentFailures(int limit);

    /**
     * 清理过期日志
     */
    void cleanExpiredLogs(int retentionDays);
}
