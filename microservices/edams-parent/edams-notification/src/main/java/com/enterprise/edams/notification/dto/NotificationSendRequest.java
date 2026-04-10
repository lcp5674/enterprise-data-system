package com.enterprise.edams.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 通知发送请求DTO
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
public class NotificationSendRequest {

    /**
     * 消息类型: EMAIL=邮件, SMS=短信, IN_APP=站内消息, PUSH=推送
     */
    @NotBlank(message = "消息类型不能为空")
    private String messageType;

    /**
     * 接收者用户ID
     */
    private String userId;

    /**
     * 接收者用户ID列表(批量发送)
     */
    private List<String> userIds;

    /**
     * 接收者邮箱(直接指定)
     */
    private String email;

    /**
     * 接收者手机号(直接指定)
     */
    private String phone;

    /**
     * 消息标题
     */
    @NotBlank(message = "消息标题不能为空")
    private String title;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
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
     * 模板变量(当使用模板时)
     */
    private Map<String, String> variables;

    /**
     * 是否异步发送
     */
    private boolean async = true;
}
