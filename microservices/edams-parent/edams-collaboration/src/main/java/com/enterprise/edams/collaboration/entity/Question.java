package com.enterprise.edams.collaboration.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 问答实体
 * 
 * @author EDAMS Team
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("col_question")
public class Question {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 问题标题
     */
    private String title;

    /**
     * 问题内容
     */
    private String content;

    /**
     * 标签（逗号分隔）
     */
    private String tags;

    /**
     * 分类
     */
    private String category;

    /**
     * 提问人ID
     */
    private String userId;

    /**
     * 提问人名称
     */
    private String userName;

    /**
     * 浏览数
     */
    private Integer viewCount;

    /**
     * 回答数
     */
    private Integer answerCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 收藏数
     */
    private Integer favoriteCount;

    /**
     * 状态：0-待回答，1-已回答，2-已解决，3-已关闭
     */
    private Integer status;

    /**
     * 最佳回答ID
     */
    private String bestAnswerId;

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
