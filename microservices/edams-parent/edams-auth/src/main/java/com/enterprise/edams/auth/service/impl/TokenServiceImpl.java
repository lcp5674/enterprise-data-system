package com.enterprise.edams.auth.service.impl;

import com.enterprise.edams.auth.service.TokenService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Token服务实现
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class TokenServiceImpl implements TokenService {

    private final SecretKey secretKey;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${auth.jwt.access-token-expiration:7200}")
    private long accessTokenExpiration;

    @Value("${auth.jwt.refresh-token-expiration:604800}")
    private long refreshTokenExpiration;

    @Value("${auth.jwt.secret:your-256-bit-secret-key-for-jwt-signing-minimum-32-chars}")
    private String jwtSecret;

    public TokenServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateAccessToken(String userId, String username, Map<String, Object> claims) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration * 1000);

        Map<String, Object> allClaims = new HashMap<>(claims);
        allClaims.put("userId", userId);
        allClaims.put("username", username);
        allClaims.put("type", "access");

        return Jwts.builder()
                .claims(allClaims)
                .subject(userId)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenExpiration * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");

        return Jwts.builder()
                .claims(claims)
                .subject(userId)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public Map<String, Object> parseAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return new HashMap<>(claims);
        } catch (JwtException e) {
            log.error("解析Token失败", e);
            return null;
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("Token已过期");
            return false;
        } catch (JwtException e) {
            log.error("Token验证失败", e);
            return false;
        }
    }

    @Override
    public String getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (JwtException e) {
            log.error("获取用户ID失败", e);
            return null;
        }
    }

    @Override
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get("username", String.class);
        } catch (JwtException e) {
            log.error("获取用户名失败", e);
            return null;
        }
    }

    @Override
    public void invalidateToken(String token) {
        blacklistToken(token);
    }

    @Override
    public void invalidateAllUserTokens(String userId) {
        String key = "token:blacklist:user:" + userId;
        redisTemplate.opsForSet().add(key, "*");
        redisTemplate.expire(key, refreshTokenExpiration, TimeUnit.SECONDS);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        String key = "token:blacklist:" + extractTokenId(token);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void blacklistToken(String token) {
        String key = "token:blacklist:" + extractTokenId(token);
        Claims claims = parseAccessToken(token);
        long ttl = 0;
        if (claims != null && claims.getExpiration() != null) {
            ttl = (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000;
        }
        if (ttl > 0) {
            redisTemplate.opsForValue().set(key, "blacklisted", ttl, TimeUnit.SECONDS);
        }
    }

    @Override
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    @Override
    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    private String extractTokenId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getId();
        } catch (JwtException e) {
            return String.valueOf(token.hashCode());
        }
    }
}
