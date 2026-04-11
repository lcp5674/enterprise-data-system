package com.enterprise.edams.chatbot.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.chatbot.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天会话Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

    /**
     * 根据用户ID查询活跃会话
     */
    @Select("SELECT * FROM chat_session WHERE user_id = #{userId} AND status = 'active' AND deleted = 0 ORDER BY last_active_time DESC")
    List<ChatSession> findActiveByUserId(@Param("userId") Long userId);

    /**
     * 查询用户的所有会话
     */
    @Select("SELECT * FROM chat_session WHERE user_id = #{userId} AND deleted = 0 ORDER BY last_active_time DESC")
    List<ChatSession> findByUserId(@Param("userId") Long userId);

    /**
     * 更新最后活跃时间
     */
    @Update("UPDATE chat_session SET last_active_time = #{lastActiveTime}, message_count = message_count + 1, updated_time = #{updatedTime} WHERE id = #{id}")
    int updateLastActiveTime(@Param("id") Long id, @Param("lastActiveTime") LocalDateTime lastActiveTime, @Param("updatedTime") LocalDateTime updatedTime);

    /**
     * 关闭会话
     */
    @Update("UPDATE chat_session SET status = 'closed', closed_time = #{closedTime}, close_reason = #{closeReason}, updated_time = #{updatedTime} WHERE id = #{id}")
    int closeSession(@Param("id") Long id, @Param("closedTime") LocalDateTime closedTime, @Param("closeReason") String closeReason, @Param("updatedTime") LocalDateTime updatedTime);

    /**
     * 统计用户会话数
     */
    @Select("SELECT COUNT(*) FROM chat_session WHERE user_id = #{userId} AND deleted = 0")
    int countByUserId(@Param("userId") Long userId);

    /**
     * 查询长时间未活跃的会话
     */
    @Select("SELECT * FROM chat_session WHERE status = 'active' AND last_active_time < #{cutoffTime} AND deleted = 0")
    List<ChatSession> findInactiveSessions(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 更新满意度评分
     */
    @Update("UPDATE chat_session SET satisfaction_score = #{score}, updated_time = #{updatedTime} WHERE id = #{id}")
    int updateSatisfactionScore(@Param("id") Long id, @Param("score") Integer score, @Param("updatedTime") LocalDateTime updatedTime);
}
