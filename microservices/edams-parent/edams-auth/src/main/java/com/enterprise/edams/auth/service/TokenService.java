package com.enterprise.edams.auth.service;

import java.util.Map;

/**
 * Token服务接口
 *
 * @author Backend Team
 * @version 1.0.0
 */
public interface TokenService {

    /**
     * 生成访问令牌
     */
    String generateAccessToken(String userId, String username, Map<String, Object> claims);

    /**
     * 生成刷新令牌
     */
    String generateRefreshToken(String userId);

    /**
     * 解析访问令牌
     */
    Map<String, Object> parseAccessToken(String token);

    /**
     * 验证令牌
     */
    boolean validateToken(String token);

    /**
     * 获取令牌中的用户ID
     */
    String getUserIdFromToken(String token);

    /**
     * 获取令牌中的用户名
     */
    String getUsernameFromToken(String token);

    /**
     * 使令牌失效
     */
    void invalidateToken(String token);

    /**
     * 使所有用户令牌失效
     */
    void invalidateAllUserTokens(String userId);

    /**
     * 检查令牌是否在黑名单中
     */
    boolean isTokenBlacklisted(String token);

    /**
     * 将令牌加入黑名单
     */
    void blacklistToken(String token);

    /**
     * 获取访问令牌过期时间（秒）
     */
    long getAccessTokenExpiration();

    /**
     * 获取刷新令牌过期时间（秒）
     */
    long getRefreshTokenExpiration();
}
