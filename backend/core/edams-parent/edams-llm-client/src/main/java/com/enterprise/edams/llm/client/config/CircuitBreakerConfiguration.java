package com.enterprise.edams.llm.client.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
public class CircuitBreakerConfig {

    private final LLMProperties llmProperties;
    private final Map<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();

    public CircuitBreakerConfig(LLMProperties llmProperties) {
        this.llmProperties = llmProperties;
    }

    @PostConstruct
    public void initCircuitBreakers() {
        Map<String, LLMProperties.ProviderConfig> providers = llmProperties.getProviders();
        if (providers == null) {
            return;
        }

        for (Map.Entry<String, LLMProperties.ProviderConfig> entry : providers.entrySet()) {
            String provider = entry.getKey();
            LLMProperties.ProviderConfig config = entry.getValue();

            if (config.getCircuitBreaker() != null) {
                CircuitBreaker circuitBreaker = createCircuitBreaker(provider, config.getCircuitBreaker());
                circuitBreakers.put(provider, circuitBreaker);
                log.info("初始化CircuitBreaker for provider: {}", provider);
            }
        }
    }

    private CircuitBreaker createCircuitBreaker(String provider, LLMProperties.CircuitBreakerConfig config) {
        CircuitBreakerConfig.CircuitBreakerConfigBuilder builder = io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .failureRateThreshold(config.getFailureRateThreshold())
                .waitDurationInOpenState(Duration.ofMillis(
                        config.getWaitDurationInOpenStateMs() != null ? config.getWaitDurationInOpenStateMs() : 60000L))
                .slidingWindowSize(config.getSlidingWindowSize() != null ? config.getSlidingWindowSize() : 10)
                .minimumNumberOfCalls(config.getMinimumNumberOfCalls() != null ? config.getMinimumNumberOfCalls() : 5)
                .slowCallDurationThreshold(Duration.ofMillis(
                        config.getSlowCallDurationThresholdMs() != null ? config.getSlowCallDurationThresholdMs() : 2000L))
                .permittedNumberOfCallsInHalfOpenState(3)
                .automaticTransitionFromOpenToHalfOpenEnabled(true);

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(builder.build());
        return registry.circuitBreaker(provider);
    }

    public CircuitBreaker getCircuitBreaker(String provider) {
        return circuitBreakers.get(provider);
    }

    public Map<String, CircuitBreaker> getAllCircuitBreakers() {
        return circuitBreakers;
    }

    public boolean isCircuitBreakerOpen(String provider) {
        CircuitBreaker breaker = circuitBreakers.get(provider);
        return breaker != null && breaker.getState() == CircuitBreaker.State.OPEN;
    }
}
