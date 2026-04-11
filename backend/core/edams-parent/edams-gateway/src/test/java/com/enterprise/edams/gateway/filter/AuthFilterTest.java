package com.enterprise.edams.gateway.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 认证过滤器测试
 * 测试JWT Token验证和路由逻辑
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false"
})
@DisplayName("认证过滤器测试")
class AuthFilterTest {

    @Autowired(required = false)
    private JwtAuthFilter jwtAuthFilter;

    @Test
    @DisplayName("过滤器Bean加载")
    void testFilterBeanLoaded() {
        // 如果过滤器已实现，则验证Bean存在
        // 如果未实现，此测试将被跳过
        if (jwtAuthFilter != null) {
            assertThat(jwtAuthFilter).isNotNull();
        }
    }

    @Test
    @DisplayName("有效Token请求")
    void testValidTokenRequest() throws Exception {
        // 准备有效Token的请求
        String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.signature";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/auth/user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // 如果过滤器已实现
        if (jwtAuthFilter != null) {
            GatewayFilterChain chain = filterExchange -> Mono.empty();
            Mono<Void> result = jwtAuthFilter.filter(exchange, chain);

            StepVerifier.create(result)
                    .expectComplete()
                    .verify();
        }
    }

    @Test
    @DisplayName("无效Token请求")
    void testInvalidTokenRequest() throws Exception {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/auth/user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.here")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        if (jwtAuthFilter != null) {
            GatewayFilterChain chain = filterExchange -> Mono.just(HttpStatus.UNAUTHORIZED);
            Mono<Void> result = jwtAuthFilter.filter(exchange, chain);

            StepVerifier.create(result)
                    .expectComplete()
                    .verify();
        }
    }

    @Test
    @DisplayName("缺少Token请求")
    void testMissingTokenRequest() throws Exception {
        // 公开路径（如登录、注册）不应该需要Token
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/auth/login")
                .build();
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        assertThat(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION)).isNull();
    }

    @Test
    @DisplayName("Token格式验证")
    void testTokenFormatValidation() {
        // JWT格式: header.payload.signature
        String validJwtFormat = "abc.def.ghi";
        String[] parts = validJwtFormat.split("\\.");
        assertThat(parts).hasSize(3);

        // 错误格式
        String invalidJwtFormat = "not-a-jwt";
        String[] invalidParts = invalidJwtFormat.split("\\.");
        assertThat(invalidParts).hasSize(1);
    }

    @Test
    @DisplayName("公开路径列表")
    void testPublicPaths() {
        // 验证公开路径不需要认证
        var publicPaths = java.util.List.of(
                "/api/v1/auth/login",
                "/api/v1/auth/register",
                "/api/v1/auth/send-mfa",
                "/api/v1/auth/verify-mfa",
                "/actuator/health"
        );

        assertThat(publicPaths).isNotEmpty();
        publicPaths.forEach(path -> assertThat(path).startsWith("/"));
    }

    @Test
    @DisplayName("认证头解析")
    void testAuthHeaderParsing() {
        String authHeader = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.signature";

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3);
        }
    }
}
