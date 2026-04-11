package com.enterprise.dataplatform.portal.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * User Activity Response DTO
 */
@Data
public class ActivityResponse {
    private String activityId;
    private String userId;
    private String action;
    private String resourceId;
    private String resourceName;
    private LocalDateTime timestamp;
    private String description;
}
