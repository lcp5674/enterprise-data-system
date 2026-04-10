package com.enterprise.edams.asset.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 流程启动请求DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class ProcessInstanceStartRequest {

    /**
     * 流程定义编码
     */
    @NotBlank(message = "流程定义编码不能为空")
    private String processDefinitionKey;

    /**
     * 业务类型
     */
    @NotBlank(message = "业务类型不能为空")
    private String businessType;

    /**
     * 业务ID
     */
    @NotBlank(message = "业务ID不能为空")
    private String businessId;

    /**
     * 流程名称(可选)
     */
    private String processName;

    /**
     * 流程变量
     */
    private Map<String, Object> variables;

    /**
     * 第一个审批人(可选)
     */
    private String firstApprover;
}
