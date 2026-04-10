package com.enterprise.edams.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 通知模板创建请求DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class NotificationTemplateCreateRequest {

    @NotBlank(message = "模板编码不能为空")
    private String code;

    @NotBlank(message = "模板名称不能为空")
    private String name;

    @NotBlank(message = "模板类型不能为空")
    private String templateType;

    @NotBlank(message = "模板标题不能为空")
    private String title;

    @NotBlank(message = "模板内容不能为空")
    private String content;

    /**
     * 模板变量(JSON格式)
     */
    private String variables;

    /**
     * 模板描述
     */
    private String description;
}
