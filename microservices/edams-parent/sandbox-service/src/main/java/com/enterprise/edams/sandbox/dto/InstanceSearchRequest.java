package com.enterprise.edams.sandbox.dto;

import com.enterprise.edams.sandbox.entity.InstanceStatus;
import com.enterprise.edams.sandbox.entity.SandboxType;
import lombok.Data;

@Data
public class InstanceSearchRequest {
    
    private Long userId;
    private String instanceName;
    private SandboxType sandboxType;
    private InstanceStatus status;
    private int pageNum = 1;
    private int pageSize = 20;
}
