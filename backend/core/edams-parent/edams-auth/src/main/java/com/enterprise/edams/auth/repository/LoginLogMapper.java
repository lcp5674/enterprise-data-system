package com.enterprise.edams.auth.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.auth.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录日志Mapper
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {

    /**
     * 查询用户最近N条登录日志
     *
     * @param userId 用户ID
     * @param limit  限制数量
     * @return 日志列表
     */
    @Select("SELECT * FROM sys_login_log WHERE user_id = #{userId} ORDER BY login_time DESC LIMIT #{limit}")
    List<LoginLog> findRecentByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 统计指定时间范围内的登录失败次数
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 失败次数
     */
    @Select("SELECT COUNT(*) FROM sys_login_log WHERE user_id = #{userId} AND status = 0 AND login_time BETWEEN #{startTime} AND #{endTime}")
    int countLoginFailures(@Param("userId") Long userId,
                           @Param("startTime") LocalDateTime startTime,
                           @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定IP的登录尝试次数（用于限流检测）
     *
     * @param ip        IP地址
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 尝试次数
     */
    @Select("SELECT COUNT(*) FROM sys_login_log WHERE ip = #{ip} AND login_time BETWEEN #{startTime} AND #{endTime}")
    int countLoginAttemptsByIp(@Param("ip") String ip,
                                @Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime);
}
