package com.enterprise.dataplatform.analytics.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * ClickHouse Health Indicator for Spring Boot Actuator
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClickHouseHealthIndicator implements HealthIndicator {

    private final JdbcTemplate clickHouseJdbcTemplate;

    private static final String HEALTH_CHECK_SQL = "SELECT 1";

    @Override
    public Health health() {
        try {
            Integer result = clickHouseJdbcTemplate.queryForObject(HEALTH_CHECK_SQL, Integer.class);
            if (result != null && result == 1) {
                return Health.up()
                        .withDetail("database", "ClickHouse")
                        .withDetail("status", "Connected")
                        .build();
            }
            return Health.down()
                    .withDetail("error", "Health check returned unexpected result")
                    .build();
        } catch (Exception e) {
            log.error("ClickHouse health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("database", "ClickHouse")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
