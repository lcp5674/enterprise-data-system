package com.enterprise.dataplatform.portal.dto.request;

import lombok.Data;

/**
 * Announcement Request DTO
 */
@Data
public class AnnouncementRequest {
    private String title;
    private String content;
    private String type; // INFO, WARNING, ALERT
    private Integer priority;
    private String targetRoles; // JSON array
    private String expiredAt; // ISO datetime
}
