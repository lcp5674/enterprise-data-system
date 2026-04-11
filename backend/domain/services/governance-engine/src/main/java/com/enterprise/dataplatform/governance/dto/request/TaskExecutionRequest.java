package com.enterprise.dataplatform.governance.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskExecutionRequest {

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    private String executionType;

    private Map<String, Object> executionParams;

    private String triggeredBy;

    private String callbackUrl;

    private Integer timeoutSeconds;
}
