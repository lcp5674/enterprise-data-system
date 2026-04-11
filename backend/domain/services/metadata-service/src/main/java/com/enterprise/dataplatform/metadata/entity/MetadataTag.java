package com.enterprise.dataplatform.metadata.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Metadata Tag Entity
 * Represents reusable tags for categorizing metadata objects
 */
@Entity
@Table(name = "metadata_tag", indexes = {
        @Index(name = "idx_tag_name", columnList = "tagName", unique = true),
        @Index(name = "idx_tag_type", columnList = "tagType")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadataTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String tagName;

    @Column(length = 50)
    private String tagType;

    @Column(length = 20)
    private String color;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
