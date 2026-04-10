package com.enterprise.edams.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程任务DTO
 * 
 * @author EDAMS Team
 */
@Data
@Schema(description = "流程任务")
public class ProcessTaskDTO {

    @Schema(description = "任务ID")
    private String id;

    @Schema(description = "流程实例ID")
    private String processInstanceId;

    @Schema(description = "流程定义名称")
    private String processDefinitionName;

    @Schema(description = "业务标题")
    private String businessTitle;

    @Schema(description = "任务节点名称")
    private String taskNodeName;

    @Schema(description = "任务类型：1-审批，2-抄送，3-会签")
    private Integer taskType;

    @Schema(description = "处理人ID")
    private String assigneeId;

    @Schema(description = "处理人名称")
    private String assigneeName;

    @Schema(description = "任务状态：0-待处理，1-已处理，2-已转办，3-已委托")
    private Integer status;

    @Schema(description = "审批结果：0-通过，1-拒绝，2-退回，3-转办，4-委托")
    private Integer result;

    @Schema(description = "审批意见")
    private String comment;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

    @Schema(description = "截止时间")
    private LocalDateTime dueTime;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "发起人名称")
    private String starterName;

    @Schema(description = "优先级")
    private Integer priority;
}
