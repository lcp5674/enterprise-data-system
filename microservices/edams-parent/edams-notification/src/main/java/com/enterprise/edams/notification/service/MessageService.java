package com.enterprise.edams.notification.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.notification.dto.NotificationMessageVO;

/**
 * 消息服务接口
 *
 * @author Backend Team
 * @version 1.0.0
 */
public interface MessageService {

    /**
     * 获取用户消息列表
     */
    Page<NotificationMessageVO> getUserMessages(String userId, String status, int pageNum, int pageSize);

    /**
     * 获取用户未读消息
     */
    java.util.List<NotificationMessageVO> getUnreadMessages(String userId);

    /**
     * 获取用户未读消息数量
     */
    long getUnreadCount(String userId);

    /**
     * 标记消息为已读
     */
    void markAsRead(String messageId);

    /**
     * 标记所有消息为已读
     */
    void markAllAsRead(String userId);

    /**
     * 删除消息
     */
    void deleteMessage(String messageId);

    /**
     * 根据业务ID获取消息
     */
    java.util.List<NotificationMessageVO> getMessagesByBusiness(String businessType, String businessId);
}
