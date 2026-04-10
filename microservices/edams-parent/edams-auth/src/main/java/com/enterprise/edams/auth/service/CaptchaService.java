package com.enterprise.edams.auth.service;

import com.enterprise.edams.auth.dto.CaptchaResponse;

/**
 * 验证码服务接口
 *
 * @author Backend Team
 * @version 1.0.0
 */
public interface CaptchaService {

    /**
     * 生成验证码
     */
    CaptchaResponse generateCaptcha();

    /**
     * 验证验证码
     */
    boolean verifyCaptcha(String captchaKey, String captchaCode);

    /**
     * 删除验证码
     */
    void deleteCaptcha(String captchaKey);
}
