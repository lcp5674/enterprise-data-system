package com.enterprise.edams.collaboration.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 提及实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("collaboration_mention")
public class Mention extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 被提及的用户ID */
    private Long mentionedUserId;

    /** 被提及的用户姓名 */
    private String mentionedUserName;

    /** 提及来源ID（评论ID、任务ID等） */
    private Long sourceId;

    /** 提及来源类型：0-评论，1-任务，2-文档，3-报告 */
    private Integer sourceType;

    /** 提及者ID */
    private Long mentionerId;

    /** 提及者姓名 */
    private String mentionerName;

    /** 状态：0-未读，1-已读，2-已处理 */
    private Integer status;

    /** 阅读时间 */
    private LocalDateTime readTime;

    /** 处理时间 */
    private LocalDateTime handledTime;

    /** 提及内容 */
    private String mentionContent;

    /** 租户ID */
    private Long tenantId;
}