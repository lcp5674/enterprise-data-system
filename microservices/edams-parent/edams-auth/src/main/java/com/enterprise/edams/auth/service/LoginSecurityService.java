package com.enterprise.edams.auth.service;

/**
 * 登录安全服务接口
 *
 * @author Backend Team
 * @version 1.0.0
 */
public interface LoginSecurityService {

    /**
     * 检查登录安全状态
     */
    void checkLoginSecurity(String identifier, String ip);

    /**
     * 记录登录失败
     */
    void recordLoginFailure(String identifier, String ip, String reason);

    /**
     * 记录登录成功
     */
    void recordLoginSuccess(String userId, String ip);

    /**
     * 解锁账户
     */
    void unlockAccount(String identifier);

    /**
     * 锁定账户
     */
    void lockAccount(String userId, String reason, long lockDurationSeconds);

    /**
     * 检查账户是否被锁定
     */
    boolean isAccountLocked(String userId);

    /**
     * 获取账户被锁定的原因
     */
    String getAccountLockReason(String userId);

    /**
     * 检查IP是否被封禁
     */
    boolean isIpBlocked(String ip);

    /**
     * 封禁IP
     */
    void blockIp(String ip, long durationSeconds);

    /**
     * 获取登录失败次数
     */
    int getLoginFailureCount(String identifier);

    /**
     * 重置登录失败次数
     */
    void resetLoginFailureCount(String identifier);
}
