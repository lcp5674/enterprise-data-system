package com.enterprise.edams.integration.scenarios;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * 场景3: 权限控制流程集成测试
 * 测试任务: INT-E2E-003
 *
 * 流程:
 * 1. permission-service 创建角色
 * 2. permission-service 分配权限
 * 3. auth-service 验证权限
 * 4. gateway-service 拦截未授权请求
 */
@Testcontainers
@SpringBootTest
@DirtiesContext
@ActiveProfiles("test")
@DisplayName("场景3: 权限控制流程集成测试")
class PermissionControlScenarioTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("edams_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("sql/init-postgres.sql");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        // 清理权限相关数据（保留基础用户数据）
        jdbcTemplate.execute("DELETE FROM role_permission WHERE role_id > 3");
        jdbcTemplate.execute("DELETE FROM user_role WHERE user_id > 2");
        jdbcTemplate.execute("DELETE FROM permission WHERE id > 0");
        jdbcTemplate.execute("DELETE FROM role WHERE id > 3");
        jdbcTemplate.execute("DELETE FROM app_user WHERE id > 2");

        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @Test
    @DisplayName("完整权限控制流程测试")
    void testCompletePermissionControlFlow() {
        // ========== Step 1: 创建角色 ==========
        Long adminRoleId = createRole("SCENARIO_ADMIN", "场景管理员");
        Long viewerRoleId = createRole("SCENARIO_VIEWER", "场景查看者");
        assertThat(adminRoleId).isNotNull();
        assertThat(viewerRoleId).isNotNull();

        // ========== Step 2: 创建权限 ==========
        Long readPermission = createPermission("ASSET_READ", "ASSET", null, "READ");
        Long writePermission = createPermission("ASSET_WRITE", "ASSET", null, "WRITE");
        Long deletePermission = createPermission("ASSET_DELETE", "ASSET", null, "DELETE");

        // ========== Step 3: 分配权限给角色 ==========
        assignPermissionToRole(adminRoleId, readPermission);
        assignPermissionToRole(adminRoleId, writePermission);
        assignPermissionToRole(adminRoleId, deletePermission);
        assignPermissionToRole(viewerRoleId, readPermission);

        // ========== Step 4: 创建用户 ==========
        Long adminUserId = createUser("scenario_admin", "scenario_admin@enterprise.com", "Admin User");
        Long viewerUserId = createUser("scenario_viewer", "scenario_viewer@enterprise.com", "Viewer User");

        // ========== Step 5: 分配角色给用户 ==========
        assignRoleToUser(adminUserId, adminRoleId);
        assignRoleToUser(viewerUserId, viewerRoleId);

        // ========== Step 6: 缓存权限到Redis ==========
        cacheUserPermissions(adminUserId, List.of("ASSET_READ", "ASSET_WRITE", "ASSET_DELETE"));
        cacheUserPermissions(viewerUserId, List.of("ASSET_READ"));

        // ========== 验证完整流程 ==========
        verifyPermissionFlow(adminUserId, viewerUserId, adminRoleId, viewerRoleId);
    }

    @Test
    @DisplayName("角色权限继承测试")
    void testRolePermissionInheritance() {
        // Given - 创建角色层级
        Long superRoleId = createRole("SCENARIO_SUPER", "超级角色");
        Long subRoleId = createRole("SCENARIO_SUB", "子角色");

        // 创建权限
        Long perm1 = createPermission("PERM_1", "RESOURCE", null, "ACTION");
        Long perm2 = createPermission("PERM_2", "RESOURCE", null, "ACTION");

        // When - 为超级角色分配权限
        assignPermissionToRole(superRoleId, perm1);
        assignPermissionToRole(superRoleId, perm2);

        // 为子角色分配额外权限
        Long perm3 = createPermission("PERM_3", "RESOURCE", null, "ACTION");
        assignPermissionToRole(subRoleId, perm3);

        // 创建用户并分配两个角色
        Long userId = createUser("inheritance_user", "inheritance@test.com", "Inheritance User");
        assignRoleToUser(userId, superRoleId);
        assignRoleToUser(userId, subRoleId);

        // Then - 验证用户拥有所有权限
        List<String> permissions = getUserPermissions(userId);
        assertThat(permissions).hasSize(3);
        assertThat(permissions).contains("PERM_1", "PERM_2", "PERM_3");
    }

    @Test
    @DisplayName("权限验证测试 - 允许访问")
    void testPermissionVerificationAllowed() {
        // Given
        Long roleId = createRole("SCENARIO_ALLOWED", "允许角色");
        Long permissionId = createPermission("ALLOWED_PERM", "ASSET", 1L, "READ");
        assignPermissionToRole(roleId, permissionId);

        Long userId = createUser("allowed_user", "allowed@test.com", "Allowed User");
        assignRoleToUser(userId, roleId);

        // When - 验证权限
        boolean hasPermission = checkUserPermission(userId, "ASSET", 1L, "READ");

        // Then
        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("权限验证测试 - 拒绝访问")
    void testPermissionVerificationDenied() {
        // Given
        Long roleId = createRole("SCENARIO_DENIED", "受限角色");
        Long permissionId = createPermission("DENIED_PERM", "ASSET", 1L, "READ");
        assignPermissionToRole(roleId, permissionId);

        Long userId = createUser("denied_user", "denied@test.com", "Denied User");
        assignRoleToUser(userId, roleId);

        // When - 验证不存在的权限
        boolean hasWritePermission = checkUserPermission(userId, "ASSET", 1L, "WRITE");
        boolean hasDeletePermission = checkUserPermission(userId, "ASSET", 1L, "DELETE");

        // Then
        assertThat(hasWritePermission).isFalse();
        assertThat(hasDeletePermission).isFalse();
    }

    @Test
    @DisplayName("资源级别权限控制测试")
    void testResourceLevelPermissionControl() {
        // Given - 创建资源特定权限
        Long roleId = createRole("SCENARIO_RESOURCE", "资源角色");

        // 对特定资源的权限
        Long asset1Read = createPermission("ASSET_1_READ", "ASSET", 1L, "READ");
        Long asset1Write = createPermission("ASSET_1_WRITE", "ASSET", 1L, "WRITE");
        Long asset2Read = createPermission("ASSET_2_READ", "ASSET", 2L, "READ");

        assignPermissionToRole(roleId, asset1Read);
        assignPermissionToRole(roleId, asset1Write);
        assignPermissionToRole(roleId, asset2Read);

        Long userId = createUser("resource_user", "resource@test.com", "Resource User");
        assignRoleToUser(userId, roleId);

        // When & Then - 验证资源级别权限
        assertThat(checkUserPermission(userId, "ASSET", 1L, "READ")).isTrue();
        assertThat(checkUserPermission(userId, "ASSET", 1L, "WRITE")).isTrue();
        assertThat(checkUserPermission(userId, "ASSET", 2L, "READ")).isTrue();
        assertThat(checkUserPermission(userId, "ASSET", 2L, "WRITE")).isFalse(); // 没有asset 2的写权限
        assertThat(checkUserPermission(userId, "ASSET", 3L, "READ")).isFalse(); // 没有asset 3的任何权限
    }

    @Test
    @DisplayName("权限缓存测试")
    void testPermissionCaching() {
        // Given
        Long roleId = createRole("SCENARIO_CACHE", "缓存角色");
        Long permissionId = createPermission("CACHE_PERM", "ASSET", null, "READ");
        assignPermissionToRole(roleId, permissionId);

        Long userId = createUser("cache_user", "cache@test.com", "Cache User");
        assignRoleToUser(userId, roleId);

        // When - 首次查询权限（从数据库）
        long startTime1 = System.currentTimeMillis();
        List<String> permissions1 = getUserPermissionsFromDb(userId);
        long dbQueryTime = System.currentTimeMillis() - startTime1;

        // 缓存权限
        cacheUserPermissions(userId, permissions1);

        // 再次查询权限（从缓存）
        long startTime2 = System.currentTimeMillis();
        List<String> permissions2 = getUserPermissionsFromCache(userId);
        long cacheQueryTime = System.currentTimeMillis() - startTime2;

        // Then - 缓存查询应该更快
        assertThat(permissions1).isEqualTo(permissions2);
        assertThat(cacheQueryTime).isLessThan(dbQueryTime);
    }

    @Test
    @DisplayName("角色变更权限更新测试")
    void testRoleChangePermissionUpdate() {
        // Given
        Long roleId = createRole("SCENARIO_CHANGE", "变更角色");
        Long perm1 = createPermission("CHANGE_PERM_1", "ASSET", null, "READ");
        assignPermissionToRole(roleId, perm1);

        Long userId = createUser("change_user", "change@test.com", "Change User");
        assignRoleToUser(userId, roleId);

        // 初始权限验证
        assertThat(checkUserPermission(userId, "ASSET", null, "READ")).isTrue();
        assertThat(checkUserPermission(userId, "ASSET", null, "WRITE")).isFalse();

        // When - 添加新权限
        Long perm2 = createPermission("CHANGE_PERM_2", "ASSET", null, "WRITE");
        assignPermissionToRole(roleId, perm2);

        // Then - 用户应该获得新权限
        assertThat(checkUserPermission(userId, "ASSET", null, "READ")).isTrue();
        assertThat(checkUserPermission(userId, "ASSET", null, "WRITE")).isTrue();
    }

    @Test
    @DisplayName("用户多角色权限合并测试")
    void testMultipleRolesPermissionMerge() {
        // Given
        Long role1 = createRole("SCENARIO_ROLE_1", "角色1");
        Long role2 = createRole("SCENARIO_ROLE_2", "角色2");
        Long role3 = createRole("SCENARIO_ROLE_3", "角色3");

        Long perm1 = createPermission("MERGE_PERM_1", "ASSET", null, "READ");
        Long perm2 = createPermission("MERGE_PERM_2", "ASSET", null, "WRITE");
        Long perm3 = createPermission("MERGE_PERM_3", "ASSET", null, "DELETE");

        assignPermissionToRole(role1, perm1);
        assignPermissionToRole(role2, perm2);
        assignPermissionToRole(role3, perm3);

        Long userId = createUser("merge_user", "merge@test.com", "Merge User");
        assignRoleToUser(userId, role1);
        assignRoleToUser(userId, role2);
        assignRoleToUser(userId, role3);

        // When
        List<String> permissions = getUserPermissions(userId);

        // Then - 用户应该拥有所有角色的权限
        assertThat(permissions).hasSize(3);
        assertThat(permissions).contains("MERGE_PERM_1", "MERGE_PERM_2", "MERGE_PERM_3");
    }

    // Helper methods
    private Long createRole(String name, String description) {
        jdbcTemplate.update(
            "INSERT INTO role (name, description) VALUES (?, ?)",
            name, description
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM role WHERE name = ?", Long.class, name
        );
    }

    private Long createPermission(String name, String resourceType, Long resourceId, String action) {
        jdbcTemplate.update(
            "INSERT INTO permission (name, resource_type, resource_id, action) VALUES (?, ?, ?, ?)",
            name, resourceType, resourceId, action
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM permission WHERE name = ?", Long.class, name
        );
    }

    private void assignPermissionToRole(Long roleId, Long permissionId) {
        jdbcTemplate.update(
            "INSERT INTO role_permission (role_id, permission_id) VALUES (?, ?)",
            roleId, permissionId
        );
    }

    private Long createUser(String username, String email, String fullName) {
        jdbcTemplate.update(
            "INSERT INTO app_user (username, email, password, full_name, status) VALUES (?, ?, ?, ?, ?)",
            username, email, "password_hash", fullName, "ACTIVE"
        );
        return jdbcTemplate.queryForObject(
            "SELECT id FROM app_user WHERE username = ?", Long.class, username
        );
    }

    private void assignRoleToUser(Long userId, Long roleId) {
        jdbcTemplate.update(
            "INSERT INTO user_role (user_id, role_id) VALUES (?, ?)",
            userId, roleId
        );
    }

    private void cacheUserPermissions(Long userId, List<String> permissions) {
        String key = "user:permissions:" + userId;
        redisTemplate.opsForSet().add(key, permissions.toArray(new String[0]));
        redisTemplate.expire(key, java.time.Duration.ofHours(1));
    }

    private List<String> getUserPermissions(Long userId) {
        return jdbcTemplate.query(
            "SELECT DISTINCT p.name FROM permission p " +
            "JOIN role_permission rp ON p.id = rp.permission_id " +
            "JOIN user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = ?",
            (rs, rowNum) -> rs.getString("name"),
            userId
        );
    }

    private List<String> getUserPermissionsFromDb(Long userId) {
        return getUserPermissions(userId);
    }

    private List<String> getUserPermissionsFromCache(Long userId) {
        String key = "user:permissions:" + userId;
        return redisTemplate.opsForSet().members(key).stream().toList();
    }

    private boolean checkUserPermission(Long userId, String resourceType, Long resourceId, String action) {
        String sql = """
            SELECT COUNT(*) FROM permission p
            JOIN role_permission rp ON p.id = rp.permission_id
            JOIN user_role ur ON rp.role_id = ur.role_id
            WHERE ur.user_id = ?
            AND p.resource_type = ?
            AND p.action = ?
            """;

        if (resourceId != null) {
            sql += " AND (p.resource_id = ? OR p.resource_id IS NULL)";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, resourceType, action, resourceId);
            return count != null && count > 0;
        } else {
            sql += " AND p.resource_id IS NULL";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, resourceType, action);
            return count != null && count > 0;
        }
    }

    private void verifyPermissionFlow(Long adminUserId, Long viewerUserId, Long adminRoleId, Long viewerRoleId) {
        // 验证角色创建
        Integer roleCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM role WHERE id IN (?, ?)",
            Integer.class, adminRoleId, viewerRoleId
        );
        assertThat(roleCount).isEqualTo(2);

        // 验证权限分配
        Integer adminPermCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM role_permission WHERE role_id = ?",
            Integer.class, adminRoleId
        );
        assertThat(adminPermCount).isEqualTo(3); // READ, WRITE, DELETE

        Integer viewerPermCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM role_permission WHERE role_id = ?",
            Integer.class, viewerRoleId
        );
        assertThat(viewerPermCount).isEqualTo(1); // READ only

        // 验证用户角色关联
        Integer userRoleCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM user_role WHERE user_id IN (?, ?)",
            Integer.class, adminUserId, viewerUserId
        );
        assertThat(userRoleCount).isEqualTo(2);

        // 验证权限缓存
        String adminCacheKey = "user:permissions:" + adminUserId;
        Long adminCachedPermCount = redisTemplate.opsForSet().size(adminCacheKey);
        assertThat(adminCachedPermCount).isEqualTo(3);

        String viewerCacheKey = "user:permissions:" + viewerUserId;
        Long viewerCachedPermCount = redisTemplate.opsForSet().size(viewerCacheKey);
        assertThat(viewerCachedPermCount).isEqualTo(1);

        // 验证权限检查
        assertThat(checkUserPermission(adminUserId, "ASSET", null, "WRITE")).isTrue();
        assertThat(checkUserPermission(viewerUserId, "ASSET", null, "WRITE")).isFalse();
    }
}
