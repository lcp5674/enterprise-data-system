package com.enterprise.edams.notification.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.feign.UserFeignClient;
import com.enterprise.edams.notification.channel.NotificationChannel;
import com.enterprise.edams.notification.entity.Notification;
import com.enterprise.edams.notification.entity.NotificationTemplate;
import com.enterprise.edams.notification.repository.NotificationMapper;
import com.enterprise.edams.notification.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 通知服务单元测试
 * 测试NotificationServiceImpl的核心业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("通知服务测试")
class NotificationServiceTest {

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private NotificationTemplateService templateService;

    @Mock
    private NotificationChannel emailChannel;

    @Mock
    private NotificationChannel smsChannel;

    @Mock
    private UserFeignClient userFeignClient;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private Notification testNotification;
    private UserFeignClient.UserInfo testUserInfo;

    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setTitle("测试通知");
        testNotification.setContent("测试内容");
        testNotification.setChannel("email");
        testNotification.setReceiverId(100L);
        testNotification.setStatus(0);
        testNotification.setIsRead(0);

        testUserInfo = new UserFeignClient.UserInfo();
        testUserInfo.setId(100L);
        testUserInfo.setUsername("testuser");
        testUserInfo.setEmail("test@example.com");
        testUserInfo.setPhone("13800138000");
    }

    @Test
    @DisplayName("测试发送通知 - 成功")
    void testSend_Success() {
        // Given
        when(userFeignClient.getUserById(100L)).thenReturn(testUserInfo);
        when(emailChannel.validateAddress("test@example.com")).thenReturn(true);
        when(emailChannel.send(any(Notification.class))).thenReturn(true);
        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
        when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);

        // 注入channelMap
        Map<String, NotificationChannel> channelMap = new HashMap<>();
        channelMap.put("email", emailChannel);
        setChannelMap(channelMap);

        // When
        Notification result = notificationService.send(testNotification);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getStatus()); // 已发送
        verify(emailChannel, times(1)).send(any(Notification.class));
    }

    @Test
    @DisplayName("测试发送通知 - 接收人ID为空")
    void testSend_NullReceiverId_ThrowsException() {
        // Given
        testNotification.setReceiverId(null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.send(testNotification);
        });
    }

    @Test
    @DisplayName("测试发送通知 - 不支持的渠道")
    void testSend_UnsupportedChannel_Fails() {
        // Given
        testNotification.setChannel("unsupported");
        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
        when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);

        // 注入channelMap（不含unsupported渠道）
        Map<String, NotificationChannel> channelMap = new HashMap<>();
        channelMap.put("email", emailChannel);
        setChannelMap(channelMap);

        // When
        Notification result = notificationService.send(testNotification);

        // Then
        assertEquals(2, result.getStatus()); // 发送失败
        assertTrue(result.getErrorMessage().contains("不支持的通知渠道"));
    }

    @Test
    @DisplayName("测试发送通知 - 地址验证失败")
    void testSend_InvalidAddress_Fails() {
        // Given
        when(userFeignClient.getUserById(100L)).thenReturn(testUserInfo);
        when(emailChannel.validateAddress("test@example.com")).thenReturn(false);
        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
        when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);

        // 注入channelMap
        Map<String, NotificationChannel> channelMap = new HashMap<>();
        channelMap.put("email", emailChannel);
        setChannelMap(channelMap);

        // When
        Notification result = notificationService.send(testNotification);

        // Then
        assertEquals(2, result.getStatus()); // 发送失败
        assertTrue(result.getErrorMessage().contains("目标地址格式不正确"));
    }

    @Test
    @DisplayName("测试发送通知 - 渠道发送异常")
    void testSend_ChannelException_Fails() {
        // Given
        when(userFeignClient.getUserById(100L)).thenReturn(testUserInfo);
        when(emailChannel.validateAddress("test@example.com")).thenReturn(true);
        when(emailChannel.send(any(Notification.class))).thenThrow(new RuntimeException("SMTP连接失败"));
        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
        when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);

        // 注入channelMap
        Map<String, NotificationChannel> channelMap = new HashMap<>();
        channelMap.put("email", emailChannel);
        setChannelMap(channelMap);

        // When
        Notification result = notificationService.send(testNotification);

        // Then
        assertEquals(2, result.getStatus()); // 发送失败
        assertTrue(result.getErrorMessage().contains("SMTP连接失败"));
    }

    @Test
    @DisplayName("测试发送通知 - 自动填充邮件地址")
    void testSend_EmailAddressAutoFill() {
        // Given
        testNotification.setTargetAddress(null); // 未设置地址
        when(userFeignClient.getUserById(100L)).thenReturn(testUserInfo);
        when(emailChannel.validateAddress("test@example.com")).thenReturn(true);
        when(emailChannel.send(any(Notification.class))).thenReturn(true);
        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
        when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);

        // 注入channelMap
        Map<String, NotificationChannel> channelMap = new HashMap<>();
        channelMap.put("email", emailChannel);
        setChannelMap(channelMap);

        // When
        Notification result = notificationService.send(testNotification);

        // Then
        assertEquals("test@example.com", result.getTargetAddress());
    }

    @Test
    @DisplayName("测试发送通知 - 自动填充手机号")
    void testSend_PhoneAddressAutoFill() {
        // Given
        testNotification.setChannel("sms");
        testNotification.setTargetAddress(null);
        when(userFeignClient.getUserById(100L)).thenReturn(testUserInfo);
        when(smsChannel.validateAddress("13800138000")).thenReturn(true);
        when(smsChannel.send(any(Notification.class))).thenReturn(true);
        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
        when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);

        // 注入channelMap
        Map<String, NotificationChannel> channelMap = new HashMap<>();
        channelMap.put("sms", smsChannel);
        setChannelMap(channelMap);

        // When
        Notification result = notificationService.send(testNotification);

        // Then
        assertEquals("13800138000", result.getTargetAddress());
    }

    @Test
    @DisplayName("测试发送通知 - 用户未找到")
    void testSend_UserNotFound() {
        // Given
        testNotification.setTargetAddress(null);
        when(userFeignClient.getUserById(100L)).thenReturn(null);
        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
        when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);

        // 注入channelMap
        Map<String, NotificationChannel> channelMap = new HashMap<>();
        channelMap.put("email", emailChannel);
        setChannelMap(channelMap);

        // When - 不应抛出异常
        Notification result = notificationService.send(testNotification);

        // Then
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试发送通知 - 系统通知不需要目标地址")
    void testSend_SystemChannel_NoAddress() {
        // Given
        testNotification.setChannel("system");
        testNotification.setTargetAddress(null);
        NotificationChannel systemChannel = mock(NotificationChannel.class);
        when(systemChannel.validateAddress("100")).thenReturn(true);
        when(systemChannel.send(any(Notification.class))).thenReturn(true);
        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
        when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);

        // 注入channelMap
        Map<String, NotificationChannel> channelMap = new HashMap<>();
        channelMap.put("system", systemChannel);
        setChannelMap(channelMap);

        // When
        Notification result = notificationService.send(testNotification);

        // Then
        assertEquals("100", result.getTargetAddress());
    }

    @Test
    @DisplayName("测试批量发送通知")
    void testSendBatch_Success() {
        // Given
        Long[] receiverIds = {100L, 101L, 102L};
        when(userFeignClient.getUserById(100L)).thenReturn(testUserInfo);
        when(userFeignClient.getUserById(101L)).thenReturn(testUserInfo);
        when(userFeignClient.getUserById(102L)).thenReturn(testUserInfo);
        when(emailChannel.validateAddress("test@example.com")).thenReturn(true);
        when(emailChannel.send(any(Notification.class))).thenReturn(true);
        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
        when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);

        // 注入channelMap
        Map<String, NotificationChannel> channelMap = new HashMap<>();
        channelMap.put("email", emailChannel);
        setChannelMap(channelMap);

        // When
        List<Notification> results = notificationService.sendBatch(receiverIds, "email", "批量测试", "批量内容");

        // Then
        assertEquals(3, results.size());
        verify(emailChannel, times(3)).send(any(Notification.class));
    }

    @Test
    @DisplayName("测试按模板发送通知 - 成功")
    void testSendByTemplate_Success() {
        // Given
        NotificationTemplate template = new NotificationTemplate();
        template.setId(1L);
        template.setCode("TPL001");
        template.setSubject("模板通知");
        template.setContent("您好，${username}，您的订单${orderId}已处理");
        template.setChannel("email");
        template.setType(1);

        Map<String, Object> params = new HashMap<>();
        params.put("username", "张三");
        params.put("orderId", "ORDER123");

        when(templateService.getByCode("TPL001")).thenReturn(template);
        when(templateService.renderTemplate(eq(template), any())).thenReturn("您好，张三，您的订单ORDER123已处理");
        when(userFeignClient.getUserById(100L)).thenReturn(testUserInfo);
        when(emailChannel.validateAddress("test@example.com")).thenReturn(true);
        when(emailChannel.send(any(Notification.class))).thenReturn(true);
        when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
        when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);

        // 注入channelMap
        Map<String, NotificationChannel> channelMap = new HashMap<>();
        channelMap.put("email", emailChannel);
        setChannelMap(channelMap);

        // When
        Notification result = notificationService.sendByTemplate("TPL001", 100L, params);

        // Then
        assertNotNull(result);
        assertEquals("模板通知", result.getTitle());
        assertEquals("您好，张三，您的订单ORDER123已处理", result.getContent());
    }

    @Test
    @DisplayName("测试按模板发送通知 - 模板不存在")
    void testSendByTemplate_TemplateNotFound_ThrowsException() {
        // Given
        when(templateService.getByCode("NOTFOUND")).thenReturn(null);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            notificationService.sendByTemplate("NOTFOUND", 100L, new HashMap<>());
        });
    }

    @Test
    @DisplayName("测试分页查询通知")
    void testQueryNotifications_Success() {
        // Given
        Page<Notification> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testNotification));
        page.setTotal(1);

        when(notificationMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // When
        IPage<Notification> result = notificationService.queryNotifications(100L, null, null, null, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("测试分页查询通知 - 带筛选条件")
    void testQueryNotifications_WithFilters() {
        // Given
        Page<Notification> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testNotification));
        page.setTotal(1);

        when(notificationMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // When
        IPage<Notification> result = notificationService.queryNotifications(100L, 1, 1, 0, 1, 10);

        // Then
        assertNotNull(result);
        verify(notificationMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试标记已读")
    void testMarkAsRead_Success() {
        // Given
        when(notificationMapper.selectById(1L)).thenReturn(testNotification);
        when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);

        // When
        notificationService.markAsRead(1L);

        // Then
        verify(notificationMapper, times(1)).updateById(any(Notification.class));
    }

    @Test
    @DisplayName("测试标记已读 - 通知不存在")
    void testMarkAsRead_NotFound_NoAction() {
        // Given
        when(notificationMapper.selectById(999L)).thenReturn(null);

        // When
        notificationService.markAsRead(999L);

        // Then
        verify(notificationMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("测试全部标记已读")
    void testMarkAllAsRead_Success() {
        // Given
        when(notificationMapper.update(any(Notification.class), any(LambdaQueryWrapper.class))).thenReturn(3);

        // When
        notificationService.markAllAsRead(100L);

        // Then
        verify(notificationMapper, times(1)).update(any(Notification.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测试获取未读数量")
    void testGetUnreadCount_Success() {
        // Given
        when(notificationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        // When
        long count = notificationService.getUnreadCount(100L);

        // Then
        assertEquals(5L, count);
    }

    /**
     * 通过反射设置channelMap
     */
    private void setChannelMap(Map<String, NotificationChannel> channelMap) {
        try {
            var field = NotificationServiceImpl.class.getDeclaredField("channelMap");
            field.setAccessible(true);
            field.set(notificationService, channelMap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set channelMap", e);
        }
    }
}
