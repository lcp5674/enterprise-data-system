package com.enterprise.edams.gateway.route;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Gateway路由配置测试
 * 验证路由是否正确配置
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        // 配置测试路由
        "spring.cloud.gateway.routes[0].id=edams-auth",
        "spring.cloud.gateway.routes[0].uri=lb://edams-auth",
        "spring.cloud.gateway.routes[0].predicates[0]=Path=/api/v1/auth/**",
        "spring.cloud.gateway.routes[0].filters[0]=StripPrefix=1",

        "spring.cloud.gateway.routes[1].id=edams-asset",
        "spring.cloud.gateway.routes[1].uri=lb://edams-asset",
        "spring.cloud.gateway.routes[1].predicates[0]=Path=/api/v1/assets/**",
        "spring.cloud.gateway.routes[1].filters[0]=StripPrefix=1",

        "spring.cloud.gateway.routes[2].id=edams-admin",
        "spring.cloud.gateway.routes[2].uri=lb://edams-admin",
        "spring.cloud.gateway.routes[2].predicates[0]=Path=/api/v1/admin/**",
        "spring.cloud.gateway.routes[2].filters[0]=StripPrefix=1",

        "spring.cloud.gateway.routes[3].id=edams-lineage",
        "spring.cloud.gateway.routes[3].uri=lb://edams-lineage",
        "spring.cloud.gateway.routes[3].predicates[0]=Path=/api/v1/lineage/**",
        "spring.cloud.gateway.routes[3].filters[0]=StripPrefix=1",

        "spring.cloud.gateway.routes[4].id=edams-quality",
        "spring.cloud.gateway.routes[4].uri=lb://edams-quality",
        "spring.cloud.gateway.routes[4].predicates[0]=Path=/api/v1/quality/**",
        "spring.cloud.gateway.routes[4].filters[0]=StripPrefix=1",

        // 禁用Nacos
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.cloud.nacos.config.enabled=false"
})
@DisplayName("网关路由测试")
class GatewayRouteTest {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    @DisplayName("路由定位器加载成功")
    void testRouteLocatorLoaded() {
        assertThat(routeLocator).isNotNull();
    }

    @Test
    @DisplayName("验证认证路由配置")
    void testAuthRouteExists() {
        long authRouteCount = routeLocator.getRoutes()
                .filter(route -> route.getId().equals("edams-auth"))
                .count();
        assertThat(authRouteCount).isEqualTo(1);
    }

    @Test
    @DisplayName("验证资产管理路由配置")
    void testAssetRouteExists() {
        long assetRouteCount = routeLocator.getRoutes()
                .filter(route -> route.getId().equals("edams-asset"))
                .count();
        assertThat(assetRouteCount).isEqualTo(1);
    }

    @Test
    @DisplayName("验证管理员路由配置")
    void testAdminRouteExists() {
        long adminRouteCount = routeLocator.getRoutes()
                .filter(route -> route.getId().equals("edams-admin"))
                .count();
        assertThat(adminRouteCount).isEqualTo(1);
    }

    @Test
    @DisplayName("验证血缘管理路由配置")
    void testLineageRouteExists() {
        long lineageRouteCount = routeLocator.getRoutes()
                .filter(route -> route.getId().equals("edams-lineage"))
                .count();
        assertThat(lineageRouteCount).isEqualTo(1);
    }

    @Test
    @DisplayName("验证质量管理路由配置")
    void testQualityRouteExists() {
        long qualityRouteCount = routeLocator.getRoutes()
                .filter(route -> route.getId().equals("edams-quality"))
                .count();
        assertThat(qualityRouteCount).isEqualTo(1);
    }

    @Test
    @DisplayName("获取所有路由ID")
    void testGetAllRouteIds() {
        var routeIds = routeLocator.getRoutes()
                .map(route -> route.getId())
                .collect(Collectors.toList());

        assertThat(routeIds).isNotEmpty();
        assertThat(routeIds).contains("edams-auth", "edams-asset", "edams-admin");
    }

    @Test
    @DisplayName("路由总数验证")
    void testTotalRouteCount() {
        long routeCount = routeLocator.getRoutes().count();
        // 至少应该有5个核心路由
        assertThat(routeCount).isGreaterThanOrEqualTo(5);
    }
}
