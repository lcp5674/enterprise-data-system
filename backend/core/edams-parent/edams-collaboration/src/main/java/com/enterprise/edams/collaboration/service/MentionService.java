package com.enterprise.edams.collaboration.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.enterprise.edams.collaboration.entity.Mention;

/**
 * 提及服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface MentionService {

    /**
     * 创建提及
     */
    Mention createMention(Mention mention);

    /**
     * 更新提及
     */
    Mention updateMention(Long id, Mention mention);

    /**
     * 获取提及
     */
    Mention getMention(Long id);

    /**
     * 删除提及
     */
    void deleteMention(Long id);

    /**
     * 分页查询提及
     */
    IPage<Mention> listMentions(Integer pageNum, Integer pageSize);

    /**
     * 根据用户ID和状态查询提及
     */
    IPage<Mention> listMentionsByUserIdAndStatus(Long userId, Integer status, Integer pageNum, Integer pageSize);

    /**
     * 根据来源查询提及
     */
    IPage<Mention> listMentionsBySource(Long sourceId, Integer sourceType, Integer pageNum, Integer pageSize);

    /**
     * 根据提及者ID查询提及
     */
    IPage<Mention> listMentionsByMentionerId(Long mentionerId, Integer pageNum, Integer pageSize);

    /**
     * 标记提及为已读
     */
    Mention markMentionAsRead(Long id);

    /**
     * 标记提及为已处理
     */
    Mention markMentionAsHandled(Long id);

    /**
     * 批量标记提及为已读
     */
    void batchMarkMentionsAsRead(Long userId);
}