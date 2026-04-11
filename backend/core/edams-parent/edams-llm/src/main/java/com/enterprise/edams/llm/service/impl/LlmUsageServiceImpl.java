package com.enterprise.edams.llm.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.edams.llm.dto.LlmUsageLogDTO;
import com.enterprise.edams.llm.entity.LlmUsageLog;
import com.enterprise.edams.llm.repository.LlmUsageLogMapper;
import com.enterprise.edams.llm.service.LlmUsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 使用日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmUsageServiceImpl extends ServiceImpl<LlmUsageLogMapper, LlmUsageLog> implements LlmUsageService {

    private final LlmUsageLogMapper usageLogMapper;

    @Override
    public Page<LlmUsageLog> selectUserPage(Long userId, int pageNum, int pageSize) {
        Page<LlmUsageLog> page = new Page<>(pageNum, pageSize);
        return usageLogMapper.selectPageByUser(page, userId);
    }

    @Override
    public Page<LlmUsageLog> selectTenantPage(Long tenantId, int pageNum, int pageSize) {
        Page<LlmUsageLog> page = new Page<>(pageNum, pageSize);
        return usageLogMapper.selectPageByTenant(page, tenantId);
    }

    @Override
    public LlmUsageLogDTO getById(Long id) {
        LlmUsageLog log = baseMapper.selectById(id);
        if (log == null) {
            return null;
        }
        return convertToDTO(log);
    }

    @Override
    @Transactional
    public LlmUsageLogDTO recordUsage(LlmUsageLogDTO dto) {
        LlmUsageLog usageLog = new LlmUsageLog();
        BeanUtils.copyProperties(dto, usageLog);
        usageLog.setRequestTime(LocalDateTime.now());
        baseMapper.insert(usageLog);
        log.info("Recorded usage: requestId={}, tokens={}, cost={}", 
                dto.getRequestId(), dto.getTotalTokens(), dto.getTotalCost());
        return convertToDTO(usageLog);
    }

    @Override
    public LlmUsageLog getDailyStats(String modelCode, LocalDateTime date) {
        return usageLogMapper.selectDailyStats(modelCode, date);
    }

    @Override
    public Long getUserDailyTokens(Long userId) {
        return usageLogMapper.sumUserDailyTokens(userId);
    }

    @Override
    public BigDecimal getUserDailyCost(Long userId) {
        return usageLogMapper.sumUserDailyCost(userId);
    }

    @Override
    public List<LlmUsageLogDTO> getRecentFailures(int limit) {
        List<LlmUsageLog> failures = usageLogMapper.selectRecentFailures(limit);
        return failures.stream().map(this::convertToDTO).toList();
    }

    @Override
    @Transactional
    public void cleanExpiredLogs(int retentionDays) {
        log.info("Cleaning usage logs older than {} days", retentionDays);
        // 实现清理逻辑
    }

    private LlmUsageLogDTO convertToDTO(LlmUsageLog usageLog) {
        LlmUsageLogDTO dto = new LlmUsageLogDTO();
        BeanUtils.copyProperties(usageLog, dto);
        return dto;
    }
}
