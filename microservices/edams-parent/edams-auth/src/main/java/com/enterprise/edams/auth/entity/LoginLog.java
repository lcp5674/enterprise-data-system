package com.enterprise.edams.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志实体
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
@TableName("login_log")
public class LoginLog {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 登录类型：PASSWORD/MOBILE_CODE/SSO/OAUTH2
     */
    private String loginType;

    /**
     * 登录来源：WEB/APP/API
     */
    private String loginSource;

    /**
     * 状态：SUCCESS/FAIL/LOCKED
     */
    private String status;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 登录地点
     */
    private String location;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * Token ID
     */
    private String tokenId;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}
