package com.enterprise.edams.aiops.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.aiops.entity.MonitorMetric;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 监控服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface MonitorService {

    /**
     * 创建监控指标
     */
    MonitorMetric createMetric(MonitorMetric metric);

    /**
     * 更新监控指标
     */
    MonitorMetric updateMetric(MonitorMetric metric);

    /**
     * 删除监控指标
     */
    void deleteMetric(Long id);

    /**
     * 根据ID查询指标
     */
    MonitorMetric getMetricById(Long id);

    /**
     * 分页查询指标
     */
    Page<MonitorMetric> pageMetrics(int pageNum, int pageSize, String targetId, String metricType, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询最新指标
     */
    MonitorMetric getLatestMetric(String targetId, String metricName);

    /**
     * 查询时间范围内指标
     */
    List<MonitorMetric> getMetricsByTimeRange(String targetId, String metricName, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 按类型统计指标
     */
    List<Map<String, Object>> countByMetricType();

    /**
     * 批量创建指标
     */
    void batchCreateMetrics(List<MonitorMetric> metrics);

    /**
     * 清理过期指标数据
     */
    int cleanExpiredMetrics(int retentionDays);
}
