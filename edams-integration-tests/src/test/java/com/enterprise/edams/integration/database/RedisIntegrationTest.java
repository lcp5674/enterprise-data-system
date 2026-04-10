package com.enterprise.edams.integration.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Redis缓存集成测试
 * 测试任务: INT-REDIS-001
 */
@Testcontainers
@SpringBootTest(classes = RedisIntegrationTest.TestConfig.class)
@ActiveProfiles("test")
@DisplayName("Redis缓存集成测试")
class RedisIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
        registry.add("spring.redis.timeout", () -> "2000ms");
        registry.add("spring.redis.lettuce.pool.max-active", () -> "8");
        registry.add("spring.redis.lettuce.pool.max-idle", () -> "8");
        registry.add("spring.redis.lettuce.pool.min-idle", () -> "0");
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // 清理Redis数据
        stringRedisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
    }

    @Test
    @DisplayName("测试Redis连接")
    void testRedisConnection() {
        String pong = stringRedisTemplate.getConnectionFactory().getConnection().ping();
        assertThat(pong).isEqualTo("PONG");
    }

    @Test
    @DisplayName("测试String类型缓存 - 基本CRUD")
    void testStringCacheCrud() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        // Given
        String key = "test:key";
        String value = "test value";

        // When - Create
        ops.set(key, value);

        // Then - Read
        String retrieved = ops.get(key);
        assertThat(retrieved).isEqualTo(value);

        // When - Update
        String newValue = "updated value";
        ops.set(key, newValue);

        // Then
        assertThat(ops.get(key)).isEqualTo(newValue);

        // When - Delete
        stringRedisTemplate.delete(key);

        // Then
        assertThat(ops.get(key)).isNull();
    }

    @Test
    @DisplayName("测试String类型缓存 - TTL过期")
    void testStringCacheTtl() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        // Given
        String key = "test:ttl";
        String value = "temporary value";

        // When - 设置2秒过期时间
        ops.set(key, value, Duration.ofSeconds(2));

        // Then - 立即读取应该存在
        assertThat(ops.get(key)).isEqualTo(value);

        // Then - 等待3秒后应该过期
        await().atMost(5, TimeUnit.SECONDS).until(() -> ops.get(key) == null);
    }

    @Test
    @DisplayName("测试Hash类型缓存")
    void testHashCache() {
        HashOperations<String, String, String> ops = stringRedisTemplate.opsForHash();

        // Given
        String key = "user:profile:1001";
        Map<String, String> userProfile = Map.of(
                "id", "1001",
                "username", "john_doe",
                "email", "john@example.com",
                "role", "ADMIN"
        );

        // When - 批量插入Hash
        ops.putAll(key, userProfile);

        // Then - 验证插入
        Map<String, String> retrieved = ops.entries(key);
        assertThat(retrieved).isEqualTo(userProfile);

        // Then - 验证单个字段
        assertThat(ops.get(key, "username")).isEqualTo("john_doe");
        assertThat(ops.get(key, "role")).isEqualTo("ADMIN");

        // When - 更新字段
        ops.put(key, "email", "john.doe@newdomain.com");

        // Then
        assertThat(ops.get(key, "email")).isEqualTo("john.doe@newdomain.com");

        // When - 删除字段
        ops.delete(key, "role");

        // Then
        assertThat(ops.hasKey(key, "role")).isFalse();
    }

    @Test
    @DisplayName("测试List类型缓存")
    void testListCache() {
        ListOperations<String, String> ops = stringRedisTemplate.opsForList();

        // Given
        String key = "recent:activities";

        // When - 从左侧插入（最新在前）
        ops.leftPush(key, "Activity 3");
        ops.leftPush(key, "Activity 2");
        ops.leftPush(key, "Activity 1");

        // Then - 验证列表内容
        List<String> activities = ops.range(key, 0, -1);
        assertThat(activities).containsExactly("Activity 1", "Activity 2", "Activity 3");

        // When - 从右侧弹出（最旧）
        String oldest = ops.rightPop(key);

        // Then
        assertThat(oldest).isEqualTo("Activity 3");
        assertThat(ops.size(key)).isEqualTo(2);

        // When - 限制列表长度（保留最新的N个）
        for (int i = 4; i <= 10; i++) {
            ops.leftPush(key, "Activity " + i);
        }
        ops.trim(key, 0, 4); // 只保留前5个

        // Then
        assertThat(ops.size(key)).isEqualTo(5);
    }

    @Test
    @DisplayName("测试Set类型缓存")
    void testSetCache() {
        SetOperations<String, String> ops = stringRedisTemplate.opsForSet();

        // Given
        String key1 = "user:roles:1001";
        String key2 = "user:roles:1002";

        // When
        ops.add(key1, "ADMIN", "EDITOR", "VIEWER");
        ops.add(key2, "EDITOR", "VIEWER", "GUEST");

        // Then - 验证成员
        assertThat(ops.isMember(key1, "ADMIN")).isTrue();
        assertThat(ops.isMember(key2, "ADMIN")).isFalse();

        // Then - 交集
        Set<String> intersection = ops.intersect(key1, key2);
        assertThat(intersection).containsExactlyInAnyOrder("EDITOR", "VIEWER");

        // Then - 并集
        Set<String> union = ops.union(key1, key2);
        assertThat(union).containsExactlyInAnyOrder("ADMIN", "EDITOR", "VIEWER", "GUEST");

        // Then - 差集
        Set<String> difference = ops.difference(key1, key2);
        assertThat(difference).containsExactly("ADMIN");
    }

    @Test
    @DisplayName("测试Token缓存 - 存储和验证")
    void testTokenCache() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        // Given - 模拟登录Token
        String userId = "user_1001";
        String token = UUID.randomUUID().toString();
        String tokenKey = "auth:token:" + token;
        String userTokenKey = "auth:user:tokens:" + userId;

        // When - 存储Token（30分钟过期）
        ops.set(tokenKey, userId, Duration.ofMinutes(30));
        stringRedisTemplate.opsForSet().add(userTokenKey, token);

        // Then - 验证Token
        String storedUserId = ops.get(tokenKey);
        assertThat(storedUserId).isEqualTo(userId);

        // Then - 验证用户Token集合
        Set<String> userTokens = stringRedisTemplate.opsForSet().members(userTokenKey);
        assertThat(userTokens).contains(token);

        // When - 模拟登出（删除Token）
        stringRedisTemplate.delete(tokenKey);
        stringRedisTemplate.opsForSet().remove(userTokenKey, token);

        // Then - 验证Token已删除
        assertThat(ops.get(tokenKey)).isNull();
    }

    @Test
    @DisplayName("测试Token刷新机制")
    void testTokenRefresh() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();

        // Given
        String token = "refresh_token_123";
        String tokenKey = "auth:refresh:" + token;

        // When - 设置较短的过期时间（5秒）
        ops.set(tokenKey, "user_1001", Duration.ofSeconds(5));

        // Then - 立即获取剩余时间
        Long ttl1 = stringRedisTemplate.getExpire(tokenKey, TimeUnit.SECONDS);
        assertThat(ttl1).isGreaterThan(0).isLessThanOrEqualTo(5);

        // When - 刷新Token（延长过期时间）
        stringRedisTemplate.expire(tokenKey, Duration.ofMinutes(30));

        // Then - 验证过期时间已延长
        Long ttl2 = stringRedisTemplate.getExpire(tokenKey, TimeUnit.SECONDS);
        assertThat(ttl2).isGreaterThan(60); // 应该大于1分钟
    }

    @Test
    @DisplayName("测试分布式锁 - 基本获取和释放")
    void testDistributedLock() {
        String lockKey = "lock:resource:123";
        String lockValue = UUID.randomUUID().toString();

        // When - 尝试获取锁（10秒过期）
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, Duration.ofSeconds(10));

        // Then
        assertThat(acquired).isTrue();

        // When - 再次尝试获取锁（应该失败）
        Boolean acquiredAgain = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "other_value", Duration.ofSeconds(10));

        // Then
        assertThat(acquiredAgain).isFalse();

        // When - 使用Lua脚本安全释放锁（只有持有锁的才能释放）
        String luaScript = """
                if redis.call('get', KEYS[1]) == ARGV[1] then
                    return redis.call('del', KEYS[1])
                else
                    return 0
                end
                """;
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(luaScript);
        redisScript.setResultType(Long.class);

        Long result = stringRedisTemplate.execute(redisScript, List.of(lockKey), lockValue);

        // Then - 应该成功删除
        assertThat(result).isEqualTo(1);

        // Then - 锁应该已释放
        assertThat(stringRedisTemplate.hasKey(lockKey)).isFalse();
    }

    @Test
    @DisplayName("测试分布式锁 - 超时机制")
    void testDistributedLockTimeout() {
        String lockKey = "lock:timeout:test";
        String lockValue = "holder";

        // When - 获取3秒过期的锁
        stringRedisTemplate.opsForValue().set(lockKey, lockValue, Duration.ofSeconds(3));

        // Then - 立即检查应该存在
        assertThat(stringRedisTemplate.hasKey(lockKey)).isTrue();

        // Then - 等待4秒后应该自动过期
        await().atMost(5, TimeUnit.SECONDS).until(() -> !stringRedisTemplate.hasKey(lockKey));

        // When - 过期后其他客户端可以获取锁
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "new_holder", Duration.ofSeconds(10));

        // Then
        assertThat(acquired).isTrue();
    }

    @Test
    @DisplayName("测试缓存对象序列化")
    void testCacheObjectSerialization() throws Exception {
        // Given - 配置序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        // Given - 创建一个复杂对象
        UserSession session = new UserSession();
        session.setUserId("user_1001");
        session.setUsername("john_doe");
        session.setRoles(List.of("ADMIN", "EDITOR"));
        session.setPermissions(Map.of("read", true, "write", true));

        String key = "session:user_1001";
        String jsonValue = objectMapper.writeValueAsString(session);

        // When - 存储JSON
        redisTemplate.opsForValue().set(key, jsonValue, Duration.ofMinutes(30));

        // Then - 读取并反序列化
        String storedJson = (String) redisTemplate.opsForValue().get(key);
        UserSession retrieved = objectMapper.readValue(storedJson, UserSession.class);

        assertThat(retrieved.getUserId()).isEqualTo(session.getUserId());
        assertThat(retrieved.getUsername()).isEqualTo(session.getUsername());
        assertThat(retrieved.getRoles()).containsExactlyElementsOf(session.getRoles());
    }

    @Test
    @DisplayName("测试批量操作")
    void testBatchOperations() {
        // Given
        Map<String, String> batchData = Map.of(
                "batch:key:1", "value1",
                "batch:key:2", "value2",
                "batch:key:3", "value3",
                "batch:key:4", "value4",
                "batch:key:5", "value5"
        );

        // When - 批量设置
        stringRedisTemplate.opsForValue().multiSet(batchData);

        // Then - 批量获取
        List<String> keys = List.of("batch:key:1", "batch:key:2", "batch:key:3", "batch:key:4", "batch:key:5");
        List<String> values = stringRedisTemplate.opsForValue().multiGet(keys);

        assertThat(values).containsExactly("value1", "value2", "value3", "value4", "value5");

        // When - 批量删除
        Long deletedCount = stringRedisTemplate.delete(keys);

        // Then
        assertThat(deletedCount).isEqualTo(5);
    }

    @Test
    @DisplayName("测试缓存穿透保护 - 布隆过滤器模式")
    void testCachePenetrationProtection() {
        // Given - 模拟布隆过滤器检查
        String bloomFilterKey = "bloom:users";
        String userId = "user_9999";

        // When - 添加用户到布隆过滤器
        stringRedisTemplate.opsForValue().set(bloomFilterKey + ":" + userId, "1");

        // Then - 检查用户是否存在
        Boolean exists = stringRedisTemplate.hasKey(bloomFilterKey + ":" + userId);
        assertThat(exists).isTrue();

        // When - 检查不存在的用户
        Boolean notExists = stringRedisTemplate.hasKey(bloomFilterKey + ":user_nonexistent");
        assertThat(notExists).isFalse();

        // 实际应用中，布隆过滤器返回false则直接返回null，避免查询数据库
    }

    @Test
    @DisplayName("测试缓存雪崩保护 - 随机过期时间")
    void testCacheAvalancheProtection() {
        // Given - 模拟大量缓存同时设置
        for (int i = 0; i < 100; i++) {
            String key = "cache:item:" + i;
            // 设置基础过期时间30分钟 + 随机0-300秒
            long randomOffset = (long) (Math.random() * 300);
            Duration ttl = Duration.ofMinutes(30).plusSeconds(randomOffset);
            stringRedisTemplate.opsForValue().set(key, "value" + i, ttl);
        }

        // Then - 验证所有缓存都存在
        for (int i = 0; i < 100; i++) {
            String key = "cache:item:" + i;
            assertThat(stringRedisTemplate.hasKey(key)).isTrue();
        }
    }

    // Helper class for testing
    static class UserSession {
        private String userId;
        private String username;
        private List<String> roles;
        private Map<String, Boolean> permissions;

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }
        public Map<String, Boolean> getPermissions() { return permissions; }
        public void setPermissions(Map<String, Boolean> permissions) { this.permissions = permissions; }
    }

    // Test configuration
    @org.springframework.boot.test.context.TestConfiguration
    static class TestConfig {
        // 配置将在主配置中处理
    }
}
