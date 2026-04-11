package com.enterprise.edams.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 登录日志实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_login_log")
public class LoginLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 登录IP地址
     */
    private String ip;

    /**
     * 浏览器User-Agent
     */
    private String userAgent;

    /**
     * 登录状态：0-失败，1-成功
     */
    private Integer status;

    /**
     * 消息（登录失败原因或成功信息）
     */
    private String message;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 地理位置
     */
    private String location;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 登录方式：password-密码，mfa-MFA，token-令牌，sso-单点登录
     */
    private String loginType;
}
