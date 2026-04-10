package com.enterprise.edams.collaboration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.enterprise.edams.collaboration.entity.Comment;
import com.enterprise.edams.collaboration.repository.CommentRepository;
import com.enterprise.edams.collaboration.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 评论服务实现
 *
 * @author EDAMS Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentRepository, Comment>
        implements CommentService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Comment createComment(String businessType, String businessId, String content, String parentId, String userId, String userName) {
        Comment comment = new Comment();
        comment.setBusinessType(businessType);
        comment.setBusinessId(businessId);
        comment.setContent(content);
        comment.setParentId(parentId);
        comment.setUserId(userId);
        comment.setUserName(userName);
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        comment.setIsTop(false);
        comment.setStatus(0);

        save(comment);

        // 如果是回复，更新父评论的回复数
        if (StringUtils.hasText(parentId)) {
            Comment parent = getById(parentId);
            if (parent != null) {
                parent.setReplyCount(parent.getReplyCount() + 1);
                updateById(parent);
            }
        }

        log.info("评论创建成功: {}-{}", businessType, businessId);
        return comment;
    }

    @Override
    public List<Comment> getCommentsByBusiness(String businessType, String businessId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getBusinessType, businessType)
                .eq(Comment::getBusinessId, businessId)
                .isNull(Comment::getParentId)
                .orderByDesc(Comment::getIsTop)
                .orderByDesc(Comment::getCreatedTime);
        return list(wrapper);
    }

    @Override
    public Page<Comment> listComments(Page<Comment> page, String businessType, String businessId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(businessType)) {
            wrapper.eq(Comment::getBusinessType, businessType);
        }
        if (StringUtils.hasText(businessId)) {
            wrapper.eq(Comment::getBusinessId, businessId);
        }
        wrapper.orderByDesc(Comment::getCreatedTime);
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(String id) {
        removeById(id);
        log.info("评论删除成功: {}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeComment(String id) {
        Comment comment = getById(id);
        if (comment != null) {
            comment.setLikeCount(comment.getLikeCount() + 1);
            updateById(comment);
        }
    }
}
