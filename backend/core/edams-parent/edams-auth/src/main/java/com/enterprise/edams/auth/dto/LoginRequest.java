package com.enterprise.edams.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录请求DTO
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度为3-50个字符")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度为6-100个字符")
    private String password;

    /**
     * MFA验证码（可选）
     */
    @Size(min = 6, max = 6, message = "MFA验证码为6位数字")
    private String mfaCode;

    /**
     * 验证码key（图形验证码）
     */
    private String captchaKey;

    /**
     * 验证码值
     */
    private String captchaCode;
}
