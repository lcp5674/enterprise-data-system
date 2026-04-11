package com.edams.sandbox.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sandbox")
public class Sandbox {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    
    @Column(name = "status", nullable = false)
    private String status; // CREATED, RUNNING, STOPPED, EXPIRED
    
    @Column(name = "sandbox_type", nullable = false)
    private String sandboxType; // SQL, API, DATA
    
    @Column(name = "resource_config")
    private String resourceConfig; // JSON格式的资源配置
    
    @Column(name = "expire_time")
    private LocalDateTime expireTime;
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
    
    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        status = "CREATED";
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedTime = LocalDateTime.now();
    }
}