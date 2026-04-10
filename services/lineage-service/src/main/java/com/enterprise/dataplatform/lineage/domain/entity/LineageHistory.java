package com.enterprise.dataplatform.lineage.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Lineage change history entity
 */
@Entity
@Table(name = "lineage_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LineageHistory {

    @Id
    @Column(name = "id", length = 32)
    private String id;

    @Column(name = "lineage_id", length = 32, nullable = false)
    private String lineageId;

    @Column(name = "change_type", length = 20)
    private String changeType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value", columnDefinition = "jsonb")
    private Map<String, Object> oldValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value", columnDefinition = "jsonb")
    private Map<String, Object> newValue;

    @Column(name = "change_reason", columnDefinition = "TEXT")
    private String changeReason;

    @Column(name = "created_by", length = 32)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_time", updatable = false)
    private LocalDateTime createdTime;
}
