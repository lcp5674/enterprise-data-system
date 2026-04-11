package com.enterprise.edams.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置属性
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT签名密钥
     */
    private String secret = "edams-jwt-secret-key-2026";

    /**
     * 访问令牌过期时间（毫秒）
     */
    private long expiration = 86400000L; // 24小时

    /**
     * 刷新令牌过期时间（毫秒）
     */
    private long refreshExpiration = 604800000L; // 7天

    /**
     * 令牌前缀
     */
    private String tokenPrefix = "Bearer ";

    /**
     * 令牌Header名称
     */
    private String headerName = "Authorization";

    /**
     * 最大登录失败次数（超过则锁定账户）
     */
    private int maxLoginAttempts = 5;

    /**
     * 账户锁定时长（分钟）
     */
    private int lockDurationMinutes = 30;

    /**
     * IP登录频率限制（每分钟最大尝试次数）
     */
    private int ipRateLimitPerMinute = 10;
}
