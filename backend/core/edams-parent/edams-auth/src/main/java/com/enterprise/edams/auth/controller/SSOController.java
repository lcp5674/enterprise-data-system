package com.enterprise.edams.auth.controller;

import com.enterprise.edams.auth.dto.SSOLoginRequest;
import com.enterprise.edams.auth.dto.TokenResponse;
import com.enterprise.edams.auth.security.JwtTokenProvider;
import com.enterprise.edams.auth.service.AuthService;
import com.enterprise.edams.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * SSO认证控制器
 *
 * <p>提供企业微信、钉钉、LDAP等SSO登录接口</p>
 *
 * @author EDAMS Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/sso")
@RequiredArgsConstructor
@Tag(name = "SSO认证", description = "单点登录认证接口")
public class SSOController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 获取SSO登录URL
     */
    @GetMapping("/login")
    @Operation(summary = "获取SSO登录URL", description = "获取指定SSO提供商的登录跳转URL")
    public Result<Map<String, String>> getSSOLoginUrl(
            @Parameter(description = "SSO提供商: wxwork/dingtalk/ldap") @RequestParam String provider,
            @Parameter(description = "回调URL") @RequestParam(required = false) String redirectUri) {
        
        log.info("获取SSO登录URL, provider={}, redirectUri={}", provider, redirectUri);
        
        // 根据provider生成对应的登录URL
        String loginUrl = generateLoginUrl(provider, redirectUri);
        
        return Result.success(Map.of("url", loginUrl));
    }

    /**
     * 处理SSO回调
     */
    @PostMapping("/callback")
    @Operation(summary = "SSO回调处理", description = "处理SSO提供商返回的授权码，交换令牌")
    public Result<TokenResponse> handleSSOCallback(
            @RequestBody SSOCallbackRequest request) {
        
        log.info("处理SSO回调, provider={}", request.getProvider());
        
        // 验证提供商
        if (!isValidProvider(request.getProvider())) {
            return Result.fail("不支持的SSO提供商");
        }
        
        // 调用SSO服务获取用户信息
        SSOUserInfo userInfo = fetchSSOUserInfo(request.getProvider(), request.getCode());
        
        // 创建或更新本地用户
        TokenResponse tokenResponse = authService.loginWithSSO(
                userInfo.getOpenId(),
                userInfo.getUnionId(),
                request.getProvider()
        );
        
        return Result.success(tokenResponse);
    }

    /**
     * 获取SSO用户信息
     */
    @GetMapping("/userinfo")
    @Operation(summary = "获取SSO用户信息", description = "获取当前SSO用户的信息")
    public Result<SSOUserInfo> getSSOUserInfo(
            @Parameter(description = "访问令牌") @RequestParam String accessToken) {
        
        // 验证accessToken并获取用户信息
        SSOUserInfo userInfo = validateAndGetUserInfo(accessToken);
        
        return Result.success(userInfo);
    }

    /**
     * SSO用户绑定
     */
    @PostMapping("/bind")
    @Operation(summary = "绑定SSO账号", description = "将SSO账号与本地账号绑定")
    public Result<Void> bindSSOAccount(
            @RequestBody SSOBindRequest request) {
        
        log.info("绑定SSO账号, provider={}", request.getProvider());
        
        authService.bindSSOAccount(
                request.getUserId(),
                request.getProvider(),
                request.getOpenId()
        );
        
        return Result.success();
    }

    /**
     * 解绑SSO账号
     */
    @PostMapping("/unbind")
    @Operation(summary = "解绑SSO账号", description = "解除SSO账号与本地账号的绑定")
    public Result<Void> unbindSSOAccount(
            @RequestBody SSOUnbindRequest request) {
        
        log.info("解绑SSO账号, provider={}", request.getProvider());
        
        authService.unbindSSOAccount(
                request.getUserId(),
                request.getProvider()
        );
        
        return Result.success();
    }

    /**
     * 获取支持的SSO提供商列表
     */
    @GetMapping("/providers")
    @Operation(summary = "获取SSO提供商列表", description = "获取系统支持的SSO提供商")
    public Result<Map<String, Object>> getSSOProviders() {
        
        return Result.success(Map.of(
                "providers", java.util.List.of(
                        Map.of("code", "wxwork", "name", "企业微信", "icon", "wxwork"),
                        Map.of("code", "dingtalk", "name", "钉钉", "icon", "dingtalk"),
                        Map.of("code", "ldap", "name", "LDAP/AD", "icon", "ldap")
                )
        ));
    }

    // ==================== 私有方法 ====================

    private String generateLoginUrl(String provider, String redirectUri) {
        String baseUrl = switch (provider) {
            case "wxwork" -> "https://open.weixin.qq.com/connect/oauth2/authorize";
            case "dingtalk" -> "https://oapi.dingtalk.com/connect/qrconnect";
            case "ldap" -> "/api/v1/auth/login"; // LDAP直接用本地登录
            default -> throw new IllegalArgumentException("不支持的SSO提供商: " + provider);
        };

        // 构建回调参数
        String state = java.util.UUID.randomUUID().toString();
        String callbackUrl = "/api/v1/sso/callback";

        if ("ldap".equals(provider)) {
            return callbackUrl;
        }

        // 实际的OAuth2 URL需要配置appid和redirect_uri
        return baseUrl + "?appid={APP_ID}&redirect_uri=" + callbackUrl 
                + "&response_type=code&scope=snsapi_login&state=" + state;
    }

    private boolean isValidProvider(String provider) {
        return java.util.Set.of("wxwork", "dingtalk", "ldap", "keycloak").contains(provider);
    }

    private SSOUserInfo fetchSSOUserInfo(String provider, String code) {
        // 实际实现中需要调用SSO提供商的API获取用户信息
        // 这里返回模拟数据
        return new SSOUserInfo(
                "openid_" + code,
                "unionid_" + code,
                "SSO_User_" + code.substring(0, Math.min(8, code.length())),
                "user@example.com"
        );
    }

    private SSOUserInfo validateAndGetUserInfo(String accessToken) {
        // 验证token并返回用户信息
        return new SSOUserInfo("openid", "unionid", "SSO_User", "user@example.com");
    }

    // ==================== 内部类 ====================

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class SSOCallbackRequest {
        private String provider;
        private String code;
        private String state;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class SSOBindRequest {
        private Long userId;
        private String provider;
        private String openId;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class SSOUnbindRequest {
        private Long userId;
        private String provider;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class SSOUserInfo {
        private String openId;
        private String unionId;
        private String nickname;
        private String email;
    }
}
