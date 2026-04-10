package com.enterprise.edams.notification.service;

import com.enterprise.edams.notification.entity.NotificationSubscription;

import java.util.List;

/**
 * 订阅服务接口
 *
 * @author Backend Team
 * @version 1.0.0
 */
public interface SubscriptionService {

    /**
     * 订阅通知
     */
    void subscribe(String userId, String eventType, String notificationChannel);

    /**
     * 取消订阅
     */
    void unsubscribe(String userId, String eventType, String notificationChannel);

    /**
     * 获取用户订阅列表
     */
    List<NotificationSubscription> getUserSubscriptions(String userId);

    /**
     * 更新订阅状态
     */
    void updateSubscriptionStatus(String subscriptionId, boolean enabled);

    /**
     * 根据事件类型获取订阅用户
     */
    List<String> getSubscribersByEventType(String eventType, String channel);
}
