package com.enterprise.edams.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.enterprise.edams.common.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user")
public class User extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 用户名（登录账号） */
    private String username;

    /** 密码（加密存储） */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 头像URL */
    private String avatar;

    /** 性别：0-未知，1-男，2-女 */
    private Integer gender;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    /** 部门ID */
    private Long departmentId;

    /** 租户ID */
    private Long tenantId;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 最后登录IP */
    private String lastLoginIp;

    /** 登录失败次数 */
    private Integer loginFailCount;

    /** 账户锁定时间 */
    private LocalDateTime lockTime;

    /** MFA是否启用：0-未启用，1-已启用 */
    private Integer mfaEnabled;

    /** MFA密钥 */
    private String mfaSecret;
}
