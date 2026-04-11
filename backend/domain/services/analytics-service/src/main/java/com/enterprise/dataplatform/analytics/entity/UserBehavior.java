package com.enterprise.dataplatform.analytics.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * User Behavior Entity - stores user activity logs for behavior analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBehavior {

    private Long id;
    private String userId;
    private String userName;
    private String department;
    private String actionType;
    private String assetId;
    private String assetName;
    private String assetType;
    private Integer duration;
    private String resultStatus;
    private String sessionId;
    private String ipAddress;
    private String userAgent;
    private String errorMessage;
    private String metadata;
    private LocalDateTime timestamp;
    private LocalDate date;
}
