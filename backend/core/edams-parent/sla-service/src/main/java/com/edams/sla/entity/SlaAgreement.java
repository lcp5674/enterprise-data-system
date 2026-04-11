package com.edams.sla.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sla_agreement")
public class SlaAgreement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "service_name", nullable = false)
    private String serviceName;
    
    @Column(name = "service_type", nullable = false)
    private String serviceType; // API, DATABASE, SYSTEM
    
    @Column(name = "target_object", nullable = false)
    private String targetObject; // 监控对象
    
    @Column(name = "metric_type", nullable = false)
    private String metricType; // RESPONSE_TIME, AVAILABILITY, ERROR_RATE
    
    @Column(name = "threshold_value", nullable = false)
    private Double thresholdValue;
    
    @Column(name = "threshold_unit", nullable = false)
    private String thresholdUnit; // ms, %, count
    
    @Column(name = "warning_level", nullable = false)
    private Double warningLevel;
    
    @Column(name = "critical_level", nullable = false)
    private Double criticalLevel;
    
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    
    @Column(name = "status", nullable = false)
    private String status; // ACTIVE, INACTIVE
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
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