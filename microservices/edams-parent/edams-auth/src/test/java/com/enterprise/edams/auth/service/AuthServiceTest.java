package com.enterprise.edams.auth.service;

import com.enterprise.edams.auth.dto.LoginRequest;
import com.enterprise.edams.auth.dto.LoginResponse;
import com.enterprise.edams.auth.dto.MFAVerifyRequest;
import com.enterprise.edams.auth.dto.MFASetupResponse;
import com.enterprise.edams.auth.entity.LoginLog;
import com.enterprise.edams.auth.entity.SysSession;
import com.enterprise.edams.auth.entity.SysUser;
import com.enterprise.edams.auth.repository.LoginLogRepository;
import com.enterprise.edams.auth.repository.SysSessionRepository;
import com.enterprise.edams.auth.repository.SysUserRepository;
import com.enterprise.edams.auth.service.impl.AuthServiceImpl;
import com.enterprise.edams.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("认证服务测试")
class AuthServiceTest {

    @Mock
    private SysUserRepository userRepository;

    @Mock
    private LoginLogRepository loginLogRepository;

    @Mock
    private SysSessionRepository sessionRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private CaptchaService captchaService;

    @Mock
    private MFAService mfaService;

    @Mock
    private LoginSecurityService loginSecurityService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private AuthServiceImpl authService;

    private SysUser testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = SysUser.builder()
                .id("user-001")
                .username("testuser")
                .password("$2a$10$encoded_password")
                .email("test@example.com")
                .phone("13800138000")
                .nickname("Test User")
                .status(1) // 正常状态
                .mfaEnabled(0)
                .isFirstLogin(0)
                .createTime(LocalDateTime.now())
                .build();

        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("password123")
                .ipAddress("127.0.0.1")
                .deviceType("WEB")
                .build();
    }

    @Test
    @DisplayName("登录成功 - 用户名密码正确")
    void testLogin_Success() {
        // Given
        doNothing().when(loginSecurityService).checkLoginSecurity(anyString(), anyString());
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        when(tokenService.generateAccessToken(anyString(), anyString(), anyMap())).thenReturn("access_token");
        when(tokenService.generateRefreshToken(anyString())).thenReturn("refresh_token");
        when(tokenService.getAccessTokenExpiration()).thenReturn(7200L);
        when(sessionRepository.insert(any(SysSession.class))).thenReturn(1L);
        when(loginLogRepository.insert(any(LoginLog.class))).thenReturn(1L);
        when(userRepository.updateById(any(SysUser.class))).thenReturn(true);
        when(tokenService.validateToken(anyString())).thenReturn(true);
        when(tokenService.isTokenBlacklisted(anyString())).thenReturn(false);
        when(tokenService.getUserIdFromToken(anyString())).thenReturn("user-001");

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access_token");
        assertThat(response.getUsername()).isEqualTo("testuser");
        verify(loginSecurityService, times(1)).recordLoginSuccess(anyString(), anyString());
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void testLogin_UserNotFound() {
        // Given
        doNothing().when(loginSecurityService).checkLoginSecurity(anyString(), anyString());
        when(userRepository.findByUsername("nonexistent")).thenReturn(null);
        when(userRepository.findByEmail("nonexistent")).thenReturn(null);
        when(userRepository.findByPhone("nonexistent")).thenReturn(null);

        loginRequest.setUsername("nonexistent");

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BusinessException.class);
        verify(loginSecurityService, times(1)).recordLoginFailure(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("登录失败 - 用户已禁用")
    void testLogin_UserDisabled() {
        // Given
        testUser.setStatus(0); // 禁用状态
        doNothing().when(loginSecurityService).checkLoginSecurity(anyString(), anyString());
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BusinessException.class);
        verify(loginSecurityService, times(1)).recordLoginFailure(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void testLogin_WrongPassword() {
        // Given
        doNothing().when(loginSecurityService).checkLoginSecurity(anyString(), anyString());
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("wrong_password", testUser.getPassword())).thenReturn(false);

        loginRequest.setPassword("wrong_password");

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BusinessException.class);
        verify(loginSecurityService, times(1)).recordLoginFailure(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("登录需要MFA验证")
    void testLogin_MFARequired() {
        // Given
        testUser.setMfaEnabled(1);
        doNothing().when(loginSecurityService).checkLoginSecurity(anyString(), anyString());
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), any(), anyLong(), any());

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMfaRequired()).isTrue();
        assertThat(response.getMfaToken()).isNotNull();
    }

    @Test
    @DisplayName("MFA验证成功")
    void testVerifyMFA_Success() {
        // Given
        MFAVerifyRequest request = MFAVerifyRequest.builder()
                .mfaToken("temp_token")
                .verifyType("TOTP")
                .code("123456")
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("mfa:temp:temp_token")).thenReturn("user-001");
        when(userRepository.selectById("user-001")).thenReturn(testUser);
        when(mfaService.verifyTotpCode(testUser.getMfaSecret(), "123456")).thenReturn(true);
        when(redisTemplate.delete(anyString())).thenReturn(true);
        when(tokenService.generateAccessToken(anyString(), anyString(), anyMap())).thenReturn("access_token");
        when(tokenService.generateRefreshToken(anyString())).thenReturn("refresh_token");
        when(tokenService.getAccessTokenExpiration()).thenReturn(7200L);
        when(sessionRepository.insert(any(SysSession.class))).thenReturn(1L);

        // When
        LoginResponse response = authService.verifyMFA(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access_token");
        verify(redisTemplate, times(1)).delete("mfa:temp:temp_token");
    }

    @Test
    @DisplayName("MFA验证失败 - 令牌过期")
    void testVerifyMFA_TokenExpired() {
        // Given
        MFAVerifyRequest request = MFAVerifyRequest.builder()
                .mfaToken("expired_token")
                .verifyType("TOTP")
                .code("123456")
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("mfa:temp:expired_token")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> authService.verifyMFA(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("MFA验证失败 - 验证码错误")
    void testVerifyMFA_WrongCode() {
        // Given
        MFAVerifyRequest request = MFAVerifyRequest.builder()
                .mfaToken("temp_token")
                .verifyType("TOTP")
                .code("wrong_code")
                .build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("mfa:temp:temp_token")).thenReturn("user-001");
        when(userRepository.selectById("user-001")).thenReturn(testUser);
        when(mfaService.verifyTotpCode(testUser.getMfaSecret(), "wrong_code")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.verifyMFA(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("登出成功")
    void testLogout_Success() {
        // Given
        String token = "valid_token";
        when(tokenService.getUserIdFromToken(token)).thenReturn("user-001");
        doNothing().when(tokenService).blacklistToken(token);
        doNothing().when(sessionRepository).invalidateAllSessionsByUserId("user-001");

        // When
        authService.logout(token);

        // Then
        verify(tokenService, times(1)).blacklistToken(token);
        verify(sessionRepository, times(1)).invalidateAllSessionsByUserId("user-001");
    }

    @Test
    @DisplayName("刷新Token成功")
    void testRefreshToken_Success() {
        // Given
        String refreshToken = "valid_refresh_token";
        when(tokenService.validateToken(refreshToken)).thenReturn(true);
        when(tokenService.isTokenBlacklisted(refreshToken)).thenReturn(false);
        when(tokenService.getUserIdFromToken(refreshToken)).thenReturn("user-001");
        when(userRepository.selectById("user-001")).thenReturn(testUser);
        when(tokenService.generateAccessToken(anyString(), anyString(), anyMap())).thenReturn("new_access_token");
        when(tokenService.generateRefreshToken(anyString())).thenReturn("new_refresh_token");
        when(tokenService.getAccessTokenExpiration()).thenReturn(7200L);
        doNothing().when(tokenService).blacklistToken(refreshToken);

        // When
        LoginResponse response = authService.refreshToken(refreshToken);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new_access_token");
        assertThat(response.getRefreshToken()).isEqualTo("new_refresh_token");
    }

    @Test
    @DisplayName("刷新Token失败 - Token无效")
    void testRefreshToken_InvalidToken() {
        // Given
        String refreshToken = "invalid_token";
        when(tokenService.validateToken(refreshToken)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("刷新Token失败 - Token已加入黑名单")
    void testRefreshToken_BlacklistedToken() {
        // Given
        String refreshToken = "blacklisted_token";
        when(tokenService.validateToken(refreshToken)).thenReturn(true);
        when(tokenService.isTokenBlacklisted(refreshToken)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.refreshToken(refreshToken))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("验证Token成功")
    void testValidateToken_Success() {
        // Given
        String token = "valid_token";
        when(tokenService.validateToken(token)).thenReturn(true);
        when(tokenService.isTokenBlacklisted(token)).thenReturn(false);

        // When
        boolean result = authService.validateToken(token);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("验证Token失败 - Token为null")
    void testValidateToken_NullToken() {
        // When
        boolean result = authService.validateToken(null);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("获取当前用户信息成功")
    void testGetCurrentUser_Success() {
        // Given
        String token = "valid_token";
        when(tokenService.getUserIdFromToken(token)).thenReturn("user-001");
        when(userRepository.selectById("user-001")).thenReturn(testUser);

        // When
        LoginResponse response = authService.getCurrentUser(token);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo("user-001");
        assertThat(response.getUsername()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("获取当前用户信息失败 - Token无效")
    void testGetCurrentUser_InvalidToken() {
        // Given
        String token = "invalid_token";
        when(tokenService.getUserIdFromToken(token)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> authService.getCurrentUser(token))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("强制登出用户成功")
    void testForceLogout_Success() {
        // Given
        String userId = "user-001";
        doNothing().when(tokenService).invalidateAllUserTokens(userId);
        doNothing().when(sessionRepository).invalidateAllSessionsByUserId(userId);

        // When
        authService.forceLogout(userId);

        // Then
        verify(tokenService, times(1)).invalidateAllUserTokens(userId);
        verify(sessionRepository, times(1)).invalidateAllSessionsByUserId(userId);
    }

    @Test
    @DisplayName("启用MFA成功")
    void testEnableMFA_Success() {
        // Given
        String userId = "user-001";
        String code = "123456";
        when(userRepository.selectById(userId)).thenReturn(testUser);
        when(mfaService.verifyTotpCode(testUser.getMfaSecret(), code)).thenReturn(true);
        when(userRepository.updateById(any(SysUser.class))).thenReturn(true);

        // When
        authService.enableMFA(userId, code);

        // Then
        verify(userRepository, times(1)).updateById(any(SysUser.class));
    }

    @Test
    @DisplayName("禁用MFA成功")
    void testDisableMFA_Success() {
        // Given
        String userId = "user-001";
        String code = "123456";
        testUser.setMfaEnabled(1);
        when(userRepository.selectById(userId)).thenReturn(testUser);
        when(mfaService.verifyTotpCode(testUser.getMfaSecret(), code)).thenReturn(true);
        when(userRepository.updateById(any(SysUser.class))).thenReturn(true);

        // When
        authService.disableMFA(userId, code);

        // Then
        verify(userRepository, times(1)).updateById(any(SysUser.class));
    }

    @Test
    @DisplayName("设置TOTP MFA")
    void testSetupMFA_TOTP() {
        // Given
        String userId = "user-001";
        MFASetupResponse expectedResponse = MFASetupResponse.builder()
                .secret("JBSWY3DPEHPK3PXP")
                .qrCodeUrl("otpauth://totp/Test:test@example.com?secret=JBSWY3DPEHPK3PXP")
                .build();

        when(mfaService.setupTotpMFA(userId)).thenReturn(expectedResponse);

        // When
        MFASetupResponse response = authService.setupMFA(userId, "TOTP");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSecret()).isEqualTo("JBSWY3DPEHPK3PXP");
    }

    @Test
    @DisplayName("设置MFA失败 - 不支持的类型")
    void testSetupMFA_UnsupportedType() {
        // Given
        String userId = "user-001";

        // When & Then
        assertThatThrownBy(() -> authService.setupMFA(userId, "UNSUPPORTED"))
                .isInstanceOf(BusinessException.class);
    }
}
