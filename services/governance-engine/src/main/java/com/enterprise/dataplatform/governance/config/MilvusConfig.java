package com.enterprise.dataplatform.governance.config;

import io.milvus.client.MilvusClient;
import io.milvus.param.ConnectParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MilvusConfig {

    @Value("${milvus.host:localhost}")
    private String host;

    @Value("${milvus.port:19530}")
    private Integer port;

    @Value("${milvus.user:}")
    private String user;

    @Value("${milvus.password:}")
    private String password;

    @Value("${milvus.collection.governance:governance_recommendations}")
    private String governanceCollection;

    @Bean
    public MilvusClient milvusClient() {
        log.info("初始化Milvus客户端: host={}, port={}", host, port);
        
        ConnectParam.Builder builder = ConnectParam.newBuilder()
                .withHost(host)
                .withPort(port);

        if (user != null && !user.isEmpty()) {
            builder.withAuthorization(user, password);
        }

        return new MilvusClient(builder.build());
    }

    @Bean
    public String governanceCollectionName() {
        return governanceCollection;
    }
}
