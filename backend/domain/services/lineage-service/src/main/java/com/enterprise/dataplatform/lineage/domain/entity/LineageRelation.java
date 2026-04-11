package com.enterprise.dataplatform.lineage.domain.entity;

import com.enterprise.dataplatform.lineage.domain.enums.LineageType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Lineage relation entity stored in PostgreSQL
 */
@Entity
@Table(name = "lineage_relation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"sourceAsset", "targetAsset"})
@EqualsAndHashCode(exclude = {"sourceAsset", "targetAsset"})
public class LineageRelation {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "source_asset_id", length = 32, nullable = false)
    private String sourceAssetId;

    @Column(name = "source_field_id", length = 32)
    private String sourceFieldId;

    @Column(name = "target_asset_id", length = 32, nullable = false)
    private String targetAssetId;

    @Column(name = "target_field_id", length = 32)
    private String targetFieldId;

    @Enumerated(EnumType.STRING)
    @Column(name = "lineage_type", length = 20, nullable = false)
    private LineageType lineageType;

    @Column(name = "transform_sql", columnDefinition = "TEXT")
    private String transformSql;

    @Column(name = "transform_desc", length = 500)
    private String transformDesc;

    @Column(name = "task_name", length = 100)
    private String taskName;

    @Column(name = "job_id", length = 100)
    private String jobId;

    @Column(name = "schedule_time", length = 50)
    private String scheduleTime;

    @Column(name = "confidence", precision = 5, scale = 2)
    @Builder.Default
    private Double confidence = 100.0;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "verified_by", length = 32)
    private String verifiedBy;

    @Column(name = "verified_time")
    private LocalDateTime verifiedTime;

    @Column(name = "verification_method", length = 20)
    private String verificationMethod;

    @Column(name = "source_system", length = 100)
    private String sourceSystem;

    @Column(name = "path_type", length = 20)
    private String pathType;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_by", length = 32)
    private String deletedBy;

    @Column(name = "deleted_time")
    private LocalDateTime deletedTime;

    @Column(name = "created_by", length = 32)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;

    @Column(name = "updated_by", length = 32)
    private String updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
}
