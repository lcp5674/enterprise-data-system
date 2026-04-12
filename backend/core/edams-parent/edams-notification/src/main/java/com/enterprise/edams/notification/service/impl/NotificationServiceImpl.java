package com.enterprise.edams.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.common.feign.UserFeignClient;
import com.enterprise.edams.notification.channel.NotificationChannel;
import com.enterprise.edams.notification.entity.Notification;
import com.enterprise.edams.notification.entity.NotificationTemplate;
import com.enterprise.edams.notification.repository.NotificationMapper;
import com.enterprise.edams.notification.service.NotificationService;
import com.enterprise.edams.notification.service.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 通知服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final NotificationTemplateService templateService;
    private final Map<String, NotificationChannel> channelMap; // 自动注入所有渠道实现
    private final UserFeignClient userFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Notification send(Notification notification) {
        // 1. 保存通知记录
        notification.setStatus(0); // 待发送
        notification.setIsRead(0);
        notification.setRetryCount(0);
        notification.setSendTime(LocalDateTime.now());
        if (notification.getReceiverId() == null) {
            throw new IllegalArgumentException("接收人ID不能为空");
        }

        // 2. 根据接收人ID查询联系方式
        enrichNotificationTargetAddress(notification);

        // 3. 保存通知记录
        notificationMapper.insert(notification);

        // 4. 获取对应渠道并发送
        NotificationChannel channel = channelMap.get(notification.getChannel());
        if (channel == null) {
            log.error("不支持的通知渠道: {}", notification.getChannel());
            notification.setStatus(2); // 发送失败
            notification.setErrorMessage("不支持的通知渠道: " + notification.getChannel());
            notificationMapper.updateById(notification);
            return notification;
        }

        // 验证目标地址
        if (!channel.validateAddress(notification.getTargetAddress())) {
            log.error("目标地址格式不正确: {}", notification.getTargetAddress());
            notification.setStatus(2);
            notification.setErrorMessage("目标地址格式不正确");
            notificationMapper.updateById(notification);
            return notification;
        }

        try {
            boolean success = channel.send(notification);
            notification.setStatus(success ? 1 : 2); // 已发送/发送失败
        } catch (Exception e) {
            log.error("通知发送异常: {}", e.getMessage(), e);
            notification.setStatus(2);
            notification.setErrorMessage(e.getMessage());
        }

        notificationMapper.updateById(notification);
        return notification;
    }

    /**
     * 根据receiverId查询用户联系信息并填充到notification
     */
    private void enrichNotificationTargetAddress(Notification notification) {
        if (notification.getTargetAddress() != null && !notification.getTargetAddress().isEmpty()) {
            return; // 已有地址，无需填充
        }

        try {
            var userInfo = userFeignClient.getUserById(notification.getReceiverId());
            if (userInfo == null) {
                log.warn("未找到用户信息: receiverId={}", notification.getReceiverId());
                return;
            }

            String channel = notification.getChannel();
            if ("email".equalsIgnoreCase(channel)) {
                notification.setTargetAddress(userInfo.getEmail());
            } else if ("sms".equalsIgnoreCase(channel)) {
                notification.setTargetAddress(userInfo.getPhone());
            } else if ("webhook".equalsIgnoreCase(channel)) {
                notification.setTargetAddress(userInfo.getWebhookUrl());
            } else if ("in_app".equalsIgnoreCase(channel) || "system".equalsIgnoreCase(channel)) {
                // 系统通知不需要目标地址
                notification.setTargetAddress(notification.getReceiverId().toString());
            } else {
                // 默认：优先使用邮箱，其次手机
                if (userInfo.getEmail() != null && !userInfo.getEmail().isEmpty()) {
                    notification.setTargetAddress(userInfo.getEmail());
                } else if (userInfo.getPhone() != null && !userInfo.getPhone().isEmpty()) {
                    notification.setTargetAddress(userInfo.getPhone());
                }
            }

            log.debug("已填充通知目标地址: channel={}, receiverId={}, target={}",
                    channel, notification.getReceiverId(), maskAddress(notification.getTargetAddress()));

        } catch (Exception e) {
            log.warn("查询用户联系信息失败: receiverId={}, error={}", notification.getReceiverId(), e.getMessage());
        }
    }

    /**
     * 地址脱敏
     */
    private String maskAddress(String address) {
        if (address == null || address.isEmpty()) return "***";
        if (address.contains("@")) {
            // 邮箱脱敏
            int atIndex = address.indexOf("@");
            String prefix = address.substring(0, atIndex);
            if (prefix.length() > 2) {
                return prefix.substring(0, 2) + "***" + address.substring(atIndex);
            }
        } else if (address.matches("^1[3-9]\\d{9}$")) {
            // 手机号脱敏
            return address.substring(0, 3) + "****" + address.substring(7);
        }
        return "***";
    }

    @Override
    public List<Notification> sendBatch(Long[] receiverIds, String channel, String title, String content) {
        List<Notification> results = new ArrayList<>();

        for (Long receiverId : receiverIds) {
            Notification notif = new Notification();
            notif.setTitle(title);
            notif.setContent(content);
            notif.setChannel(channel);
            notif.setReceiverId(receiverId);
            // 目标地址将在send方法中根据receiverId自动填充
            results.add(send(notif));
        }
        return results;
    }

    @Override
    public Notification sendByTemplate(String templateCode, Long receiverId,
                                        Map<String, Object> params) {
        // 1. 查找模板
        NotificationTemplate template = templateService.getByCode(templateCode);
        if (template == null) {
            throw new RuntimeException("通知模板不存在: " + templateCode);
        }

        // 2. 渲染模板内容
        String renderedContent = templateService.renderTemplate(template, params);

        // 3. 创建并保存通知
        Notification notification = new Notification();
        notification.setTitle(template.getSubject());
        notification.setContent(renderedContent);
        notification.setType(template.getType());
        notification.setChannel(template.getChannel());
        notification.setReceiverId(receiverId);
        notification.setTemplateId(template.getId());

        // 将参数序列化为JSON保存（用于审计）
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = 
                    new com.fasterxml.jackson.databind.ObjectMapper();
            notification.setTemplateParams(mapper.writeValueAsString(params));
        } catch (Exception ignored) {}

        return send(notification);
    }

    @Override
    public IPage<Notification> queryNotifications(Long receiverId, Integer type, Integer status,
                                                   Integer isRead, int pageNum, int pageSize) {
        Page<Notification> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();

        if (receiverId != null) wrapper.eq(Notification::getReceiverId, receiverId);
        if (type != null) wrapper.eq(Notification::getType, type);
        if (status != null) wrapper.eq(Notification::getStatus, status);
        if (isRead != null) wrapper.eq(Notification::getIsRead, isRead);

        wrapper.orderByDesc(Notification::getCreatedTime);
        return notificationMapper.selectPage(page, wrapper);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notif = notificationMapper.selectById(notificationId);
        if (notif == null) return;
        notif.setIsRead(1);
        notif.setReadTime(LocalDateTime.now());
        notificationMapper.updateById(notif);
    }

    @Override
    public void markAllAsRead(Long receiverId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getReceiverId, receiverId)
               .eq(Notification::getIsRead, 0);

        Notification updateEntity = new Notification();
        updateEntity.setIsRead(1);
        updateEntity.setReadTime(LocalDateTime.now());
        notificationMapper.update(updateEntity, wrapper);
    }

    @Override
    public long getUnreadCount(Long receiverId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getReceiverId, receiverId)
               .eq(Notification::getIsRead, 0);
        return notificationMapper.selectCount(wrapper);
    }
}
