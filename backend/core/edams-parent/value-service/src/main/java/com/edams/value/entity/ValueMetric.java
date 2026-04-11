package com.edams.value.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "value_metric")
public class ValueMetric {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "metric_name", nullable = false)
    private String metricName;
    
    @Column(name = "metric_type", nullable = false)
    private String metricType; // BUSINESS, TECHNICAL, FINANCIAL
    
    @Column(name = "metric_description")
    private String metricDescription;
    
    @Column(name = "calculation_method", nullable = false)
    private String calculationMethod; // FORMULA, AI_MODEL
    
    @Column(name = "calculation_formula")
    private String calculationFormula;
    
    @Column(name = "weight", nullable = false)
    private Double weight;
    
    @Column(name = "max_value", nullable = false)
    private Double maxValue;
    
    @Column(name = "min_value", nullable = false)
    private Double minValue;
    
    @Column(name = "reference_data")
    private String referenceData; // JSON格式的参考数据
    
    @Column(name = "status", nullable = false)
    private String status; // ACTIVE, INACTIVE
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
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