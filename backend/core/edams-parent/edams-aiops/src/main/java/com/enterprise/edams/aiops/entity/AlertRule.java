package com.enterprise.edams.aiops.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * 告警规则实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("alert_rule")
public class AlertRule extends BaseEntity {

    /**
     * 规则名称
     */
    @TableField("rule_name")
    private String ruleName;

    /**
     * 规则描述
     */
    @TableField("description")
    private String description;

    /**
     * 规则类型：threshold, trend, pattern, anomaly
     */
    @TableField("rule_type")
    private String ruleType;

    /**
     * 指标名称
     */
    @TableField("metric_name")
    private String metricName;

    /**
     * 目标类型：service, system, component
     */
    @TableField("target_type")
    private String targetType;

    /**
     * 目标ID
     */
    @TableField("target_id")
    private String targetId;

    /**
     * 条件表达式
     */
    @TableField("condition_expr")
    private String conditionExpr;

    /**
     * 阈值
     */
    @TableField("threshold")
    private String threshold;

    /**
     * 比较操作符：gt, lt, eq, gte, lte, neq
     */
    @TableField("operator")
    private String operator;

    /**
     * 告警级别：critical, warning, info
     */
    @TableField("alert_level")
    private String alertLevel;

    /**
     * 持续时间（秒）
     */
    @TableField("duration_seconds")
    private Integer durationSeconds;

    /**
     * 评估间隔（秒）
     */
    @TableField("evaluate_interval")
    private Integer evaluateInterval;

    /**
     * 是否启用
     */
    @TableField("enabled")
    private Integer enabled;

    /**
     * 是否发送通知
     */
    @TableField("notify_enabled")
    private Integer notifyEnabled;

    /**
     * 通知渠道：email, sms, webhook, dingtalk, wechat
     */
    @TableField("notify_channels")
    private String notifyChannels;

    /**
     * 通知模板ID
     */
    @TableField("template_id")
    private Long templateId;

    /**
     * 告警收敛
     */
    @TableField("aggregation_enabled")
    private Integer aggregationEnabled;

    /**
     * 收敛时间窗口（分钟）
     */
    @TableField("aggregation_window")
    private Integer aggregationWindow;

    /**
     * 最大告警次数
     */
    @TableField("max_alerts")
    private Integer maxAlerts;

    /**
     * 沉默开始时间
     */
    @TableField("silence_start")
    private String silenceStart;

    /**
     * 沉默结束时间
     */
    @TableField("silence_end")
    private String silenceEnd;

    /**
     * 关联的动作ID
     */
    @TableField("action_ids")
    private String actionIds;

    /**
     * 标签(JSON)
     */
    @TableField("tags")
    private String tags;

    /**
     * 租户ID
     */
    @TableField("tenant_id")
    private Long tenantId;

    /**
     * 判断规则是否启用
     */
    public boolean isActive() {
        return enabled != null && enabled == 1;
    }
}
