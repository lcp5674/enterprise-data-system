package com.enterprise.dataplatform.masking.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "masking_rule", indexes = {
    @Index(name = "idx_rule_asset_id", columnList = "assetId"),
    @Index(name = "idx_rule_column_name", columnList = "columnName")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaskingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_id", nullable = false, length = 64)
    private String assetId;

    @Column(name = "asset_name", length = 256)
    private String assetName;

    @Column(name = "column_name", nullable = false, length = 128)
    private String columnName;

    @Column(name = "column_type", length = 64)
    private String columnType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "masking_config_id")
    private MaskingConfig maskingConfig;

    @Column(name = "masking_type", length = 32)
    private String maskingType;

    @Column(name = "priority", nullable = false)
    private Integer priority;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "condition_expression", columnDefinition = "TEXT")
    private String conditionExpression;

    @Column(name = "classification_level", length = 32)
    private String classificationLevel;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
