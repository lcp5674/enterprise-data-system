package com.enterprise.dataplatform.ruleengine.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 规则集实体
 * 将多条规则组合成一个规则集，便于批量执行
 */
@Data
@Entity
@Table(name = "rule_set")
public class RuleSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 规则集名称 */
    @Column(name = "set_name", nullable = false, length = 200)
    private String setName;

    /** 规则集编码 */
    @Column(name = "set_code", nullable = false, unique = true, length = 100)
    private String setCode;

    /** 规则集描述 */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 规则分类 */
    @Column(name = "category", length = 50)
    private String category;

    /** 状态 */
    @Column(name = "status", length = 20)
    private String status = "DRAFT";

    /** 规则数量 */
    @Column(name = "rule_count")
    private Integer ruleCount = 0;

    /** 创建者 */
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    @UpdateTimestamp
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
