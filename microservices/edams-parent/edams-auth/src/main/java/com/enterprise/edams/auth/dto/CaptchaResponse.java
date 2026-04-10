package com.enterprise.edams.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 验证码响应DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "验证码响应")
public class CaptchaResponse {

    @Schema(description = "验证码Key")
    private String captchaKey;

    @Schema(description = "验证码图片Base64")
    private String captchaImage;

    @Schema(description = "验证码过期时间（秒）")
    private Long expiresIn;
}
