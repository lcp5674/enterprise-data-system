package com.enterprise.edams.auth.controller;

import com.enterprise.edams.auth.dto.*;
import com.enterprise.edams.auth.service.AuthService;
import com.enterprise.edams.auth.service.CaptchaService;
import com.enterprise.edams.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名密码登录，支持验证码")
    public Result<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        // 获取客户端IP
        String ipAddress = getClientIp(httpRequest);
        request.setIpAddress(ipAddress);
        request.setUserAgent(httpRequest.getHeader("User-Agent"));

        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    @PostMapping("/mfa/verify")
    @Operation(summary = "MFA验证", description = "验证MFA验证码完成登录")
    public Result<LoginResponse> verifyMFA(
            @Valid @RequestBody MFAVerifyRequest request,
            HttpServletRequest httpRequest) {

        request.setDeviceType(httpRequest.getHeader("X-Device-Type"));
        request.setDeviceId(httpRequest.getHeader("X-Device-Id"));

        LoginResponse response = authService.verifyMFA(request);
        return Result.success(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "使当前Token失效")
    public Result<Void> logout(
            @Parameter(description = "访问令牌") @RequestHeader("Authorization") String authorization) {

        String token = extractToken(authorization);
        authService.logout(token);
        return Result.success();
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "使用刷新令牌获取新的访问令牌")
    public Result<LoginResponse> refresh(
            @Valid @RequestBody TokenRefreshRequest request) {

        LoginResponse response = authService.refreshToken(request.getRefreshToken());
        return Result.success(response);
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户", description = "获取当前登录用户信息")
    public Result<LoginResponse> getCurrentUser(
            @Parameter(description = "访问令牌") @RequestHeader("Authorization") String authorization) {

        String token = extractToken(authorization);
        LoginResponse response = authService.getCurrentUser(token);
        return Result.success(response);
    }

    @PostMapping("/captcha")
    @Operation(summary = "获取验证码", description = "获取图片验证码")
    public Result<CaptchaResponse> getCaptcha() {
        CaptchaResponse response = captchaService.generateCaptcha();
        return Result.success(response);
    }

    @PostMapping("/force-logout/{userId}")
    @Operation(summary = "强制登出用户", description = "强制指定用户登出")
    public Result<Void> forceLogout(
            @Parameter(description = "用户ID") @PathVariable String userId) {

        authService.forceLogout(userId);
        return Result.success();
    }

    @PostMapping("/force-logout-all")
    @Operation(summary = "强制登出所有用户", description = "强制所有用户登出")
    public Result<Void> forceLogoutAll() {
        authService.forceLogoutAll();
        return Result.success();
    }

    private String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理时取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
