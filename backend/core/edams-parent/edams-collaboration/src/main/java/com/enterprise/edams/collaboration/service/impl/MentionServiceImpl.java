package com.enterprise.edams.collaboration.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.collaboration.entity.Mention;
import com.enterprise.edams.collaboration.repository.MentionMapper;
import com.enterprise.edams.collaboration.service.MentionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 提及服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MentionServiceImpl implements MentionService {

    private final MentionMapper mentionMapper;

    @Override
    @Transactional
    public Mention createMention(Mention mention) {
        mention.setStatus(0);
        mentionMapper.insert(mention);
        return mention;
    }

    @Override
    @Transactional
    public Mention updateMention(Long id, Mention mention) {
        Mention existing = mentionMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("提及不存在");
        }

        mentionMapper.updateById(mention);
        return mentionMapper.selectById(id);
    }

    @Override
    public Mention getMention(Long id) {
        return mentionMapper.selectById(id);
    }

    @Override
    @Transactional
    public void deleteMention(Long id) {
        mentionMapper.deleteById(id);
    }

    @Override
    public IPage<Mention> listMentions(Integer pageNum, Integer pageSize) {
        Page<Mention> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Mention> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("create_time");
        return mentionMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Mention> listMentionsByUserIdAndStatus(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        Page<Mention> page = new Page<>(pageNum, pageSize);
        return mentionMapper.findByUserIdAndStatus(page, userId, status);
    }

    @Override
    public IPage<Mention> listMentionsBySource(Long sourceId, Integer sourceType, Integer pageNum, Integer pageSize) {
        Page<Mention> page = new Page<>(pageNum, pageSize);
        return mentionMapper.findBySource(page, sourceId, sourceType);
    }

    @Override
    public IPage<Mention> listMentionsByMentionerId(Long mentionerId, Integer pageNum, Integer pageSize) {
        Page<Mention> page = new Page<>(pageNum, pageSize);
        return mentionMapper.findByMentionerId(page, mentionerId);
    }

    @Override
    @Transactional
    public Mention markMentionAsRead(Long id) {
        Mention mention = mentionMapper.selectById(id);
        if (mention == null) {
            throw new BusinessException("提及不存在");
        }

        mention.setStatus(1);
        mention.setReadTime(LocalDateTime.now());
        mentionMapper.updateById(mention);
        return mention;
    }

    @Override
    @Transactional
    public Mention markMentionAsHandled(Long id) {
        Mention mention = mentionMapper.selectById(id);
        if (mention == null) {
            throw new BusinessException("提及不存在");
        }

        mention.setStatus(2);
        mention.setHandledTime(LocalDateTime.now());
        mentionMapper.updateById(mention);
        return mention;
    }

    @Override
    @Transactional
    public void batchMarkMentionsAsRead(Long userId) {
        QueryWrapper<Mention> wrapper = new QueryWrapper<>();
        wrapper.eq("mentioned_user_id", userId);
        wrapper.eq("status", 0);
        
        Mention mention = new Mention();
        mention.setStatus(1);
        mention.setReadTime(LocalDateTime.now());
        mentionMapper.update(mention, wrapper);
    }
}