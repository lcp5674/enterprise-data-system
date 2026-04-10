package com.enterprise.edams.auth.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.enterprise.edams.auth.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录日志Mapper
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Mapper
public interface LoginLogRepository extends BaseMapper<LoginLog> {

    /**
     * 查询用户最近的登录记录
     */
    List<LoginLog> findRecentByUserId(@Param("userId") String userId, @Param("limit") int limit);

    /**
     * 查询用户在指定时间内的登录失败次数
     */
    int countFailedLoginByUserIdAfter(@Param("userId") String userId, @Param("failTime") LocalDateTime failTime);

    /**
     * 查询用户在指定IP的登录失败次数
     */
    int countFailedLoginByIpAfter(@Param("ip") String ip, @Param("failTime") LocalDateTime failTime);
}
