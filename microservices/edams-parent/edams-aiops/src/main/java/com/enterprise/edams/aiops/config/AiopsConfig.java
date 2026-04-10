package com.enterprise.edams.aiops.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * AIOps配置类
 * 
 * 配置项:
 * - Redis缓存配置
 * - 异步任务线程池
 * - WebClient for Prometheus API
 * - OpenAPI文档配置
 *
 * @author Backend Team - AIOps
 */
@Configuration
@EnableAsync
public class AiopsConfig {

    @Value("${spring.prometheus.url:http://localhost:9090}")
    private String prometheusUrl;

    /**
     * Redis配置
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 异步任务线程池配置
     * 用于执行AIOps后台分析任务
     */
    @Bean(name = "aiopsTaskExecutor")
    public Executor aiopsTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("aiops-async-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * WebClient配置用于Prometheus API调用
     */
    @Bean
    public WebClient.Builder prometheusWebClientBuilder() {
        return WebClient.builder()
                .baseUrl(prometheusUrl)
                .defaultHeader("Content-Type", "application/json");
    }

    /**
     * RestTemplate配置用于HTTP请求
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * OpenAPI文档配置
     */
    @Bean
    public OpenAPI aiopsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AIOps智能运维服务 API")
                        .description("企业数据资产管理系统AIOps智能运维服务API文档，包含异常检测、容量规划、根因分析、健康评分和告警优化功能")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("AIOps Team")
                                .email("aiops@enterprise.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("/").description("当前服务器"),
                        new Server().url(prometheusUrl).description("Prometheus服务器")
                ));
    }
}
