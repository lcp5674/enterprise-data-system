package com.enterprise.edams.aiops.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 异常记录实体
 * 
 * 用于存储检测到的系统异常事件
 *
 * @author AIOps Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("aiops_anomaly_record")
public class AnomalyRecord {

    /**
     * 异常记录ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 异常类型: CPU, MEMORY, DISK, NETWORK, RESPONSE_TIME, ERROR_RATE
     */
    private String anomalyType;

    /**
     * 指标名称
     */
    private String metricName;

    /**
     * 指标当前值
     */
    private Double currentValue;

    /**
     * 指标阈值
     */
    private Double threshold;

    /**
     * 异常严重程度: LOW, MEDIUM, HIGH, CRITICAL
     */
    private String severity;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 主机/节点标识
     */
    private String host;

    /**
     * 异常描述
     */
    private String description;

    /**
     * 异常状态: DETECTED, ACKNOWLEDGED, RESOLVED
     */
    private String status;

    /**
     * 检测时间
     */
    private LocalDateTime detectedAt;

    /**
     * 确认时间
     */
    private LocalDateTime acknowledgedAt;

    /**
     * 解决时间
     */
    private LocalDateTime resolvedAt;

    /**
     * 关联的告警ID列表 (JSON格式)
     */
    private String relatedAlertIds;

    /**
     * 根因分析结果
     */
    private String rootCause;

    /**
     * 建议的处理措施
     */
    private String suggestedAction;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
