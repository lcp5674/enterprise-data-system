package com.edams.sla.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sla_report")
public class SlaReport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "agreement_id", nullable = false)
    private Long agreementId;
    
    @Column(name = "report_period", nullable = false)
    private String reportPeriod; // DAILY, WEEKLY, MONTHLY
    
    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;
    
    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;
    
    @Column(name = "metric_value", nullable = false)
    private Double metricValue;
    
    @Column(name = "compliance_rate", nullable = false)
    private Double complianceRate;
    
    @Column(name = "violation_count", nullable = false)
    private Integer violationCount;
    
    @Column(name = "warning_count", nullable = false)
    private Integer warningCount;
    
    @Column(name = "critical_count", nullable = false)
    private Integer criticalCount;
    
    @Column(name = "total_samples", nullable = false)
    private Integer totalSamples;
    
    @Column(name = "avg_value", nullable = false)
    private Double avgValue;
    
    @Column(name = "max_value", nullable = false)
    private Double maxValue;
    
    @Column(name = "min_value", nullable = false)
    private Double minValue;
    
    @Column(name = "analysis_result", nullable = false)
    private String analysisResult; // COMPLIANT, WARNING, CRITICAL
    
    @Column(name = "report_content", columnDefinition = "TEXT")
    private String reportContent;
    
    @Column(name = "generated_by", nullable = false)
    private Long generatedBy;
    
    @Column(name = "generated_time", nullable = false)
    private LocalDateTime generatedTime;
    
    @Column(name = "notification_status", nullable = false)
    private String notificationStatus; // PENDING, SENT
    
    @PrePersist
    protected void onCreate() {
        generatedTime = LocalDateTime.now();
        notificationStatus = "PENDING";
    }
}