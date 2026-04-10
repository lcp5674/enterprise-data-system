package com.enterprise.edams.aiops.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 异常检测请求DTO
 *
 * @author AIOps Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyDetectionRequest {

    /**
     * 服务名称
     */
    @NotBlank(message = "服务名称不能为空")
    private String serviceName;

    /**
     * 主机/节点标识
     */
    private String host;

    /**
     * 指标名称列表
     */
    private List<String> metricNames;

    /**
     * 异常类型: CPU, MEMORY, DISK, NETWORK, RESPONSE_TIME, ERROR_RATE
     */
    private String anomalyType;

    /**
     * 检测开始时间
     */
    @NotNull(message = "检测开始时间不能为空")
    private LocalDateTime startTime;

    /**
     * 检测结束时间
     */
    @NotNull(message = "检测结束时间不能为空")
    private LocalDateTime endTime;

    /**
     * 检测算法: STATISTICAL, ML_BASED, HYBRID
     */
    private String detectionMethod;

    /**
     * 置信度阈值 (0-1)
     */
    private Double confidenceThreshold;

    /**
     * 是否包含历史分析
     */
    private Boolean includeHistoricalAnalysis;
}
