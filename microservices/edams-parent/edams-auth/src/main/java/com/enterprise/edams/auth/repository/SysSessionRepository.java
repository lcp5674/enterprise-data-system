package com.enterprise.edams.auth.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.auth.entity.SysSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话Mapper
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Mapper
public interface SysSessionRepository extends BaseMapper<SysSession> {

    /**
     * 根据会话ID查询会话
     */
    @Select("SELECT * FROM sys_session WHERE session_id = #{sessionId} AND is_active = 1 LIMIT 1")
    SysSession findBySessionId(@Param("sessionId") String sessionId);

    /**
     * 根据用户ID查询有效会话
     */
    @Select("SELECT * FROM sys_session WHERE user_id = #{userId} AND is_active = 1 AND expire_time > NOW()")
    List<SysSession> findActiveSessionsByUserId(@Param("userId") String userId);

    /**
     * 根据用户ID和设备ID查询会话
     */
    @Select("SELECT * FROM sys_session WHERE user_id = #{userId} AND device_id = #{deviceId} AND is_active = 1 LIMIT 1")
    SysSession findByUserIdAndDeviceId(@Param("userId") String userId, @Param("deviceId") String deviceId);

    /**
     * 使会话失效
     */
    @Update("UPDATE sys_session SET is_active = 0 WHERE session_id = #{sessionId}")
    int invalidateSession(@Param("sessionId") String sessionId);

    /**
     * 使用户所有会话失效
     */
    @Update("UPDATE sys_session SET is_active = 0 WHERE user_id = #{userId}")
    int invalidateAllSessionsByUserId(@Param("userId") String userId);

    /**
     * 删除过期会话
     */
    int deleteExpiredSessions(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 更新最后活跃时间
     */
    @Update("UPDATE sys_session SET last_active_time = #{lastActiveTime} WHERE session_id = #{sessionId}")
    int updateLastActiveTime(@Param("sessionId") String sessionId, @Param("lastActiveTime") LocalDateTime lastActiveTime);
}
