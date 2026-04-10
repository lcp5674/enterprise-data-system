package com.enterprise.dataplatform.governance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceTaskResponse {

    private Long id;

    private String taskName;

    private String taskType;

    private Long policyId;

    private String policyName;

    private String description;

    private Integer priority;

    private String priorityLabel;

    private String status;

    private String targetAssetId;

    private String targetAssetType;

    private Map<String, Object> taskConfig;

    private String dependsOnTasks;

    private String scheduleExpression;

    private LocalDateTime scheduledTime;

    private LocalDateTime actualStartTime;

    private LocalDateTime actualEndTime;

    private Long executionDuration;

    private String assignedTo;

    private Integer retryCount;

    private String lastError;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;
}
