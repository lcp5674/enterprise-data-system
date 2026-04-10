package com.enterprise.dataplatform.quality.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 检查执行请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckExecutionRequest {

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    private String executionParams;

    private String executor;
}
