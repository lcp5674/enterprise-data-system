package com.enterprise.dataplatform.ruleengine.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 规则定义实体
 */
@Data
@Entity
@Table(name = "rule_definition")
public class RuleDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 规则名称 */
    @Column(name = "rule_name", nullable = false, length = 200)
    private String ruleName;

    /** 规则编码（唯一） */
    @Column(name = "rule_code", nullable = false, unique = true, length = 100)
    private String ruleCode;

    /** 规则分类: QUALITY, COMPLIANCE, VALUE, LIFECYCLE, GOVERNANCE */
    @Column(name = "category", nullable = false, length = 50)
    private String category;

    /** 规则描述 */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 规则DRL内容 */
    @Column(name = "rule_content", columnDefinition = "TEXT")
    private String ruleContent;

    /** 规则文件路径 */
    @Column(name = "rule_file_path", length = 500)
    private String ruleFilePath;

    /** 状态: ACTIVE, INACTIVE, DRAFT */
    @Column(name = "status", nullable = false, length = 20)
    private String status = "DRAFT";

    /** 优先级 */
    @Column(name = "priority")
    private Integer priority = 0;

    /** 版本号 */
    @Column(name = "version", length = 20)
    private String version = "v1.0";

    /** 触发条件 */
    @Column(name = "trigger_condition", columnDefinition = "TEXT")
    private String triggerCondition;

    /** 规则参数配置（JSON格式） */
    @Column(name = "parameters", columnDefinition = "TEXT")
    private String parameters;

    /** 创建者 */
    @Column(name = "created_by", length = 100)
    private String createdBy;

    /** 修改者 */
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @CreationTimestamp
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    /** 逻辑删除 */
    @Column(name = "deleted")
    private Integer deleted = 0;
}
