package com.enterprise.edams.aiops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.aiops.entity.MonitorMetric;
import com.enterprise.edams.aiops.repository.MonitorMetricMapper;
import com.enterprise.edams.aiops.service.MonitorService;
import com.enterprise.edams.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 监控服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorServiceImpl implements MonitorService {

    private final MonitorMetricMapper monitorMetricMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MonitorMetric createMetric(MonitorMetric metric) {
        if (metric.getCollectTime() == null) {
            metric.setCollectTime(LocalDateTime.now());
        }
        metric.setTenantId(metric.getTenantId() != null ? metric.getTenantId() : 1L);
        monitorMetricMapper.insert(metric);
        log.info("创建监控指标: {}", metric.getMetricName());
        return metric;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MonitorMetric updateMetric(MonitorMetric metric) {
        MonitorMetric existing = monitorMetricMapper.selectById(metric.getId());
        if (existing == null) {
            throw new BusinessException("指标不存在");
        }
        monitorMetricMapper.updateById(metric);
        log.info("更新监控指标: {}", metric.getId());
        return metric;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMetric(Long id) {
        monitorMetricMapper.deleteById(id);
        log.info("删除监控指标: {}", id);
    }

    @Override
    public MonitorMetric getMetricById(Long id) {
        return monitorMetricMapper.selectById(id);
    }

    @Override
    public Page<MonitorMetric> pageMetrics(int pageNum, int pageSize, String targetId, String metricType, LocalDateTime startTime, LocalDateTime endTime) {
        Page<MonitorMetric> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MonitorMetric> wrapper = new LambdaQueryWrapper<>();
        
        if (targetId != null && !targetId.isEmpty()) {
            wrapper.eq(MonitorMetric::getTargetId, targetId);
        }
        if (metricType != null && !metricType.isEmpty()) {
            wrapper.eq(MonitorMetric::getMetricType, metricType);
        }
        if (startTime != null) {
            wrapper.ge(MonitorMetric::getCollectTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(MonitorMetric::getCollectTime, endTime);
        }
        
        wrapper.orderByDesc(MonitorMetric::getCollectTime);
        return monitorMetricMapper.selectPage(page, wrapper);
    }

    @Override
    public MonitorMetric getLatestMetric(String targetId, String metricName) {
        return monitorMetricMapper.findLatestByTargetId(targetId);
    }

    @Override
    public List<MonitorMetric> getMetricsByTimeRange(String targetId, String metricName, LocalDateTime startTime, LocalDateTime endTime) {
        return monitorMetricMapper.findByTimeRange(targetId, metricName, startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> countByMetricType() {
        return monitorMetricMapper.countByMetricType();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCreateMetrics(List<MonitorMetric> metrics) {
        for (MonitorMetric metric : metrics) {
            if (metric.getCollectTime() == null) {
                metric.setCollectTime(LocalDateTime.now());
            }
            metric.setTenantId(metric.getTenantId() != null ? metric.getTenantId() : 1L);
        }
        monitorMetricMapper.insertBatch(metrics);
        log.info("批量创建监控指标: {}条", metrics.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanExpiredMetrics(int retentionDays) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
        LambdaQueryWrapper<MonitorMetric> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(MonitorMetric::getCollectTime, cutoffTime);
        int count = monitorMetricMapper.delete(wrapper);
        log.info("清理过期监控指标: {}条", count);
        return count;
    }
}
