package com.edams.incentive.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "incentive_rule")
public class IncentiveRule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "rule_name", nullable = false)
    private String ruleName;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "rule_type", nullable = false)
    private String ruleType; // CONTRIBUTION, QUALITY, COLLABORATION
    
    @Column(name = "points_value", nullable = false)
    private Integer pointsValue;
    
    @Column(name = "trigger_condition", nullable = false)
    private String triggerCondition;
    
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