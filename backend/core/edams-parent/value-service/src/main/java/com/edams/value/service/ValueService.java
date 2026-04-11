package com.edams.value.service;

import com.edams.common.model.PageResult;
import com.edams.value.entity.DataValue;
import com.edams.value.entity.ValueMetric;

import java.util.Map;

public interface ValueService {
    
    // 数据价值评估
    DataValue assessDataValue(Long assetId, Long assessorId);
    DataValue getValueById(Long id);
    PageResult<DataValue> listValues(Map<String, Object> params);
    DataValue updateValue(Long id, DataValue value);
    void deleteValue(Long id);
    
    // 价值度量标准管理
    ValueMetric createMetric(ValueMetric metric);
    ValueMetric updateMetric(Long id, ValueMetric metric);
    void deleteMetric(Long id);
    ValueMetric getMetricById(Long id);
    PageResult<ValueMetric> listMetrics(Map<String, Object> params);
    
    // 价值分析
    Map<String, Object> analyzeAssetValue(Long assetId);
    Map<String, Object> analyzeTrend(Long assetId);
    Map<String, Object> compareValues(Long assetId1, Long assetId2);
    
    // 价值预测
    Map<String, Object> predictFutureValue(Long assetId);
    void calculateValueTrends();
    
    // 价值报告
    Map<String, Object> generateValueReport(Long assetId);
}