package com.enterprise.edams.auth.security;

import com.enterprise.edams.auth.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * JWT令牌提供者
 *
 * <p>负责JWT令牌的生成、验证、解析和刷新</p>
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Redis中存储的令牌黑名单前缀
     */
    private static final String TOKEN_BLACKLIST_PREFIX = "edams:token:blacklist:";
    /**
     * Redis中存储的刷新令牌前缀
     */
    private static final String REFRESH_TOKEN_PREFIX = "edams:token:refresh:";
    /**
     * 用户在线状态前缀
     */
    private static final String USER_ONLINE_PREFIX = "edams:user:online:";

    /**
     * 获取签名密钥
     */
    public SecretKey getSigningKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成访问令牌（AccessToken）
     */
    public String generateAccessToken(Long userId, String username, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("roles", roles)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成刷新令牌（RefreshToken）
     */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getRefreshExpiration());
        String tokenId = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .id(tokenId)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();

        // 将刷新令牌存入Redis
        stringRedisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + userId,
                token,
                jwtProperties.getRefreshExpiration(),
                TimeUnit.MILLISECONDS);

        // 记录用户在线状态
        stringRedisTemplate.opsForValue().set(
                USER_ONLINE_PREFIX + userId,
                String.valueOf(System.currentTimeMillis()),
                jwtProperties.getRefreshExpiration(),
                TimeUnit.MILLISECONDS);

        return token;
    }

    /**
     * 验证令牌有效性
     */
    public boolean validateToken(String token) {
        try {
            // 检查是否在黑名单中
            if (isTokenBlacklisted(token)) {
                log.warn("Token已失效（在黑名单中）");
                return false;
            }

            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException e) {
            log.warn("无效的JWT令牌格式");
        } catch (ExpiredJwtException e) {
            log.warn("JWT令牌已过期");
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的JWT令牌");
        } catch (IllegalArgumentException e) {
            log.warn("JWT令牌为空");
        } catch (Exception e) {
            log.warn("JWT令牌验证失败: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 从令牌中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        if (claims != null) {
            return Long.parseLong(claims.getSubject());
        }
        return null;
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseClaims(token);
        if (claims != null) {
            return claims.get("username", String.class);
        }
        return null;
    }

    /**
     * 从令牌中获取角色列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = parseClaims(token);
        if (claims != null && claims.get("roles") != null) {
            return claims.get("roles", List.class);
        }
        return Collections.emptyList();
    }

    /**
     * 检查令牌是否即将过期（剩余时间小于30分钟）
     */
    public boolean isTokenExpiringSoon(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            Date expiration = claims.getExpiration();
            long diff = expiration.getTime() - System.currentTimeMillis();
            // 剩余时间小于30分钟
            return diff < 1800000L;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 将令牌加入黑名单（用于注销登录）
     */
    public void blacklistToken(String token) {
        try {
            Claims claims = parseClaims(token);
            if (claims != null) {
                Date expiration = claims.getExpiration();
                long ttl = expiration.getTime() - System.currentTimeMillis();
                
                if (ttl > 0) {
                    stringRedisTemplate.opsForValue().set(
                            TOKEN_BLACKLIST_PREFIX + token,
                            "1",
                            ttl,
                            TimeUnit.MILLISECONDS);
                    log.info("令牌已加入黑名单: {}", token.substring(0, Math.min(20, token.length())));
                }
            }
        } catch (Exception e) {
            log.error("将令牌加入黑名单失败", e);
        }
    }

    /**
     * 移除用户所有在线状态（强制下线）
     */
    public void invalidateUserTokens(Long userId) {
        stringRedisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);
        stringRedisTemplate.delete(USER_ONLINE_PREFIX + userId);
        log.info("用户{}的所有令牌已失效", userId);
    }

    /**
     * 检查令牌是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        Boolean hasKey = stringRedisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token);
        return Boolean.TRUE.equals(hasKey);
    }

    /**
     * 解析令牌Claims
     */
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // 即使过期也返回Claims以获取信息
        } catch (Exception e) {
            log.warn("解析JWT令牌Claims失败: {}", e.getMessage());
            return null;
        }
    }
}
