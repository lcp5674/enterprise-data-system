package com.enterprise.edams.collaboration.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评论实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("collaboration_comment")
public class Comment extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 评论内容 */
    private String content;

    /** 评论类型：0-文本，1-图片，2-文件，3-链接 */
    private Integer commentType;

    /** 引用ID（可以是数据资产ID、任务ID等） */
    private Long referenceId;

    /** 引用类型：0-数据资产，1-任务，2-文档，3-报告 */
    private Integer referenceType;

    /** 父评论ID（0表示根评论） */
    private Long parentId;

    /** 评论者ID */
    private Long userId;

    /** 评论者姓名 */
    private String userName;

    /** 评论者头像 */
    private String userAvatar;

    /** 状态：0-草稿，1-已发布，2-已删除，3-审核中，4-审核失败 */
    private Integer status;

    /** 点赞数 */
    private Integer likeCount;

    /** 回复数 */
    private Integer replyCount;

    /** 最后回复时间 */
    private LocalDateTime lastReplyTime;

    /** 是否置顶：0-否，1-是 */
    private Integer isTop;

    /** 置顶时间 */
    private LocalDateTime topTime;

    /** 是否精华：0-否，1-是 */
    private Integer isEssence;

    /** 租户ID */
    private Long tenantId;

    /** 附件信息（JSON格式） */
    private String attachmentInfo;
}