package com.enterprise.edams.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知消息实体
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
@TableName("notification_message")
public class NotificationMessage {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 消息类型: EMAIL=邮件, SMS=短信, IN_APP=站内消息, PUSH=推送
     */
    private String messageType;

    /**
     * 接收者用户ID
     */
    private String userId;

    /**
     * 接收者邮箱
     */
    private String email;

    /**
     * 接收者手机号
     */
    private String phone;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 关联的业务类型
     */
    private String businessType;

    /**
     * 关联的业务ID
     */
    private String businessId;

    /**
     * 消息状态: PENDING=待发送, SENDING=发送中, SENT=已发送, FAILED=发送失败, READ=已读
     */
    private String status;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 发送失败原因
     */
    private String errorMessage;

    /**
     * 重试次数
     */
    private Integer retryCount;

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
