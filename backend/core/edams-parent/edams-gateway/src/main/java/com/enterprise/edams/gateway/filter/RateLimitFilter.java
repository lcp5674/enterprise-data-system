package com.enterprise.edams.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 限流过滤器
 *
 * <p>基于Redis实现的滑动窗口限流，支持IP级别和用户级别的频率控制</p>
 * <ul>
 *   <li><b>IP级限流</b>：限制同一IP的请求频率，防止DDoS攻击</li>
 *   <li><b>User级限流</b>：限制同一用户的API调用频率，防止滥用</li>
 *   <li><b>全局限流</b>：限制整个网关的总QPS</li>
 * </ul>
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * IP级别：每秒最大请求数
     */
    @Value("${gateway.rate-limit.ip.requests-per-second:100}")
    private int ipRequestsPerSecond;

    /**
     * IP级别：每分钟最大请求数
     */
    @Value("${gateway.rate-limit.ip.requests-per-minute:500}")
    private int ipRequestsPerMinute;

    /**
     * 用户级别：每秒最大请求数
     */
    @Value("${gateway.rate-limit.user.requests-per-second:50}")
    private int userRequestsPerSecond;

    /**
     * 用户级别：每分钟最大请求数
     */
    @Value("${gateway.rate-limit.user.requests-per-minute:200}")
    private int userRequestsPerMinute;

    /**
     * 全局级别：每秒总请求数
     */
    @Value("${gateway.rate-limit.global.requests-per-second:1000}")
    private int globalRequestsPerSecond;

    public RateLimitFilter(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 获取客户端IP
        String clientIp = getClientIp(request);
        
        // 获取用户ID（如果有认证信息）
        String userId = request.getHeaders().getFirst("X-User-Id");

        // 1. 检查全局限流
        if (checkGlobalRateLimit()) {
            return writeRateLimitResponse(exchange, "服务器繁忙，请稍后重试");
        }

        // 2. 检查IP级限流（按分钟）
        if (!checkIpRateLimit(clientIp)) {
            log.warn("[RateLimit] IP {} 触发限流", clientIp);
            return writeRateLimitResponse(exchange, "请求过于频繁，请稍后重试");
        }

        // 3. 检查用户级限流（如果已认证）
        if (userId != null && !userId.isEmpty() && !userId.equals("anonymous")) {
            if (!checkUserRateLimit(userId)) {
                log.warn("[RateLimit] 用户 {} 触发限流", userId);
                return writeRateLimitResponse(exchange, "操作过于频繁，请降低调用频率");
            }
        }

        return chain.filter(exchange);
    }

    /**
     * 全局限流检查（基于固定窗口）
     */
    private boolean checkGlobalRateLimit() {
        String key = "edams:ratelimit:global:second:" + getCurrentSecond();
        Long currentCount = stringRedisTemplate.opsForValue().increment(key);
        
        if (currentCount != null && currentCount == 1) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(2));
        }

        return currentCount != null && currentCount > globalRequestsPerSecond;
    }

    /**
     * IP级别限流检查（滑动窗口 - 按分钟）
     */
    private boolean checkIpRateLimit(String ip) {
        String minuteKey = "edams:ratelimit:ip:" + ip + ":" + getCurrentMinute();
        
        Long count = stringRedisTemplate.opsForValue().increment(minuteKey);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(minuteKey, Duration.ofMinutes(2));
        }

        // 如果超过限制
        if (count != null && count > ipRequestsPerMinute) {
            return false;
        }

        // 同时检查每秒限制（更严格的保护）
        String secondKey = "edams:ratelimit:ip:" + ip + ":s:" + getCurrentSecond();
        Long secCount = stringRedisTemplate.opsForValue().increment(secondKey);
        if (secCount != null && secCount == 1) {
            stringRedisTemplate.expire(secondKey, Duration.ofSeconds(5));
        }
        return !(secCount != null && secCount > ipRequestsPerSecond);
    }

    /**
     * 用户级别限流检查
     */
    private boolean checkUserRateLimit(String userId) {
        String minuteKey = "edams:ratelimit:user:" + userId + ":" + getCurrentMinute();
        
        Long count = stringRedisTemplate.opsForValue().increment(minuteKey);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(minuteKey, Duration.ofMinutes(2));
        }

        if (count != null && count > userRequestsPerMinute) {
            return false;
        }

        String secondKey = "edams:ratelimit:user:" + userId + ":s:" + getCurrentSecond();
        Long secCount = stringRedisTemplate.opsForValue().increment(secondKey);
        if (secCount != null && secCount == 1) {
            stringRedisTemplate.expire(secondKey, Duration.ofSeconds(5));
        }
        return !(secCount != null && secCount > userRequestsPerSecond);
    }

    /**
     * 写入限流响应（429 Too Many Requests）
     */
    private Mono<Void> writeRateLimitResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setHttpStatus(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("code", 429);
        body.put("message", message);
        body.put("timestamp", System.currentTimeMillis());

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            response.setRawStatusCode(HttpStatus.TOO_MANY_REQUESTS.value());
            return response.writeWith(
                    reactor.core.publisher.Mono.just(response.bufferFactory().wrap(bytes))
            );
        } catch (JsonProcessingException e) {
            log.error("写入限流响应失败", e);
            return response.setComplete();
        }
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(ServerHttpRequest request) {
        String ip = request.getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddress() != null ? 
                    Objects.requireNonNull(request.getRemoteAddress()).getAddress().getHostAddress() : "unknown";
        }
        if (ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取当前秒的时间戳key
     */
    private String getCurrentSecond() {
        return String.valueOf(System.currentTimeMillis() / 1000);
    }

    /**
     * 获取当前分钟的时间戳key
     */
    private String getCurrentMinute() {
        return String.valueOf(System.currentTimeMillis() / 60000);
    }

    /**
     * 过滤器优先级：在AuthFilter之前执行限流检查
     */
    @Override
    public int getOrder() {
        return -95; // 在AuthFilter之前执行
    }
}
