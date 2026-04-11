package com.enterprise.dataplatform.metadata.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Metadata Field Entity
 * Represents field-level metadata within a metadata object
 */
@Entity
@Table(name = "metadata_field", indexes = {
        @Index(name = "idx_field_object_id", columnList = "objectId"),
        @Index(name = "idx_field_name", columnList = "fieldName")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadataField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String objectId;

    @Column(nullable = false, length = 255)
    private String fieldName;

    @Column(length = 100)
    private String fieldType;

    private Integer fieldLength;

    private Boolean nullable;

    private Boolean primaryKey;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String sampleValues;

    @Column(length = 50)
    private String sensitivityLevel;

    @Column(columnDefinition = "TEXT")
    private String businessComment;

    private Integer ordinalPosition;
}
