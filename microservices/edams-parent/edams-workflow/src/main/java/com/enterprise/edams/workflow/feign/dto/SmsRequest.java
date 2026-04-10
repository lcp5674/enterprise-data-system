package com.enterprise.edams.workflow.feign.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 短信请求DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class SmsRequest {

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    private String phone;

    /**
     * 内容
     */
    @NotBlank(message = "内容不能为空")
    private String content;
}
