package com.enterprise.edams.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 流程实例启动请求
 * 
 * @author EDAMS Team
 */
@Data
@Schema(description = "流程实例启动请求")
public class ProcessInstanceStartRequest {

    @NotBlank(message = "流程定义Key不能为空")
    @Schema(description = "流程定义Key", required = true)
    private String processDefinitionKey;

    @Schema(description = "业务类型")
    private String businessType;

    @Schema(description = "业务ID")
    private String businessId;

    @Schema(description = "业务标题")
    private String businessTitle;

    @Schema(description = "优先级：1-低，2-中，3-高，4-紧急")
    private Integer priority;

    @Schema(description = "表单数据")
    private Map<String, Object> formData;

    @Schema(description = "流程变量")
    private Map<String, Object> variables;
}
