package com.enterprise.dataplatform.portal.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Announcement Response DTO
 */
@Data
public class AnnouncementResponse {
    private Long id;
    private String title;
    private String content;
    private String type;
    private Integer priority;
    private String publishedBy;
    private LocalDateTime publishedAt;
    private LocalDateTime expiredAt;
    private String status;
    private List<String> targetRoles;
}
