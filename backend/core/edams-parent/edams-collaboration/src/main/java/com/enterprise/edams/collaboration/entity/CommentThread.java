package com.enterprise.edams.collaboration.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 评论线程实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("collaboration_comment_thread")
public class CommentThread extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 线程标题 */
    private String threadTitle;

    /** 线程描述 */
    private String threadDescription;

    /** 引用ID（可以是数据资产ID、任务ID等） */
    private Long referenceId;

    /** 引用类型：0-数据资产，1-任务，2-文档，3-报告 */
    private Integer referenceType;

    /** 创建者ID */
    private Long creatorId;

    /** 创建者姓名 */
    private String creatorName;

    /** 状态：0-草稿，1-活跃，2-关闭，3-归档 */
    private Integer status;

    /** 评论总数 */
    private Integer commentCount;

    /** 最后评论时间 */
    private String lastCommentTime;

    /** 是否锁定：0-否，1-是 */
    private Integer isLocked;

    /** 锁定原因 */
    private String lockReason;

    /** 租户ID */
    private Long tenantId;
}