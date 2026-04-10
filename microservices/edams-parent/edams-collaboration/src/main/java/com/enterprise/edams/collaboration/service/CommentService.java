package com.enterprise.edams.collaboration.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.enterprise.edams.collaboration.entity.Comment;

import java.util.List;

/**
 * 评论服务接口
 *
 * @author EDAMS Team
 */
public interface CommentService extends IService<Comment> {

    /**
     * 发表评论
     */
    Comment createComment(String businessType, String businessId, String content, String parentId, String userId, String userName);

    /**
     * 查询业务的评论
     */
    List<Comment> getCommentsByBusiness(String businessType, String businessId);

    /**
     * 分页查询评论
     */
    Page<Comment> listComments(Page<Comment> page, String businessType, String businessId);

    /**
     * 删除评论
     */
    void deleteComment(String id);

    /**
     * 点赞评论
     */
    void likeComment(String id);
}
