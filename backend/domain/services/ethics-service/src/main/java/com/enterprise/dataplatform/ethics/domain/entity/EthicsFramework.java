package com.enterprise.dataplatform.ethics.domain.entity;

import com.enterprise.dataplatform.ethics.domain.enums.EthicsLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ethics_framework", indexes = {
    @Index(name = "idx_framework_code", columnList = "frameworkCode"),
    @Index(name = "idx_framework_name", columnList = "frameworkName"),
    @Index(name = "idx_framework_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EthicsFramework {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "framework_code", nullable = false, unique = true, length = 64)
    private String frameworkCode;

    @Column(name = "framework_name", nullable = false, length = 128)
    private String frameworkName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "principles", columnDefinition = "JSON")
    @Convert(converter = JsonListConverter.class)
    @Builder.Default
    private List<String> principles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_threshold", length = 32)
    private EthicsLevel riskThreshold;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private String status = "DRAFT";

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "creator", length = 64)
    private String creator;

    @CreationTimestamp
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "updater", length = 64)
    private String updater;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "tags", length = 512)
    private String tags;

    @Column(name = "category", length = 64)
    private String category;

    @Column(name = "source", length = 128)
    private String source;
}
