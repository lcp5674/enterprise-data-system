package com.edams.value.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "data_value")
public class DataValue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "asset_id", nullable = false)
    private Long assetId;
    
    @Column(name = "asset_type", nullable = false)
    private String assetType; // DATASET, API, DATABASE
    
    @Column(name = "asset_name", nullable = false)
    private String assetName;
    
    @Column(name = "value_score", nullable = false)
    private Double valueScore;
    
    @Column(name = "value_category", nullable = false)
    private String valueCategory; // BUSINESS, TECHNICAL, FINANCIAL
    
    @Column(name = "assessment_method", nullable = false)
    private String assessmentMethod; // AI, MANUAL, HYBRID
    
    @Column(name = "assessment_factors", nullable = false, columnDefinition = "TEXT")
    private String assessmentFactors; // JSON格式的评估因子
    
    @Column(name = "value_details", columnDefinition = "TEXT")
    private String valueDetails; // JSON格式的价值详情
    
    @Column(name = "value_trend")
    private Double valueTrend; // 价值趋势（增长率）
    
    @Column(name = "assessment_date", nullable = false)
    private LocalDateTime assessmentDate;
    
    @Column(name = "assessor_id", nullable = false)
    private Long assessorId;
    
    @Column(name = "validity_period", nullable = false)
    private Integer validityPeriod; // 有效期（天）
    
    @Column(name = "status", nullable = false)
    private String status; // ACTIVE, INACTIVE
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
    
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        status = "ACTIVE";
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}