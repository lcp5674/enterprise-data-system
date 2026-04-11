package com.enterprise.edams.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Gateway应用上下文测试
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.cloud.gateway.routes[0].id=edams-auth",
        "spring.cloud.gateway.routes[0].uri=lb://edams-auth",
        "spring.cloud.gateway.routes[0].predicates[0]=Path=/api/v1/auth/**",
        "spring.cloud.gateway.routes[0].filters[0]=StripPrefix=1",
        "spring.cloud.gateway.routes[1].id=edams-asset",
        "spring.cloud.gateway.routes[1].uri=lb://edams-asset",
        "spring.cloud.gateway.routes[1].predicates[0]=Path=/api/v1/assets/**",
        "spring.cloud.gateway.routes[1].filters[0]=StripPrefix=1"
})
class GatewayApplicationTest {

    @Test
    void contextLoads() {
        // 测试应用上下文是否正常加载
    }
}
