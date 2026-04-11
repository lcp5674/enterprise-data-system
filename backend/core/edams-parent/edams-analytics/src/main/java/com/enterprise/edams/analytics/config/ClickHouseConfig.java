package com.enterprise.edams.analytics.config;

import com.clickhouse.client.ClickHouseClient;
import com.clickhouse.client.ClickHouseNode;
import com.clickhouse.jdbc.ClickHouseConnection;
import com.clickhouse.jdbc.ClickHouseDataSource;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * ClickHouse配置类
 *
 * @author Architecture Team
 * @version 1.0.0
 */
@Configuration
@ConfigurationProperties(prefix = "clickhouse")
@Data
public class ClickHouseConfig {

    private String url;
    private String username;
    private String password;
    private ConnectionPool connectionPool = new ConnectionPool();

    @Data
    public static class ConnectionPool {
        private int size = 20;
        private int maxSize = 50;
        private int minSize = 5;
        private int connectionTimeout = 30000;
        private int socketTimeout = 300000;
    }

    @Bean
    public DataSource clickHouseDataSource() throws SQLException {
        ClickHouseDataSource dataSource = new ClickHouseDataSource(url);
        return dataSource;
    }
}
