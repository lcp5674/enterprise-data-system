package com.enterprise.dataplatform.analytics.config;

import com.clickhouse.client.ClickHouseClient;
import com.clickhouse.client.ClickHouseClientBuilder;
import com.clickhouse.client.config.ClickHouseClientOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.time.Duration;

/**
 * ClickHouse configuration for OLAP analytics
 */
@Configuration
public class ClickHouseConfig {

    @Value("${spring.clickhouse.host:localhost}")
    private String host;

    @Value("${spring.clickhouse.port:8123}")
    private int port;

    @Value("${spring.clickhouse.database:edams_analytics}")
    private String database;

    @Value("${spring.clickhouse.username:default}")
    private String username;

    @Value("${spring.clickhouse.password:}")
    private String password;

    @Value("${spring.clickhouse.connection-timeout:10000}")
    private int connectionTimeout;

    @Value("${spring.clickhouse.socket-timeout:30000}")
    private int socketTimeout;

    @Value("${spring.clickhouse.max-connections:50}")
    private int maxConnections;

    /**
     * Configure ClickHouse DataSource using HikariCP
     */
    @Bean
    public DataSource clickHouseDataSource() {
        String jdbcUrl = String.format(
            "jdbc:clickhouse://%s:%d/%s?compress=1&socket_timeout=%d&connection_timeout=%d",
            host, port, database, socketTimeout, connectionTimeout
        );

        return DataSourceBuilder.create()
                .url(jdbcUrl)
                .username(username)
                .password(password)
                .driverClassName("com.clickhouse.jdbc.ClickHouseDriver")
                .type(org.apache.commons.dbcp2.BasicDataSource.class)
                .build();
    }

    /**
     * Configure JdbcTemplate for ClickHouse operations
     */
    @Bean
    public JdbcTemplate clickHouseJdbcTemplate(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.setQueryTimeout(300);
        return jdbcTemplate;
    }

    /**
     * Configure ClickHouse HTTP client for advanced operations
     */
    @Bean
    public ClickHouseClient clickHouseClient() {
        return ClickHouseClient.builder()
                .nodeHost(host)
                .nodePort(port)
                .database(database)
                .option(ClickHouseClientOption.CONNECT_TIMEOUT, Duration.ofMillis(connectionTimeout))
                .option(ClickHouseClientOption.SOCKET_TIMEOUT, Duration.ofMillis(socketTimeout))
                .option(ClickHouseClientOption.MAX_COMPRESSED_SIZE, 10 * 1024 * 1024)
                .build();
    }
}
