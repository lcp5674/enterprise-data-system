package com.enterprise.edams.collaboration.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 反应实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("collaboration_reaction")
public class Reaction extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 评论ID */
    private Long commentId;

    /** 反应类型：0-点赞，1-喜欢，2-赞同，3-反对，4-疑问，5-惊讶 */
    private Integer reactionType;

    /** 反应者ID */
    private Long userId;

    /** 反应者姓名 */
    private String userName;

    /** 反应时间 */
    private LocalDateTime reactionTime;

    /** 租户ID */
    private Long tenantId;
}