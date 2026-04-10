package com.enterprise.edams.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 流程定义创建请求
 * 
 * @author EDAMS Team
 */
@Data
@Schema(description = "流程定义创建请求")
public class ProcessDefinitionCreateRequest {

    @NotBlank(message = "流程定义Key不能为空")
    @Schema(description = "流程定义Key", required = true)
    private String processKey;

    @NotBlank(message = "流程定义名称不能为空")
    @Schema(description = "流程定义名称", required = true)
    private String name;

    @Schema(description = "流程定义描述")
    private String description;

    @Schema(description = "流程分类")
    private String category;

    @Schema(description = "BPMN XML内容")
    private String bpmnXml;
}
