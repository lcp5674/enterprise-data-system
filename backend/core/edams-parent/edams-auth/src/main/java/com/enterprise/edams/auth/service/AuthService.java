package com.enterprise.edams.auth.service;

import com.enterprise.edams.auth.dto.LoginRequest;
import com.enterprise.edams.auth.dto.RegisterRequest;
import com.enterprise.edams.auth.dto.TokenResponse;
import com.enterprise.edams.auth.dto.UserInfoDTO;

/**
 * 认证服务接口
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
public interface AuthService {

    /**
     * 用户登录
     */
    TokenResponse login(LoginRequest request, String ip, String userAgent);

    /**
     * 用户注册
     */
    void register(RegisterRequest request);

    /**
     * 刷新令牌
     */
    TokenResponse refreshToken(String refreshToken);

    /**
     * 注销登录
     */
    void logout(String accessToken, Long userId);

    /**
     * 获取当前用户信息
     */
    UserInfoDTO getCurrentUserInfo(Long userId);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置密码（通过验证码）
     */
    void resetPassword(String username, String verificationCode, String newPassword);

    /**
     * 发送重置密码验证码
     * @param account 用户账户（用户名/邮箱/手机号）
     */
    void sendResetCode(String account);
}
