package com.enterprise.edams.auth.entity;

import com.enterprise.edams.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * 系统用户实体
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 密码盐值
     */
    private String passwordSalt;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 工号
     */
    private String employeeNo;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 部门ID
     */
    private String departmentId;

    /**
     * 职位
     */
    private String position;

    /**
     * 上级ID
     */
    private String managerId;

    /**
     * 状态：1-启用，0-禁用，2-锁定
     */
    private Integer status;

    /**
     * 用户类型：1-内部用户，2-外部用户，3-系统用户
     */
    private Integer userType;

    /**
     * 来源类型：LOCAL/LDAP/OAUTH2
     */
    private String sourceType;

    /**
     * 第三方用户ID
     */
    private String sourceId;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 连续登录失败次数
     */
    private Integer loginFailCount;

    /**
     * 密码过期时间
     */
    private LocalDateTime passwordExpireTime;

    /**
     * 是否启用MFA
     */
    private Integer mfaEnabled;

    /**
     * MFA密钥（加密存储）
     */
    private String mfaSecret;

    /**
     * MFA备用码（JSON数组）
     */
    private String mfaBackupCodes;

    /**
     * 是否首次登录
     */
    private Integer isFirstLogin;
}
