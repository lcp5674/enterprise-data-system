package com.enterprise.edams.notification.service.impl;

import com.enterprise.edams.notification.entity.NotificationSubscription;
import com.enterprise.edams.notification.repository.NotificationSubscriptionRepository;
import com.enterprise.edams.notification.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 订阅服务实现
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final NotificationSubscriptionRepository subscriptionRepository;

    @Override
    @Transactional
    public void subscribe(String userId, String eventType, String notificationChannel) {
        log.info("订阅通知: userId={}, eventType={}, channel={}", userId, eventType, notificationChannel);

        // 检查是否已存在订阅
        List<NotificationSubscription> existing = subscriptionRepository.findByUserIdAndEventType(userId, eventType);
        boolean alreadySubscribed = existing.stream()
                .anyMatch(s -> notificationChannel.equals(s.getNotificationChannel()));

        if (alreadySubscribed) {
            log.info("订阅已存在，无需重复订阅");
            return;
        }

        NotificationSubscription subscription = NotificationSubscription.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .eventType(eventType)
                .notificationChannel(notificationChannel)
                .enabled(1)
                .createdBy(getCurrentUsername())
                .createdTime(LocalDateTime.now())
                .build();

        subscriptionRepository.insert(subscription);
    }

    @Override
    @Transactional
    public void unsubscribe(String userId, String eventType, String notificationChannel) {
        log.info("取消订阅: userId={}, eventType={}, channel={}", userId, eventType, notificationChannel);

        List<NotificationSubscription> subscriptions = subscriptionRepository.findByUserIdAndEventType(userId, eventType);
        subscriptions.stream()
                .filter(s -> notificationChannel.equals(s.getNotificationChannel()))
                .forEach(s -> {
                    s.setEnabled(0);
                    s.setUpdatedBy(getCurrentUsername());
                    s.setUpdatedTime(LocalDateTime.now());
                    subscriptionRepository.updateById(s);
                });
    }

    @Override
    public List<NotificationSubscription> getUserSubscriptions(String userId) {
        return subscriptionRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void updateSubscriptionStatus(String subscriptionId, boolean enabled) {
        NotificationSubscription subscription = subscriptionRepository.selectById(subscriptionId);
        if (subscription == null) {
            return;
        }

        subscription.setEnabled(enabled ? 1 : 0);
        subscription.setUpdatedBy(getCurrentUsername());
        subscription.setUpdatedTime(LocalDateTime.now());
        subscriptionRepository.updateById(subscription);
    }

    @Override
    public List<String> getSubscribersByEventType(String eventType, String channel) {
        List<NotificationSubscription> subscriptions = subscriptionRepository.findSubscribersByEventType(eventType, channel);
        return subscriptions.stream()
                .map(NotificationSubscription::getUserId)
                .distinct()
                .collect(Collectors.toList());
    }

    // ========== 私有方法 ==========

    private String getCurrentUsername() {
        try {
            return org.springframework.security.core.SecurityContextHolder.getContext()
                    .getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
}
