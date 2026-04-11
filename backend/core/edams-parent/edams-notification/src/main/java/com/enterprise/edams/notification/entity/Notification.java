package com.enterprise.edams.notification.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_notification")
public class Notification extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 类型：1-系统通知，2-审批通知，3-告警通知，4-任务通知，5-其他 */
    private Integer type;

    /** 渠道：email, sms, webhook, inapp, wechat, dingtalk等 */
    private String channel;

    /** 发送状态：0-待发送，1-已发送，2-发送失败，3-部分失败 */
    private Integer status;

    /** 接收人ID（用户ID） */
    private Long receiverId;

    /** 接收人姓名 */
    private String receiverName;

    /** 接收地址（邮箱/手机号/webhook URL） */
    private String targetAddress;

    /** 模板ID（如果使用了模板） */
    private Long templateId;

    /** 模板参数JSON */
    private String templateParams;

    /** 发送时间 */
    private LocalDateTime sendTime;

    /** 阅读时间 */
    private LocalDateTime readTime;

    /** 是否已读：0-未读，1-已读 */
    private Integer isRead;

    /** 错误信息（发送失败时记录） */
    private String errorMessage;

    /** 重试次数 */
    private Integer retryCount;

    /** 来源模块 */
    private String sourceModule;

    /** 业务关联ID */
    private String businessKey;
}
