package com.enterprise.edams.sla.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SLA定义实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sla_definition")
public class SlaDefinition extends BaseEntity {
    
    /**
     * SLA编码
     */
    @TableField("sla_code")
    private String slaCode;
    
    /**
     * SLA名称
     */
    @TableField("sla_name")
    private String slaName;
    
    /**
     * 资产ID
     */
    @TableField("asset_id")
    private Long assetId;
    
    /**
     * SLA类型
     */
    @TableField("sla_type")
    private SlaType slaType;
    
    /**
     * 目标值
     */
    @TableField("target_value")
    private BigDecimal targetValue;
    
    /**
     * 单位
     */
    @TableField("unit")
    private String unit;
    
    /**
     * 比较操作符
     */
    @TableField("operator")
    private CompareOperator operator;
    
    /**
     * 窗口类型
     */
    @TableField("window_type")
    private WindowType windowType;
    
    /**
     * 窗口大小
     */
    @TableField("window_size")
    private Integer windowSize;
    
    /**
     * 严重程度
     */
    @TableField("severity")
    private SlaSeverity severity;
    
    /**
     * 联系人
     */
    @TableField("contact")
    private String contact;
    
    /**
     * 告警方式
     */
    @TableField("alert_methods")
    private String alertMethods;
    
    /**
     * 状态
     */
    @TableField("status")
    private SlaStatus status;
    
    /**
     * 描述
     */
    @TableField("description")
    private String description;
}

enum SlaType {
    AVAILABILITY("可用性"),
    RESPONSE_TIME("响应时间"),
    QUALITY_SCORE("质量评分"),
    DATA_FRESHNESS("数据新鲜度"),
    ERROR_RATE("错误率"),
    THROUGHPUT("吞吐量");
    
    private final String description;
    SlaType(String description) { this.description = description; }
}

enum CompareOperator {
    GREATER_THAN("大于"),
    GREATER_EQUAL("大于等于"),
    LESS_THAN("小于"),
    LESS_EQUAL("小于等于"),
    EQUAL("等于");
    
    private final String description;
    CompareOperator(String description) { this.description = description; }
}

enum WindowType {
    MINUTE("分钟"),
    HOUR("小时"),
    DAY("天"),
    WEEK("周"),
    MONTH("月");
    
    private final String description;
    WindowType(String description) { this.description = description; }
}

enum SlaSeverity {
    CRITICAL("严重"),
    HIGH("高"),
    MEDIUM("中"),
    LOW("低");
    
    private final String description;
    SlaSeverity(String description) { this.description = description; }
}

enum SlaStatus {
    ACTIVE("生效中"),
    INACTIVE("已停用"),
    BREACHED("已违反"),
    PENDING("待审核");
    
    private final String description;
    SlaStatus(String description) { this.description = description; }
}
