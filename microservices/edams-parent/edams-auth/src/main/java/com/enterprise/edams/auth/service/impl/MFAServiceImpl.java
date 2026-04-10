package com.enterprise.edams.auth.service.impl;

import com.enterprise.edams.auth.dto.MFASetupResponse;
import com.enterprise.edams.auth.entity.SysUser;
import com.enterprise.edams.auth.repository.SysUserRepository;
import com.enterprise.edams.auth.service.MFAService;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.common.result.ResultCode;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevans.totp.qr.QrData;
import dev.samstevens.totp.time.SystemTimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MFA服务实现
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MFAServiceImpl implements MFAService {

    private final SysUserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${auth.mfa.issuer:EDAMS}")
    private String issuer;

    @Value("${auth.mfa.verification-window:1}")
    private int verificationWindow;

    @Override
    public String generateSecret() {
        return dev.samstevens.totp.secret.SecretGenerator.generate();
    }

    @Override
    public String generateQrCodeUrl(String secret, String username) {
        QrData qrData = new QrData.Builder()
                .label(username)
                .secret(secret)
                .issuer(issuer)
                .algorithm(Algorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        return qrData.getUri();
    }

    @Override
    public List<String> generateBackupCodes() {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            codes.add(code);
        }
        return codes;
    }

    @Override
    public boolean verifyTotpCode(String secret, String code) {
        if (secret == null || code == null) {
            return false;
        }

        try {
            dev.samstevens.totp.secret.Secret secretObj = dev.samstevens.totp.secret.Secret.fromBase32(secret);
            CodeGenerator codeGenerator = new DefaultCodeGenerator(Algorithm.SHA1, new DefaultCodeGeneratorFactory());
            TimeProvider timeProvider = new SystemTimeProvider();

            CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
            verifier.setAllowedTimePeriodDiscrepancy(verificationWindow);

            return verifier.isValidCode(secretObj, code);
        } catch (Exception e) {
            log.error("验证TOTP失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public MFASetupResponse setupTotpMFA(String userId) {
        log.info("设置TOTP MFA: userId={}", userId);

        SysUser user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.AUTH_USER_NOT_FOUND);
        }

        // 生成密钥
        String secret = generateSecret();
        String qrCodeUrl = generateQrCodeUrl(secret, user.getUsername());
        List<String> backupCodes = generateBackupCodes();

        // 保存密钥（加密存储）
        user.setMfaSecret(secret);
        user.setMfaBackupCodes(backupCodes.toString());
        userRepository.updateById(user);

        return MFASetupResponse.builder()
                .mfaType("TOTP")
                .secret(secret)
                .qrCodeUrl(qrCodeUrl)
                .backupCodes(backupCodes)
                .enabled(false)
                .build();
    }

    @Override
    @Transactional
    public MFASetupResponse setupEmailMFA(String userId) {
        log.info("设置邮箱 MFA: userId={}", userId);

        SysUser user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.AUTH_USER_NOT_FOUND);
        }

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "用户邮箱未设置");
        }

        // 生成验证码
        String code = generateEmailCode();
        String cacheKey = "mfa:email:code:" + userId;
        redisTemplate.opsForValue().set(cacheKey, code, 300, TimeUnit.SECONDS);

        // 发送邮件
        sendEmailCodeToUser(user.getEmail(), code);

        return MFASetupResponse.builder()
                .mfaType("EMAIL")
                .enabled(false)
                .build();
    }

    @Override
    @Transactional
    public MFASetupResponse setupSmsMFA(String userId) {
        log.info("设置短信 MFA: userId={}", userId);

        SysUser user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.AUTH_USER_NOT_FOUND);
        }

        if (user.getPhone() == null || user.getPhone().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "用户手机号未设置");
        }

        // 生成验证码
        String code = generateEmailCode();
        String cacheKey = "mfa:sms:code:" + userId;
        redisTemplate.opsForValue().set(cacheKey, code, 300, TimeUnit.SECONDS);

        // 发送短信
        sendSmsCodeToUser(user.getPhone(), code);

        return MFASetupResponse.builder()
                .mfaType("SMS")
                .enabled(false)
                .build();
    }

    @Override
    public void sendEmailCode(String userId) {
        SysUser user = userRepository.selectById(userId);
        if (user == null || user.getEmail() == null) {
            throw new BusinessException(ResultCode.AUTH_USER_NOT_FOUND);
        }

        String code = generateEmailCode();
        String cacheKey = "mfa:email:code:" + userId;
        redisTemplate.opsForValue().set(cacheKey, code, 300, TimeUnit.SECONDS);

        sendEmailCodeToUser(user.getEmail(), code);
    }

    @Override
    public void sendSmsCode(String userId) {
        SysUser user = userRepository.selectById(userId);
        if (user == null || user.getPhone() == null) {
            throw new BusinessException(ResultCode.AUTH_USER_NOT_FOUND);
        }

        String code = generateEmailCode();
        String cacheKey = "mfa:sms:code:" + userId;
        redisTemplate.opsForValue().set(cacheKey, code, 300, TimeUnit.SECONDS);

        sendSmsCodeToUser(user.getPhone(), code);
    }

    @Override
    public boolean verifyEmailCode(String userId, String code) {
        String cacheKey = "mfa:email:code:" + userId;
        Object storedCode = redisTemplate.opsForValue().get(cacheKey);

        if (storedCode == null) {
            return false;
        }

        boolean result = storedCode.toString().equals(code);
        if (result) {
            redisTemplate.delete(cacheKey);
        }

        return result;
    }

    @Override
    public boolean verifySmsCode(String userId, String code) {
        String cacheKey = "mfa:sms:code:" + userId;
        Object storedCode = redisTemplate.opsForValue().get(cacheKey);

        if (storedCode == null) {
            return false;
        }

        boolean result = storedCode.toString().equals(code);
        if (result) {
            redisTemplate.delete(cacheKey);
        }

        return result;
    }

    private String generateEmailCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    private void sendEmailCodeToUser(String email, String code) {
        // TODO: 调用邮件服务发送验证码
        log.info("发送邮箱验证码到 {}: {}", email, code);
    }

    private void sendSmsCodeToUser(String phone, String code) {
        // TODO: 调用短信服务发送验证码
        log.info("发送短信验证码到 {}: {}", phone, code);
    }
}
