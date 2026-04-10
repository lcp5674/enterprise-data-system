package com.enterprise.edams.collaboration.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 订阅实体
 * 
 * @author EDAMS Team
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("col_subscription")
public class Subscription {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 订阅类型：1-资产，2-数据表，3-问题，4-用户，5-标签
     */
    private Integer subscribeType;

    /**
     * 订阅对象ID
     */
    private String subscribeId;

    /**
     * 订阅对象名称
     */
    private String subscribeName;

    /**
     * 通知方式：1-站内消息，2-邮件，3-短信
     */
    private String notifyType;

    /**
     * 是否启用通知
     */
    private Boolean notifyEnabled;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Boolean deleted;
}
