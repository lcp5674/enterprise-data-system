package com.enterprise.edams.collaboration.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.collaboration.entity.Reaction;

/**
 * 反应服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface ReactionService {

    /**
     * 创建反应
     */
    Reaction createReaction(Reaction reaction);

    /**
     * 更新反应
     */
    Reaction updateReaction(Long id, Reaction reaction);

    /**
     * 获取反应
     */
    Reaction getReaction(Long id);

    /**
     * 删除反应
     */
    void deleteReaction(Long id);

    /**
     * 分页查询反应
     */
    IPage<Reaction> listReactions(Integer pageNum, Integer pageSize);

    /**
     * 根据评论ID查询反应
     */
    IPage<Reaction> listReactionsByCommentId(Long commentId, Integer pageNum, Integer pageSize);

    /**
     * 根据用户ID查询反应
     */
    IPage<Reaction> listReactionsByUserId(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 根据评论ID和反应类型查询反应
     */
    IPage<Reaction> listReactionsByCommentIdAndType(Long commentId, Integer reactionType, Integer pageNum, Integer pageSize);

    /**
     * 统计反应数量
     */
    Integer countReactionsByCommentIdAndType(Long commentId, Integer reactionType);
}