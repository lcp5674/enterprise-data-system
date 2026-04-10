package com.enterprise.edams.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 任务审批请求
 * 
 * @author EDAMS Team
 */
@Data
@Schema(description = "任务审批请求")
public class TaskApproveRequest {

    @NotBlank(message = "任务ID不能为空")
    @Schema(description = "任务ID", required = true)
    private String taskId;

    @Schema(description = "审批结果：0-通过，1-拒绝，2-退回")
    private Integer result;

    @Schema(description = "审批意见")
    private String comment;

    @Schema(description = "退回节点ID（退回时使用）")
    private String backNodeId;

    @Schema(description = "转办人ID（转办时使用）")
    private String transferToUserId;

    @Schema(description = "委托人ID（委托时使用）")
    private String delegateToUserId;

    @Schema(description = "表单数据")
    private Map<String, Object> formData;
}
