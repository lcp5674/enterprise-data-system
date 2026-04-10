package com.enterprise.dataplatform.standard.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI文档配置
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8084}")
    private int serverPort;

    @Bean
    public OpenAPI standardServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("数据标准服务 API")
                        .description("企业数据资产管理系统的数据标准管理服务，提供数据标准的定义、映射、合规检查等功能")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("数据平台团队")
                                .email("dataplatform@enterprise.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("本地环境"),
                        new Server().url("http://standard-service:8084").description("服务环境")
                ));
    }
}
