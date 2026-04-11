package com.enterprise.edams.insight.service;

import com.enterprise.edams.insight.entity.AnomalyDetection;
import com.enterprise.edams.insight.entity.TrendPrediction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 智能洞察服务接口
 */
public interface InsightService {

    // ==================== 异常检测 ====================

    /**
     * 检测异常数据
     */
    List<AnomalyDetection> detectAnomalies(Long assetId, String detectionType);

    /**
     * 获取异常列表
     */
    List<AnomalyDetection> getAnomalyList(Long assetId, String status, String severity, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取异常详情
     */
    AnomalyDetection getAnomalyDetail(Long id);

    /**
     * 处理异常
     */
    boolean handleAnomaly(Long id, String status, String remark);

    // ==================== 趋势预测 ====================

    /**
     * 生成趋势预测
     */
    TrendPrediction generatePrediction(Long assetId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取预测列表
     */
    List<TrendPrediction> getPredictionList(Long assetId);

    /**
     * 获取预测详情
     */
    TrendPrediction getPredictionDetail(Long id);

    // ==================== 智能推荐 ====================

    /**
     * 获取数据推荐
     */
    List<Map<String, Object>> getRecommendations(Long assetId, String recommendationType);

    /**
     * 获取统计信息
     */
    Map<String, Object> getStatistics();
}
