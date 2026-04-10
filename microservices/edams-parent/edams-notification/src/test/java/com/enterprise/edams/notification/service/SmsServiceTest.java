package com.enterprise.edams.notification.service;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.dysmsapi20170525.models.SendSmsResponseBody;
import com.enterprise.edams.notification.service.impl.SmsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 短信服务单元测试
 *
 * @author Backend Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("短信服务单元测试")
class SmsServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private Client aliyunSmsClient;

    private SmsServiceImpl smsService;

    private static final String TEST_PHONE = "13800138000";
    private static final String TEST_CONTENT = "测试短信内容";
    private static final String TEST_CODE = "123456";

    @BeforeEach
    void setUp() {
        smsService = new SmsServiceImpl(redisTemplate);

        // 设置配置属性
        ReflectionTestUtils.setField(smsService, "smsEnabled", true);
        ReflectionTestUtils.setField(smsService, "accessKeyId", "test-access-key-id");
        ReflectionTestUtils.setField(smsService, "accessKeySecret", "test-access-key-secret");
        ReflectionTestUtils.setField(smsService, "endpoint", "dysmsapi.aliyuncs.com");
        ReflectionTestUtils.setField(smsService, "signName", "EDAMS测试");
        ReflectionTestUtils.setField(smsService, "frequencyLimit", 5);
        ReflectionTestUtils.setField(smsService, "frequencyWindowMinutes", 60);
        ReflectionTestUtils.setField(smsService, "aliyunSmsClient", aliyunSmsClient);

        // 初始化服务
        ReflectionTestUtils.invokeMethod(smsService, "init");
    }

    @Test
    @DisplayName("发送短信 - 成功")
    void testSend_Success() throws Exception {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(0); // 频率限制计数为0

        SendSmsResponse mockResponse = new SendSmsResponse();
        SendSmsResponseBody body = new SendSmsResponseBody();
        body.setCode("OK");
        body.setMessage("发送成功");
        body.setRequestId("test-request-id");
        mockResponse.setBody(body);

        when(aliyunSmsClient.sendSms(any(SendSmsRequest.class))).thenReturn(mockResponse);
        when(redisTemplate.opsForValue().increment(anyString())).thenReturn(1L);

        // When
        smsService.send(TEST_PHONE, TEST_CONTENT);

        // Then
        verify(aliyunSmsClient, times(1)).sendSms(any(SendSmsRequest.class));
        verify(redisTemplate.opsForValue(), times(1)).increment(anyString());
    }

    @Test
    @DisplayName("发送短信 - 短信服务未启用")
    void testSend_NotEnabled() {
        // Given
        ReflectionTestUtils.setField(smsService, "smsEnabled", false);

        // When
        smsService.send(TEST_PHONE, TEST_CONTENT);

        // Then - 未启用时不发送
        verify(aliyunSmsClient, never()).sendSms(any(SendSmsRequest.class));
    }

    @Test
    @DisplayName("发送短信 - 手机号格式错误")
    void testSend_InvalidPhone() {
        // Given
        String invalidPhone = "12345";

        // When & Then
        assertThatThrownBy(() -> smsService.send(invalidPhone, TEST_CONTENT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("手机号格式不正确");

        verify(aliyunSmsClient, never()).sendSms(any(SendSmsRequest.class));
    }

    @Test
    @DisplayName("发送短信 - 手机号为空")
    void testSend_NullPhone() {
        // When & Then
        assertThatThrownBy(() -> smsService.send(null, TEST_CONTENT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("手机号格式不正确");
    }

    @Test
    @DisplayName("发送短信 - 频率超限")
    void testSend_FrequencyLimitExceeded() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(5); // 已达到频率限制

        // When & Then
        assertThatThrownBy(() -> smsService.send(TEST_PHONE, TEST_CONTENT))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("短信发送频率超限");

        verify(aliyunSmsClient, never()).sendSms(any(SendSmsRequest.class));
    }

    @Test
    @DisplayName("发送短信 - 阿里云返回失败")
    void testSend_AliyunFailure() throws Exception {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(0);

        SendSmsResponse mockResponse = new SendSmsResponse();
        SendSmsResponseBody body = new SendSmsResponseBody();
        body.setCode("isv.BUSINESS_LIMIT_CONTROL");
        body.setMessage("触发限流");
        mockResponse.setBody(body);

        when(aliyunSmsClient.sendSms(any(SendSmsRequest.class))).thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> smsService.send(TEST_PHONE, TEST_CONTENT))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("短信发送失败");
    }

    @Test
    @DisplayName("发送模板短信 - 成功")
    void testSendTemplate_Success() throws Exception {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(0);

        SendSmsResponse mockResponse = new SendSmsResponse();
        SendSmsResponseBody body = new SendSmsResponseBody();
        body.setCode("OK");
        body.setMessage("发送成功");
        mockResponse.setBody(body);

        when(aliyunSmsClient.sendSms(any(SendSmsRequest.class))).thenReturn(mockResponse);
        when(redisTemplate.opsForValue().increment(anyString())).thenReturn(1L);

        Map<String, String> variables = new HashMap<>();
        variables.put("name", "张三");
        variables.put("taskName", "数据审批");

        // When
        smsService.sendTemplate(TEST_PHONE, "SMS_TASK_REMINDER_TEMPLATE", variables);

        // Then
        verify(aliyunSmsClient, times(1)).sendSms(any(SendSmsRequest.class));
    }

    @Test
    @DisplayName("发送模板短信 - 模板参数为空")
    void testSendTemplate_EmptyVariables() throws Exception {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(0);

        SendSmsResponse mockResponse = new SendSmsResponse();
        SendSmsResponseBody body = new SendSmsResponseBody();
        body.setCode("OK");
        body.setMessage("发送成功");
        mockResponse.setBody(body);

        when(aliyunSmsClient.sendSms(any(SendSmsRequest.class))).thenReturn(mockResponse);
        when(redisTemplate.opsForValue().increment(anyString())).thenReturn(1L);

        // When
        smsService.sendTemplate(TEST_PHONE, "SMS_DIRECT_TEMPLATE", new HashMap<>());

        // Then
        verify(aliyunSmsClient, times(1)).sendSms(any(SendSmsRequest.class));
    }

    @Test
    @DisplayName("发送验证码 - 成功")
    void testSendVerifyCode_Success() throws Exception {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(0);
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any());

        SendSmsResponse mockResponse = new SendSmsResponse();
        SendSmsResponseBody body = new SendSmsResponseBody();
        body.setCode("OK");
        body.setMessage("发送成功");
        mockResponse.setBody(body);

        when(aliyunSmsClient.sendSms(any(SendSmsRequest.class))).thenReturn(mockResponse);
        when(redisTemplate.opsForValue().increment(anyString())).thenReturn(1L);

        // When
        smsService.sendVerifyCode(TEST_PHONE, TEST_CODE);

        // Then
        // 验证验证码已保存到Redis
        verify(valueOperations, times(1)).set(contains(TEST_PHONE), eq(TEST_CODE), anyLong(), any());
        verify(aliyunSmsClient, times(1)).sendSms(any(SendSmsRequest.class));
    }

    @Test
    @DisplayName("发送验证码 - 验证手机号格式")
    void testSendVerifyCode_InvalidPhone() {
        // When & Then
        assertThatThrownBy(() -> smsService.sendVerifyCode("invalid-phone", TEST_CODE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("手机号格式不正确");
    }

    @Test
    @DisplayName("发送验证码 - 频率超限")
    void testSendVerifyCode_FrequencyLimit() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(5); // 已达限制

        // When & Then
        assertThatThrownBy(() -> smsService.sendVerifyCode(TEST_PHONE, TEST_CODE))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("验证码发送过于频繁");
    }

    @Test
    @DisplayName("验证手机号 - 正确格式")
    void testValidatePhone_Valid() {
        // Given
        String validPhone = "13800138000";

        // When
        boolean result = ReflectionTestUtils.invokeMethod(smsService, "validatePhone", validPhone);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("验证手机号 - 联通号段")
    void testValidatePhone_ChinaUnicom() {
        // Given
        String unicomPhone = "18612345678";

        // When
        boolean result = ReflectionTestUtils.invokeMethod(smsService, "validatePhone", unicomPhone);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("验证手机号 - 电信号段")
    void testValidatePhone_ChinaTelecom() {
        // Given
        String telecomPhone = "19912345678";

        // When
        boolean result = ReflectionTestUtils.invokeMethod(smsService, "validatePhone", telecomPhone);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("验证手机号 - 错误格式")
    void testValidatePhone_Invalid() {
        // Given
        String invalidPhone = "12345678901"; // 11位但不是有效号段

        // When
        boolean result = ReflectionTestUtils.invokeMethod(smsService, "validatePhone", invalidPhone);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("验证手机号 - 空字符串")
    void testValidatePhone_Empty() {
        // When
        boolean result = ReflectionTestUtils.invokeMethod(smsService, "validatePhone", "");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("检查频率限制 - 未超限")
    void testCheckFrequencyLimit_NotExceeded() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(3); // 小于限制5

        // When
        boolean result = ReflectionTestUtils.invokeMethod(smsService, "checkFrequencyLimit", TEST_PHONE);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("检查频率限制 - 刚好达到限制")
    void testCheckFrequencyLimit_AtLimit() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(5); // 等于限制

        // When
        boolean result = ReflectionTestUtils.invokeMethod(smsService, "checkFrequencyLimit", TEST_PHONE);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("检查频率限制 - 无历史记录")
    void testCheckFrequencyLimit_NoHistory() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        // When
        boolean result = ReflectionTestUtils.invokeMethod(smsService, "checkFrequencyLimit", TEST_PHONE);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("构建模板参数JSON - 正常")
    void testBuildTemplateParam_Normal() {
        // Given
        Map<String, String> variables = new HashMap<>();
        variables.put("code", "123456");
        variables.put("name", "测试");

        // When
        String result = ReflectionTestUtils.invokeMethod(smsService, "buildTemplateParam", variables);

        // Then
        assertThat(result).isEqualTo("{\"code\":\"123456\",\"name\":\"测试\"}");
    }

    @Test
    @DisplayName("构建模板参数JSON - 空参数")
    void testBuildTemplateParam_Empty() {
        // When
        String result = ReflectionTestUtils.invokeMethod(smsService, "buildTemplateParam", new HashMap<>());

        // Then
        assertThat(result).isEqualTo("{}");
    }

    @Test
    @DisplayName("构建模板参数JSON - null参数")
    void testBuildTemplateParam_Null() {
        // When
        String result = ReflectionTestUtils.invokeMethod(smsService, "buildTemplateParam", (Map<String, String>) null);

        // Then
        assertThat(result).isEqualTo("{}");
    }

    @Test
    @DisplayName("检查阿里云配置 - 有效配置")
    void testHasValidAliyunConfig_Valid() {
        // When
        boolean result = ReflectionTestUtils.invokeMethod(smsService, "hasValidAliyunConfig");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("检查阿里云配置 - accessKeyId为空")
    void testHasValidAliyunConfig_MissingAccessKeyId() {
        // Given
        ReflectionTestUtils.setField(smsService, "accessKeyId", "");

        // When
        boolean result = ReflectionTestUtils.invokeMethod(smsService, "hasValidAliyunConfig");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("检查阿里云配置 - accessKeySecret为空")
    void testHasValidAliyunConfig_MissingAccessKeySecret() {
        // Given
        ReflectionTestUtils.setField(smsService, "accessKeySecret", "");

        // When
        boolean result = ReflectionTestUtils.invokeMethod(smsService, "hasValidAliyunConfig");

        // Then
        assertThat(result).isFalse();
    }
}
