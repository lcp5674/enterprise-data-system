package com.enterprise.dataplatform.admin.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * System Config Response DTO
 */
@Data
public class SystemConfigResponse {
    private Long id;
    private String configKey;
    private String configValue;
    private String configType;
    private String description;
    private String category;
    private String updatedBy;
    private LocalDateTime updatedAt;
}
