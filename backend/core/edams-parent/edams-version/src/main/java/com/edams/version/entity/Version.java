package com.edams.version.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "version")
public class Version {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "version_number", nullable = false)
    private String versionNumber;
    
    @Column(name = "object_type", nullable = false)
    private String objectType; // DATASET, API, RULE
    
    @Column(name = "object_id", nullable = false)
    private Long objectId;
    
    @Column(name = "object_name", nullable = false)
    private String objectName;
    
    @Column(name = "version_content", nullable = false, columnDefinition = "TEXT")
    private String versionContent;
    
    @Column(name = "change_log", columnDefinition = "TEXT")
    private String changeLog;
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent;
    
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
    }
}