package com.enterprise.dataplatform.lineage.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Lineage snapshot entity for point-in-time recovery
 */
@Entity
@Table(name = "lineage_snapshot")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineageSnapshot {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "snapshot_name", length = 100)
    private String snapshotName;

    @Column(name = "snapshot_time", nullable = false)
    private LocalDateTime snapshotTime;

    @Column(name = "asset_count")
    private Integer assetCount;

    @Column(name = "relation_count")
    private Integer relationCount;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "COMPLETED";

    @Column(name = "created_by", length = 32)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;
}
