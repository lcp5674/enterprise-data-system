package com.enterprise.edams.collaboration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.collaboration.entity.Reaction;
import com.enterprise.edams.collaboration.repository.ReactionMapper;
import com.enterprise.edams.collaboration.service.ReactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 反应服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReactionServiceImpl implements ReactionService {

    private final ReactionMapper reactionMapper;

    @Override
    @Transactional
    public Reaction createReaction(Reaction reaction) {
        reaction.setReactionTime(LocalDateTime.now());
        reactionMapper.insert(reaction);
        return reaction;
    }

    @Override
    @Transactional
    public Reaction updateReaction(Long id, Reaction reaction) {
        Reaction existing = reactionMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("反应不存在");
        }

        reactionMapper.updateById(reaction);
        return reactionMapper.selectById(id);
    }

    @Override
    public Reaction getReaction(Long id) {
        return reactionMapper.selectById(id);
    }

    @Override
    @Transactional
    public void deleteReaction(Long id) {
        reactionMapper.deleteById(id);
    }

    @Override
    public IPage<Reaction> listReactions(Integer pageNum, Integer pageSize) {
        Page<Reaction> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Reaction> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("reaction_time");
        return reactionMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Reaction> listReactionsByCommentId(Long commentId, Integer pageNum, Integer pageSize) {
        Page<Reaction> page = new Page<>(pageNum, pageSize);
        return reactionMapper.findByCommentId(page, commentId);
    }

    @Override
    public IPage<Reaction> listReactionsByUserId(Long userId, Integer pageNum, Integer pageSize) {
        Page<Reaction> page = new Page<>(pageNum, pageSize);
        return reactionMapper.findByUserId(page, userId);
    }

    @Override
    public IPage<Reaction> listReactionsByCommentIdAndType(Long commentId, Integer reactionType, Integer pageNum, Integer pageSize) {
        Page<Reaction> page = new Page<>(pageNum, pageSize);
        return reactionMapper.findByCommentIdAndType(page, commentId, reactionType);
    }

    @Override
    public Integer countReactionsByCommentIdAndType(Long commentId, Integer reactionType) {
        return reactionMapper.countByCommentIdAndType(commentId, reactionType);
    }
}