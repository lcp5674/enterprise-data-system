package com.enterprise.dataplatform.admin.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Tenant Response DTO
 */
@Data
public class TenantResponse {
    private Long id;
    private String tenantId;
    private String tenantName;
    private String description;
    private Integer maxUsers;
    private Long maxStorage;
    private String features;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private TenantStats stats;

    @Data
    public static class TenantStats {
        private Long currentUsers;
        private Long totalAssets;
        private Long usedStorage;
    }
}
