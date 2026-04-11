package com.enterprise.edams.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 请求日志过滤器
 *
 * <p>记录所有经过网关的HTTP请求日志，包含请求和响应的关键信息</p>
 * <ul>
 *   <li>请求方法、路径、来源IP</li>
 *   <li>响应状态码</li>
 *   <li>请求处理耗时</li>
 *   <li>路由到的目标服务</li>
 * </ul>
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class LogFilter implements GlobalFilter, Ordered {

    /**
     * 请求计数器（用于统计总请求数）
     */
    private static final AtomicLong REQUEST_COUNTER = new AtomicLong(0);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 记录开始时间
        Instant startTime = Instant.now();
        long requestId = REQUEST_COUNTER.incrementAndGet();

        // 获取请求信息
        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
        String path = request.getURI().getPath();
        String query = request.getURI().getQuery();
        String ip = getClientIp(request);
        String userAgent = getUserAgent(request);
        String serviceRoute = getServiceRoute(exchange);

        log.info("[Gateway-Request #{}] {} {}{} | IP: {} | Route: {}",
                requestId, method, path,
                (query != null ? "?" + query : ""),
                ip,
                serviceRoute != null ? serviceRoute : "N/A");

        // 包装响应以记录耗时和状态码
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            long durationMs = Duration.between(startTime, Instant.now()).toMillis();
            int statusCode = response.getStatusCode() != null ? 
                    response.getStatusCode().value() : -1;

            // 根据状态码选择不同日志级别
            if (statusCode >= 500) {
                log.error("[Gateway-Response #{}] {} {} -> {} ({}ms) [ERROR]",
                        requestId, method, path, statusCode, durationMs);
            } else if (statusCode >= 400) {
                log.warn("[Gateway-Response #{}] {} {} -> {} ({}ms) [WARN]",
                        requestId, method, path, statusCode, durationMs);
            } else if (durationMs > 1000) {
                log.warn("[Gateway-Response #{}] {} {} -> {} ({}ms) [SLOW]",
                        requestId, method, path, statusCode, durationMs);
            } else {
                log.debug("[Gateway-Response #{}] {} {} -> {} ({}ms)",
                        requestId, method, path, statusCode, durationMs);
            }
        }));
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
            return request.getRemoteAddress() != null ?
                    Objects.requireNonNull(request.getRemoteAddress()).getAddress().getHostAddress() : "-";
        }
        if (ip.contains(",")) {
            return ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取User-Agent（截断避免过长）
     */
    private String getUserAgent(ServerHttpRequest request) {
        String ua = request.getHeaders().getFirst("User-Agent");
        if (ua == null || ua.length() <= 200) {
            return ua;
        }
        return ua.substring(0, 200) + "...(truncated)";
    }

    /**
     * 获取路由目标服务名称
     */
    private String getServiceRoute(ServerWebExchange exchange) {
        Object routeId = exchange.getAttribute(
                org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ID_ATTR);
        return routeId != null ? routeId.toString() : null;
    }

    /**
     * 过滤器优先级：最高优先级执行，确保所有请求都被记录
     */
    @Override
    public int getOrder() {
        return -100; // 最高优先级之一，确保最先执行
    }
}
