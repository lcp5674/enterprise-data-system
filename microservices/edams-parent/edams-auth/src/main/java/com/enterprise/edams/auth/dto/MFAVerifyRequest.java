package com.enterprise.edams.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * MFA验证请求DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
@Schema(description = "MFA验证请求")
public class MFAVerifyRequest {

    @Schema(description = "MFA临时令牌")
    @NotBlank(message = "MFA令牌不能为空")
    private String mfaToken;

    @Schema(description = "验证码（TOTP）或备用码")
    @NotBlank(message = "验证码不能为空")
    private String code;

    @Schema(description = "验证类型: TOTP/BACKUP_CODE")
    private String verifyType = "TOTP";

    @Schema(description = "记住设备")
    private Boolean rememberDevice = false;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "设备类型: WEB/APP/API")
    private String deviceType = "WEB";
}
