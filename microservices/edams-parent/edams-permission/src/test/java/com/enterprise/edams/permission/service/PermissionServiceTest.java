package com.enterprise.edams.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enterprise.edams.common.exception.BusinessException;
import com.enterprise.edams.permission.dto.PermissionCreateRequest;
import com.enterprise.edams.permission.dto.PermissionVO;
import com.enterprise.edams.permission.entity.SysPermission;
import com.enterprise.edams.permission.repository.SysPermissionRepository;
import com.enterprise.edams.permission.repository.SysRolePermissionRepository;
import com.enterprise.edams.permission.repository.SysUserRoleRepository;
import com.enterprise.edams.permission.service.impl.PermissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PermissionService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("权限服务测试")
class PermissionServiceTest {

    @Mock
    private SysPermissionRepository permissionRepository;

    @Mock
    private SysRolePermissionRepository rolePermissionRepository;

    @Mock
    private SysUserRoleRepository userRoleRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private PermissionServiceImpl permissionService;

    private SysPermission testPermission;
    private PermissionCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testPermission = SysPermission.builder()
                .id("perm-001")
                .name("测试权限")
                .code("test:permission")
                .permissionType("BUTTON")
                .module("test")
                .resourcePath("/api/test")
                .httpMethod("GET")
                .status(1)
                .sortOrder(0)
                .isDeleted(0)
                .createdBy("admin")
                .createdTime(LocalDateTime.now())
                .build();

        createRequest = PermissionCreateRequest.builder()
                .name("测试权限")
                .code("test:permission")
                .permissionType("BUTTON")
                .module("test")
                .resourcePath("/api/test")
                .httpMethod("GET")
                .status(1)
                .build();
    }

    @Test
    @DisplayName("创建权限 - 成功")
    void testCreatePermission_Success() {
        // Given
        when(permissionRepository.existsByCode("test:permission")).thenReturn(false);
        when(permissionRepository.insert(any(SysPermission.class))).thenReturn(1);

        // When
        PermissionVO result = permissionService.createPermission(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("test:permission");
        verify(permissionRepository, times(1)).insert(any(SysPermission.class));
    }

    @Test
    @DisplayName("创建权限 - 编码已存在")
    void testCreatePermission_CodeExists() {
        // Given
        when(permissionRepository.existsByCode("test:permission")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> permissionService.createPermission(createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("权限编码已存在");
    }

    @Test
    @DisplayName("更新权限 - 成功")
    void testUpdatePermission_Success() {
        // Given
        when(permissionRepository.selectById("perm-001")).thenReturn(testPermission);
        when(permissionRepository.updateById(any(SysPermission.class))).thenReturn(true);

        PermissionCreateRequest updateRequest = PermissionCreateRequest.builder()
                .name("更新后的权限")
                .description("更新描述")
                .build();

        // When
        PermissionVO result = permissionService.updatePermission("perm-001", updateRequest);

        // Then
        assertThat(result).isNotNull();
        verify(permissionRepository, times(1)).updateById(any(SysPermission.class));
    }

    @Test
    @DisplayName("更新权限 - 权限不存在")
    void testUpdatePermission_NotFound() {
        // Given
        when(permissionRepository.selectById("non-existent")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> permissionService.updatePermission("non-existent", createRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("权限不存在");
    }

    @Test
    @DisplayName("删除权限 - 成功")
    void testDeletePermission_Success() {
        // Given
        when(permissionRepository.selectById("perm-001")).thenReturn(testPermission);
        doNothing().when(rolePermissionRepository).deleteByPermissionId("perm-001");
        when(permissionRepository.updateById(any(SysPermission.class))).thenReturn(true);

        // When
        permissionService.deletePermission("perm-001");

        // Then
        verify(rolePermissionRepository, times(1)).deleteByPermissionId("perm-001");
        verify(permissionRepository, times(1)).updateById(any(SysPermission.class));
    }

    @Test
    @DisplayName("删除权限 - 权限不存在")
    void testDeletePermission_NotFound() {
        // Given
        when(permissionRepository.selectById("non-existent")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> permissionService.deletePermission("non-existent"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("权限不存在");
    }

    @Test
    @DisplayName("获取权限详情 - 成功")
    void testGetPermissionById_Success() {
        // Given
        when(permissionRepository.selectById("perm-001")).thenReturn(testPermission);

        // When
        PermissionVO result = permissionService.getPermissionById("perm-001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("perm-001");
        assertThat(result.getCode()).isEqualTo("test:permission");
    }

    @Test
    @DisplayName("获取权限详情 - 权限不存在")
    void testGetPermissionById_NotFound() {
        // Given
        when(permissionRepository.selectById("non-existent")).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> permissionService.getPermissionById("non-existent"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("权限不存在");
    }

    @Test
    @DisplayName("根据编码获取权限 - 成功")
    void testGetPermissionByCode_Success() {
        // Given
        when(permissionRepository.findByCode("test:permission")).thenReturn(testPermission);

        // When
        PermissionVO result = permissionService.getPermissionByCode("test:permission");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCode()).isEqualTo("test:permission");
    }

    @Test
    @DisplayName("分页查询权限")
    void testPagePermissions() {
        // Given
        Page<SysPermission> page = new Page<>(1, 10);
        Page<SysPermission> resultPage = new Page<>(1, 10);
        resultPage.setRecords(List.of(testPermission));
        resultPage.setTotal(1);

        when(permissionRepository.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(resultPage);

        // When
        Page<PermissionVO> result = permissionService.pagePermissions("test", "BUTTON", "test", 1, 10);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSize(1);
    }

    @Test
    @DisplayName("获取所有权限列表")
    void testListAllPermissions() {
        // Given
        when(permissionRepository.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(testPermission));

        // When
        List<PermissionVO> result = permissionService.listAllPermissions();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("根据模块获取权限列表")
    void testListPermissionsByModule() {
        // Given
        when(permissionRepository.findByModule("test")).thenReturn(List.of(testPermission));

        // When
        List<PermissionVO> result = permissionService.listPermissionsByModule("test");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("根据类型获取权限列表")
    void testListPermissionsByType() {
        // Given
        when(permissionRepository.findByPermissionType("BUTTON")).thenReturn(List.of(testPermission));

        // When
        List<PermissionVO> result = permissionService.listPermissionsByType("BUTTON");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("获取权限树")
    void testGetPermissionTree() {
        // Given
        when(permissionRepository.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(testPermission));

        // When
        List<PermissionVO> result = permissionService.getPermissionTree();

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("验证编码是否存在 - 存在")
    void testCheckCodeExists_True() {
        // Given
        when(permissionRepository.existsByCode("test:permission")).thenReturn(true);

        // When
        boolean result = permissionService.checkCodeExists("test:permission");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("验证编码是否存在 - 不存在")
    void testCheckCodeExists_False() {
        // Given
        when(permissionRepository.existsByCode("new:permission")).thenReturn(false);

        // When
        boolean result = permissionService.checkCodeExists("new:permission");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("获取角色的权限列表")
    void testGetPermissionsByRoleId() {
        // Given
        when(rolePermissionRepository.findPermissionIdsByRoleId("role-001"))
                .thenReturn(List.of("perm-001"));
        when(permissionRepository.selectBatchIds(List.of("perm-001")))
                .thenReturn(List.of(testPermission));

        // When
        List<PermissionVO> result = permissionService.getPermissionsByRoleId("role-001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("获取用户的权限列表 - 从缓存")
    void testGetUserPermissions_FromCache() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(List.of(
                PermissionVO.builder().id("perm-001").code("test:permission").build()
        ));

        // When
        List<PermissionVO> result = permissionService.getUserPermissions("user-001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("获取用户的权限列表 - 从数据库")
    void testGetUserPermissions_FromDatabase() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(userRoleRepository.findRoleIdsByUserId("user-001")).thenReturn(List.of("role-001"));
        when(rolePermissionRepository.findPermissionIdsByRoleId("role-001")).thenReturn(List.of("perm-001"));
        when(permissionRepository.selectBatchIds(List.of("perm-001"))).thenReturn(List.of(testPermission));
        doNothing().when(valueOperations).set(anyString(), any(), anyLong(), any());

        // When
        List<PermissionVO> result = permissionService.getUserPermissions("user-001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(valueOperations, times(1)).set(anyString(), any(), anyLong(), any());
    }

    @Test
    @DisplayName("获取用户权限编码列表")
    void testGetUserPermissionCodes() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(List.of(
                PermissionVO.builder().id("perm-001").code("test:permission").build()
        ));

        // When
        List<String> result = permissionService.getUserPermissionCodes("user-001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).contains("test:permission");
    }

    @Test
    @DisplayName("检查用户权限 - 超级管理员")
    void testHasPermission_SuperAdmin() {
        // When
        boolean result = permissionService.hasPermission("SUPER_ADMIN", "any:permission");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("检查用户权限 - 有权限")
    void testHasPermission_HasPermission() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(List.of(
                PermissionVO.builder().id("perm-001").code("test:permission").build()
        ));

        // When
        boolean result = permissionService.hasPermission("user-001", "test:permission");

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("检查用户权限 - 无权限")
    void testHasPermission_NoPermission() {
        // Given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(List.of(
                PermissionVO.builder().id("perm-001").code("test:permission").build()
        ));

        // When
        boolean result = permissionService.hasPermission("user-001", "other:permission");

        // Then
        assertThat(result).isFalse();
    }
}
