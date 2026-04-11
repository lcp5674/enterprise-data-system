package com.enterprise.edams.collaboration.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.collaboration.entity.Comment;

/**
 * 评论服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface CommentService {

    /**
     * 创建评论
     */
    Comment createComment(Comment comment);

    /**
     * 更新评论
     */
    Comment updateComment(Long id, Comment comment);

    /**
     * 获取评论
     */
    Comment getComment(Long id);

    /**
     * 删除评论
     */
    void deleteComment(Long id);

    /**
     * 分页查询评论
     */
    IPage<Comment> listComments(Integer pageNum, Integer pageSize);

    /**
     * 根据引用查询评论
     */
    IPage<Comment> listCommentsByReference(Long referenceId, Integer referenceType, Integer pageNum, Integer pageSize);

    /**
     * 根据父评论ID查询回复
     */
    IPage<Comment> listRepliesByParentId(Long parentId, Integer pageNum, Integer pageSize);

    /**
     * 根据用户ID查询评论
     */
    IPage<Comment> listCommentsByUserId(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 搜索评论
     */
    IPage<Comment> searchComments(String keyword, Integer pageNum, Integer pageSize);

    /**
     * 统计评论数量
     */
    Integer countCommentsByReference(Long referenceId, Integer referenceType);

    /**
     * 点赞评论
     */
    Comment likeComment(Long id);

    /**
     * 取消点赞评论
     */
    Comment unlikeComment(Long id);

    /**
     * 置顶评论
     */
    Comment topComment(Long id);

    /**
     * 取消置顶评论
     */
    Comment cancelTopComment(Long id);

    /**
     * 设置精华评论
     */
    Comment essenceComment(Long id);

    /**
     * 取消精华评论
     */
    Comment cancelEssenceComment(Long id);
}