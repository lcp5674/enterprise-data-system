package com.enterprise.dataplatform.lineage.config;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Neo4j configuration
 */
@Configuration
public class Neo4jConfig {

    @Value("${spring.neo4j.uri}")
    private String uri;

    @Value("${spring.neo4j.authentication.username}")
    private String username;

    @Value("${spring.neo4j.authentication.password}")
    private String password;

    @Bean
    public Driver neo4jDriver() {
        return org.neo4j.driver.GraphDatabase.driver(uri,
                org.neo4j.driver.AuthTokens.basic(username, password));
    }

    @Bean
    public SessionConfig sessionConfig() {
        return SessionConfig.builder()
                .withDefaultAccessMode(org.neo4j.driver.AccessMode.READ)
                .build();
    }
}
