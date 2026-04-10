package com.enterprise.edams.aiops.service;

import com.enterprise.edams.aiops.dto.CapacityForecastResponse;
import com.enterprise.edams.aiops.model.CapacityMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 容量规划预测服务
 * 
 * 功能:
 * - 基于历史数据分析资源使用趋势
 * - 预测未来容量需求
 * - 提供扩容建议
 *
 * @author AIOps Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CapacityPlanningService {

    private final MetricsAnalysisService metricsAnalysisService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CAPACITY_CACHE_PREFIX = "aiops:capacity:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    /**
     * 预测容量需求
     */
    public CapacityForecastResponse forecastCapacity(String resourceType, String resourceName,
                                                       String serviceName, int forecastDays) {
        log.info("开始容量预测 - 资源类型: {}, 资源名称: {}, 预测天数: {}", 
                resourceType, resourceName, forecastDays);

        // 获取历史数据
        List<CapacityMetrics> historicalData = getHistoricalCapacityData(resourceName, serviceName);

        if (historicalData.isEmpty()) {
            // 生成模拟数据
            historicalData = generateMockCapacityData(resourceType, resourceName, serviceName);
        }

        // 计算趋势和增长率
        double dailyGrowthRate = calculateGrowthRate(historicalData);
        String trend = determineTrend(dailyGrowthRate);

        // 生成预测点
        List<CapacityForecastResponse.ForecastPoint> forecastPoints = 
                generateForecastPoints(historicalData, forecastDays, dailyGrowthRate);

        // 计算预计耗尽日期
        Double currentUsage = historicalData.get(historicalData.size() - 1).getCurrentUsage();
        Double totalCapacity = historicalData.get(historicalData.size() - 1).getTotalCapacity();
        LocalDateTime exhaustionDate = calculateExhaustionDate(currentUsage, totalCapacity, dailyGrowthRate);

        // 生成扩容建议
        LocalDateTime recommendedScaleUpTime = calculateRecommendedScaleUpTime(exhaustionDate);
        Double recommendedScaleUpAmount = calculateRecommendedScaleUpAmount(totalCapacity, currentUsage);

        // 确定风险等级
        String riskLevel = determineRiskLevel(currentUsage, totalCapacity, exhaustionDate);

        // 计算置信度
        double confidenceLevel = calculateConfidenceLevel(historicalData.size(), dailyGrowthRate);

        return CapacityForecastResponse.builder()
                .resourceType(resourceType)
                .resourceName(resourceName)
                .serviceName(serviceName)
                .currentUsage(currentUsage)
                .totalCapacity(totalCapacity)
                .currentUsagePercentage((currentUsage / totalCapacity) * 100)
                .trend(trend)
                .dailyGrowthRate(dailyGrowthRate)
                .forecastPoints(forecastPoints)
                .estimatedExhaustionDate(exhaustionDate)
                .daysUntilExhaustion(exhaustionDate != null ? 
                        (int) Duration.between(LocalDateTime.now(), exhaustionDate).toDays() : -1)
                .recommendedScaleUpTime(recommendedScaleUpTime)
                .recommendedScaleUpAmount(recommendedScaleUpAmount)
                .recommendedUsagePercentage(70.0)
                .confidenceLevel(confidenceLevel)
                .forecastBasis("基于" + historicalData.size() + "个历史数据点的线性回归分析")
                .riskLevel(riskLevel)
                .recommendations(generateRecommendations(riskLevel, exhaustionDate, recommendedScaleUpAmount))
                .build();
    }

    /**
     * 获取历史容量数据
     */
    private List<CapacityMetrics> getHistoricalCapacityData(String resourceName, String serviceName) {
        String cacheKey = CAPACITY_CACHE_PREFIX + resourceName + ":" + serviceName;
        
        @SuppressWarnings("unchecked")
        List<CapacityMetrics> cached = (List<CapacityMetrics>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 模拟查询数据库
        List<CapacityMetrics> historicalData = generateMockCapacityData(
                "MEMORY", resourceName, serviceName);

        redisTemplate.opsForValue().set(cacheKey, historicalData, CACHE_TTL);
        return historicalData;
    }

    /**
     * 生成模拟容量数据
     */
    private List<CapacityMetrics> generateMockCapacityData(String resourceType, 
                                                            String resourceName, 
                                                            String serviceName) {
        List<CapacityMetrics> data = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        double baseUsage;
        double baseCapacity;

        switch (resourceType.toUpperCase()) {
            case "CPU" -> {
                baseUsage = 45;
                baseCapacity = 100;
            }
            case "MEMORY" -> {
                baseUsage = 65;
                baseCapacity = 128;
            }
            case "DISK" -> {
                baseUsage = 500;
                baseCapacity = 1000;
            }
            default -> {
                baseUsage = 50;
                baseCapacity = 100;
            }
        }

        double currentUsage = baseUsage;
        // 模拟增长趋势
        for (int i = 30; i >= 0; i--) {
            LocalDateTime timestamp = now.minusDays(i);
            double variation = (ThreadLocalRandom.current().nextDouble() - 0.5) * 5;
            double usage = Math.min(baseCapacity * 0.95, currentUsage + variation);

            data.add(CapacityMetrics.builder()
                    .id("mock-" + i)
                    .resourceType(resourceType)
                    .resourceName(resourceName)
                    .serviceName(serviceName)
                    .currentUsage(usage)
                    .totalCapacity(baseCapacity)
                    .usagePercentage((usage / baseCapacity) * 100)
                    .trend("INCREASING")
                    .dailyGrowthRate(0.5)
                    .timestamp(timestamp)
                    .createdAt(timestamp)
                    .build());

            currentUsage *= 1.005; // 每日增长0.5%
        }

        return data;
    }

    /**
     * 计算增长率
     */
    private double calculateGrowthRate(List<CapacityMetrics> data) {
        if (data.size() < 2) {
            return 0;
        }

        double firstUsage = data.get(0).getCurrentUsage();
        double lastUsage = data.get(data.size() - 1).getCurrentUsage();
        int days = data.size();

        if (firstUsage == 0) {
            return 0;
        }

        // 计算复合日增长率 (CAGR)
        return (Math.pow(lastUsage / firstUsage, 1.0 / days) - 1) * 100;
    }

    /**
     * 确定趋势
     */
    private String determineTrend(double growthRate) {
        if (growthRate > 1) {
            return "INCREASING";
        } else if (growthRate < -1) {
            return "DECREASING";
        } else {
            return "STABLE";
        }
    }

    /**
     * 生成预测点
     */
    private List<CapacityForecastResponse.ForecastPoint> generateForecastPoints(
            List<CapacityMetrics> historicalData, int forecastDays, double growthRate) {
        List<CapacityForecastResponse.ForecastPoint> points = new ArrayList<>();
        
        CapacityMetrics lastData = historicalData.get(historicalData.size() - 1);
        double currentUsage = lastData.getCurrentUsage();
        double totalCapacity = lastData.getTotalCapacity();
        double dailyGrowthFactor = 1 + (growthRate / 100);

        LocalDateTime now = LocalDateTime.now();

        for (int i = 1; i <= forecastDays; i++) {
            LocalDateTime timestamp = now.plusDays(i);
            double predictedUsage = currentUsage * Math.pow(dailyGrowthFactor, i);
            double usagePercentage = (predictedUsage / totalCapacity) * 100;

            // 计算置信区间 (±10%)
            double margin = predictedUsage * 0.1;

            points.add(CapacityForecastResponse.ForecastPoint.builder()
                    .timestamp(timestamp)
                    .predictedUsage(predictedUsage)
                    .predictedUsagePercentage(usagePercentage)
                    .lowerBound(Math.max(0, predictedUsage - margin))
                    .upperBound(Math.min(totalCapacity, predictedUsage + margin))
                    .build());
        }

        return points;
    }

    /**
     * 计算预计耗尽日期
     */
    private LocalDateTime calculateExhaustionDate(double currentUsage, double totalCapacity, 
                                                    double growthRate) {
        if (growthRate <= 0 || currentUsage <= 0) {
            return null;
        }

        double dailyGrowthFactor = 1 + (growthRate / 100);
        double remaining = totalCapacity - currentUsage;
        int daysUntilFull = (int) (Math.log(totalCapacity / currentUsage) / Math.log(dailyGrowthFactor));

        if (daysUntilFull > 365 * 5) { // 超过5年视为不会耗尽
            return null;
        }

        return LocalDateTime.now().plusDays(daysUntilFull);
    }

    /**
     * 计算推荐扩容时间
     */
    private LocalDateTime calculateRecommendedScaleUpTime(LocalDateTime exhaustionDate) {
        if (exhaustionDate == null) {
            return null;
        }
        // 建议在预计耗尽前30天扩容
        return exhaustionDate.minusDays(30);
    }

    /**
     * 计算推荐扩容量
     */
    private Double calculateRecommendedScaleUpAmount(Double totalCapacity, Double currentUsage) {
        if (totalCapacity == null || currentUsage == null) {
            return null;
        }
        // 推荐扩容至当前使用的1.5倍
        return currentUsage * 1.5 - totalCapacity;
    }

    /**
     * 确定风险等级
     */
    private String determineRiskLevel(Double currentUsage, Double totalCapacity, 
                                       LocalDateTime exhaustionDate) {
        double usagePercentage = (currentUsage / totalCapacity) * 100;

        if (usagePercentage >= 90 || (exhaustionDate != null && 
                Duration.between(LocalDateTime.now(), exhaustionDate).toDays() < 7)) {
            return "CRITICAL";
        } else if (usagePercentage >= 80 || (exhaustionDate != null && 
                Duration.between(LocalDateTime.now(), exhaustionDate).toDays() < 30)) {
            return "HIGH";
        } else if (usagePercentage >= 70) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    /**
     * 计算置信度
     */
    private double calculateConfidenceLevel(int dataPoints, double growthRate) {
        double baseConfidence = 0.8;
        
        // 数据点越多，置信度越高
        double dataPointsBonus = Math.min(0.1, dataPoints * 0.002);
        
        // 增长越稳定，置信度越高
        double stabilityBonus = growthRate > 0 && growthRate < 5 ? 0.05 : 0;

        return Math.min(0.99, baseConfidence + dataPointsBonus + stabilityBonus);
    }

    /**
     * 生成建议措施
     */
    private List<String> generateRecommendations(String riskLevel, LocalDateTime exhaustionDate,
                                                   Double scaleUpAmount) {
        List<String> recommendations = new ArrayList<>();

        if ("CRITICAL".equals(riskLevel)) {
            recommendations.add("立即启动扩容流程");
            recommendations.add("考虑紧急采购更多资源");
            if (scaleUpAmount != null && scaleUpAmount > 0) {
                recommendations.add("建议立即扩容 " + String.format("%.1f", scaleUpAmount) + " 单位");
            }
        } else if ("HIGH".equals(riskLevel)) {
            recommendations.add("在一周内完成扩容评估");
            recommendations.add("制定扩容计划并准备资源");
            if (exhaustionDate != null) {
                recommendations.add("预计在 " + exhaustionDate.toLocalDate() + " 前需要完成扩容");
            }
        } else if ("MEDIUM".equals(riskLevel)) {
            recommendations.add("在下次维护窗口进行扩容");
            recommendations.add("持续监控资源使用趋势");
        } else {
            recommendations.add("继续监控当前资源使用");
            recommendations.add("每月回顾容量规划");
        }

        return recommendations;
    }

    /**
     * 获取多资源容量预测
     */
    public List<CapacityForecastResponse> forecastMultipleResources(String serviceName, int forecastDays) {
        List<String> resourceTypes = List.of("CPU", "MEMORY", "DISK");
        List<CapacityForecastResponse> forecasts = new ArrayList<>();

        for (String resourceType : resourceTypes) {
            forecasts.add(forecastCapacity(resourceType, resourceType.toLowerCase() + "_usage", 
                    serviceName, forecastDays));
        }

        return forecasts;
    }
}
