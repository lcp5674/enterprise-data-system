package com.enterprise.edams.auth.service;

import com.enterprise.edams.auth.dto.LoginRequest;
import com.enterprise.edams.auth.dto.LoginResponse;
import com.enterprise.edams.auth.dto.MFAVerifyRequest;
import com.enterprise.edams.auth.dto.MFASetupResponse;

/**
 * 认证服务接口
 *
 * @author Backend Team
 * @version 1.0.0
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * MFA验证
     */
    LoginResponse verifyMFA(MFAVerifyRequest request);

    /**
     * 用户登出
     */
    void logout(String token);

    /**
     * 刷新Token
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * 验证Token
     */
    boolean validateToken(String token);

    /**
     * 获取当前用户信息
     */
    LoginResponse getCurrentUser(String token);

    /**
     * 强制登出用户
     */
    void forceLogout(String userId);

    /**
     * 强制登出所有用户
     */
    void forceLogoutAll();

    /**
     * 初始化MFA
     */
    MFASetupResponse setupMFA(String userId, String mfaType);

    /**
     * 启用MFA
     */
    void enableMFA(String userId, String code);

    /**
     * 禁用MFA
     */
    void disableMFA(String userId, String code);

    /**
     * 验证MFA备用码
     */
    boolean verifyBackupCode(String userId, String backupCode);
}
