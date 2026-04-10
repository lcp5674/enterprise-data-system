package com.enterprise.edams.permission.controller;

import com.enterprise.edams.common.result.Result;
import com.enterprise.edams.permission.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 权限验证控制器
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "权限验证", description = "权限验证相关接口")
public class PermissionCheckController {

    private final PermissionService permissionService;

    @GetMapping("/permissions")
    @Operation(summary = "获取当前用户权限", description = "获取当前登录用户的所有权限")
    public Result<Map<String, Object>> getCurrentUserPermissions() {
        String userId = getCurrentUserId();
        List<String> permissionCodes = permissionService.getUserPermissionCodes(userId);
        List<String> menuPaths = permissionService.getUserPermissions(userId).stream()
                .filter(p -> "MENU".equals(p.getPermissionType()))
                .map(p -> p.getResourcePath())
                .filter(path -> path != null && !path.isEmpty())
                .toList();

        return Result.success(Map.of(
                "userId", userId,
                "permissions", permissionCodes,
                "menuPaths", menuPaths
        ));
    }

    @PostMapping("/check")
    @Operation(summary = "检查权限", description = "检查当前用户是否具有指定权限")
    public Result<Boolean> checkPermission(
            @Parameter(description = "权限编码") @RequestParam String permissionCode) {
        String userId = getCurrentUserId();
        boolean hasPermission = permissionService.hasPermission(userId, permissionCode);
        return Result.success(hasPermission);
    }

    @PostMapping("/check-batch")
    @Operation(summary = "批量检查权限", description = "检查当前用户是否具有指定的所有权限")
    public Result<Map<String, Boolean>> checkPermissionsBatch(
            @Parameter(description = "权限编码列表") @RequestBody List<String> permissionCodes) {
        String userId = getCurrentUserId();
        List<String> userPermissions = permissionService.getUserPermissionCodes(userId);
        
        Map<String, Boolean> results = permissionCodes.stream()
                .collect(java.util.stream.Collectors.toMap(
                        code -> code,
                        code -> userPermissions.contains(code)
                ));
        
        return Result.success(results);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "anonymous";
    }
}
