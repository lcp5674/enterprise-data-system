package com.enterprise.edams.aiops.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.aiops.entity.AnomalyRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 异常检测服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface AnomalyDetectionService {

    /**
     * 检测异常
     */
    AnomalyRecord detectAnomaly(Long metricId);

    /**
     * 批量检测异常
     */
    List<AnomalyRecord> batchDetectAnomalies(List<Long> metricIds);

    /**
     * 更新异常记录
     */
    AnomalyRecord updateAnomalyRecord(AnomalyRecord record);

    /**
     * 删除异常记录
     */
    void deleteAnomalyRecord(Long id);

    /**
     * 根据ID查询异常
     */
    AnomalyRecord getAnomalyById(Long id);

    /**
     * 分页查询异常
     */
    Page<AnomalyRecord> pageAnomalies(int pageNum, int pageSize, String severity, String status, String targetId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询活跃异常
     */
    List<AnomalyRecord> getActiveAnomalies();

    /**
     * 查询持续中的异常
     */
    List<AnomalyRecord> getOngoingAnomalies();

    /**
     * 分析异常原因
     */
    String analyzeAnomaly(Long anomalyId);

    /**
     * 解决异常
     */
    void resolveAnomaly(Long id, String resolution);

    /**
     * 忽略异常
     */
    void ignoreAnomaly(Long id, String reason);

    /**
     * 按类型统计异常
     */
    List<Map<String, Object>> countByType();

    /**
     * 获取高置信度异常
     */
    List<AnomalyRecord> getHighConfidenceAnomalies(double minConfidence);

    /**
     * 定时检测异常
     */
    void scheduledAnomalyDetection();
}
