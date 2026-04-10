package com.enterprise.edams.auth.feign;

import com.enterprise.edams.auth.feign.fallback.PermissionFeignClientFallback;
import com.enterprise.edams.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * 权限服务Feign客户端
 * 用于从权限服务获取用户角色、权限和菜单信息
 *
 * @author Backend Team
 * @version 1.0.0
 */
@FeignClient(
    name = "edams-permission",
    url = "${feign.permission.url:}",
    fallback = PermissionFeignClientFallback.class,
    configuration = FeignConfig.class
)
public interface PermissionFeignClient {

    /**
     * 获取用户角色列表
     *
     * @param userId 用户ID
     * @return 用户角色信息列表
     */
    @GetMapping("/api/v1/users/{userId}/roles")
    Result<List<Map<String, Object>>> getUserRoles(@PathVariable("userId") String userId);

    /**
     * 获取用户权限编码列表
     *
     * @param userId 用户ID
     * @return 权限编码列表
     */
    @GetMapping("/api/v1/users/{userId}/permissions")
    Result<List<String>> getUserPermissions(@PathVariable("userId") String userId);

    /**
     * 获取用户菜单列表
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    @GetMapping("/api/v1/users/{userId}/menus")
    Result<List<Map<String, Object>>> getUserMenus(@PathVariable("userId") String userId);

    /**
     * 分配用户角色
     *
     * @param userId  用户ID
     * @param roleIds 角色ID列表
     * @return 操作结果
     */
    @PutMapping("/api/v1/users/{userId}/roles")
    Result<Void> assignRoles(@PathVariable("userId") String userId, @RequestBody List<String> roleIds);
}
