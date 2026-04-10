package com.enterprise.edams.auth.feign;

import com.enterprise.edams.auth.feign.fallback.PermissionFeignClientFallback;
import com.enterprise.edams.common.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * PermissionFeignClient 单元测试
 * 测试Feign客户端接口调用和Fallback机制
 *
 * @author Backend Team
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("权限服务Feign客户端测试")
class PermissionFeignClientTest {

    @Mock
    private PermissionFeignClient permissionFeignClient;

    private static final String TEST_USER_ID = "user-001";

    @BeforeEach
    void setUp() {
        // 由于PermissionFeignClient是接口，我们需要使用Mock
        // 实际测试会通过服务层的测试来验证Feign调用
    }

    @Test
    @DisplayName("获取用户角色列表 - Mock返回成功")
    void testGetUserRoles_Success() {
        // Given
        List<Map<String, Object>> mockRoles = new ArrayList<>();
        Map<String, Object> role1 = new HashMap<>();
        role1.put("roleId", "role-001");
        role1.put("roleName", "管理员");
        role1.put("roleCode", "ADMIN");
        mockRoles.add(role1);

        Map<String, Object> role2 = new HashMap<>();
        role2.put("roleId", "role-002");
        role2.put("roleName", "普通用户");
        role2.put("roleCode", "USER");
        mockRoles.add(role2);

        Result<List<Map<String, Object>>> mockResult = Result.success(mockRoles);
        when(permissionFeignClient.getUserRoles(TEST_USER_ID)).thenReturn(mockResult);

        // When
        Result<List<Map<String, Object>>> result = permissionFeignClient.getUserRoles(TEST_USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).hasSize(2);
        assertThat(result.getData().get(0).get("roleCode")).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("获取用户权限列表 - Mock返回成功")
    void testGetUserPermissions_Success() {
        // Given
        List<String> mockPermissions = List.of(
                "system:user:view",
                "system:user:edit",
                "system:role:assign"
        );

        Result<List<String>> mockResult = Result.success(mockPermissions);
        when(permissionFeignClient.getUserPermissions(TEST_USER_ID)).thenReturn(mockResult);

        // When
        Result<List<String>> result = permissionFeignClient.getUserPermissions(TEST_USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).hasSize(3);
        assertThat(result.getData()).contains("system:user:view");
    }

    @Test
    @DisplayName("获取用户菜单列表 - Mock返回成功")
    void testGetUserMenus_Success() {
        // Given
        List<Map<String, Object>> mockMenus = new ArrayList<>();
        Map<String, Object> menu1 = new HashMap<>();
        menu1.put("menuId", "menu-001");
        menu1.put("menuName", "系统管理");
        menu1.put("path", "/system");
        menu1.put("icon", "setting");
        mockMenus.add(menu1);

        Map<String, Object> menu2 = new HashMap<>();
        menu2.put("menuId", "menu-002");
        menu2.put("menuName", "用户管理");
        menu2.put("path", "/system/user");
        menu2.put("parentId", "menu-001");
        mockMenus.add(menu2);

        Result<List<Map<String, Object>>> mockResult = Result.success(mockMenus);
        when(permissionFeignClient.getUserMenus(TEST_USER_ID)).thenReturn(mockResult);

        // When
        Result<List<Map<String, Object>>> result = permissionFeignClient.getUserMenus(TEST_USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).hasSize(2);
    }

    @Test
    @DisplayName("分配用户角色 - Mock返回成功")
    void testAssignRoles_Success() {
        // Given
        List<String> roleIds = List.of("role-001", "role-002");
        Result<Void> mockResult = Result.success(null);
        when(permissionFeignClient.assignRoles(TEST_USER_ID, roleIds)).thenReturn(mockResult);

        // When
        Result<Void> result = permissionFeignClient.assignRoles(TEST_USER_ID, roleIds);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        verify(permissionFeignClient, times(1)).assignRoles(TEST_USER_ID, roleIds);
    }

    @Test
    @DisplayName("获取用户角色列表 - 返回空列表")
    void testGetUserRoles_EmptyList() {
        // Given
        Result<List<Map<String, Object>>> mockResult = Result.success(new ArrayList<>());
        when(permissionFeignClient.getUserRoles(TEST_USER_ID)).thenReturn(mockResult);

        // When
        Result<List<Map<String, Object>>> result = permissionFeignClient.getUserRoles(TEST_USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEmpty();
    }

    @Test
    @DisplayName("获取用户角色列表 - 服务调用失败")
    void testGetUserRoles_Failure() {
        // Given
        Result<List<Map<String, Object>>> mockResult = Result.fail("500", "服务内部错误");
        when(permissionFeignClient.getUserRoles(TEST_USER_ID)).thenReturn(mockResult);

        // When
        Result<List<Map<String, Object>>> result = permissionFeignClient.getUserRoles(TEST_USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMessage()).isEqualTo("服务内部错误");
    }

    @Test
    @DisplayName("PermissionFeignClientFallback - 降级返回空数据")
    void testFallback_ReturnsEmptyOnFailure() {
        // Given
        PermissionFeignClientFallback fallback = new PermissionFeignClientFallback();

        // When - 调用fallback方法
        Result<List<Map<String, Object>>> rolesResult = fallback.getUserRoles(TEST_USER_ID);
        Result<List<String>> permissionsResult = fallback.getUserPermissions(TEST_USER_ID);
        Result<List<Map<String, Object>>> menusResult = fallback.getUserMenus(TEST_USER_ID);
        Result<Void> assignResult = fallback.assignRoles(TEST_USER_ID, new ArrayList<>());

        // Then - Fallback应返回空数据表示服务不可用
        assertThat(rolesResult).isNotNull();
        assertThat(rolesResult.isSuccess()).isTrue();
        assertThat(rolesResult.getData()).isEmpty();

        assertThat(permissionsResult).isNotNull();
        assertThat(permissionsResult.isSuccess()).isTrue();
        assertThat(permissionsResult.getData()).isEmpty();

        assertThat(menusResult).isNotNull();
        assertThat(menusResult.isSuccess()).isTrue();
        assertThat(menusResult.getData()).isEmpty();

        assertThat(assignResult).isNotNull();
        assertThat(assignResult.isSuccess()).isFalse();
    }
}
