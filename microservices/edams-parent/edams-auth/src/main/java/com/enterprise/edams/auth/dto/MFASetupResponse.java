package com.enterprise.edams.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * MFA设置响应DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "MFA设置响应")
public class MFASetupResponse {

    @Schema(description = "MFA类型: TOTP/EMAIL/SMS")
    private String mfaType;

    @Schema(description = "密钥（仅TOTP类型返回）")
    private String secret;

    @Schema(description = "二维码URL（仅TOTP类型返回）")
    private String qrCodeUrl;

    @Schema(description = "备用码列表")
    private List<String> backupCodes;

    @Schema(description = "MFA绑定状态")
    private Boolean enabled;
}
