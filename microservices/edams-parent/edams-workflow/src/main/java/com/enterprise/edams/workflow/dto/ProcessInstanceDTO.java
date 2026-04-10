package com.enterprise.edams.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程实例DTO
 * 
 * @author EDAMS Team
 */
@Data
@Schema(description = "流程实例")
public class ProcessInstanceDTO {

    @Schema(description = "流程实例ID")
    private String id;

    @Schema(description = "流程定义ID")
    private String processDefinitionId;

    @Schema(description = "流程定义名称")
    private String processDefinitionName;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "业务ID")
    private String businessId;

    @Schema(description = "业务标题")
    private String businessTitle;

    @Schema(description = "发起人ID")
    private String starterId;

    @Schema(description = "发起人名称")
    private String starterName;

    @Schema(description = "发起人部门名称")
    private String starterDeptName;

    @Schema(description = "当前节点名称")
    private String currentNodeName;

    @Schema(description = "当前处理人名称")
    private String currentAssigneeName;

    @Schema(description = "流程状态：0-运行中，1-已完成，2-已终止，3-已挂起")
    private Integer status;

    @Schema(description = "优先级：1-低，2-中，3-高，4-紧急")
    private Integer priority;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "持续时间（毫秒）")
    private Long duration;

    @Schema(description = "结果：0-通过，1-拒绝，2-撤回")
    private Integer result;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
}
