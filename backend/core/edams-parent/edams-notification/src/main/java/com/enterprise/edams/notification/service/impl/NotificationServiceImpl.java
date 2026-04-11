package com.enterprise.edams.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
        notificationMapper.insert(notification);

        // 2. 获取对应渠道并发送
        NotificationChannel channel = channelMap.get(notification.getChannel());
        if (channel == null) {
            log.error("不支持的通知渠道: {}", notification.getChannel());
            notification.setStatus(2); // 发送失败
            notification.setErrorMessage("不支持的通知渠道: " + notification.getChannel());
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

    @Override
    public List<Notification> sendBatch(Long[] receiverIds, String channel, String title, String content) {
        List<Notification> results = new ArrayList<>();
        
        for (Long receiverId : receiverIds) {
            Notification notif = new Notification();
            notif.setTitle(title);
            notif.setContent(content);
            notif.setChannel(channel);
            notif.setReceiverId(receiverId);
            
            // TODO: 根据receiverId查询用户邮箱/手机号等地址信息
            // 这里简化处理
            notif.setTargetAddress("");
            
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
