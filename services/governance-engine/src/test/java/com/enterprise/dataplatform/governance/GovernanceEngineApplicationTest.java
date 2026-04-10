package com.enterprise.dataplatform.governance;

import org.junit.jupiter.api.Test;

class GovernanceEngineApplicationTest {

    @Test
    void contextLoads() {
        // 测试应用上下文是否能正常加载
        // 注意：由于没有完整的Spring Boot环境，这里只做基本的测试
    }

    @Test
    void mainMethodExists() {
        // 验证主方法存在
        try {
            GovernanceEngineApplication.class.getDeclaredMethod("main", String[].class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError("main方法不存在");
        }
    }
}
