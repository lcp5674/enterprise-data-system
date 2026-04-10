package com.enterprise.edams.asset.dto.quality;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 质量规则DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class QualityRuleDTO {

    /**
     * 规则ID
     */
    private Long id;

    /**
     * 规则编码
     */
    private String ruleCode;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则类型
     */
    private String ruleType;

    /**
     * 规则描述
     */
    private String description;

    /**
     * 关联的资产ID
     */
    private String assetId;

    /**
     * 关联的数据源ID
     */
    private String datasourceId;

    /**
     * SQL表达式
     */
    private String sqlExpression;

    /**
     * 期望值
     */
    private String expectedValue;

    /**
     * 阈值
     */
    private Double threshold;

    /**
     * 规则状态: DRAFT, PUBLISHED, ENABLED, DISABLED
     */
    private String status;

    /**
     * 严重程度: INFO, WARNING, CRITICAL
     */
    private String severity;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 附加属性
     */
    private Map<String, Object> attributes;
}
