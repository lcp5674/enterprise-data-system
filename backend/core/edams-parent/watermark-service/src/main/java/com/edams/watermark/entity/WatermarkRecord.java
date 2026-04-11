package com.edams.watermark.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "watermark_record")
public class WatermarkRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "watermark_id", nullable = false)
    private Long watermarkId;
    
    @Column(name = "detection_time", nullable = false)
    private LocalDateTime detectionTime;
    
    @Column(name = "detection_result", nullable = false)
    private String detectionResult;
    
    @Column(name = "detection_by", nullable = false)
    private Long detectionBy;
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
    }
}