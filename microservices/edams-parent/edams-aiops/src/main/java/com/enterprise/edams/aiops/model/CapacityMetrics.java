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
 * 容量指标实体
 * 
 * 存储系统资源容量使用情况及预测数据
 *
 * @author AIOps Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("aiops_capacity_metrics")
public class CapacityMetrics {

    /**
     * 指标记录ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 资源类型: CPU, MEMORY, DISK, NETWORK, DATABASE_CONNECTION, THREAD_POOL
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
     * 主机/节点标识
     */
    private String host;

    /**
     * 当前使用量
     */
    private Double currentUsage;

    /**
     * 总量/容量
     */
    private Double totalCapacity;

    /**
     * 使用率百分比
     */
    private Double usagePercentage;

    /**
     * 趋势: INCREASING, DECREASING, STABLE
     */
    private String trend;

    /**
     * 日增长率
     */
    private Double dailyGrowthRate;

    /**
     * 预计耗尽日期 (null表示不会耗尽)
     */
    private LocalDateTime estimatedExhaustionDate;

    /**
     * 推荐扩容时间
     */
    private LocalDateTime recommendedScaleUpTime;

    /**
     * 推荐扩容量
     */
    private Double recommendedScaleUpAmount;

    /**
     * 预测数据点 (JSON格式，包含未来时间点的预测值)
     */
    private String forecastDataPoints;

    /**
     * 置信度
     */
    private Double confidenceLevel;

    /**
     * 数据时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
