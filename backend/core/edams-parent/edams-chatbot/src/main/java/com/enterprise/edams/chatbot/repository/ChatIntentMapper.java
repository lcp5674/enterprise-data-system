package com.enterprise.edams.chatbot.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.chatbot.entity.ChatIntent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 聊天意图Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface ChatIntentMapper extends BaseMapper<ChatIntent> {

    /**
     * 根据意图类型查询
     */
    @Select("SELECT * FROM chat_intent WHERE intent_type = #{intentType} AND enabled = 1 AND deleted = 0")
    List<ChatIntent> findByIntentType(@Param("intentType") String intentType);

    /**
     * 查询所有启用的意图
     */
    @Select("SELECT * FROM chat_intent WHERE enabled = 1 AND deleted = 0 ORDER BY priority ASC")
    List<ChatIntent> findAllEnabled();

    /**
     * 根据目标服务查询意图
     */
    @Select("SELECT * FROM chat_intent WHERE target_service = #{targetService} AND enabled = 1 AND deleted = 0")
    List<ChatIntent> findByTargetService(@Param("targetService") String targetService);

    /**
     * 查询父意图下的子意图
     */
    @Select("SELECT * FROM chat_intent WHERE parent_intent_id = #{parentId} AND enabled = 1 AND deleted = 0 ORDER BY priority ASC")
    List<ChatIntent> findByParentId(@Param("parentId") Long parentId);

    /**
     * 查询后续意图
     */
    @Select("SELECT * FROM chat_intent WHERE intent_type IN (${followUpIntents}) AND enabled = 1 AND deleted = 0")
    List<ChatIntent> findFollowUpIntents(@Param("followUpIntents") String followUpIntents);

    /**
     * 统计意图使用次数
     */
    @Select("SELECT intent_type, COUNT(*) as usage_count FROM chat_message WHERE intent_type IS NOT NULL AND deleted = 0 GROUP BY intent_type ORDER BY usage_count DESC")
    List<java.util.Map<String, Object>> countIntentUsage();
}
