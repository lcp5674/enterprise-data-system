package com.enterprise.edams.notification.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.notification.entity.NotificationMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知消息Mapper
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Mapper
public interface NotificationMessageRepository extends BaseMapper<NotificationMessage> {

    /**
     * 根据用户ID查询未读消息
     */
    @Select("SELECT * FROM notification_message WHERE user_id = #{userId} AND status = 'SENT' ORDER BY created_time DESC")
    List<NotificationMessage> findUnreadByUserId(@Param("userId") String userId);

    /**
     * 根据用户ID查询消息
     */
    @Select("SELECT * FROM notification_message WHERE user_id = #{userId} ORDER BY created_time DESC")
    List<NotificationMessage> findByUserId(@Param("userId") String userId);

    /**
     * 统计用户未读消息数量
     */
    @Select("SELECT COUNT(*) FROM notification_message WHERE user_id = #{userId} AND status = 'SENT'")
    long countUnreadByUserId(@Param("userId") String userId);

    /**
     * 根据业务ID查询消息
     */
    @Select("SELECT * FROM notification_message WHERE business_type = #{businessType} AND business_id = #{businessId}")
    List<NotificationMessage> findByBusiness(@Param("businessType") String businessType, @Param("businessId") String businessId);

    /**
     * 查询待发送的消息
     */
    @Select("SELECT * FROM notification_message WHERE status = 'PENDING' AND send_time <= #{currentTime} ORDER BY created_time LIMIT #{limit}")
    List<NotificationMessage> findPendingMessages(@Param("currentTime") LocalDateTime currentTime, @Param("limit") int limit);

    /**
     * 标记消息为已读
     */
    @Update("UPDATE notification_message SET status = 'READ', read_time = #{readTime} WHERE id = #{id}")
    int markAsRead(@Param("id") String id, @Param("readTime") LocalDateTime readTime);
}
