package com.enterprise.edams.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.common.result.ResultCode;
import com.enterprise.edams.notification.dto.NotificationMessageVO;
import com.enterprise.edams.notification.entity.NotificationMessage;
import com.enterprise.edams.notification.repository.NotificationMessageRepository;
import com.enterprise.edams.notification.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息服务实现
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final NotificationMessageRepository messageRepository;

    @Override
    public Page<NotificationMessageVO> getUserMessages(String userId, String status, int pageNum, int pageSize) {
        LambdaQueryWrapper<NotificationMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotificationMessage::getUserId, userId);

        if (status != null && !status.isEmpty()) {
            wrapper.eq(NotificationMessage::getStatus, status);
        }

        wrapper.orderByDesc(NotificationMessage::getCreatedTime);

        Page<NotificationMessage> page = new Page<>(pageNum, pageSize);
        Page<NotificationMessage> result = messageRepository.selectPage(page, wrapper);

        Page<NotificationMessageVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public List<NotificationMessageVO> getUnreadMessages(String userId) {
        return messageRepository.findUnreadByUserId(userId).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(String userId) {
        return messageRepository.countUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public void markAsRead(String messageId) {
        NotificationMessage message = messageRepository.selectById(messageId);
        if (message == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "消息不存在");
        }

        message.setStatus("READ");
        message.setReadTime(LocalDateTime.now());
        messageRepository.updateById(message);

        log.info("标记消息已读: messageId={}", messageId);
    }

    @Override
    @Transactional
    public void markAllAsRead(String userId) {
        List<NotificationMessage> unreadMessages = messageRepository.findUnreadByUserId(userId);
        LocalDateTime readTime = LocalDateTime.now();

        for (NotificationMessage message : unreadMessages) {
            message.setStatus("READ");
            message.setReadTime(readTime);
            messageRepository.updateById(message);
        }

        log.info("标记所有消息已读: userId={}, count={}", userId, unreadMessages.size());
    }

    @Override
    @Transactional
    public void deleteMessage(String messageId) {
        NotificationMessage message = messageRepository.selectById(messageId);
        if (message == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "消息不存在");
        }

        messageRepository.deleteById(messageId);
        log.info("删除消息: messageId={}", messageId);
    }

    @Override
    public List<NotificationMessageVO> getMessagesByBusiness(String businessType, String businessId) {
        return messageRepository.findByBusiness(businessType, businessId).stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    // ========== 私有方法 ==========

    private NotificationMessageVO convertToVO(NotificationMessage message) {
        NotificationMessageVO vo = new NotificationMessageVO();
        BeanUtils.copyProperties(message, vo);
        return vo;
    }
}
