package com.edams.version.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "version_diff")
public class VersionDiff {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "version_id_from", nullable = false)
    private Long versionIdFrom;
    
    @Column(name = "version_id_to", nullable = false)
    private Long versionIdTo;
    
    @Column(name = "diff_content", nullable = false, columnDefinition = "TEXT")
    private String diffContent;
    
    @Column(name = "diff_type", nullable = false)
    private String diffType; // ADD, DELETE, MODIFY
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
    }
}