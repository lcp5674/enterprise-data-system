package com.enterprise.edams.collaboration.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 评论实体
 * 
 * @author EDAMS Team
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("col_comment")
public class Comment {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 父评论ID（回复时使用）
     */
    private String parentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论人ID
     */
    private String userId;

    /**
     * 评论人名称
     */
    private String userName;

    /**
     * 评论人头像
     */
    private String userAvatar;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 回复数
     */
    private Integer replyCount;

    /**
     * 是否置顶
     */
    private Boolean isTop;

    /**
     * 状态：0-正常，1-已删除，2-已屏蔽
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Boolean deleted;
}
