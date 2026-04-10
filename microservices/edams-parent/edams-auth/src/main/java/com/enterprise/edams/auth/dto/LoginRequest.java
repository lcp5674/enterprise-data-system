package com.enterprise.edams.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
@Schema(description = "登录请求")
public class LoginRequest {

    @Schema(description = "用户名/邮箱/手机号")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "验证码")
    private String captcha;

    @Schema(description = "验证码Key")
    private String captchaKey;

    @Schema(description = "记住我")
    private Boolean rememberMe = false;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "设备类型: WEB/APP/API")
    private String deviceType = "WEB";
}
