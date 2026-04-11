package com.enterprise.edams.chatbot.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.chatbot.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天消息Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 根据会话ID查询消息列表
     */
    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} AND deleted = 0 ORDER BY message_time ASC")
    List<ChatMessage> findBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 查询会话最近的N条消息
     */
    @Select("SELECT * FROM chat_message WHERE session_id = #{sessionId} AND deleted = 0 ORDER BY message_time DESC LIMIT #{limit}")
    List<ChatMessage> findRecentBySessionId(@Param("sessionId") Long sessionId, @Param("limit") int limit);

    /**
     * 统计会话消息数
     */
    @Select("SELECT COUNT(*) FROM chat_message WHERE session_id = #{sessionId} AND deleted = 0")
    int countBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 标记消息为已读
     */
    @Update("UPDATE chat_message SET is_read = 1, updated_time = #{updatedTime} WHERE id = #{id}")
    int markAsRead(@Param("id") Long id, @Param("updatedTime") LocalDateTime updatedTime);

    /**
     * 根据意图类型统计消息
     */
    @Select("SELECT intent_type, COUNT(*) as count FROM chat_message WHERE deleted = 0 GROUP BY intent_type")
    List<java.util.Map<String, Object>> countByIntentType();

    /**
     * 查询未读消息数
     */
    @Select("SELECT COUNT(*) FROM chat_message WHERE session_id = #{sessionId} AND role = 'assistant' AND is_read = 0 AND deleted = 0")
    int countUnreadMessages(@Param("sessionId") Long sessionId);

    /**
     * 更新消息评分
     */
    @Update("UPDATE chat_message SET rating = #{rating}, feedback = #{feedback}, updated_time = #{updatedTime} WHERE id = #{id}")
    int updateRating(@Param("id") Long id, @Param("rating") Integer rating, @Param("feedback") String feedback, @Param("updatedTime") LocalDateTime updatedTime);
}
