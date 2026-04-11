package com.edams.incentive.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "points_record")
public class PointsRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "rule_id", nullable = false)
    private Long ruleId;
    
    @Column(name = "points", nullable = false)
    private Integer points;
    
    @Column(name = "description", nullable = false)
    private String description;
    
    @Column(name = "trigger_event", nullable = false)
    private String triggerEvent;
    
    @Column(name = "trigger_time", nullable = false)
    private LocalDateTime triggerTime;
    
    @Column(name = "status", nullable = false)
    private String status; // ISSUED, REDEEMED
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        status = "ISSUED";
    }
}