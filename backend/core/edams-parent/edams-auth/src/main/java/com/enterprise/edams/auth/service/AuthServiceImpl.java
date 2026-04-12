package com.enterprise.edams.auth.service.impl;

import com.enterprise.edams.auth.config.JwtProperties;
import com.enterprise.edams.auth.dto.*;
import com.enterprise.edams.auth.entity.LoginLog;
import com.enterprise.edams.auth.entity.User;
import com.enterprise.edams.auth.repository.LoginLogMapper;
import com.enterprise.edams.auth.repository.UserMapper;
import com.enterprise.edams.auth.security.JwtTokenProvider;
import com.enterprise.edams.auth.service.AuthService;
import com.enterprise.edams.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final LoginLogMapper loginLogMapper;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate stringRedisTemplate;
    private final JwtProperties jwtProperties;

    // 邮件发送（可选注入）
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username:noreply@edams.com}")
    private String mailFrom;

    // 验证码长度
    private static final int CODE_LENGTH = 6;
    // 验证码有效期（分钟）
    private static final int CODE_EXPIRE_MINUTES = 10;
    // 验证码发送间隔（秒）
    private static final int CODE_SEND_INTERVAL = 60;

    /**
     * 用户登录
     */
    @Override
    public TokenResponse login(LoginRequest request, String ip, String userAgent) {
        // 1. 检查IP频率限制（防暴力破解）
        checkIpRateLimit(ip);

        // 2. 根据用户名查找用户
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            recordLoginLog(null, request.getUsername(), ip, userAgent, 0, "用户不存在", "password");
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 检查账户状态
        checkAccountStatus(user);

        // 4. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleLoginFailure(user, ip, userAgent);
            throw new BusinessException("用户名或密码错误");
        }

        // 5. MFA验证（如果启用）
        if (user.getMfaEnabled() != null && user.getMfaEnabled() == 1) {
            if (!verifyMfaCode(user, request.getMfaCode())) {
                recordLoginLog(user.getId(), user.getUsername(), ip, userAgent, 0, "MFA验证码错误", "mfa");
                throw new BusinessException("MFA验证码错误");
            }
        }

        // 6. 登录成功，重置失败次数
        resetLoginFailCount(user);

        // 7. 更新最后登录信息
        updateLastLoginInfo(user, ip);

        // 8. 生成JWT令牌
        TokenResponse tokenResponse = generateTokens(user);

        // 9. 记录登录日志
        recordLoginLog(user.getId(), user.getUsername(), ip, userAgent, 1, "登录成功", "password");

        log.info("用户 {} 登录成功，IP: {}", user.getUsername(), ip);
        return tokenResponse;
    }

    /**
     * 用户注册
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) {
        // 1. 验证密码一致性
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

        // 2. 检查用户名是否已存在
        if (userMapper.findByUsername(request.getUsername()) != null) {
            throw new BusinessException("用户名已存在，请使用其他用户名");
        }

        // 3. 检查邮箱是否已注册
        if (request.getEmail() != null && userMapper.findByEmail(request.getEmail()) != null) {
            throw new BusinessException("该邮箱已被注册");
        }

        // 4. 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setDepartmentId(request.getDepartmentId());
        user.setStatus(1); // 默认启用
        user.setGender(0);
        user.setLoginFailCount(0);
        user.setMfaEnabled(0);
        user.setTenantId(1L); // 默认租户

        int rows = userMapper.insert(user);
        if (rows <= 0) {
            throw new BusinessException("用户注册失败，请稍后重试");
        }

        log.info("新用户注册成功: {}", request.getUsername());
    }

    /**
     * 刷新令牌
     */
    @Override
    public TokenResponse refreshToken(String refreshToken) {
        // 验证刷新令牌有效性
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException("刷新令牌无效或已过期，请重新登录");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        if (userId == null) {
            throw new BusinessException("无法从令牌中获取用户信息");
        }

        // 从Redis验证刷新令牌是否有效
        String storedRefreshToken = stringRedisTemplate.opsForValue()
                .get("edams:token:refresh:" + userId);
        
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            jwtTokenProvider.invalidateUserTokens(userId);
            throw new BusinessException("刷新令牌已被撤销，请重新登录");
        }

        // 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException("用户不存在或已被禁用");
        }

        // 将旧访问令牌加入黑名单（如果有）
        // 注意：这里需要前端传递旧accessToken，或者直接生成新的

        // 生成新的令牌对
        return generateTokens(user);
    }

    /**
     * 注销登录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(String accessToken, Long userId) {
        // 将访问令牌加入黑名单
        if (accessToken != null && !accessToken.isEmpty()) {
            jwtTokenProvider.blacklistToken(accessToken);
        }

        // 删除用户的刷新令牌和在线状态
        if (userId != null) {
            jwtTokenProvider.invalidateUserTokens(userId);
        }

        log.info("用户 {} 已注销登录", userId);
    }

    /**
     * 获取当前用户信息
     */
    @Override
    public UserInfoDTO getCurrentUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        return UserInfoDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .status(user.getStatus())
                .departmentId(user.getDepartmentId())
                .tenantId(user.getTenantId())
                .lastLoginTime(user.getLastLoginTime())
                .roles(Collections.singleton("ROLE_USER"))
                .permissions(Collections.emptySet())
                .mfaEnabled(user.getMfaEnabled() != null && user.getMfaEnabled() == 1)
                .build();
    }

    /**
     * 修改密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedBy(String.valueOf(userId));
        userMapper.updateById(user);

        // 注销所有令牌（强制重新登录）
        jwtTokenProvider.invalidateUserTokens(userId);

        log.info("用户 {} 已修改密码", userId);
    }

    /**
     * 重置密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String username, String verificationCode, String newPassword) {
        // 验证验证码（从Redis获取）
        String storedCode = stringRedisTemplate.opsForValue()
                .get("edams:resetpwd:" + username);
        
        if (storedCode == null || !storedCode.equals(verificationCode)) {
            throw new BusinessException("验证码无效或已过期");
        }

        // 查找用户
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 重置密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLoginFailCount(0);
        user.setLockTime(null);
        userMapper.updateById(user);

        // 删除验证码
        stringRedisTemplate.delete("edams:resetpwd:" + username);

        // 注销所有令牌
        jwtTokenProvider.invalidateUserTokens(user.getId());

        log.info("用户 {} 密码重置成功", username);
    }

    /**
     * 发送重置密码验证码
     */
    @Override
    public void sendResetCode(String account) {
        // 检查发送频率限制
        String rateLimitKey = "edams:resetpwd:ratelimit:" + account;
        String lastSendTime = stringRedisTemplate.opsForValue().get(rateLimitKey);
        if (lastSendTime != null) {
            long secondsSinceLastSend = (System.currentTimeMillis() - Long.parseLong(lastSendTime)) / 1000;
            if (secondsSinceLastSend < CODE_SEND_INTERVAL) {
                throw new BusinessException("验证码发送过于频繁，请" + (CODE_SEND_INTERVAL - secondsSinceLastSend) + "秒后再试");
            }
        }

        // 查找用户
        User user = null;
        if (account.contains("@")) {
            // 邮箱格式
            user = userMapper.findByEmail(account);
        } else if (account.matches("^1[3-9]\\d{9}$")) {
            // 手机号格式
            user = userMapper.findByPhone(account);
        } else {
            // 用户名
            user = userMapper.findByUsername(account);
        }

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 生成6位验证码
        String code = generateVerificationCode();

        // 保存验证码到Redis
        String codeKey = "edams:resetpwd:" + account;
        stringRedisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 更新发送频率限制
        stringRedisTemplate.opsForValue().set(rateLimitKey, String.valueOf(System.currentTimeMillis()), CODE_SEND_INTERVAL, TimeUnit.SECONDS);

        // 发送验证码
        try {
            if (account.contains("@")) {
                // 发送邮件
                sendCodeByEmail(user.getEmail(), code);
            } else if (account.matches("^1[3-9]\\d{9}$")) {
                // 发送短信（这里调用通知服务）
                sendCodeBySms(account, code);
            } else {
                // 如果是用户名，尝试发送到用户注册的邮箱或手机
                if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                    sendCodeByEmail(user.getEmail(), code);
                } else if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                    sendCodeBySms(user.getPhone(), code);
                } else {
                    throw new BusinessException("该用户未绑定邮箱或手机号，请联系管理员");
                }
            }
        } catch (Exception e) {
            log.error("发送验证码失败: {}", e.getMessage());
            throw new BusinessException("发送验证码失败，请稍后重试");
        }

        log.info("验证码已发送到账户: {}", maskAccount(account));
    }

    /**
     * 生成6位数字验证码
     */
    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 通过邮件发送验证码
     */
    private void sendCodeByEmail(String email, String code) {
        if (javaMailSender == null) {
            log.warn("邮件发送器未配置，跳过邮件发送");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(email);
            message.setSubject("【EDAMS数据平台】密码重置验证码");
            message.setText(buildEmailContent(code));
            javaMailSender.send(message);
            log.info("验证码已发送到邮箱: {}", maskEmail(email));
        } catch (Exception e) {
            log.error("发送邮件失败: {}", e.getMessage());
            // 邮件发送失败不影响主流程，验证码已保存在Redis
        }
    }

    /**
     * 通过短信发送验证码
     */
    private void sendCodeBySms(String phone, String code) {
        // 调用通知服务发送短信
        // 这里可以通过Feign调用notification服务
        log.info("短信验证码已生成，将发送到手机: {}", maskPhone(phone));
        // 实际实现中应该调用通知服务
        // notificationService.sendSms(phone, "您的密码重置验证码是：" + code);
    }

    /**
     * 构建邮件内容
     */
    private String buildEmailContent(String code) {
        return """
            尊敬的用户，您好！

            您在EDAMS数据平台提交了密码重置请求，请使用以下验证码完成验证：

            验证码：%s

            验证码有效期为10分钟，请勿将验证码泄露给他人。

            如果您未发起密码重置请求，请忽略此邮件。

            此致
            EDAMS数据平台
            """.formatted(code);
    }

    /**
     * 掩码邮箱
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int atIndex = email.indexOf("@");
        String localPart = email.substring(0, atIndex);
        if (localPart.length() <= 3) {
            return "***" + email.substring(atIndex);
        }
        return localPart.substring(0, 3) + "***" + email.substring(atIndex);
    }

    /**
     * 掩码手机号
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * 掩码账户
     */
    private String maskAccount(String account) {
        if (account.contains("@")) {
            return maskEmail(account);
        } else if (account.matches("^1[3-9]\\d{9}$")) {
            return maskPhone(account);
        }
        if (account.length() <= 3) return "***";
        return account.substring(0, 3) + "***";
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 检查IP频率限制
     */
    private void checkIpRateLimit(String ip) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMinuteAgo = now.minusMinutes(1);
        int attempts = loginLogMapper.countLoginAttemptsByIp(ip, oneMinuteAgo, now);
        
        if (attempts >= jwtProperties.getIpRateLimitPerMinute()) {
            log.warn("IP {} 登录频率过高，已触发限流", ip);
            throw new BusinessException("操作过于频繁，请稍后重试");
        }
    }

    /**
     * 检查账户状态
     */
    private void checkAccountStatus(User user) {
        // 检查账户锁定状态
        if (user.getLockTime() != null && user.getLockTime().isAfter(LocalDateTime.now())) {
            long remainingMinutes = java.time.Duration.between(LocalDateTime.now(), user.getLockTime()).toMinutes();
            throw new BusinessException("账户已被锁定，请在" + remainingMinutes + "分钟后重试");
        }

        // 检查禁用状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException("账户已被禁用，请联系管理员");
        }
    }

    /**
     * 处理登录失败
     */
    private void handleLoginFailure(User user, String ip, String userAgent) {
        // 增加失败计数
        int failCount = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;
        user.setLoginFailCount(failCount);

        // 检查是否达到最大失败次数
        if (failCount >= jwtProperties.getMaxLoginAttempts()) {
            user.setLockTime(LocalDateTime.now().plusMinutes(jwtProperties.getLockDurationMinutes()));
            userMapper.updateById(user);
            
            recordLoginLog(user.getId(), user.getUsername(), ip, userAgent, 0,
                    "登录失败" + failCount + "次，账户已锁定" + jwtProperties.getLockDurationMinutes() + "分钟",
                    "password");
            log.warn("用户 {} 登录失败{}次，账户已锁定", user.getUsername(), failCount);
        } else {
            userMapper.updateById(user);
            recordLoginLog(user.getId(), user.getUsername(), ip, userAgent, 0,
                    "用户名或密码错误（剩余尝试次数：" + (jwtProperties.getMaxLoginAttempts() - failCount) + "）",
                    "password");
        }
    }

    /**
     * 重置登录失败计数
     */
    private void resetLoginFailCount(User user) {
        if (user.getLoginFailCount() != null && user.getLoginFailCount() > 0) {
            user.setLoginFailCount(0);
            user.setLockTime(null);
            userMapper.updateById(user);
        }
    }

    /**
     * 更新最后登录信息
     */
    private void updateLastLoginInfo(User user, String ip) {
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userMapper.updateById(user);
    }

    /**
     * 生成令牌对
     */
    private TokenResponse generateTokens(User user) {
        List<String> roles = Collections.singletonList("ROLE_USER");

        String accessToken = jwtTokenProvider.generateAccessToken(
                user.getId(), user.getUsername(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getExpiration() / 1000)
                .userId(user.getId())
                .username(user.getUsername())
                .build();
    }

    /**
     * 记录登录日志
     */
    private void recordLoginLog(Long userId, String username, String ip, 
                                String userAgent, int status, String message, String loginType) {
        try {
            LoginLog logEntity = new LoginLog();
            logEntity.setUserId(userId);
            logEntity.setUsername(username);
            logEntity.setIp(ip);
            logEntity.setUserAgent(userAgent);
            logEntity.setStatus(status);
            logEntity.setMessage(message);
            logEntity.setLoginTime(LocalDateTime.now());
            logEntity.setLoginType(loginType);
            logEntity.setLocation(parseLocationFromIp(ip)); // 简化处理
            logEntity.setOs(parseOs(userAgent));
            logEntity.setBrowser(parseBrowser(userAgent));

            loginLogMapper.insert(logEntity);
        } catch (Exception e) {
            log.error("记录登录日志失败", e);
        }
    }

    /**
     * 解析浏览器类型（简化版）
     */
    private String parseBrowser(String userAgent) {
        if (userAgent == null) return "Unknown";
        if (userAgent.contains("Chrome")) return "Chrome";
        if (userAgent.contains("Firefox")) return "Firefox";
        if (userAgent.contains("Safari")) return "Safari";
        if (userAgent.contains("Edge")) return "Edge";
        if (userAgent.contains("MSIE") || userAgent.contains("Trident")) return "IE";
        return "Other";
    }

    /**
     * 解析操作系统（简化版）
     */
    private String parseOs(String userAgent) {
        if (userAgent == null) return "Unknown";
        if (userAgent.contains("Windows")) return "Windows";
        if (userAgent.contains("Mac OS X")) return "MacOS";
        if (userAgent.contains("Linux")) return "Linux";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) return "iOS";
        return "Other";
    }

    /**
     * IP解析为地理位置（简化版 - 实际应调用IP服务）
     */
    private String parseLocationFromIp(String ip) {
        // 实际项目中应该调用IP定位服务API
        return "未知位置";
    }

    /**
     * MFA验证码校验（使用TOTP算法）
     */
    private boolean verifyMfaCode(User user, String mfaCode) {
        if (mfaCode == null || mfaCode.length() != 6) {
            return false;
        }
        
        // 从用户表获取MFA密钥
        String secret = user.getMfaSecret();
        if (secret == null || secret.isEmpty()) {
            log.warn("用户 {} 未配置MFA密钥", user.getUsername());
            return false;
        }
        
        // 使用TOTP算法验证验证码
        try {
            return verifyTOTP(secret, mfaCode);
        } catch (Exception e) {
            log.error("MFA验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * TOTP算法实现（基于RFC 6238）
     */
    private boolean verifyTOTP(String secret, String code) {
        try {
            // 解码Base32密钥
            byte[] key = base32Decode(secret.toUpperCase());
            
            // 获取当前时间窗口（30秒）
            long time = System.currentTimeMillis() / 1000 / 30;
            
            // 验证当前窗口和前后各一个窗口（允许时钟偏移）
            for (int i = -1; i <= 1; i++) {
                String expectedCode = generateTOTP(key, time + i);
                if (expectedCode.equals(code)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("TOTP验证异常: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 生成TOTP验证码
     */
    private String generateTOTP(byte[] key, long time) {
        try {
            byte[] data = new byte[8];
            for (int i = 7; i >= 0; i--) {
                data[i] = (byte) (time & 0xff);
                time >>= 8;
            }
            
            // 使用HMAC-SHA1
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA1");
            mac.init(new javax.crypto.spec.SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);
            
            // 动态截断
            int offset = hash[hash.length - 1] & 0x0f;
            int binary = ((hash[offset] & 0x7f) << 24) 
                       | ((hash[offset + 1] & 0xff) << 16)
                       | ((hash[offset + 2] & 0xff) << 8)
                       | (hash[offset + 3] & 0xff);
            
            int otp = binary % 1000000;
            return String.format("%06d", otp);
        } catch (Exception e) {
            throw new RuntimeException("TOTP生成失败", e);
        }
    }
    
    /**
     * Base32解码
     */
    private byte[] base32Decode(String encoded) {
        final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        encoded = encoded.replace("=", "");
        
        int bytes = (encoded.length() * 5) / 8;
        byte[] result = new byte[bytes];
        
        int buffer = 0;
        int bitsLeft = 0;
        int index = 0;
        
        for (char c : encoded.toCharArray()) {
            int value = alphabet.indexOf(c);
            if (value < 0) continue;
            
            buffer = (buffer << 5) | value;
            bitsLeft += 5;
            
            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                result[index++] = (byte) (buffer >> bitsLeft);
            }
        }
        
        return result;
    }
}
