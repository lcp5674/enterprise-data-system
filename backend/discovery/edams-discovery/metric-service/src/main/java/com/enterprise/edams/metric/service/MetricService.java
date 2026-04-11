package com.enterprise.edams.metric.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.metric.entity.BusinessMetric;

import java.util.List;
import java.util.Map;

/**
 * 指标管理服务接口
 */
public interface MetricService {

    /**
     * 创建业务指标
     */
    Long createMetric(BusinessMetric metric);

    /**
     * 更新业务指标
     */
    boolean updateMetric(Long id, BusinessMetric metric);

    /**
     * 删除业务指标
     */
    boolean deleteMetric(Long id);

    /**
     * 获取指标详情
     */
    BusinessMetric getMetricById(Long id);

    /**
     * 获取指标详情
     */
    BusinessMetric getMetricByCode(String code);

    /**
     * 分页查询指标
     */
    IPage<BusinessMetric> listMetrics(String name, String domain, String status, int pageNum, int pageSize);

    /**
     * 获取指标血缘
     */
    List<Map<String, Object>> getMetricLineage(Long metricId);

    /**
     * 获取指标统计
     */
    Map<String, Object> getStatistics();
}
