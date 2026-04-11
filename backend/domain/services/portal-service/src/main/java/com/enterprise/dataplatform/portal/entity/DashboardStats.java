package com.enterprise.dataplatform.portal.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Dashboard Statistics Entity
 * 用户工作台统计数据
 */
@Data
@Entity
@Table(name = "dashboard_stats")
public class DashboardStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "stats_date", nullable = false)
    private LocalDate statsDate;

    @Column(name = "total_assets")
    private Long totalAssets = 0L;

    @Column(name = "active_assets")
    private Long activeAssets = 0L;

    @Column(name = "quality_score")
    private Double qualityScore = 0.0;

    @Column(name = "pending_tasks")
    private Integer pendingTasks = 0;

    @Column(name = "recent_views", columnDefinition = "TEXT")
    private String recentViews; // JSON array

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
