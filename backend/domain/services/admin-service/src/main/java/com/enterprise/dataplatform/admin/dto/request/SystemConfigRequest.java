package com.enterprise.dataplatform.admin.dto.request;

import lombok.Data;

/**
 * System Config Request DTO
 */
@Data
public class SystemConfigRequest {
    private String configKey;
    private String configValue;
    private String configType; // STRING, NUMBER, BOOLEAN, JSON
    private String description;
    private String category;
}
