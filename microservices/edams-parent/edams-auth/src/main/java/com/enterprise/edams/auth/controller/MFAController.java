package com.enterprise.edams.auth.controller;

import com.enterprise.edams.auth.dto.MFASetupResponse;
import com.enterprise.edams.auth.service.AuthService;
import com.enterprise.edams.auth.service.MFAService;
import com.enterprise.edams.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * MFA控制器
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth/mfa")
@RequiredArgsConstructor
@Tag(name = "MFA管理", description = "多因素认证相关接口")
public class MFAController {

    private final AuthService authService;
    private final MFAService mfaService;

    @GetMapping("/setup")
    @Operation(summary = "获取MFA设置信息", description = "获取MFA设置信息（用户需先完成初始设置）")
    public Result<MFASetupResponse> getMFASetup(
            @Parameter(description = "访问令牌") @RequestHeader("Authorization") String authorization) {

        // TODO: 从Token中获取用户ID
        String userId = extractUserId(authorization);
        String secret = mfaService.generateSecret();
        String qrCodeUrl = mfaService.generateQrCodeUrl(secret, userId);

        return Result.success(MFASetupResponse.builder()
                .mfaType("TOTP")
                .secret(secret)
                .qrCodeUrl(qrCodeUrl)
                .enabled(false)
                .build());
    }

    @PostMapping("/setup/{type}")
    @Operation(summary = "设置MFA", description = "设置MFA认证方式")
    public Result<MFASetupResponse> setupMFA(
            @Parameter(description = "MFA类型: TOTP/EMAIL/SMS") @PathVariable String type,
            @Parameter(description = "访问令牌") @RequestHeader("Authorization") String authorization) {

        String userId = extractUserId(authorization);
        MFASetupResponse response = authService.setupMFA(userId, type);
        return Result.success(response);
    }

    @PostMapping("/enable")
    @Operation(summary = "启用MFA", description = "启用MFA认证")
    public Result<Void> enableMFA(
            @Valid @RequestBody MFAEnableRequest request,
            @Parameter(description = "访问令牌") @RequestHeader("Authorization") String authorization) {

        String userId = extractUserId(authorization);
        authService.enableMFA(userId, request.getCode());
        return Result.success();
    }

    @PostMapping("/disable")
    @Operation(summary = "禁用MFA", description = "禁用MFA认证")
    public Result<Void> disableMFA(
            @Valid @RequestBody MFADisableRequest request,
            @Parameter(description = "访问令牌") @RequestHeader("Authorization") String authorization) {

        String userId = extractUserId(authorization);
        authService.disableMFA(userId, request.getCode());
        return Result.success();
    }

    @PostMapping("/send-email-code")
    @Operation(summary = "发送邮箱验证码", description = "发送邮箱验证码用于MFA验证")
    public Result<Void> sendEmailCode(
            @Parameter(description = "访问令牌") @RequestHeader("Authorization") String authorization) {

        String userId = extractUserId(authorization);
        mfaService.sendEmailCode(userId);
        return Result.success();
    }

    @PostMapping("/send-sms-code")
    @Operation(summary = "发送短信验证码", description = "发送短信验证码用于MFA验证")
    public Result<Void> sendSmsCode(
            @Parameter(description = "访问令牌") @RequestHeader("Authorization") String authorization) {

        String userId = extractUserId(authorization);
        mfaService.sendSmsCode(userId);
        return Result.success();
    }

    private String extractUserId(String authorization) {
        // TODO: 从Token中解析用户ID
        return "user-123";
    }

    // DTO内部类
    @lombok.Data
    public static class MFAEnableRequest {
        @NotBlank(message = "验证码不能为空")
        private String code;
    }

    @lombok.Data
    public static class MFADisableRequest {
        @NotBlank(message = "验证码不能为空")
        private String code;
    }
}
