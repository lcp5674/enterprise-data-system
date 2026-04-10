package com.enterprise.edams.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知订阅实体
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
@TableName("notification_subscription")
public class NotificationSubscription {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 通知方式: EMAIL=邮件, SMS=短信, IN_APP=站内消息, PUSH=推送
     */
    private String notificationChannel;

    /**
     * 是否启用: 0=禁用, 1=启用
     */
    private Integer enabled;

    /**
     * 创建者
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新者
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
