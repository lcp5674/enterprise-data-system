package com.enterprise.edams.auth.service.impl;

import com.enterprise.edams.auth.entity.LoginLog;
import com.enterprise.edams.auth.entity.SysUser;
import com.enterprise.edams.auth.repository.LoginLogRepository;
import com.enterprise.edams.auth.repository.SysUserRepository;
import com.enterprise.edams.auth.service.LoginSecurityService;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 登录安全服务实现
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginSecurityServiceImpl implements LoginSecurityService {

    private final LoginLogRepository loginLogRepository;
    private final SysUserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${auth.security.max-fail-count:5}")
    private int maxFailCount;

    @Value("${auth.security.lock-duration:1800}")
    private long lockDuration;

    @Value("${auth.security.ip-block-max-fail-count:10}")
    private int ipBlockMaxFailCount;

    @Value("${auth.security.ip-block-duration:3600}")
    private long ipBlockDuration;

    @Override
    public void checkLoginSecurity(String identifier, String ip) {
        // 1. 检查IP是否被封禁
        if (isIpBlocked(ip)) {
            throw new BusinessException(ResultCode.AUTH_IP_BLOCKED);
        }

        // 2. 检查账户是否被锁定
        if (identifier != null && !identifier.contains("@") && !identifier.matches("\\d+")) {
            SysUser user = userRepository.findByUsername(identifier);
            if (user != null && user.getStatus() == 2) {
                String lockReason = getAccountLockReason(user.getId());
                throw new BusinessException(ResultCode.AUTH_ACCOUNT_LOCKED, lockReason);
            }
        }
    }

    @Override
    @Transactional
    public void recordLoginFailure(String identifier, String ip, String reason) {
        log.info("登录失败: identifier={}, ip={}, reason={}", identifier, ip, reason);

        // 增加失败计数
        String key = "login:fail:" + identifier;
        Long failCount = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, lockDuration, TimeUnit.SECONDS);

        // IP失败计数
        String ipKey = "login:fail:ip:" + ip;
        Long ipFailCount = redisTemplate.opsForValue().increment(ipKey);
        redisTemplate.expire(ipKey, ipBlockDuration, TimeUnit.SECONDS);

        // 如果超过最大失败次数，锁定账户
        if (failCount != null && failCount >= maxFailCount && identifier != null) {
            SysUser user = userRepository.findByUsername(identifier);
            if (user != null) {
                lockAccount(user.getId(), "连续登录失败" + failCount + "次", lockDuration);
            }
        }

        // 如果IP失败次数过多，封禁IP
        if (ipFailCount != null && ipFailCount >= ipBlockMaxFailCount) {
            blockIp(ip, ipBlockDuration);
        }

        // 记录登录日志
        recordLoginLog(identifier, ip, "FAIL", reason);
    }

    @Override
    @Transactional
    public void recordLoginSuccess(String userId, String ip) {
        log.info("登录成功: userId={}, ip={}", userId, ip);

        // 重置失败计数
        resetLoginFailureCount(userId);

        // 更新用户最后登录信息
        SysUser user = userRepository.selectById(userId);
        if (user != null) {
            user.setLoginFailCount(0);
            user.setLastLoginTime(LocalDateTime.now());
            user.setLastLoginIp(ip);
            userRepository.updateById(user);
        }

        // 记录登录日志
        recordLoginLog(userId, ip, "SUCCESS", null);
    }

    @Override
    @Transactional
    public void unlockAccount(String identifier) {
        SysUser user = userRepository.findByUsername(identifier);
        if (user != null) {
            user.setStatus(1);
            user.setLoginFailCount(0);
            userRepository.updateById(user);

            // 删除锁定标记
            String key = "account:lock:" + user.getId();
            redisTemplate.delete(key);

            log.info("账户已解锁: userId={}", user.getId());
        }
    }

    @Override
    @Transactional
    public void lockAccount(String userId, String reason, long lockDurationSeconds) {
        log.info("锁定账户: userId={}, reason={}", userId, reason);

        SysUser user = userRepository.selectById(userId);
        if (user != null) {
            user.setStatus(2);
            userRepository.updateById(user);

            // 设置锁定标记
            String key = "account:lock:" + userId;
            redisTemplate.opsForValue().set(key, reason, lockDurationSeconds, TimeUnit.SECONDS);
        }
    }

    @Override
    public boolean isAccountLocked(String userId) {
        String key = "account:lock:" + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public String getAccountLockReason(String userId) {
        String key = "account:lock:" + userId;
        Object reason = redisTemplate.opsForValue().get(key);
        return reason != null ? reason.toString() : null;
    }

    @Override
    public boolean isIpBlocked(String ip) {
        String key = "ip:block:" + ip;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void blockIp(String ip, long durationSeconds) {
        log.info("封禁IP: ip={}, duration={}", ip, durationSeconds);

        String key = "ip:block:" + ip;
        redisTemplate.opsForValue().set(key, "blocked", durationSeconds, TimeUnit.SECONDS);
    }

    @Override
    public int getLoginFailureCount(String identifier) {
        String key = "login:fail:" + identifier;
        Object count = redisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count.toString()) : 0;
    }

    @Override
    public void resetLoginFailureCount(String identifier) {
        String key = "login:fail:" + identifier;
        redisTemplate.delete(key);
    }

    private void recordLoginLog(String identifier, String ip, String status, String failReason) {
        // 根据identifier获取用户信息
        SysUser user = null;
        if (identifier != null) {
            user = userRepository.findByUsername(identifier);
            if (user == null) {
                user = userRepository.findByEmail(identifier);
            }
        }

        LoginLog log = LoginLog.builder()
                .userId(user != null ? user.getId() : null)
                .username(identifier)
                .loginType("PASSWORD")
                .status(status)
                .failReason(failReason)
                .ipAddress(ip)
                .createdTime(LocalDateTime.now())
                .build();

        loginLogRepository.insert(log);
    }
}
