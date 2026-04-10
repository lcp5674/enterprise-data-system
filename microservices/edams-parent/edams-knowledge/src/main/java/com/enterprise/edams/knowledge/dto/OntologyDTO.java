package com.enterprise.edams.knowledge.dto;

import lombok.Data;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 本体DTO
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Data
@Builder
public class OntologyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 本体ID
     */
    private String ontologyId;

    /**
     * 本体名称
     */
    private String name;

    /**
     * 本体描述
     */
    private String description;

    /**
     * 版本号
     */
    private String version;

    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 状态
     */
    private String status;

    /**
     * 所属图谱ID
     */
    private String graphId;

    /**
     * 实体类型列表
     */
    private List<EntityTypeDTO> entityTypes;

    /**
     * 关系类型列表
     */
    private List<RelationTypeDTO> relationTypes;

    /**
     * 属性定义
     */
    private List<PropertyDefinitionDTO> properties;

    /**
     * 约束规则
     */
    private List<ConstraintRuleDTO> constraints;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 实体类型DTO
     */
    @Data
    @Builder
    public static class EntityTypeDTO implements Serializable {
        private String typeId;
        private String typeName;
        private String description;
        private String parentTypeId;
        private List<PropertyDefinitionDTO> attributes;
        private List<ConstraintRuleDTO> validationRules;
        private String icon;
        private String color;
    }

    /**
     * 关系类型DTO
     */
    @Data
    @Builder
    public static class RelationTypeDTO implements Serializable {
        private String typeId;
        private String typeName;
        private String description;
        private String direction;
        private String sourceTypeConstraint;
        private String targetTypeConstraint;
        private Boolean reversible;
        private String inverseRelationTypeId;
        private String cardinality;
        private List<PropertyDefinitionDTO> attributes;
    }

    /**
     * 属性定义DTO
     */
    @Data
    @Builder
    public static class PropertyDefinitionDTO implements Serializable {
        private String propertyName;
        private String dataType;
        private Boolean required;
        private String defaultValue;
        private String description;
        private Map<String, Object> constraints;
    }

    /**
     * 约束规则DTO
     */
    @Data
    @Builder
    public static class ConstraintRuleDTO implements Serializable {
        private String ruleId;
        private String ruleType;
        private String ruleExpression;
        private String errorMessage;
    }
}
