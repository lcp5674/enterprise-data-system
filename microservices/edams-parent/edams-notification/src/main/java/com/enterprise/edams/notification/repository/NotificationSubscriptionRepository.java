package com.enterprise.edams.notification.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.notification.entity.NotificationSubscription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 通知订阅Mapper
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Mapper
public interface NotificationSubscriptionRepository extends BaseMapper<NotificationSubscription> {

    /**
     * 根据用户ID查询订阅
     */
    @Select("SELECT * FROM notification_subscription WHERE user_id = #{userId} AND enabled = 1")
    List<NotificationSubscription> findByUserId(@Param("userId") String userId);

    /**
     * 根据用户ID和事件类型查询订阅
     */
    @Select("SELECT * FROM notification_subscription WHERE user_id = #{userId} AND event_type = #{eventType} AND enabled = 1")
    List<NotificationSubscription> findByUserIdAndEventType(@Param("userId") String userId, @Param("eventType") String eventType);

    /**
     * 根据事件类型查询订阅的用户
     */
    @Select("""
        SELECT ns.* FROM notification_subscription ns
        INNER JOIN sys_user su ON ns.user_id = su.id
        WHERE ns.event_type = #{eventType}
        AND ns.enabled = 1
        AND ns.notification_channel = #{channel}
        AND su.status = 1
        AND su.is_deleted = 0
        """)
    List<NotificationSubscription> findSubscribersByEventType(@Param("eventType") String eventType, @Param("channel") String channel);
}
