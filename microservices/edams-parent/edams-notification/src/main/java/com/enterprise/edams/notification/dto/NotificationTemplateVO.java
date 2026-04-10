package com.enterprise.edams.notification.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知模板VO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class NotificationTemplateVO {

    private String id;
    private String code;
    private String name;
    private String templateType;
    private String title;
    private String content;
    private String variables;
    private String description;
    private Integer status;
    private String createdBy;
    private LocalDateTime createdTime;
    private String updatedBy;
    private LocalDateTime updatedTime;
}
