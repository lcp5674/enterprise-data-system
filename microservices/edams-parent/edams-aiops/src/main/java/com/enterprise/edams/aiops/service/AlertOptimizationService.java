package com.enterprise.edams.aiops.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 告警优化服务
 * 
 * 功能:
 * - 智能告警聚合(避免告警风暴)
 * - 告警收敛和关联
 * - 告警优先级排序
 *
 * @author AIOps Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertOptimizationService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ALERT_AGGREGATION_PREFIX = "aiops:alert:aggregation:";
    private static final Duration AGGREGATION_WINDOW = Duration.ofMinutes(5);

    /**
     * 告警聚合策略
     */
    public enum AggregationStrategy {
        TIME_WINDOW,      // 时间窗口聚合
        METRIC_BASED,     // 基于指标聚合
        SERVICE_BASED,    // 基于服务聚合
        SEVERITY_BASED    // 基于严重程度聚合
    }

    /**
     * 聚合后的告警
     */
    public static class AggregatedAlert {
        private String alertId;
        private String alertType;
        private String serviceName;
        private String severity;
        private int count;
        private List<String> originalAlertIds;
        private String summary;
        private LocalDateTime firstOccurrence;
        private LocalDateTime lastOccurrence;
        private String recommendedAction;

        public String getAlertId() { return alertId; }
        public void setAlertId(String alertId) { this.alertId = alertId; }
        public String getAlertType() { return alertType; }
        public void setAlertType(String alertType) { this.alertType = alertType; }
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public List<String> getOriginalAlertIds() { return originalAlertIds; }
        public void setOriginalAlertIds(List<String> originalAlertIds) { this.originalAlertIds = originalAlertIds; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public LocalDateTime getFirstOccurrence() { return firstOccurrence; }
        public void setFirstOccurrence(LocalDateTime firstOccurrence) { this.firstOccurrence = firstOccurrence; }
        public LocalDateTime getLastOccurrence() { return lastOccurrence; }
        public void setLastOccurrence(LocalDateTime lastOccurrence) { this.lastOccurrence = lastOccurrence; }
        public String getRecommendedAction() { return recommendedAction; }
        public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }
    }

    /**
     * 原始告警
     */
    public static class Alert {
        private String alertId;
        private String alertType;
        private String metricName;
        private String serviceName;
        private String severity;
        private String host;
        private double value;
        private double threshold;
        private LocalDateTime timestamp;
        private String description;

        public String getAlertId() { return alertId; }
        public void setAlertId(String alertId) { this.alertId = alertId; }
        public String getAlertType() { return alertType; }
        public void setAlertType(String alertType) { this.alertType = alertType; }
        public String getMetricName() { return metricName; }
        public void setMetricName(String metricName) { this.metricName = metricName; }
        public String getServiceName() { return serviceName; }
        public void setServiceName(String serviceName) { this.serviceName = serviceName; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public double getValue() { return value; }
        public void setValue(double value) { this.value = value; }
        public double getThreshold() { return threshold; }
        public void setThreshold(double threshold) { this.threshold = threshold; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    /**
     * 智能聚合告警
     */
    public List<AggregatedAlert> aggregateAlerts(List<Alert> alerts, AggregationStrategy strategy) {
        log.info("开始聚合告警 - 告警数量: {}, 策略: {}", alerts.size(), strategy);

        Map<String, List<Alert>> groupedAlerts = switch (strategy) {
            case TIME_WINDOW -> groupByTimeWindow(alerts);
            case METRIC_BASED -> groupByMetric(alerts);
            case SERVICE_BASED -> groupByService(alerts);
            case SEVERITY_BASED -> groupBySeverity(alerts);
        };

        List<AggregatedAlert> aggregatedAlerts = new ArrayList<>();
        for (Map.Entry<String, List<Alert>> entry : groupedAlerts.entrySet()) {
            AggregatedAlert aggAlert = createAggregatedAlert(entry.getValue());
            if (aggAlert.getCount() > 0) {
                aggregatedAlerts.add(aggAlert);
            }
        }

        // 按严重程度和数量排序
        aggregatedAlerts.sort((a, b) -> {
            int severityCompare = getSeverityWeight(b.getSeverity()) - getSeverityWeight(a.getSeverity());
            if (severityCompare != 0) {
                return severityCompare;
            }
            return Integer.compare(b.getCount(), a.getCount());
        });

        log.info("告警聚合完成 - 聚合后数量: {}", aggregatedAlerts.size());
        return aggregatedAlerts;
    }

    /**
     * 按时间窗口分组
     */
    private Map<String, List<Alert>> groupByTimeWindow(List<Alert> alerts) {
        Map<String, List<Alert>> grouped = new ConcurrentHashMap<>();
        LocalDateTime now = LocalDateTime.now();

        for (Alert alert : alerts) {
            long minutesDiff = Duration.between(alert.getTimestamp(), now).toMinutes();
            long windowKey = minutesDiff / AGGREGATION_WINDOW.toMinutes();
            String key = alert.getServiceName() + "_" + alert.getMetricName() + "_" + windowKey;

            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(alert);
        }

        return grouped;
    }

    /**
     * 按指标分组
     */
    private Map<String, List<Alert>> groupByMetric(List<Alert> alerts) {
        return alerts.stream()
                .collect(Collectors.groupingBy(alert -> 
                        alert.getServiceName() + "_" + alert.getMetricName()));
    }

    /**
     * 按服务分组
     */
    private Map<String, List<Alert>> groupByService(List<Alert> alerts) {
        return alerts.stream()
                .collect(Collectors.groupingBy(Alert::getServiceName));
    }

    /**
     * 按严重程度分组
     */
    private Map<String, List<Alert>> groupBySeverity(List<Alert> alerts) {
        return alerts.stream()
                .collect(Collectors.groupingBy(Alert::getSeverity));
    }

    /**
     * 创建聚合告警
     */
    private AggregatedAlert createAggregatedAlert(List<Alert> alerts) {
        AggregatedAlert result = new AggregatedAlert();
        
        if (alerts.isEmpty()) {
            result.setCount(0);
            return result;
        }

        Alert firstAlert = alerts.get(0);
        result.setAlertId(UUID.randomUUID().toString());
        result.setAlertType(firstAlert.getAlertType());
        result.setServiceName(firstAlert.getServiceName());
        result.setSeverity(determineAggregatedSeverity(alerts));
        result.setCount(alerts.size());
        result.setOriginalAlertIds(alerts.stream().map(Alert::getAlertId).collect(Collectors.toList()));
        result.setFirstOccurrence(alerts.stream()
                .map(Alert::getTimestamp)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now()));
        result.setLastOccurrence(alerts.stream()
                .map(Alert::getTimestamp)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now()));

        // 生成摘要
        result.setSummary(generateAlertSummary(alerts, result));
        
        // 推荐行动
        result.setRecommendedAction(generateRecommendedAction(alerts, result));

        return result;
    }

    /**
     * 确定聚合后的严重程度
     */
    private String determineAggregatedSeverity(List<Alert> alerts) {
        Set<String> severities = alerts.stream()
                .map(Alert::getSeverity)
                .collect(Collectors.toSet());

        if (severities.contains("CRITICAL")) {
            return "CRITICAL";
        } else if (severities.contains("HIGH")) {
            return "HIGH";
        } else if (severities.contains("MEDIUM")) {
            return "MEDIUM";
        }
        return "LOW";
    }

    /**
     * 生成告警摘要
     */
    private String generateAlertSummary(List<Alert> alerts, AggregatedAlert result) {
        if (alerts.size() == 1) {
            return alerts.get(0).getDescription();
        }
        return String.format("在%s服务中检测到%d次%s告警，最近发生在%s",
                result.getServiceName(),
                alerts.size(),
                result.getAlertType(),
                result.getLastOccurrence().format(java.time.format.DateTimeFormatter.ISO_LOCAL_TIME));
    }

    /**
     * 生成推荐行动
     */
    private String generateRecommendedAction(List<Alert> alerts, AggregatedAlert result) {
        String severity = result.getSeverity();
        int count = alerts.size();

        if ("CRITICAL".equals(severity)) {
            return "立即处理：该告警已触发" + count + "次，建议立即检查系统状态并进行故障排查";
        } else if ("HIGH".equals(severity)) {
            return "优先处理：在" + count + "分钟内检测到" + count + "次告警，建议在1小时内进行处理";
        } else if ("MEDIUM".equals(severity)) {
            return "计划处理：累计" + count + "次告警，建议在下次维护窗口进行处理";
        } else {
            return "观察处理：累计" + count + "次轻微告警，建议持续监控";
        }
    }

    /**
     * 获取严重程度权重
     */
    private int getSeverityWeight(String severity) {
        return switch (severity) {
            case "CRITICAL" -> 4;
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            case "LOW" -> 1;
            default -> 0;
        };
    }

    /**
     * 告警收敛 - 识别根因告警
     */
    public List<Alert> convergeAlerts(List<Alert> alerts) {
        log.info("开始告警收敛 - 输入告警数量: {}", alerts.size());

        // 基于因果关系的告警收敛
        Map<String, Alert> rootCauseAlerts = new LinkedHashMap<>();
        
        for (Alert alert : alerts) {
            String rootCauseKey = determineRootCauseKey(alert);
            if (!rootCauseAlerts.containsKey(rootCauseKey)) {
                rootCauseAlerts.put(rootCauseKey, alert);
            }
        }

        List<Alert> converged = new ArrayList<>(rootCauseAlerts.values());
        log.info("告警收敛完成 - 收敛后告警数量: {}", converged.size());
        
        return converged;
    }

    /**
     * 确定根因告警键
     */
    private String determineRootCauseKey(Alert alert) {
        // 简化逻辑：按服务+指标类型确定根因
        return alert.getServiceName() + "_" + alert.getAlertType();
    }

    /**
     * 告警关联 - 找出相关的告警
     */
    public Map<String, List<Alert>> correlateAlerts(List<Alert> alerts) {
        Map<String, List<Alert>> correlations = new HashMap<>();

        for (Alert alert : alerts) {
            String correlationId = alert.getServiceName() + "_" + alert.getHost();
            correlations.computeIfAbsent(correlationId, k -> new ArrayList<>()).add(alert);
        }

        return correlations;
    }

    /**
     * 告警优先级排序
     */
    public List<Alert> prioritizeAlerts(List<Alert> alerts) {
        return alerts.stream()
                .sorted((a, b) -> {
                    int severityCompare = getSeverityWeight(b.getSeverity()) - getSeverityWeight(a.getSeverity());
                    if (severityCompare != 0) {
                        return severityCompare;
                    }
                    // 相同严重程度时，按时间排序
                    return b.getTimestamp().compareTo(a.getTimestamp());
                })
                .collect(Collectors.toList());
    }
}
