package com.enterprise.edams.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * JWT认证过滤器
 *
 * <p>全局认证过滤器，对需要认证的请求进行JWT令牌验证</p>
 * <ul>
 *   <li>从Authorization Header中提取Bearer Token</li>
 *   <li>验证Token签名和有效期</li>
 *   <li>检查Token是否在黑名单中（Redis）</li>
 *   <li>将用户信息注入请求头转发给下游服务</li>
 * </ul>
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * JWT密钥（与auth服务保持一致）
     * ⚠️ 警告：此值必须通过环境变量 JWT_SECRET 设置，禁止硬编码！
     */
    @Value("${jwt.secret:}")
    private String jwtSecret;

    /**
     * 不需要认证的路径白名单（支持Ant模式匹配）
     */
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            // 认证服务公开接口
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh-token",
            "/api/v1/auth/captcha/**",
            "/api/v1/auth/password/reset/**",
            
            // 健康检查
            "/actuator/health",
            "/actuator/info",
            
            // API文档
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            
            // 静态资源（如果有前端部署）
            "/index.html",
            "/favicon.ico",
            "/static/**",
            "/assets/**"
    );

    public AuthFilter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        log.debug("[Gateway] 请求路径: {}", path);

        // 跳过OPTIONS预检请求
        if ("OPTIONS".equals(request.getMethod().name())) {
            return chain.filter(exchange);
        }

        // 检查是否在白名单中
        if (isExcludedPath(path)) {
            log.debug("[Gateway] 白名单路径，跳过认证: {}", path);
            return chain.filter(exchange);
        }

        // 提取Token
        String token = extractToken(request);
        
        if (!StringUtils.hasText(token)) {
            return writeErrorResponse(exchange, 2000, "未提供认证令牌，请先登录");
        }

        // 验证Token
        Claims claims = validateToken(token);
        if (claims == null) {
            return writeErrorResponse(exchange, 2001, "认证令牌无效或已过期，请重新登录");
        }

        // 将用户信息注入请求头（传递给下游微服务）
        ServerHttpRequest mutatedRequest = mutateRequestWithUserInfo(request, claims);
        
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        return chain.filter(mutatedExchange);
    }

    /**
     * 判断路径是否在排除列表中
     */
    private boolean isExcludedPath(String path) {
        for (String pattern : EXCLUDE_PATHS) {
            if (PATH_MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从请求头提取JWT Token
     */
    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * 验证JWT令牌
     */
    private Claims validateToken(String token) {
        try {
            // 检查黑名单
            Boolean blacklisted = stringRedisTemplate.hasKey("edams:token:blacklist:" + token);
            if (Boolean.TRUE.equals(blacklisted)) {
                log.warn("[Gateway] Token已在黑名单中: {}", maskToken(token));
                return null;
            }

            // 解析并验证令牌
            byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            
            return Jwts.parser()
                    .verifyWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
                    
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("[Gateway] Token已过期: {}", e.getMessage());
            return null;
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("[Gateway] Token格式错误: {}", e.getMessage());
            return null;
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.warn("[Gateway] 不支持的Token类型: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("[Gateway] Token验证异常", e);
            return null;
        }
    }

    /**
     * 将用户信息注入到请求头中
     */
    private ServerHttpRequest mutateRequestWithUserInfo(ServerHttpRequest originalRequest, Claims claims) {
        String userId = claims.getSubject();
        String username = claims.get("username", String.class);

        return originalRequest.mutate()
                .header("X-User-Id", userId)
                .header("X-Username", username)
                .header("Trace-Id", generateTraceId())
                .build();
    }

    /**
     * 写入错误响应
     */
    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, int code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setHttpStatus(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("code", code);
        body.put("message", message);
        body.put("timestamp", System.currentTimeMillis());

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            response.setRawStatusCode(HttpStatus.UNAUTHORIZED.value());
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("写入错误响应失败", e);
            return response.setComplete();
        }
    }

    /**
     * 生成追踪ID
     */
    private String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    /**
     * 对Token进行脱敏处理
     */
    private String maskToken(String token) {
        if (token == null || token.length() <= 10) return "****";
        return token.substring(0, 6) + "****" + token.substring(token.length() - 4);
    }

    /**
     * 过滤器优先级：数字越小优先级越高
     * AuthFilter应该在限流和日志过滤器之后、路由之前执行
     */
    @Override
    public int getOrder() {
        // 在日志过滤器之后执行
        return -90;
    }
}
