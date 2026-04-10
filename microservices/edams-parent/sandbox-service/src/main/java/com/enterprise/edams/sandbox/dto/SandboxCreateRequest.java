package com.enterprise.edams.sandbox.dto;

import com.enterprise.edams.sandbox.entity.SandboxType;
import lombok.Data;

@Data
public class SandboxCreateRequest {
    
    private String instanceName;
    private SandboxType sandboxType;
    private Long userId;
    private String userName;
    private String assetIds;
    private String description;
    private Integer validHours;
    private String resourceConfig;
}
