package com.enterprise.edams.notification.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.notification.entity.Notification;
import java.util.List;

/**
 * 通知服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface NotificationService {

    /** 发送通知（单条） */
    Notification send(Notification notification);

    /** 批量发送通知（同一内容给多个人） */
    List<Notification> sendBatch(Long[] receiverIds, String channel, String title, String content);

    /** 使用模板发送通知 */
    Notification sendByTemplate(String templateCode, Long receiverId, 
                                 java.util.Map<String, Object> params);

    /** 分页查询通知 */
    IPage<Notification> queryNotifications(Long receiverId, Integer type, Integer status,
                                            Integer isRead, int pageNum, int pageSize);

    /** 标记已读 */
    void markAsRead(Long notificationId);

    /** 批量标记已读 */
    void markAllAsRead(Long receiverId);

    /** 获取未读数量 */
    long getUnreadCount(Long receiverId);
}
