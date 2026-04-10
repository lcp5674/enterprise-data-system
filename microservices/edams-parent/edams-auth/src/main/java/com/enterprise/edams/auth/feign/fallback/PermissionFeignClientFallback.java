package com.enterprise.edams.auth.feign.fallback;

import com.enterprise.edams.auth.feign.PermissionFeignClient;
import com.enterprise.edams.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 权限服务Feign客户端降级处理
 *
 * @author Backend Team
 * @version 1.0.0
 */
@Slf4j
@Component
public class PermissionFeignClientFallback implements PermissionFeignClient {

    @Override
    public Result<List<Map<String, Object>>> getUserRoles(String userId) {
        log.warn("Feign调用权限服务获取用户角色失败, userId: {}, 返回空列表", userId);
        return Result.success(Collections.emptyList());
    }

    @Override
    public Result<List<String>> getUserPermissions(String userId) {
        log.warn("Feign调用权限服务获取用户权限失败, userId: {}, 返回空列表", userId);
        return Result.success(Collections.emptyList());
    }

    @Override
    public Result<List<Map<String, Object>>> getUserMenus(String userId) {
        log.warn("Feign调用权限服务获取用户菜单失败, userId: {}, 返回空列表", userId);
        return Result.success(Collections.emptyList());
    }

    @Override
    public Result<Void> assignRoles(String userId, List<String> roleIds) {
        log.warn("Feign调用权限服务分配角色失败, userId: {}, roleIds: {}", userId, roleIds);
        return Result.success(null);
    }
}
