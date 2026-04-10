package com.enterprise.edams.workflow.feign.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 邮件请求DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class EmailRequest {

    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    private String email;

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
}
