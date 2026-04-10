package com.enterprise.edams.auth.service;

import com.enterprise.edams.auth.dto.MFASetupResponse;

import java.util.List;

/**
 * MFA服务接口
 *
 * @author Backend Team
 * @version 1.0.0
 */
public interface MFAService {

    /**
     * 生成MFA密钥
     */
    String generateSecret();

    /**
     * 生成二维码URL
     */
    String generateQrCodeUrl(String secret, String username);

    /**
     * 生成备用码
     */
    List<String> generateBackupCodes();

    /**
     * 验证TOTP验证码
     */
    boolean verifyTotpCode(String secret, String code);

    /**
     * 设置TOTP MFA
     */
    MFASetupResponse setupTotpMFA(String userId);

    /**
     * 启用邮箱MFA
     */
    MFASetupResponse setupEmailMFA(String userId);

    /**
     * 启用短信MFA
     */
    MFASetupResponse setupSmsMFA(String userId);

    /**
     * 发送邮箱验证码
     */
    void sendEmailCode(String userId);

    /**
     * 发送短信验证码
     */
    void sendSmsCode(String userId);

    /**
     * 验证邮箱验证码
     */
    boolean verifyEmailCode(String userId, String code);

    /**
     * 验证短信验证码
     */
    boolean verifySmsCode(String userId, String code);
}
