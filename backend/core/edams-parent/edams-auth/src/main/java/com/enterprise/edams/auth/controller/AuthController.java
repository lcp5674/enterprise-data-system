package com.enterprise.edams.auth.controller;

import com.enterprise.edams.auth.dto.*;
import com.enterprise.edams.auth.security.JwtTokenProvider;
import com.enterprise.edams.auth.service.AuthService;
import com.enterprise.edams.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * <p>提供登录、注册、令牌刷新、注销等认证相关API</p>
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户登录、注册、令牌管理等接口")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "通过用户名和密码进行身份验证，返回JWT令牌")
    public Result<TokenResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        TokenResponse tokenResponse = authService.login(request, ip, userAgent);
        return Result.success(tokenResponse);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户账号")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success();
    }

    /**
     * 刷新访问令牌
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public Result<TokenResponse> refreshToken(
            @Parameter(description = "刷新令牌") @RequestParam("refreshToken") String refreshToken) {
        
        TokenResponse tokenResponse = authService.refreshToken(refreshToken);
        return Result.success(tokenResponse);
    }

    /**
     * 注销登录
     */
    @PostMapping("/logout")
    @Operation(summary = "注销登录", description = "注销当前用户，使当前令牌失效")
    public Result<Void> logout(HttpServletRequest request) {
        String token = extractTokenFromHeader(request);
        
        // 从令牌中获取userId
        Long userId = null;
        if (token != null && jwtTokenProvider.validateToken(token)) {
            userId = jwtTokenProvider.getUserIdFromToken(token);
        }
        
        authService.logout(token, userId);
        return Result.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前用户", description = "获取已认证用户的详细信息")
    public Result<UserInfoDTO> getCurrentUser(HttpServletRequest request) {
        Long userId = extractUserIdFromRequest(request);
        UserInfoDTO userInfo = authService.getCurrentUserInfo(userId);
        return Result.success(userInfo);
    }

    /**
     * 修改密码
     */
    @PostMapping("/password")
    @Operation(summary = "修改密码", description = "修改当前用户的登录密码")
    public Result<Void> changePassword(
            HttpServletRequest request,
            @RequestBody ChangePasswordRequest passwordRequest) {
        
        Long userId = extractUserIdFromRequest(request);
        authService.changePassword(userId, passwordRequest.getOldPassword(), passwordRequest.getNewPassword());
        return Result.success();
    }

    /**
     * 重置密码（发送验证码）
     */
    @PostMapping("/password/reset/send-code")
    @Operation(summary = "发送重置密码验证码", description = "向用户邮箱或手机号发送重置密码的验证码")
    public Result<Void> sendResetCode(
            @Parameter(description = "用户名/邮箱/手机号") @RequestParam("account") String account) {
        // TODO: 实现验证码发送逻辑
        return Result.success();
    }

    /**
     * 重置密码（提交新密码）
     */
    @PostMapping("/password/reset/submit")
    @Operation(summary = "重置密码", description = "使用验证码重置用户密码")
    public Result<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getAccount(), request.getVerificationCode(), request.getNewPassword());
        return Result.success();
    }

    /**
     * 检查令牌有效性
     */
    @GetMapping("/check-token")
    @Operation(summary = "检查令牌有效性", description = "检查当前JWT令牌是否有效")
    public Result<Boolean> checkTokenValidity(HttpServletRequest request) {
        String token = extractTokenFromHeader(request);
        if (token == null) {
            return Result.success(false);
        }
        boolean valid = jwtTokenProvider.validateToken(token);
        return Result.success(valid);
    }

    // ==================== 内部DTO类 ====================

    /**
     * 修改密码请求
     */
    @lombok.Data
    public static class ChangePasswordRequest {
        /** 原密码 */
        private String oldPassword;

        /** 新密码 */
        private String newPassword;
    }

    /**
     * 重置密码请求
     */
    @lombok.Data
    public static class ResetPasswordRequest {
        /** 账户（用户名/邮箱） */
        private String account;

        /** 验证码 */
        private String verificationCode;

        /** 新密码 */
        private String newPassword;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 从请求头中提取JWT Token
     */
    private String extractTokenFromHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    /**
     * 从请求头中提取用户ID
     */
    private Long extractUserIdFromRequest(HttpServletRequest request) {
        String token = extractTokenFromHeader(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            return jwtTokenProvider.getUserIdFromToken(token);
        }
        throw new RuntimeException("无法从令牌中解析用户信息");
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
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
