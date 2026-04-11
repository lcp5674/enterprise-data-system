package com.enterprise.edams.notification.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 通知模板实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_notification_template")
public class NotificationTemplate extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 模板名称 */
    private String name;

    /** 模板编码（唯一标识） */
    private String code;

    /** 适用渠道：email/sms/webhook/inapp等 */
    private String channel;

    /** 类型：1-系统，2-审批，3-告警，4-任务 */
    private Integer type;

    /** 邮件主题（邮件渠道使用） */
    private String subject;

    /** 内容模板（支持变量占位符，如${username}、${content}） */
    private String contentTemplate;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 描述 */
    private String description;
}
