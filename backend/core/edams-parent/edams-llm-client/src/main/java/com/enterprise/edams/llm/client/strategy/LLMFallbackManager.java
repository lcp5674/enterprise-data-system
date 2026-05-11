package com.enterprise.edams.llm.client.strategy;

import com.enterprise.edams.llm.client.LLMClient;
import com.enterprise.edams.llm.client.config.CircuitBreakerConfig;
import com.enterprise.edams.llm.client.config.LLMProperties;
import com.enterprise.edams.llm.client.exception.LLMException;
import com.enterprise.edams.llm.client.exception.LLMAvailabilityException;
import com.enterprise.edams.llm.client.model.ChatRequest;
import com.enterprise.edams.llm.client.model.LLMResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class LLMFallbackManager {

    private final Map<String, LLMClient> clients;
    private final LLMProperties llmProperties;
    private final CircuitBreakerConfig circuitBreakerConfig;

    public LLMFallbackManager(List<LLMClient> clientList, LLMProperties llmProperties,
                              CircuitBreakerConfig circuitBreakerConfig) {
        this.clients = new ConcurrentHashMap<>();
        for (LLMClient client : clientList) {
            this.clients.put(client.getProvider(), client);
        }
        this.llmProperties = llmProperties;
        this.circuitBreakerConfig = circuitBreakerConfig;
    }

    public LLMResponse chatWithFallback(ChatRequest request) {
        List<String> providers = getProviderPriority();

        LLMException lastException = null;

        for (String provider : providers) {
            if (!isProviderEnabled(provider)) {
                log.debug("Provider {} 未启用，跳过", provider);
                continue;
            }

            if (isCircuitBreakerOpen(provider)) {
                log.warn("Provider {} 的熔断器已打开，跳过", provider);
                continue;
            }

            LLMClient client = clients.get(provider);
            if (client == null) {
                log.warn("未找到Provider: {}", provider);
                continue;
            }

            try {
                log.info("尝试调用Provider: {}", provider);
                LLMResponse response = client.chat(request);

                if (response != null && Boolean.TRUE.equals(response.getSuccess())) {
                    log.info("Provider {} 调用成功", provider);
                    return response;
                }

                if (response != null && response.getErrorMessage() != null) {
                    log.warn("Provider {} 返回错误: {}", provider, response.getErrorMessage());
                    lastException = new LLMException(provider, response.getErrorMessage());
                }

            } catch (Exception e) {
                log.warn("Provider {} 调用失败: {}", provider, e.getMessage());
                lastException = new LLMException(provider, "Provider调用失败", e);

                if (e instanceof LLMException llmEx) {
                    if (llmEx.getErrorCode() != null && "RATE_LIMIT".equals(llmEx.getErrorCode())) {
                        log.warn("Provider {} 触发限流，继续尝试其他Provider", provider);
                    }
                }
            }
        }

        String errorMsg = String.format("所有LLM Provider都不可用，已尝试: %s",
                providers.stream().filter(clients::containsKey).collect(Collectors.joining(", ")));
        log.error(errorMsg);

        if (lastException != null) {
            throw lastException;
        }
        throw new LLMAvailabilityException("all");
    }

    public LLMResponse chatWithSpecificProvider(ChatRequest request, String provider) {
        LLMClient client = clients.get(provider);
        if (client == null) {
            throw new LLMException(provider, "未知的Provider: " + provider);
        }

        if (!client.healthCheck()) {
            throw new LLMAvailabilityException(provider);
        }

        return client.chat(request);
    }

    public List<String> getAvailableProviders() {
        return clients.values().stream()
                .filter(client -> isProviderEnabled(client.getProvider()))
                .filter(client -> !isCircuitBreakerOpen(client.getProvider()))
                .filter(LLMClient::healthCheck)
                .map(LLMClient::getProvider)
                .collect(Collectors.toList());
    }

    public Map<String, ProviderStatus> getProviderStatuses() {
        Map<String, ProviderStatus> statuses = new HashMap<>();

        for (Map.Entry<String, LLMClient> entry : clients.entrySet()) {
            String provider = entry.getKey();
            LLMClient client = entry.getValue();

            ProviderStatus status = new ProviderStatus();
            status.setProvider(provider);
            status.setEnabled(isProviderEnabled(provider));
            status.setCircuitBreakerOpen(isCircuitBreakerOpen(provider));

            try {
                status.setHealthy(client.healthCheck());
            } catch (Exception e) {
                status.setHealthy(false);
                status.setErrorMessage(e.getMessage());
            }

            statuses.put(provider, status);
        }

        return statuses;
    }

    private List<String> getProviderPriority() {
        if (llmProperties.getFallback() != null &&
                llmProperties.getFallback().getPriority() != null &&
                !llmProperties.getFallback().getPriority().isEmpty()) {
            return llmProperties.getFallback().getPriority();
        }

        return List.of("openai", "qwen", "zhipu");
    }

    private boolean isProviderEnabled(String provider) {
        if (llmProperties.getProviders() == null) {
            return true;
        }

        LLMProperties.ProviderConfig config = llmProperties.getProviders().get(provider);
        return config == null || config.isEnabled();
    }

    private boolean isCircuitBreakerOpen(String provider) {
        return circuitBreakerConfig.isCircuitBreakerOpen(provider);
    }

    @lombok.Data
    public static class ProviderStatus {
        private String provider;
        private boolean enabled;
        private boolean healthy;
        private boolean circuitBreakerOpen;
        private String errorMessage;
    }
}
