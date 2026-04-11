package com.enterprise.edams.aiops.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 告警实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("aiops_alert")
public class Alert extends BaseEntity {

    /**
     * 告警标题
     */
    @TableField("alert_title")
    private String alertTitle;

    /**
     * 告警描述
     */
    @TableField("alert_description")
    private String alertDescription;

    /**
     * 告警级别：critical, warning, info
     */
    @TableField("alert_level")
    private String alertLevel;

    /**
     * 告警状态：pending, acknowledged, resolved, closed
     */
    @TableField("alert_status")
    private String alertStatus;

    /**
     * 关联的服务/系统ID
     */
    @TableField("target_id")
    private String targetId;

    /**
     * 关联的目标名称
     */
    @TableField("target_name")
    private String targetName;

    /**
     * 触发条件
     */
    @TableField("trigger_condition")
    private String triggerCondition;

    /**
     * 当前值
     */
    @TableField("current_value")
    private String currentValue;

    /**
     * 告警阈值
     */
    @TableField("threshold")
    private String threshold;

    /**
     * 告警时间
     */
    @TableField("alert_time")
    private LocalDateTime alertTime;

    /**
     * 确认时间
     */
    @TableField("ack_time")
    private LocalDateTime ackTime;

    /**
     * 确认人
     */
    @TableField("ack_by")
    private String ackBy;

    /**
     * 解决时间
     */
    @TableField("resolve_time")
    private LocalDateTime resolveTime;

    /**
     * 解决人
     */
    @TableField("resolve_by")
    private String resolveBy;

    /**
     * 解决方案
     */
    @TableField("solution")
    private String solution;

    /**
     * 关联规则ID
     */
    @TableField("rule_id")
    private Long ruleId;

    /**
     * 通知状态
     */
    @TableField("notification_sent")
    private Integer notificationSent;

    /**
     * 通知时间
     */
    @TableField("notification_time")
    private LocalDateTime notificationTime;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 判断是否为严重告警
     */
    public boolean isCritical() {
        return "critical".equalsIgnoreCase(alertLevel);
    }

    /**
     * 判断告警是否已处理
     */
    public boolean isResolved() {
        return "resolved".equalsIgnoreCase(alertStatus) || "closed".equalsIgnoreCase(alertStatus);
    }
}
