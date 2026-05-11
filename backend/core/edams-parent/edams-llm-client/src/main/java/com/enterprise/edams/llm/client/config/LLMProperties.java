package com.enterprise.edams.llm.client.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "llm")
public class LLMProperties {

    private Map<String, ProviderConfig> providers;
    private FallbackConfig fallback;

    @Data
    public static class ProviderConfig {
        private boolean enabled = true;
        private String apiKey;
        private String baseUrl;
        private List<String> models;
        private Long timeoutMs = 120000L;
        private Integer maxRetries = 3;
        private CircuitBreakerConfig circuitBreaker;
    }

    @Data
    public static class CircuitBreakerConfig {
        private Float failureRateThreshold = 50f;
        private Long waitDurationInOpenStateMs = 60000L;
        private Integer slidingWindowSize = 10;
        private Integer minimumNumberOfCalls = 5;
        private Long slowCallDurationThresholdMs = 2000L;
    }

    @Data
    public static class FallbackConfig {
        private boolean enabled = true;
        private String strategy = "priority";
        private List<String> priority;
    }
}
