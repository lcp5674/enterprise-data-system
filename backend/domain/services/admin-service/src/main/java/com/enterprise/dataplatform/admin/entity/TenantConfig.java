package com.enterprise.dataplatform.admin.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Tenant Configuration Entity
 * 租户配置
 */
@Data
@Entity
@Table(name = "tenant_config")
public class TenantConfig {

    public enum TenantStatus {
        ACTIVE,     // 激活
        SUSPENDED   // 暂停
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", unique = true, nullable = false)
    private String tenantId;

    @Column(name = "tenant_name", nullable = false)
    private String tenantName;

    @Column(name = "description")
    private String description;

    @Column(name = "max_users")
    private Integer maxUsers = 100;

    @Column(name = "max_storage")
    private Long maxStorage = 1073741824L; // 1GB default

    @Column(name = "features", columnDefinition = "TEXT")
    private String features; // JSON object

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenantStatus status = TenantStatus.ACTIVE;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
