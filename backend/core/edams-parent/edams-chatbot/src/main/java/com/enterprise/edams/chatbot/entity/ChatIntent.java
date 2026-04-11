package com.enterprise.edams.chatbot.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 聊天意图实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("chat_intent")
public class ChatIntent extends BaseEntity {

    /**
     * 意图类型：QUERY_METADATA, QUERY_QUALITY, QUERY_LINEAGE, GENERATE_REPORT, CHECK_STATUS, FAQ
     */
    @TableField("intent_type")
    private String intentType;

    /**
     * 意图描述
     */
    @TableField("description")
    private String description;

    /**
     * 关键词(JSON数组)
     */
    @TableField("keywords")
    private String keywords;

    /**
     * 匹配模式
     */
    @TableField("pattern")
    private String pattern;

    /**
     * 意图示例
     */
    @TableField("examples")
    private String examples;

    /**
     * 默认响应模板
     */
    @TableField("response_template")
    private String responseTemplate;

    /**
     * 关联的服务
     */
    @TableField("target_service")
    private String targetService;

    /**
     * 关联的API端点
     */
    @TableField("api_endpoint")
    private String apiEndpoint;

    /**
     * 优先级
     */
    @TableField("priority")
    private Integer priority;

    /**
     * 置信度阈值
     */
    @TableField("confidence_threshold")
    private BigDecimal confidenceThreshold;

    /**
     * 是否启用
     */
    @TableField("enabled")
    private Integer enabled;

    /**
     * 槽位定义(JSON)
     */
    @TableField("slots")
    private String slots;

    /**
     * 槽位提取规则(JSON)
     */
    @TableField("slot_rules")
    private String slotRules;

    /**
     * 后续意图
     */
    @TableField("follow_up_intents")
    private String followUpIntents;

    /**
     * 父意图ID
     */
    @TableField("parent_intent_id")
    private Long parentIntentId;

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
     * 判断意图是否启用
     */
    public boolean isActive() {
        return enabled != null && enabled == 1;
    }
}
