package com.enterprise.dataplatform.governance.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GovernanceTaskRequest {

    @NotBlank(message = "任务名称不能为空")
    private String taskName;

    @NotBlank(message = "任务类型不能为空")
    private String taskType;

    @NotNull(message = "策略ID不能为空")
    private Long policyId;

    private String description;

    @NotNull(message = "任务优先级不能为空")
    private Integer priority;

    private String targetAssetId;

    private String targetAssetType;

    private Map<String, Object> taskConfig;

    private List<String> dependsOnTasks;

    private String scheduleExpression;

    private LocalDateTime scheduledTime;

    private String assignedTo;

    private String callbackUrl;
}
