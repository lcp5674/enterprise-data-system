package com.enterprise.dataplatform.metadata.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Metadata Object Entity
 * Represents a metadata entity in the data catalog (TABLE, VIEW, API, FILE, STREAM)
 */
@Entity
@Table(name = "metadata_object", indexes = {
        @Index(name = "idx_object_id", columnList = "objectId", unique = true),
        @Index(name = "idx_object_type", columnList = "objectType"),
        @Index(name = "idx_domain_code", columnList = "domainCode"),
        @Index(name = "idx_owner", columnList = "owner"),
        @Index(name = "idx_sensitivity", columnList = "sensitivity"),
        @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadataObject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String objectId;

    @Column(nullable = false, length = 50)
    private String objectType;

    @Column(length = 100)
    private String domainCode;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 255)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "schema_info", columnDefinition = "TEXT")
    private String schemaInfo;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(length = 255)
    private String owner;

    @Column(length = 255)
    private String ownerEmail;

    @Column(length = 50)
    private String sensitivity;

    @Column(length = 50)
    private String status;

    @Column(length = 255)
    private String dataSource;

    @Column(length = 500)
    private String locationPath;

    private Long rowCount;

    private Long sizeBytes;

    private LocalDateTime lastUpdated;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
