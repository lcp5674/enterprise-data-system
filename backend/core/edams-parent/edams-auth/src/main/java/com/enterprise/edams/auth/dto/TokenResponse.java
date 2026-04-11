package com.enterprise.edams.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JWT令牌响应DTO
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    /**
     * 访问令牌（AccessToken）
     */
    private String accessToken;

    /**
     * 刷新令牌（RefreshToken）
     */
    private String refreshToken;

    /**
     * 令牌类型：Bearer
     */
    private String tokenType = "Bearer";

    /**
     * 过期时间（秒）
     */
    private Long expiresIn;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;
}
