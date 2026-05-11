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
import org.junit.jupiter.api.Nested;
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
        testNotification = createTestNotification();
        testUserInfo = createTestUserInfo();
    }

    private Notification createTestNotification() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setTitle("测试通知");
        notification.setContent("测试内容");
        notification.setChannel("email");
        notification.setReceiverId(100L);
        notification.setStatus(0);
        notification.setIsRead(0);
        return notification;
    }

    private UserFeignClient.UserInfo createTestUserInfo() {
        UserFeignClient.UserInfo userInfo = new UserFeignClient.UserInfo();
        userInfo.setId(100L);
        userInfo.setUsername("testuser");
        userInfo.setEmail("test@example.com");
        userInfo.setPhone("13800138000");
        return userInfo;
    }

    private Map<String, NotificationChannel> createChannelMap(NotificationChannel... channels) {
        Map<String, NotificationChannel> channelMap = new HashMap<>();
        for (NotificationChannel channel : channels) {
            if (channel == emailChannel) {
                channelMap.put("email", emailChannel);
            } else if (channel == smsChannel) {
                channelMap.put("sms", smsChannel);
            }
        }
        return channelMap;
    }

    @Nested
    @DisplayName("通知发送测试")
    class NotificationSendingTests {

        @Test
        @DisplayName("应成功发送邮件通知")
        void shouldSendEmailSuccessfully() {
            when(userFeignClient.getUserById(100L)).thenReturn(testUserInfo);
            when(emailChannel.validateAddress("test@example.com")).thenReturn(true);
            when(emailChannel.send(any(Notification.class))).thenReturn(true);
            when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
            when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);
            setChannelMap(createChannelMap(emailChannel));

            Notification result = notificationService.send(testNotification);

            assertNotNull(result);
            assertEquals(1, result.getStatus());
            verify(emailChannel, times(1)).send(any(Notification.class));
        }

        @Test
        @DisplayName("当接收人ID为空时应抛出异常")
        void shouldThrowExceptionWhenReceiverIdIsNull() {
            testNotification.setReceiverId(null);

            assertThrows(IllegalArgumentException.class, () -> {
                notificationService.send(testNotification);
            });
        }

        @Test
        @DisplayName("当不支持渠道时应标记发送失败")
        void shouldMarkAsFailedForUnsupportedChannel() {
            testNotification.setChannel("unsupported");
            when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
            when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);
            setChannelMap(new HashMap<>());

            Notification result = notificationService.send(testNotification);

            assertEquals(2, result.getStatus());
            assertTrue(result.getErrorMessage().contains("不支持的通知渠道"));
        }

        @Test
        @DisplayName("当地址验证失败时应标记发送失败")
        void shouldMarkAsFailedWhenAddressValidationFails() {
            when(userFeignClient.getUserById(100L)).thenReturn(testUserInfo);
            when(emailChannel.validateAddress("test@example.com")).thenReturn(false);
            when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
            when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);
            setChannelMap(createChannelMap(emailChannel));

            Notification result = notificationService.send(testNotification);

            assertEquals(2, result.getStatus());
            assertTrue(result.getErrorMessage().contains("目标地址格式不正确"));
        }

        @Test
        @DisplayName("当渠道发送异常时应捕获异常")
        void shouldCatchExceptionFromChannelSend() {
            when(userFeignClient.getUserById(100L)).thenReturn(testUserInfo);
            when(emailChannel.validateAddress("test@example.com")).thenReturn(true);
            when(emailChannel.send(any(Notification.class))).thenThrow(new RuntimeException("SMTP连接失败"));
            when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
            when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);
            setChannelMap(createChannelMap(emailChannel));

            Notification result = notificationService.send(testNotification);

            assertEquals(2, result.getStatus());
            assertTrue(result.getErrorMessage().contains("SMTP连接失败"));
        }
    }

    @Nested
    @DisplayName("地址自动填充测试")
    class AddressAutoFillTests {

        @Test
        @DisplayName("应自动填充邮件地址")
        void shouldAutoFillEmailAddress() {
            testNotification.setTargetAddress(null);
            when(userFeignClient.getUserById(100L)).thenReturn(testUserInfo);
            when(emailChannel.validateAddress("test@example.com")).thenReturn(true);
            when(emailChannel.send(any(Notification.class))).thenReturn(true);
            when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
            when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);
            setChannelMap(createChannelMap(emailChannel));

            Notification result = notificationService.send(testNotification);

            assertEquals("test@example.com", result.getTargetAddress());
        }

        @Test
        @DisplayName("应自动填充手机号")
        void shouldAutoFillPhoneNumber() {
            testNotification.setChannel("sms");
            testNotification.setTargetAddress(null);
            when(userFeignClient.getUserById(100L)).thenReturn(testUserInfo);
            when(smsChannel.validateAddress("13800138000")).thenReturn(true);
            when(smsChannel.send(any(Notification.class))).thenReturn(true);
            when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
            when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);
            setChannelMap(createChannelMap(smsChannel));

            Notification result = notificationService.send(testNotification);

            assertEquals("13800138000", result.getTargetAddress());
        }

        @Test
        @DisplayName("当用户未找到时应继续处理")
        void shouldContinueWhenUserNotFound() {
            testNotification.setTargetAddress(null);
            when(userFeignClient.getUserById(100L)).thenReturn(null);
            when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
            when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);
            setChannelMap(createChannelMap(emailChannel));

            Notification result = notificationService.send(testNotification);

            assertNotNull(result);
        }

        @Test
        @DisplayName("当地址已设置时应跳过自动填充")
        void shouldSkipAutoFillWhenAddressExists() {
            testNotification.setTargetAddress("existing@example.com");
            when(emailChannel.validateAddress("existing@example.com")).thenReturn(true);
            when(emailChannel.send(any(Notification.class))).thenReturn(true);
            when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
            when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);
            setChannelMap(createChannelMap(emailChannel));

            Notification result = notificationService.send(testNotification);

            verify(userFeignClient, never()).getUserById(any());
        }

        @Test
        @DisplayName("系统通知应使用receiverId作为地址")
        void shouldUseReceiverIdForSystemNotification() {
            testNotification.setChannel("system");
            testNotification.setTargetAddress(null);
            NotificationChannel systemChannel = mock(NotificationChannel.class);
            when(systemChannel.validateAddress("100")).thenReturn(true);
            when(systemChannel.send(any(Notification.class))).thenReturn(true);
            when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
            when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);
            setChannelMap(Map.of("system", systemChannel));

            Notification result = notificationService.send(testNotification);

            assertEquals("100", result.getTargetAddress());
        }
    }

    @Nested
    @DisplayName("批量发送测试")
    class BatchSendingTests {

        @Test
        @DisplayName("应成功批量发送通知")
        void shouldSendBatchNotificationsSuccessfully() {
            Long[] receiverIds = {100L, 101L, 102L};
            when(userFeignClient.getUserById(anyLong())).thenReturn(testUserInfo);
            when(emailChannel.validateAddress("test@example.com")).thenReturn(true);
            when(emailChannel.send(any(Notification.class))).thenReturn(true);
            when(notificationMapper.insert(any(Notification.class))).thenReturn(1);
            when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);
            setChannelMap(createChannelMap(emailChannel));

            List<Notification> results = notificationService.sendBatch(receiverIds, "email", "批量测试", "批量内容");

            assertEquals(3, results.size());
            verify(emailChannel, times(3)).send(any(Notification.class));
        }

        @Test
        @DisplayName("批量发送空列表应返回空结果")
        void shouldReturnEmptyListForEmptyBatch() {
            Long[] receiverIds = {};
            List<Notification> results = notificationService.sendBatch(receiverIds, "email", "测试", "内容");

            assertTrue(results.isEmpty());
        }
    }

    @Nested
    @DisplayName("模板发送测试")
    class TemplateSendingTests {

        @Test
        @DisplayName("应成功按模板发送通知")
        void shouldSendByTemplateSuccessfully() {
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
            setChannelMap(createChannelMap(emailChannel));

            Notification result = notificationService.sendByTemplate("TPL001", 100L, params);

            assertNotNull(result);
            assertEquals("模板通知", result.getTitle());
            assertEquals("您好，张三，您的订单ORDER123已处理", result.getContent());
        }

        @Test
        @DisplayName("当模板不存在时应抛出异常")
        void shouldThrowExceptionWhenTemplateNotFound() {
            when(templateService.getByCode("NOTFOUND")).thenReturn(null);

            assertThrows(RuntimeException.class, () -> {
                notificationService.sendByTemplate("NOTFOUND", 100L, new HashMap<>());
            });
        }
    }

    @Nested
    @DisplayName("分页查询测试")
    class QueryTests {

        @Test
        @DisplayName("应成功分页查询通知")
        void shouldQueryNotificationsSuccessfully() {
            Page<Notification> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(testNotification));
            page.setTotal(1);

            when(notificationMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            IPage<Notification> result = notificationService.queryNotifications(100L, null, null, null, 1, 10);

            assertNotNull(result);
            assertEquals(1, result.getTotal());
            assertEquals(1, result.getRecords().size());
        }

        @Test
        @DisplayName("应支持按条件筛选查询")
        void shouldQueryWithFilters() {
            Page<Notification> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(testNotification));
            page.setTotal(1);

            when(notificationMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            IPage<Notification> result = notificationService.queryNotifications(100L, 1, 1, 0, 1, 10);

            assertNotNull(result);
            verify(notificationMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        }
    }

    @Nested
    @DisplayName("已读状态测试")
    class ReadStatusTests {

        @Test
        @DisplayName("应成功标记单条通知为已读")
        void shouldMarkNotificationAsRead() {
            when(notificationMapper.selectById(1L)).thenReturn(testNotification);
            when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);

            notificationService.markAsRead(1L);

            verify(notificationMapper, times(1)).updateById(any(Notification.class));
        }

        @Test
        @DisplayName("标记已读不存在通知应不操作")
        void shouldDoNothingWhenNotificationNotFound() {
            when(notificationMapper.selectById(999L)).thenReturn(null);

            notificationService.markAsRead(999L);

            verify(notificationMapper, never()).updateById(any());
        }

        @Test
        @DisplayName("应成功批量标记已读")
        void shouldMarkAllAsRead() {
            when(notificationMapper.update(any(Notification.class), any(LambdaQueryWrapper.class))).thenReturn(3);

            notificationService.markAllAsRead(100L);

            verify(notificationMapper, times(1)).update(any(Notification.class), any(LambdaQueryWrapper.class));
        }

        @Test
        @DisplayName("应成功获取未读数量")
        void shouldGetUnreadCount() {
            when(notificationMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

            long count = notificationService.getUnreadCount(100L);

            assertEquals(5L, count);
        }
    }

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
