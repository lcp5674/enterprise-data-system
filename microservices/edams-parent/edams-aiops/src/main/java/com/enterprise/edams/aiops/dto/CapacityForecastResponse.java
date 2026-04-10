package com.enterprise.edams.aiops.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 容量预测响应DTO
 *
 * @author AIOps Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CapacityForecastResponse {

    /**
     * 资源类型
     */
    private String resourceType;

    /**
     * 资源名称
     */
    private String resourceName;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 当前使用量
     */
    private Double currentUsage;

    /**
     * 总容量
     */
    private Double totalCapacity;

    /**
     * 当前使用率百分比
     */
    private Double currentUsagePercentage;

    /**
     * 趋势方向: INCREASING, DECREASING, STABLE
     */
    private String trend;

    /**
     * 日均增长率
     */
    private Double dailyGrowthRate;

    /**
     * 预测时间点列表
     */
    private List<ForecastPoint> forecastPoints;

    /**
     * 预计耗尽日期
     */
    private LocalDateTime estimatedExhaustionDate;

    /**
     * 剩余可用天数
     */
    private Integer daysUntilExhaustion;

    /**
     * 推荐扩容时间
     */
    private LocalDateTime recommendedScaleUpTime;

    /**
     * 推荐扩容量
     */
    private Double recommendedScaleUpAmount;

    /**
     * 推荐扩容后使用率
     */
    private Double recommendedUsagePercentage;

    /**
     * 预测置信度
     */
    private Double confidenceLevel;

    /**
     * 预测依据说明
     */
    private String forecastBasis;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 建议措施
     */
    private List<String> recommendations;

    /**
     * 预测数据点
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForecastPoint {
        /**
         * 预测时间点
         */
        private LocalDateTime timestamp;

        /**
         * 预测使用量
         */
        private Double predictedUsage;

        /**
         * 预测使用率
         */
        private Double predictedUsagePercentage;

        /**
         * 置信区间下限
         */
        private Double lowerBound;

        /**
         * 置信区间上限
         */
        private Double upperBound;
    }
}
