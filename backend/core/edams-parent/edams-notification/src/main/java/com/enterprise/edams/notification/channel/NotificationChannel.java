package com.enterprise.edams.notification.channel;

import com.enterprise.edams.notification.entity.Notification;

/**
 * 通知渠道接口
 *
 * <p>渠道适配器模式的核心接口，所有通知渠道都实现此接口</p>
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface NotificationChannel {

    /**
     * 获取渠道类型标识（如：email、sms、webhook）
     */
    String getChannelType();

    /**
     * 发送通知
     *
     * @param notification 通知实体（包含目标地址和内容等）
     * @return true-发送成功，false-发送失败
     */
    boolean send(Notification notification);

    /**
     * 批量发送通知
     *
     * @param notifications 通知列表
     * @return 发送结果列表，与输入列表一一对应
     */
    default boolean[] sendBatch(Notification[] notifications) {
        boolean[] results = new boolean[notifications.length];
        for (int i = 0; i < notifications.length; i++) {
            results[i] = send(notifications[i]);
        }
        return results;
    }

    /**
     * 验证目标地址是否合法
     *
     * @param targetAddress 目标地址
     * @return true-地址合法
     */
    boolean validateAddress(String targetAddress);

    /**
     * 检查渠道是否可用
     */
    boolean isAvailable();
}
