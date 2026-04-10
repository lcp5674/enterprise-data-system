package com.enterprise.edams.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.edams.auth.dto.*;
import com.enterprise.edams.auth.entity.LoginLog;
import com.enterprise.edams.auth.entity.SysSession;
import com.enterprise.edams.auth.entity.SysUser;
import com.enterprise.edams.auth.repository.LoginLogRepository;
import com.enterprise.edams.auth.repository.SysSessionRepository;
import com.enterprise.edams.auth.repository.SysUserRepository;
import com.enterprise.edams.auth.service.*;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserRepository userRepository;
    private final LoginLogRepository loginLogRepository;
    private final SysSessionRepository sessionRepository;
    private final TokenService tokenService;
    private final CaptchaService captchaService;
    private final MFAService mfaService;
    private final LoginSecurityService loginSecurityService;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final com.enterprise.edams.auth.feign.PermissionFeignClient permissionFeignClient;

    @Value("${auth.jwt.access-token-expiration:7200}")
    private long accessTokenExpiration;

    @Value("${auth.mfa.temp-token-expiration:300}")
    private long mfaTempTokenExpiration;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("用户登录尝试: {}", request.getUsername());

        // 1. 检查登录安全状态
        loginSecurityService.checkLoginSecurity(request.getUsername(), request.getIpAddress());

        // 2. 验证验证码（如果启用）
        if (request.getCaptchaKey() != null && !request.getCaptchaKey().isEmpty()) {
            if (!captchaService.verifyCaptcha(request.getCaptchaKey(), request.getCaptcha())) {
                throw new BusinessException(ResultCode.AUTH_CAPTCHA_ERROR);
            }
            captchaService.deleteCaptcha(request.getCaptchaKey());
        }

        // 3. 查询用户
        SysUser user = userRepository.findByUsername(request.getUsername());
        if (user == null) {
            user = userRepository.findByEmail(request.getUsername());
        }
        if (user == null) {
            user = userRepository.findByPhone(request.getUsername());
        }

        if (user == null) {
            loginSecurityService.recordLoginFailure(request.getUsername(), request.getIpAddress(), "用户不存在");
            throw new BusinessException(ResultCode.AUTH_USER_NOT_FOUND);
        }

        // 4. 检查用户状态
        if (user.getStatus() == 0) {
            loginSecurityService.recordLoginFailure(user.getId(), request.getIpAddress(), "用户已禁用");
            throw new BusinessException(ResultCode.AUTH_USER_DISABLED);
        }
        if (user.getStatus() == 2) {
            String lockReason = loginSecurityService.getAccountLockReason(user.getId());
            loginSecurityService.recordLoginFailure(user.getId(), request.getIpAddress(), "账户已锁定: " + lockReason);
            throw new BusinessException(ResultCode.AUTH_ACCOUNT_LOCKED, lockReason);
        }

        // 5. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            loginSecurityService.recordLoginFailure(user.getId(), request.getIpAddress(), "密码错误");
            throw new BusinessException(ResultCode.AUTH_PASSWORD_ERROR);
        }

        // 6. 检查密码是否过期
        if (user.getPasswordExpireTime() != null && LocalDateTime.now().isAfter(user.getPasswordExpireTime())) {
            throw new BusinessException(ResultCode.AUTH_PASSWORD_EXPIRED);
        }

        // 7. 记录登录成功
        loginSecurityService.recordLoginSuccess(user.getId(), request.getIpAddress());

        // 8. 检查是否需要MFA验证
        if (user.getMfaEnabled() != null && user.getMfaEnabled() == 1) {
            return handleMFARequired(user, request);
        }

        // 9. 首次登录处理
        if (user.getIsFirstLogin() != null && user.getIsFirstLogin() == 1) {
            return handleFirstLogin(user, request);
        }

        // 10. 生成Token并返回
        return generateLoginResponse(user, request);
    }

    @Override
    @Transactional
    public LoginResponse verifyMFA(MFAVerifyRequest request) {
        log.info("MFA验证: userId from token");

        // 1. 验证MFA临时令牌
        String userId = (String) redisTemplate.opsForValue().get("mfa:temp:" + request.getMfaToken());
        if (userId == null) {
            throw new BusinessException(ResultCode.AUTH_MFA_TOKEN_EXPIRED);
        }

        SysUser user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.AUTH_USER_NOT_FOUND);
        }

        // 2. 验证MFA代码
        boolean verified = false;
        if ("TOTP".equals(request.getVerifyType())) {
            verified = mfaService.verifyTotpCode(user.getMfaSecret(), request.getCode());
        } else if ("BACKUP_CODE".equals(request.getVerifyType())) {
            verified = verifyBackupCode(userId, request.getCode());
        }

        if (!verified) {
            throw new BusinessException(ResultCode.AUTH_MFA_CODE_ERROR);
        }

        // 3. 删除MFA临时令牌
        redisTemplate.delete("mfa:temp:" + request.getMfaToken());

        // 4. 生成登录响应
        return generateLoginResponse(user, null);
    }

    @Override
    @Transactional
    public void logout(String token) {
        log.info("用户登出");

        // 1. 解析Token获取用户ID
        String userId = tokenService.getUserIdFromToken(token);
        if (userId == null) {
            return;
        }

        // 2. 使Token失效
        tokenService.blacklistToken(token);

        // 3. 使会话失效
        sessionRepository.invalidateAllSessionsByUserId(userId);
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        log.info("刷新Token");

        // 1. 验证刷新令牌
        if (!tokenService.validateToken(refreshToken) || tokenService.isTokenBlacklisted(refreshToken)) {
            throw new BusinessException(ResultCode.AUTH_TOKEN_INVALID);
        }

        // 2. 获取用户ID
        String userId = tokenService.getUserIdFromToken(refreshToken);
        if (userId == null) {
            throw new BusinessException(ResultCode.AUTH_TOKEN_INVALID);
        }

        // 3. 查询用户
        SysUser user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.AUTH_USER_NOT_FOUND);
        }

        // 4. 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.AUTH_USER_DISABLED);
        }

        // 5. 使旧刷新令牌失效
        tokenService.blacklistToken(refreshToken);

        // 6. 生成新的Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());

        String newAccessToken = tokenService.generateAccessToken(user.getId(), user.getUsername(), claims);
        String newRefreshToken = tokenService.generateRefreshToken(user.getId());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenService.getAccessTokenExpiration())
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        return tokenService.validateToken(token) && !tokenService.isTokenBlacklisted(token);
    }

    @Override
    public LoginResponse getCurrentUser(String token) {
        String userId = tokenService.getUserIdFromToken(token);
        if (userId == null) {
            throw new BusinessException(ResultCode.AUTH_TOKEN_INVALID);
        }

        SysUser user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.AUTH_USER_NOT_FOUND);
        }

        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .mfaRequired(user.getMfaEnabled() != null && user.getMfaEnabled() == 1)
                .build();
    }

    @Override
    @Transactional
    public void forceLogout(String userId) {
        log.info("强制登出用户: {}", userId);

        // 使所有Token失效
        tokenService.invalidateAllUserTokens(userId);

        // 使所有会话失效
        sessionRepository.invalidateAllSessionsByUserId(userId);
    }

    @Override
    @Transactional
    public void forceLogoutAll() {
        log.info("强制登出所有用户");

        // 清除所有会话
        // 注意：生产环境需要分批处理
        LambdaQueryWrapper<SysSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysSession::getIsActive, 1);
        List<SysSession> sessions = sessionRepository.selectList(wrapper);
        for (SysSession session : sessions) {
            tokenService.blacklistToken(session.getSessionId());
        }
        sessionRepository.delete(wrapper);
    }

    @Override
    public MFASetupResponse setupMFA(String userId, String mfaType) {
        log.info("设置MFA: userId={}, type={}", userId, mfaType);

        return switch (mfaType.toUpperCase()) {
            case "TOTP" -> mfaService.setupTotpMFA(userId);
            case "EMAIL" -> mfaService.setupEmailMFA(userId);
            case "SMS" -> mfaService.setupSmsMFA(userId);
            default -> throw new BusinessException(ResultCode.PARAM_INVALID, "不支持的MFA类型");
        };
    }

    @Override
    @Transactional
    public void enableMFA(String userId, String code) {
        log.info("启用MFA: userId={}", userId);

        SysUser user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.AUTH_USER_NOT_FOUND);
        }

        // 验证代码
        if (!mfaService.verifyTotpCode(user.getMfaSecret(), code)) {
            throw new BusinessException(ResultCode.AUTH_MFA_CODE_ERROR);
        }

        // 启用MFA
        user.setMfaEnabled(1);
        userRepository.updateById(user);
    }

    @Override
    @Transactional
    public void disableMFA(String userId, String code) {
        log.info("禁用MFA: userId={}", userId);

        SysUser user = userRepository.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.AUTH_USER_NOT_FOUND);
        }

        // 验证代码
        if (!mfaService.verifyTotpCode(user.getMfaSecret(), code)) {
            throw new BusinessException(ResultCode.AUTH_MFA_CODE_ERROR);
        }

        // 禁用MFA
        user.setMfaEnabled(0);
        user.setMfaSecret(null);
        user.setMfaBackupCodes(null);
        userRepository.updateById(user);
    }

    @Override
    public boolean verifyBackupCode(String userId, String backupCode) {
        SysUser user = userRepository.selectById(userId);
        if (user == null || user.getMfaBackupCodes() == null) {
            return false;
        }

        // 解析备用码列表
        String[] codes = user.getMfaBackupCodes().replaceAll("[\\[\\]\"]", "").split(",");
        for (String code : codes) {
            if (code.trim().equals(backupCode.trim())) {
                // 使用后删除备用码
                removeUsedBackupCode(userId, backupCode);
                return true;
            }
        }
        return false;
    }

    // ========== 私有方法 ==========

    private LoginResponse handleMFARequired(SysUser user, LoginRequest request) {
        // 生成MFA临时令牌
        String mfaToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("mfa:temp:" + mfaToken, user.getId(), mfaTempTokenExpiration, TimeUnit.SECONDS);

        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .mfaRequired(true)
                .mfaToken(mfaToken)
                .build();
    }

    private LoginResponse handleFirstLogin(SysUser user, LoginRequest request) {
        // 更新首次登录标记
        user.setIsFirstLogin(0);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(request.getIpAddress());
        userRepository.updateById(user);

        // 记录登录日志
        recordLoginLog(user, "PASSWORD", request, "SUCCESS");

        return generateLoginResponse(user, request);
    }

    private LoginResponse generateLoginResponse(SysUser user, LoginRequest request) {
        // 更新最后登录信息
        if (request != null && request.getIpAddress() != null) {
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(request.getIpAddress());
            userRepository.updateById(user);
        }

        // 生成Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("roles", getUserRoles(user.getId()));
        claims.put("permissions", getUserPermissions(user.getId()));

        String accessToken = tokenService.generateAccessToken(user.getId(), user.getUsername(), claims);
        String refreshToken = tokenService.generateRefreshToken(user.getId());

        // 创建会话
        createSession(user, request);

        // 记录登录日志
        if (request != null) {
            recordLoginLog(user, "PASSWORD", request, "SUCCESS");
        }

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenService.getAccessTokenExpiration())
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .roles(getUserRoles(user.getId()))
                .permissions(getUserPermissions(user.getId()))
                .firstLogin(user.getIsFirstLogin() != null && user.getIsFirstLogin() == 1)
                .build();
    }

    private void createSession(SysUser user, LoginRequest request) {
        String sessionId = UUID.randomUUID().toString();
        SysSession session = SysSession.builder()
                .sessionId(sessionId)
                .userId(user.getId())
                .deviceId(request != null ? request.getDeviceId() : null)
                .deviceType(request != null && request.getDeviceType() != null ? request.getDeviceType() : "WEB")
                .ipAddress(request != null ? request.getIpAddress() : null)
                .userAgent(request != null ? request.getUserAgent() : null)
                .loginTime(LocalDateTime.now())
                .lastActiveTime(LocalDateTime.now())
                .expireTime(LocalDateTime.now().plusSeconds(tokenService.getAccessTokenExpiration()))
                .isActive(1)
                .createdBy(user.getUsername())
                .createdTime(LocalDateTime.now())
                .build();
        sessionRepository.insert(session);
    }

    private void recordLoginLog(SysUser user, String loginType, LoginRequest request, String status) {
        LoginLog log = LoginLog.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .loginType(loginType)
                .loginSource(request.getDeviceType())
                .status(status)
                .ipAddress(request.getIpAddress())
                .userAgent(request.getUserAgent())
                .deviceId(request.getDeviceId())
                .deviceType(request.getDeviceType())
                .createdTime(LocalDateTime.now())
                .build();
        loginLogRepository.insert(log);
    }

    private List<String> getUserRoles(String userId) {
        try {
            var result = permissionFeignClient.getUserRoles(userId);
            if (result != null && result.getData() != null) {
                return result.getData().stream()
                        .map(role -> (String) role.get("roleCode"))
                        .filter(code -> code != null)
                        .toList();
            }
        } catch (Exception e) {
            log.warn("从权限服务获取用户角色失败, userId: {}, error: {}", userId, e.getMessage());
        }
        return Collections.emptyList();
    }

    private List<String> getUserPermissions(String userId) {
        try {
            var result = permissionFeignClient.getUserPermissions(userId);
            if (result != null && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.warn("从权限服务获取用户权限失败, userId: {}, error: {}", userId, e.getMessage());
        }
        return Collections.emptyList();
    }

    private void removeUsedBackupCode(String userId, String usedCode) {
        SysUser user = userRepository.selectById(userId);
        if (user != null && user.getMfaBackupCodes() != null) {
            String[] codes = user.getMfaBackupCodes().replaceAll("[\\[\\]\"]", "").split(",");
            List<String> remainingCodes = new ArrayList<>();
            for (String code : codes) {
                if (!code.trim().equals(usedCode.trim())) {
                    remainingCodes.add(code.trim());
                }
            }
            user.setMfaBackupCodes(remainingCodes.toString());
            userRepository.updateById(user);
        }
    }
}
