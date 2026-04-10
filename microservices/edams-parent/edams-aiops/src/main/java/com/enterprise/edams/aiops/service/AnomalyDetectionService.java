package com.enterprise.edams.aiops.service;

import com.enterprise.edams.aiops.dto.AnomalyDetectionRequest;
import com.enterprise.edams.aiops.model.AnomalyRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 智能异常检测服务
 * 
 * 功能:
 * - 基于时间序列数据分析
 * - 检测CPU、内存、磁盘、响应时间等指标的异常
 * - 使用统计方法(3σ原则、移动平均)检测异常
 * - 记录异常事件并告警
 *
 * @author AIOps Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnomalyDetectionService {

    private final MetricsAnalysisService metricsAnalysisService;
    private final AlertOptimizationService alertOptimizationService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ANOMALY_CACHE_PREFIX = "aiops:anomaly:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    /**
     * 检测异常
     */
    public List<AnomalyRecord> detectAnomalies(AnomalyDetectionRequest request) {
        log.info("开始异常检测 - 服务: {}, 时间范围: {} ~ {}", 
                request.getServiceName(), request.getStartTime(), request.getEndTime());

        // 获取指标数据
        Map<String, List<Double>> metricData = metricsAnalysisService.getMetricTimeSeriesData(
                request.getServiceName(), 
                request.getMetricNames(),
                request.getStartTime(),
                request.getEndTime());

        List<AnomalyRecord> anomalies = new ArrayList<>();

        // 对每个指标进行异常检测
        for (Map.Entry<String, List<Double>> entry : metricData.entrySet()) {
            String metricName = entry.getKey();
            List<Double> values = entry.getValue();

            if (values.size() < 3) {
                continue;
            }

            // 使用3σ原则检测异常
            List<AnomalyRecord> detectedAnomalies = detectUsingThreeSigma(metricName, values, request);
            anomalies.addAll(detectedAnomalies);

            // 使用移动平均检测异常
            List<AnomalyRecord> maAnomalies = detectUsingMovingAverage(metricName, values, request);
            anomalies.addAll(maAnomalies);
        }

        // 去重并按严重程度排序
        List<AnomalyRecord> uniqueAnomalies = anomalies.stream()
                .distinct()
                .sorted(Comparator.comparing(this::getSeverityPriority))
                .collect(Collectors.toList());

        log.info("检测到 {} 个异常", uniqueAnomalies.size());

        return uniqueAnomalies;
    }

    /**
     * 使用3σ原则检测异常
     */
    private List<AnomalyRecord> detectUsingThreeSigma(String metricName, List<Double> values, 
                                                       AnomalyDetectionRequest request) {
        List<AnomalyRecord> anomalies = new ArrayList<>();

        double mean = calculateMean(values);
        double stdDev = calculateStdDev(values, mean);

        if (stdDev == 0) {
            return anomalies;
        }

        double threshold3Sigma = 3 * stdDev;

        for (int i = 0; i < values.size(); i++) {
            double value = values.get(i);
            double deviation = Math.abs(value - mean);

            if (deviation > threshold3Sigma) {
                AnomalyRecord anomaly = buildAnomalyRecord(
                        metricName, value, mean + (value > mean ? threshold3Sigma : -threshold3Sigma),
                        calculateSeverity(deviation / stdDev),
                        request
                );
                anomalies.add(anomaly);
            }
        }

        return anomalies;
    }

    /**
     * 使用移动平均检测异常
     */
    private List<AnomalyRecord> detectUsingMovingAverage(String metricName, List<Double> values,
                                                          AnomalyDetectionRequest request) {
        List<AnomalyRecord> anomalies = new ArrayList<>();

        int windowSize = Math.min(5, values.size() - 1);
        double threshold = 0.5; // 50%的偏差阈值

        for (int i = windowSize; i < values.size(); i++) {
            // 计算移动平均
            double ma = calculateMean(values.subList(i - windowSize, i));
            double currentValue = values.get(i);

            double deviation = Math.abs(currentValue - ma) / ma;

            if (deviation > threshold) {
                AnomalyRecord anomaly = buildAnomalyRecord(
                        metricName, currentValue, ma,
                        calculateSeverityFromDeviation(deviation),
                        request
                );
                anomalies.add(anomaly);
            }
        }

        return anomalies;
    }

    /**
     * 构建异常记录
     */
    private AnomalyRecord buildAnomalyRecord(String metricName, Double currentValue, 
                                              Double threshold, String severity,
                                              AnomalyDetectionRequest request) {
        return AnomalyRecord.builder()
                .id(UUID.randomUUID().toString())
                .anomalyType(determineAnomalyType(metricName))
                .metricName(metricName)
                .currentValue(currentValue)
                .threshold(threshold)
                .severity(severity)
                .serviceName(request.getServiceName())
                .host(request.getHost())
                .description(String.format("检测到%s指标异常: 当前值%.2f, 阈值%.2f", 
                        metricName, currentValue, threshold))
                .status("DETECTED")
                .detectedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 计算严重程度
     */
    private String calculateSeverity(double sigmaLevel) {
        if (sigmaLevel > 4) {
            return "CRITICAL";
        } else if (sigmaLevel > 3.5) {
            return "HIGH";
        } else if (sigmaLevel > 3) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * 根据偏差计算严重程度
     */
    private String calculateSeverityFromDeviation(double deviation) {
        if (deviation > 1.0) {
            return "CRITICAL";
        } else if (deviation > 0.7) {
            return "HIGH";
        } else if (deviation > 0.5) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * 根据指标名称确定异常类型
     */
    private String determineAnomalyType(String metricName) {
        String upperName = metricName.toUpperCase();
        if (upperName.contains("CPU")) {
            return "CPU";
        } else if (upperName.contains("MEMORY") || upperName.contains("MEM")) {
            return "MEMORY";
        } else if (upperName.contains("DISK")) {
            return "DISK";
        } else if (upperName.contains("NETWORK") || upperName.contains("NET")) {
            return "NETWORK";
        } else if (upperName.contains("RESPONSE") || upperName.contains("LATENCY")) {
            return "RESPONSE_TIME";
        } else if (upperName.contains("ERROR") || upperName.contains("FAIL")) {
            return "ERROR_RATE";
        }
        return "OTHER";
    }

    /**
     * 计算平均值
     */
    private double calculateMean(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    /**
     * 计算标准差
     */
    private double calculateStdDev(List<Double> values, double mean) {
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0);
        return Math.sqrt(variance);
    }

    /**
     * 获取异常严重程度优先级
     */
    private int getSeverityPriority(AnomalyRecord record) {
        return switch (record.getSeverity()) {
            case "CRITICAL" -> 1;
            case "HIGH" -> 2;
            case "MEDIUM" -> 3;
            case "LOW" -> 4;
            default -> 5;
        };
    }

    /**
     * 异步检测异常
     */
    @Async("aiopsTaskExecutor")
    public CompletableFuture<List<AnomalyRecord>> detectAnomaliesAsync(AnomalyDetectionRequest request) {
        return CompletableFuture.completedFuture(detectAnomalies(request));
    }

    /**
     * 缓存异常检测结果
     */
    public void cacheAnomalyResults(String requestId, List<AnomalyRecord> results) {
        String cacheKey = ANOMALY_CACHE_PREFIX + requestId;
        redisTemplate.opsForValue().set(cacheKey, results, CACHE_TTL);
    }

    /**
     * 获取缓存的异常检测结果
     */
    @SuppressWarnings("unchecked")
    public List<AnomalyRecord> getCachedAnomalyResults(String requestId) {
        String cacheKey = ANOMALY_CACHE_PREFIX + requestId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof List) {
            return (List<AnomalyRecord>) cached;
        }
        return null;
    }
}
