package com.enterprise.edams.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Token刷新请求DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
@Schema(description = "Token刷新请求")
public class TokenRefreshRequest {

    @Schema(description = "刷新令牌")
    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;

    @Schema(description = "访问令牌")
    private String accessToken;
}
