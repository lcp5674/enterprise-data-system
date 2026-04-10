package com.enterprise.edams.sla.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SLA违反记录实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sla_violation")
public class SlaViolation extends BaseEntity {
    
    /**
     * SLA定义ID
     */
    @TableField("sla_definition_id")
    private Long slaDefinitionId;
    
    /**
     * SLA编码
     */
    @TableField("sla_code")
    private String slaCode;
    
    /**
     * 资产ID
     */
    @TableField("asset_id")
    private Long assetId;
    
    /**
     * 违反时间
     */
    @TableField("violation_time")
    private LocalDateTime violationTime;
    
    /**
     * 实际值
     */
    @TableField("actual_value")
    private BigDecimal actualValue;
    
    /**
     * 目标值
     */
    @TableField("target_value")
    private BigDecimal targetValue;
    
    /**
     * 违反程度
     */
    @TableField("violation_degree")
    private BigDecimal violationDegree;
    
    /**
     * 严重程度
     */
    @TableField("severity")
    private SlaSeverity severity;
    
    /**
     * 违反状态
     */
    @TableField("status")
    private ViolationStatus status;
    
    /**
     * 告警状态
     */
    @TableField("alert_status")
    private AlertStatus alertStatus;
    
    /**
     * 升级状态
     */
    @TableField("escalation_status")
    private EscalationStatus escalationStatus;
    
    /**
     * 处理人
     */
    @TableField("handler")
    private String handler;
    
    /**
     * 处理时间
     */
    @TableField("handle_time")
    private LocalDateTime handleTime;
    
    /**
     * 处理结果
     */
    @TableField("handle_result")
    private String handleResult;
    
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}

enum ViolationStatus {
    OPEN("待处理"),
    IN_PROGRESS("处理中"),
    RESOLVED("已解决"),
    CLOSED("已关闭"),
    IGNORED("已忽略");
    
    private final String description;
    ViolationStatus(String description) { this.description = description; }
}

enum AlertStatus {
    PENDING("待发送"),
    SENT("已发送"),
    ACKNOWLEDGED("已确认"),
    RESOLVED("已解除");
    
    private final String description;
    AlertStatus(String description) { this.description = description; }
}

enum EscalationStatus {
    NONE("无需升级"),
    LEVEL_1("一级升级"),
    LEVEL_2("二级升级"),
    LEVEL_3("三级升级"),
    ESCALATED("已升级");
    
    private final String description;
    EscalationStatus(String description) { this.description = description; }
}
