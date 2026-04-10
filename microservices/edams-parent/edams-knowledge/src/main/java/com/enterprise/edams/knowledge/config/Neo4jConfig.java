package com.enterprise.edams.knowledge.config;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.core.DatabaseSelectionProvider;
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Neo4j配置类
 *
 * @author Knowledge Team
 * @version 1.0.0
 */
@Configuration
@EnableTransactionManagement
public class Neo4jConfig {

    /**
     * Neo4j事务管理器
     *
     * @param driver Neo4j驱动
     * @param databaseSelectionProvider 数据库选择提供者
     * @return 事务管理器
     */
    @Bean
    public PlatformTransactionManager neo4jTransactionManager(
            Driver driver,
            DatabaseSelectionProvider databaseSelectionProvider) {
        return new Neo4jTransactionManager(driver, databaseSelectionProvider);
    }

    /**
     * 获取Neo4j会话
     *
     * @param driver Neo4j驱动
     * @return Neo4j会话
     */
    @Bean
    public Session session(Driver driver) {
        return driver.session();
    }

    /**
     * 获取带数据库选择的会话配置
     *
     * @param driver Neo4j驱动
     * @param databaseSelectionProvider 数据库选择提供者
     * @return 会话配置构建器
     */
    @Bean
    public SessionConfig.Builder sessionConfigBuilder(
            Driver driver,
            DatabaseSelectionProvider databaseSelectionProvider) {
        return SessionConfig.builder()
                .withDatabase(databaseSelectionProvider.getDatabaseSelection().getValue());
    }
}
