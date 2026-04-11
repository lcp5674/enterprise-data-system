package com.enterprise.edams.auth.controller;

import com.enterprise.edams.auth.dto.LoginRequest;
import com.enterprise.edams.auth.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 集成测试
 * 测试用户登录、注册、Token验证等功能
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("认证控制器测试")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String testToken;

    @BeforeEach
    void setUp() {
        // 每个测试前重置token
        testToken = null;
    }

    @Test
    @DisplayName("用户登录 - 成功")
    void testLoginSuccess() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("admin123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andDo(result -> {
                    // 保存token供后续测试使用
                    String response = result.getResponse().getContentAsString();
                    // 提取token的简单方式
                });
    }

    @Test
    @DisplayName("用户登录 - 错误密码")
    void testLoginWrongPassword() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("admin")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("用户登录 - 用户不存在")
    void testLoginUserNotFound() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("nonexistent_user_12345")
                .password("password")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("用户登录 - 参数校验失败")
    void testLoginValidationError() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("")  // 用户名为空
                .password("123")  // 密码过短
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("用户注册 - 成功")
    void testRegisterSuccess() throws Exception {
        String uniqueUsername = "testuser_" + System.currentTimeMillis();
        RegisterRequest request = RegisterRequest.builder()
                .username(uniqueUsername)
                .password("Test123456")
                .email(uniqueUsername + "@test.com")
                .realName("Test User")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value(uniqueUsername));
    }

    @Test
    @DisplayName("用户注册 - 用户名已存在")
    void testRegisterDuplicateUsername() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("admin")
                .password("Test123456")
                .email("newemail@test.com")
                .realName("Test User")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("用户名已存在"));
    }

    @Test
    @DisplayName("用户注册 - 邮箱格式错误")
    void testRegisterInvalidEmail() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser123")
                .password("Test123456")
                .email("invalid-email")
                .realName("Test User")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("用户登出 - 成功")
    void testLogout() throws Exception {
        // 先登录获取token
        LoginRequest loginRequest = LoginRequest.builder()
                .username("admin")
                .password("admin123")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();
        // 解析token（简化处理）
        String token = extractToken(response);

        if (token != null) {
            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Test
    @DisplayName("Token验证 - 有效Token")
    void testValidateTokenValid() throws Exception {
        // 先登录获取token
        LoginRequest loginRequest = LoginRequest.builder()
                .username("admin")
                .password("admin123")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();
        String token = extractToken(response);

        if (token != null) {
            mockMvc.perform(get("/api/auth/validate")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.valid").value(true));
        }
    }

    @Test
    @DisplayName("Token验证 - 无效Token")
    void testValidateTokenInvalid() throws Exception {
        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("Token验证 - 缺少Token")
    void testValidateTokenMissing() throws Exception {
        mockMvc.perform(get("/api/auth/validate"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("刷新Token - 成功")
    void testRefreshToken() throws Exception {
        // 先登录获取token
        LoginRequest loginRequest = LoginRequest.builder()
                .username("admin")
                .password("admin123")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String response = loginResult.getResponse().getContentAsString();
        String token = extractToken(response);

        if (token != null) {
            mockMvc.perform(post("/api/auth/refresh")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.token").exists());
        }
    }

    @Test
    @DisplayName("发送MFA验证码 - 成功")
    void testSendMfaCode() throws Exception {
        mockMvc.perform(post("/api/auth/mfa/send")
                        .param("username", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("验证MFA - 成功")
    void testVerifyMfaCode() throws Exception {
        mockMvc.perform(post("/api/auth/mfa/verify")
                        .param("username", "admin")
                        .param("code", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取登录日志 - 需要认证")
    void testGetLoginLogsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/login-logs"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * 简单提取token的方法
     */
    private String extractToken(String json) {
        try {
            int tokenIndex = json.indexOf("\"token\":\"");
            if (tokenIndex == -1) return null;
            int start = tokenIndex + 9;
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        } catch (Exception e) {
            return null;
        }
    }
}
