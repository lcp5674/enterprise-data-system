package com.enterprise.edams.gateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 网关全局异常处理器
 *
 * <p>统一处理网关层面的异常，包括：</p>
 * <ul>
 *   <li>服务不可达（503）</li>
 *   <li>路由未找到（404）</li>
 *   <li>请求超时（504）</li>
 *   <li>连接被拒绝（502）</li>
 *   <li>其他未知异常</li>
 * </ul>
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Component
@Order(-2) // 高优先级
public class ErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 根据异常类型确定状态码和消息
        HttpStatus httpStatus = determineHttpStatus(ex);
        String message = determineMessage(ex);

        log.error("[Gateway-Error] {} - {} | Path: {}", 
                httpStatus.value(), message,
                exchange.getRequest().getURI().getPath(), ex);

        Map<String, Object> errorBody = buildErrorResponse(httpStatus, message, ex.getClass().getSimpleName());

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorBody);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            response.setStatusCode(httpStatus);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("序列化错误响应失败", e);
            return response.setComplete();
        }
    }

    /**
     * 根据异常类型确定HTTP状态码
     */
    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof NotFoundException) {
            return HttpStatus.NOT_FOUND; // 404 - 路由未找到
        }
        
        if (ex instanceof ResponseStatusException statusEx) {
            return (HttpStatus) statusEx.getStatusCode(); // 使用异常自带的状态码
        }

        String errorMessage = ex.getMessage();
        if (errorMessage != null) {
            if (errorMessage.contains("Connection refused")) {
                return HttpStatus.BAD_GATEWAY; // 502 - 服务拒绝连接
            }
            if (errorMessage.contains("timeout") || errorMessage.contains("Timeout")) {
                return HttpStatus.GATEWAY_TIMEOUT; // 504 - 网关超时
            }
        }

        // 检查异常类名判断类型（Spring Cloud Gateway特定）
        String className = ex.getClass().getSimpleName();
        switch (className) {
            case "ConnectException":
                return HttpStatus.BAD_GATEWAY; // 502
            case "ReadTimeoutException":
                return HttpStatus.GATEWAY_TIMEOUT; // 504
            case "ServiceUnavailableException":
                return HttpStatus.SERVICE_UNAVAILABLE; // 503
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR; // 500
        }
    }

    /**
     * 根据异常类型确定用户友好的错误消息
     */
    private String determineMessage(Throwable ex) {
        if (ex instanceof NotFoundException) {
            return "请求的服务或接口不存在";
        }

        if (ex instanceof ResponseStatusException statusEx) {
            String reason = statusEx.getReason();
            return reason != null ? reason : "请求失败";
        }

        String errorMsg = ex.getMessage();
        
        if (errorMsg == null) {
            return "服务器内部错误，请联系管理员";
        }

        // 将技术信息转换为用户友好消息
        if (errorMsg.contains("Connection refused")) {
            return "目标服务暂时不可用，请稍后重试";
        }
        if (errorMsg.contains("timeout")) {
            return "服务处理超时，请降低请求频率后重试";
        }
        if (errorMsg.contains("Unable to find instance for")) {
            return errorMsg.replaceAll("Unable to find instance for (.+)", "服务 $1 当前没有可用实例");
        }

        // 默认：隐藏敏感信息，返回通用错误
        return "网关处理请求时发生错误: " + truncateMessage(errorMsg);
    }

    /**
     * 构建标准化的错误响应体
     */
    private Map<String, Object> buildErrorResponse(HttpStatus httpStatus, String message, String exceptionType) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", mapToErrorCode(httpStatus));
        body.put("message", message);
        body.put("status", httpStatus.value());
        body.put("error", httpStatus.getReasonPhrase());
        body.put("exception", exceptionType);
        body.put("timestamp", System.currentTimeMillis());
        body.put("path", ""); // path在handle方法中获取
        return body;
    }

    /**
     * 将HTTP状态码映射为业务错误码
     */
    private int mapToErrorCode(HttpStatus status) {
        return switch (status) {
            case BAD_REQUEST -> 1003;      // 参数无效
            case UNAUTHORIZED -> 2000;    // 未认证
            case FORBIDDEN -> 2003;       // 无权限
            case NOT_FOUND -> 3000;       // 资源不存在
            case TOO_MANY_REQUESTS -> 429;// 频率限制
            case BAD_GATEWAY -> 1001;     // 服务不可达
            case GATEWAY_TIMEOUT -> 1002; // 网关超时
            case SERVICE_UNAVAILABLE -> 1001; // 服务不可用
            default -> 1000;             // 系统错误
        };
    }

    /**
     * 截断过长的错误消息
     */
    private String truncateMessage(String msg) {
        if (msg.length() <= 150) {
            return msg;
        }
        return msg.substring(0, 150) + "...(truncated)";
    }
}
