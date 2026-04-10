package com.enterprise.edams.workflow.feign.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 站内消息请求DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class InAppMessageRequest {

    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 内容
     */
    @NotBlank(message = "内容不能为空")
    private String content;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务ID
     */
    private String businessId;
}
