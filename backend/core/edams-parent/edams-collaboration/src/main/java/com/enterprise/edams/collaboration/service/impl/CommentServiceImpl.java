package com.enterprise.edams.collaboration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.collaboration.entity.Comment;
import com.enterprise.edams.collaboration.repository.CommentMapper;
import com.enterprise.edams.collaboration.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 评论服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public Comment createComment(Comment comment) {
        comment.setStatus(1);
        comment.setLikeCount(0);
        comment.setReplyCount(0);
        comment.setIsTop(0);
        comment.setIsEssence(0);
        commentMapper.insert(comment);
        return comment;
    }

    @Override
    @Transactional
    public Comment updateComment(Long id, Comment comment) {
        Comment existing = commentMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("评论不存在");
        }

        commentMapper.updateById(comment);
        return commentMapper.selectById(id);
    }

    @Override
    public Comment getComment(Long id) {
        return commentMapper.selectById(id);
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        commentMapper.deleteById(id);
    }

    @Override
    public IPage<Comment> listComments(Integer pageNum, Integer pageSize) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        return commentMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Comment> listCommentsByReference(Long referenceId, Integer referenceType, Integer pageNum, Integer pageSize) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        return commentMapper.findByReference(page, referenceId, referenceType);
    }

    @Override
    public IPage<Comment> listRepliesByParentId(Long parentId, Integer pageNum, Integer pageSize) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        return commentMapper.findByParentId(page, parentId);
    }

    @Override
    public IPage<Comment> listCommentsByUserId(Long userId, Integer pageNum, Integer pageSize) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        return commentMapper.findByUserId(page, userId);
    }

    @Override
    public IPage<Comment> searchComments(String keyword, Integer pageNum, Integer pageSize) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        return commentMapper.searchByKeyword(page, keyword);
    }

    @Override
    public Integer countCommentsByReference(Long referenceId, Integer referenceType) {
        return commentMapper.countByReference(referenceId, referenceType);
    }

    @Override
    @Transactional
    public Comment likeComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        comment.setLikeCount(comment.getLikeCount() + 1);
        commentMapper.updateById(comment);
        return comment;
    }

    @Override
    @Transactional
    public Comment unlikeComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        if (comment.getLikeCount() > 0) {
            comment.setLikeCount(comment.getLikeCount() - 1);
        }
        commentMapper.updateById(comment);
        return comment;
    }

    @Override
    @Transactional
    public Comment topComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        comment.setIsTop(1);
        comment.setTopTime(LocalDateTime.now());
        commentMapper.updateById(comment);
        return comment;
    }

    @Override
    @Transactional
    public Comment cancelTopComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        comment.setIsTop(0);
        commentMapper.updateById(comment);
        return comment;
    }

    @Override
    @Transactional
    public Comment essenceComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
           9hrow new BusinessException("评论不存在");
        }

        comment.setIsEssence(1);
        commentMapper.updateById(comment);
        return comment;
    }

    @Override
    @Transactional
    public Comment cancelEssenceComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        comment.setIsEssence(0);
        commentMapper.updateById(comment);
        return comment;
    }
}