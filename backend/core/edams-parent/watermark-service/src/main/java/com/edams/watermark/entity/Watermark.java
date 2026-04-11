package com.edams.watermark.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "watermark")
public class Watermark {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "asset_id", nullable = false)
    private Long assetId;
    
    @Column(name = "asset_type", nullable = false)
    private String assetType;
    
    @Column(name = "watermark_type", nullable = false)
    private String watermarkType; // DIGITAL, VISIBLE, AUDIO
    
    @Column(name = "watermark_content", nullable = false)
    private String watermarkContent;
    
    @Column(name = "embedded_position")
    private String embeddedPosition;
    
    @Column(name = "embedded_time", nullable = false)
    private LocalDateTime embeddedTime;
    
    @Column(name = "embedded_by", nullable = false)
    private Long embeddedBy;
    
    @Column(name = "detection_result")
    private String detectionResult;
    
    @Column(name = "status", nullable = false)
    private String status; // EMBEDDED, REMOVED
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        status = "EMBEDDED";
    }
}