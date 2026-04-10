package com.enterprise.edams.notification.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知模板实体
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification_template")
public class NotificationTemplate extends BaseEntity {

    /**
     * 模板编码
     */
    private String code;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板类型: EMAIL=邮件, SMS=短信, IN_APP=站内消息, PUSH=推送
     */
    private String templateType;

    /**
     * 模板标题
     */
    private String title;

    /**
     * 模板内容
     */
    private String content;

    /**
     * 模板变量(JSON格式)
     */
    private String variables;

    /**
     * 模板状态: 0=禁用, 1=启用
     */
    private Integer status;

    /**
     * 模板描述
     */
    private String description;
}
