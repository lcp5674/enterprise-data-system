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
public class TaskExecutionResponse {

    private Long id;

    private Long taskId;

    private String taskName;

    private String executionId;

    private String executionType;

    private String status;

    private Map<String, Object> executionParams;

    private Map<String, Object> executionResult;

    private String triggeredBy;

    private String triggerSource;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long duration;

    private Integer attemptCount;

    private String errorMessage;

    private String stackTrace;

    private String callbackUrl;

    private LocalDateTime createdAt;
}
