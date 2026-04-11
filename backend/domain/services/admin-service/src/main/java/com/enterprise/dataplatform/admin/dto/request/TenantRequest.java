package com.enterprise.dataplatform.admin.dto.request;

import lombok.Data;

/**
 * Tenant Request DTO
 */
@Data
public class TenantRequest {
    private String tenantId;
    private String tenantName;
    private String description;
    private Integer maxUsers;
    private Long maxStorage;
    private String features; // JSON
    private String expiredAt; // ISO datetime
}
