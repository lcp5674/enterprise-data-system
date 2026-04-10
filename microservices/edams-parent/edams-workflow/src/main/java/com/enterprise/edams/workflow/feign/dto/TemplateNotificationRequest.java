package com.enterprise.edams.workflow.feign.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 模板通知请求DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class TemplateNotificationRequest {

    /**
     * 模板编码
     */
    @NotBlank(message = "模板编码不能为空")
    private String templateCode;

    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 模板变量
     */
    private java.util.Map<String, String> variables;
}
