package com.enterprise.edams.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * JwtTokenProvider 单元测试
 * 测试JWT Token的生成和验证功能
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("JWT Token提供者测试")
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_USERNAME = "testuser";
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_ROLE = "ADMIN";

    private String generatedToken;

    @BeforeEach
    void setUp() {
        // 生成测试token
        generatedToken = jwtTokenProvider.generateToken(TEST_USERNAME, TEST_USER_ID, TEST_ROLE);
    }

    @Test
    @DisplayName("生成Token - 成功")
    void testGenerateToken() {
        assertThat(generatedToken).isNotNull();
        assertThat(generatedToken).isNotEmpty();
        // JWT格式: header.payload.signature
        assertThat(generatedToken.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("从Token中提取用户名")
    void testGetUsernameFromToken() {
        String username = jwtTokenProvider.getUsernameFromToken(generatedToken);
        assertThat(username).isEqualTo(TEST_USERNAME);
    }

    @Test
    @DisplayName("从Token中提取用户ID")
    void testGetUserIdFromToken() {
        Long userId = jwtTokenProvider.getUserIdFromToken(generatedToken);
        assertThat(userId).isEqualTo(TEST_USER_ID);
    }

    @Test
    @DisplayName("验证Token - 有效Token")
    void testValidateTokenValid() {
        boolean isValid = jwtTokenProvider.validateToken(generatedToken);
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("验证Token - 无效Token")
    void testValidateTokenInvalid() {
        boolean isValid = jwtTokenProvider.validateToken("invalid.token.here");
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("验证Token - 空Token")
    void testValidateTokenEmpty() {
        boolean isValid = jwtTokenProvider.validateToken("");
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("验证Token - 格式错误Token")
    void testValidateTokenMalformed() {
        boolean isValid = jwtTokenProvider.validateToken("not-a-jwt-at-all");
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("获取Token过期时间")
    void testGetExpirationTime() {
        long expirationTime = jwtTokenProvider.getExpirationTime();
        assertThat(expirationTime).isGreaterThan(0);
        // 默认24小时 = 86400000ms
        assertThat(expirationTime).isEqualTo(86400000L);
    }

    @Test
    @DisplayName("检查Token是否即将过期")
    void testIsTokenExpiringSoon() {
        // 新生成的token不应该即将过期
        boolean isExpiringSoon = jwtTokenProvider.isTokenExpiringSoon(generatedToken);
        assertThat(isExpiringSoon).isFalse();

        // 测试即将过期的token（使用过期时间极短的token）
        String shortExpiryToken = jwtTokenProvider.generateToken(TEST_USERNAME, TEST_USER_ID, TEST_ROLE);
        // 注意：这里测试的是默认实现，实际过期时间由配置决定
        boolean isExpiringSoon2 = jwtTokenProvider.isTokenExpiringSoon(shortExpiryToken);
        // 新token不应该即将过期
        assertThat(isExpiringSoon2).isFalse();
    }

    @Test
    @DisplayName("生成带不同角色的Token")
    void testGenerateTokenWithDifferentRoles() {
        String userToken = jwtTokenProvider.generateToken("user", 2L, "USER");
        String adminToken = jwtTokenProvider.generateToken("admin", 1L, "ADMIN");

        assertThat(userToken).isNotNull();
        assertThat(adminToken).isNotNull();
        assertThat(userToken).isNotEqualTo(adminToken);

        // 验证不同角色的token
        assertThat(jwtTokenProvider.getUsernameFromToken(userToken)).isEqualTo("user");
        assertThat(jwtTokenProvider.getUsernameFromToken(adminToken)).isEqualTo("admin");
    }

    @Test
    @DisplayName("Token刷新测试")
    void testTokenRefresh() {
        // 获取原token的过期时间
        long originalExpiry = jwtTokenProvider.getExpirationTimeFromToken(generatedToken);

        // 刷新token
        String refreshedToken = jwtTokenProvider.refreshToken(generatedToken);

        assertThat(refreshedToken).isNotNull();
        assertThat(refreshedToken).isNotEqualTo(generatedToken);

        // 验证刷新后的token仍然有效
        assertThat(jwtTokenProvider.validateToken(refreshedToken)).isTrue();
        assertThat(jwtTokenProvider.getUsernameFromToken(refreshedToken)).isEqualTo(TEST_USERNAME);
    }

    @Test
    @DisplayName("Token解析异常处理")
    void testTokenParsingException() {
        // 解析格式错误的token应该返回null或抛出异常
        assertThatThrownBy(() -> jwtTokenProvider.getUsernameFromToken("malformed.token"))
                .isInstanceOf(Exception.class);
    }
}
