package com.enterprise.edams.notification.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知消息VO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class NotificationMessageVO {

    private String id;
    private String messageType;
    private String userId;
    private String userName;
    private String email;
    private String phone;
    private String title;
    private String content;
    private String businessType;
    private String businessId;
    private String status;
    private LocalDateTime sendTime;
    private LocalDateTime readTime;
    private String errorMessage;
    private Integer retryCount;
    private String createdBy;
    private LocalDateTime createdTime;
}
