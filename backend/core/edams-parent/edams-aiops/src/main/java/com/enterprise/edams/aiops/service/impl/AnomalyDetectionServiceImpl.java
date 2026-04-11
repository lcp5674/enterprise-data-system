package com.enterprise.edams.aiops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.aiops.entity.AnomalyRecord;
import com.enterprise.edams.aiops.entity.MonitorMetric;
import com.enterprise.edams.aiops.repository.AnomalyRecordMapper;
import com.enterprise.edams.aiops.repository.MonitorMetricMapper;
import com.enterprise.edams.aiops.service.AnomalyDetectionService;
import com.enterprise.edams.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 异常检测服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnomalyDetectionServiceImpl implements AnomalyDetectionService {

    private final AnomalyRecordMapper anomalyRecordMapper;
    private final MonitorMetricMapper monitorMetricMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnomalyRecord detectAnomaly(Long metricId) {
        MonitorMetric metric = monitorMetricMapper.selectById(metricId);
        if (metric == null) {
            throw new BusinessException("指标不存在");
        }
        
        // 简单异常检测：基于历史平均值判断
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<MonitorMetric> historyMetrics = monitorMetricMapper.findByTimeRange(
                metric.getTargetId(), metric.getMetricName(), oneHourAgo, LocalDateTime.now());
        
        if (historyMetrics.size() < 5) {
            log.debug("历史数据不足，无法进行异常检测: {}", metricId);
            return null;
        }
        
        BigDecimal avgValue = historyMetrics.stream()
                .map(MonitorMetric::getMetricValue)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(historyMetrics.size()), 4, RoundingMode.HALF_UP);
        
        BigDecimal stdDev = calculateStdDev(historyMetrics, avgValue);
        BigDecimal deviation = metric.getMetricValue().subtract(avgValue).abs();
        
        // 超过2倍标准差认为异常
        if (stdDev.compareTo(BigDecimal.ZERO) > 0 && deviation.compareTo(stdDev.multiply(BigDecimal.valueOf(2))) > 0) {
            BigDecimal deviationPercent = avgValue.compareTo(BigDecimal.ZERO) != 0
                    ? deviation.divide(avgValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                    : BigDecimal.ZERO;
            
            String severity = deviationPercent.compareTo(BigDecimal.valueOf(50)) > 0 ? "high" 
                    : deviationPercent.compareTo(BigDecimal.valueOf(30)) > 0 ? "medium" : "low";
            
            AnomalyRecord anomaly = AnomalyRecord.builder()
                    .anomalyType("outlier")
                    .severity(severity)
                    .metricId(metricId)
                    .metricName(metric.getMetricName())
                    .targetId(metric.getTargetId())
                    .targetName(metric.getTargetName())
                    .description("检测到指标异常偏离")
                    .detectTime(LocalDateTime.now())
                    .startTime(metric.getCollectTime())
                    .anomalyValue(metric.getMetricValue())
                    .expectedValue(avgValue)
                    .deviationPercent(deviationPercent)
                    .algorithm("statistical_stddev")
                    .confidence(BigDecimal.valueOf(0.85))
                    .status("detected")
                    .tenantId(metric.getTenantId())
                    .build();
            
            anomalyRecordMapper.insert(anomaly);
            log.info("检测到异常: metric={}, deviation={}%", metric.getMetricName(), deviationPercent);
            return anomaly;
        }
        
        return null;
    }

    private BigDecimal calculateStdDev(List<MonitorMetric> metrics, BigDecimal avg) {
        BigDecimal sumSquaredDiff = metrics.stream()
                .map(m -> m.getMetricValue())
                .filter(v -> v != null)
                .map(v -> v.subtract(avg).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return BigDecimal.valueOf(Math.sqrt(sumSquaredDiff.divide(
                BigDecimal.valueOf(metrics.size()), 4, RoundingMode.HALF_UP).doubleValue()));
    }

    @Override
    public List<AnomalyRecord> batchDetectAnomalies(List<Long> metricIds) {
        List<AnomalyRecord> anomalies = new ArrayList<>();
        for (Long metricId : metricIds) {
            try {
                AnomalyRecord anomaly = detectAnomaly(metricId);
                if (anomaly != null) {
                    anomalies.add(anomaly);
                }
            } catch (Exception e) {
                log.error("批量检测异常时出错: metricId={}", metricId, e);
            }
        }
        return anomalies;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnomalyRecord updateAnomalyRecord(AnomalyRecord record) {
        anomalyRecordMapper.updateById(record);
        log.info("更新异常记录: {}", record.getId());
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAnomalyRecord(Long id) {
        anomalyRecordMapper.deleteById(id);
        log.info("删除异常记录: {}", id);
    }

    @Override
    public AnomalyRecord getAnomalyById(Long id) {
        return anomalyRecordMapper.selectById(id);
    }

    @Override
    public Page<AnomalyRecord> pageAnomalies(int pageNum, int pageSize, String severity, String status, String targetId, LocalDateTime startTime, LocalDateTime endTime) {
        Page<AnomalyRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AnomalyRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (severity != null && !severity.isEmpty()) {
            wrapper.eq(AnomalyRecord::getSeverity, severity);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(AnomalyRecord::getStatus, status);
        }
        if (targetId != null && !targetId.isEmpty()) {
            wrapper.eq(AnomalyRecord::getTargetId, targetId);
        }
        if (startTime != null) {
            wrapper.ge(AnomalyRecord::getDetectTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(AnomalyRecord::getDetectTime, endTime);
        }
        
        wrapper.orderByDesc(AnomalyRecord::getDetectTime);
        return anomalyRecordMapper.selectPage(page, wrapper);
    }

    @Override
    public List<AnomalyRecord> getActiveAnomalies() {
        return anomalyRecordMapper.findActiveAnomalies();
    }

    @Override
    public List<AnomalyRecord> getOngoingAnomalies() {
        return anomalyRecordMapper.findOngoingAnomalies();
    }

    @Override
    public String analyzeAnomaly(Long anomalyId) {
        AnomalyRecord anomaly = anomalyRecordMapper.selectById(anomalyId);
        if (anomaly == null) {
            throw new BusinessException("异常记录不存在");
        }
        
        // 简化分析：基于异常类型和严重程度提供建议
        String analysis = String.format("检测到[%s]类型的异常，严重程度[%s]，当前值[%s]，期望值[%s]，偏差[%s%%]",
                anomaly.getAnomalyType(),
                anomaly.getSeverity(),
                anomaly.getAnomalyValue(),
                anomaly.getExpectedValue(),
                anomaly.getDeviationPercent());
        
        // 更新分析结果
        anomaly.setAnalysisResult(analysis);
        anomalyRecordMapper.updateById(anomaly);
        
        return analysis;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolveAnomaly(Long id, String resolution) {
        AnomalyRecord anomaly = new AnomalyRecord();
        anomaly.setId(id);
        anomaly.setStatus("resolved");
        anomaly.setEndTime(LocalDateTime.now());
        anomaly.setAnalysisResult(resolution);
        anomalyRecordMapper.updateById(anomaly);
        log.info("解决异常: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ignoreAnomaly(Long id, String reason) {
        AnomalyRecord anomaly = new AnomalyRecord();
        anomaly.setId(id);
        anomaly.setStatus("ignored");
        anomaly.setEndTime(LocalDateTime.now());
        anomaly.setAnalysisResult("忽略原因: " + reason);
        anomalyRecordMapper.updateById(anomaly);
        log.info("忽略异常: {} - {}", id, reason);
    }

    @Override
    public List<Map<String, Object>> countByType() {
        return anomalyRecordMapper.countByType();
    }

    @Override
    public List<AnomalyRecord> getHighConfidenceAnomalies(double minConfidence) {
        return anomalyRecordMapper.findHighConfidenceAnomalies(BigDecimal.valueOf(minConfidence));
    }

    @Override
    @Scheduled(fixedDelayString = "${aiops.anomaly-detection.interval:300000}")
    public void scheduledAnomalyDetection() {
        log.debug("执行定时异常检测...");
        List<MonitorMetric> metrics = monitorMetricMapper.findForAlertEvaluation(100);
        batchDetectAnomalies(metrics.stream().map(MonitorMetric::getId).toList());
    }
}
