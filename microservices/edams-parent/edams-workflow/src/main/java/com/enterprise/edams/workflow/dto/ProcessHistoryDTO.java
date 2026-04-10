package com.enterprise.edams.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 流程历史记录DTO
 * 
 * @author EDAMS Team
 */
@Data
@Schema(description = "流程历史记录")
public class ProcessHistoryDTO {

    @Schema(description = "历史记录ID")
    private String id;

    @Schema(description = "流程实例ID")
    private String processInstanceId;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "节点类型：1-开始节点，2-任务节点，3-网关，4-结束节点")
    private Integer nodeType;

    @Schema(description = "操作人名称")
    private String operatorName;

    @Schema(description = "操作类型：1-发起，2-审批，3-转办，4-委托，5-退回，6-终止，7-撤回")
    private Integer operationType;

    @Schema(description = "操作结果：0-通过，1-拒绝，2-退回，3-转办，4-委托，5-终止")
    private Integer operationResult;

    @Schema(description = "操作意见")
    private String comment;

    @Schema(description = "操作时间")
    private LocalDateTime operationTime;

    @Schema(description = "持续时间（毫秒）")
    private Long duration;
}
