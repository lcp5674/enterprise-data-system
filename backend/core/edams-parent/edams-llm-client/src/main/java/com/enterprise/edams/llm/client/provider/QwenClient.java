package com.enterprise.edams.llm.client.provider;

import com.enterprise.edams.llm.client.LLMClient;
import com.enterprise.edams.llm.client.config.LLMProperties;
import com.enterprise.edams.llm.client.exception.LLMException;
import com.enterprise.edams.llm.client.exception.LLMRateLimitException;
import com.enterprise.edams.llm.client.exception.LLMTimeoutException;
import com.enterprise.edams.llm.client.model.ChatMessage;
import com.enterprise.edams.llm.client.model.ChatRequest;
import com.enterprise.edams.llm.client.model.LLMResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class QwenClient implements LLMClient {

    private static final String PROVIDER_NAME = "qwen";
    private static final String DEFAULT_MODEL = "qwen-turbo";

    private static final Map<String, String> MODEL_MAPPING = new HashMap<>();

    static {
        MODEL_MAPPING.put("qwen-turbo", "qwen-turbo");
        MODEL_MAPPING.put("qwen-plus", "qwen-plus");
        MODEL_MAPPING.put("qwen-max", "qwen-max");
        MODEL_MAPPING.put("qwen-max-longcontext", "qwen-max-longcontext");
        MODEL_MAPPING.put("qwen-long", "qwen-long");
    }

    private final WebClient webClient;
    private final String apiKey;
    private final String baseUrl;
    private final Long timeoutMs;

    public QwenClient(LLMProperties llmProperties) {
        LLMProperties.ProviderConfig config = llmProperties.getProviders().get(PROVIDER_NAME);

        if (config == null) {
            throw new IllegalStateException("Qwen provider configuration not found");
        }

        this.apiKey = config.getApiKey();
        this.baseUrl = config.getBaseUrl();
        this.timeoutMs = config.getTimeoutMs() != null ? config.getTimeoutMs() : 120000L;

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    @Override
    public LLMResponse chat(ChatRequest request) {
        long startTime = System.currentTimeMillis();
        String model = mapToQwenModel(request.getModel());

        try {
            QwenChatRequest qwenRequest = buildQwenRequest(request, model);

            QwenChatResponse response = webClient.post()
                    .uri("/services/aigc/text-generation/generation")
                    .bodyValue(qwenRequest)
                    .retrieve()
                    .bodyToMono(QwenChatResponse.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            if (response == null) {
                throw new LLMException(PROVIDER_NAME, "通义千问API返回空响应");
            }

            if (!Boolean.TRUE.equals(response.getSuccess()) || response.getCode() != null) {
                throw new LLMException(PROVIDER_NAME,
                        "通义千问API错误: " + (response.getMessage() != null ? response.getMessage() : response.getCode()));
            }

            long latencyMs = System.currentTimeMillis() - startTime;
            return convertToLLMResponse(response, model, latencyMs);

        } catch (WebClientResponseException e) {
            log.error("通义千问API调用失败: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            handleHttpError(e);
            throw new LLMException(PROVIDER_NAME, "通义千问API调用失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("通义千问API调用异常: {}", e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                throw new LLMTimeoutException(PROVIDER_NAME, timeoutMs, e);
            }
            throw new LLMException(PROVIDER_NAME, "通义千问API调用异常", e);
        }
    }

    @Override
    public Flux<String> streamChat(ChatRequest request) {
        String model = mapToQwenModel(request.getModel());
        QwenChatRequest qwenRequest = buildQwenRequest(request, model);

        Map<String, Object> params = (Map<String, Object>) qwenRequest.getParameters();
        if (params == null) {
            params = new HashMap<>();
        }
        params.put("stream", true);

        return webClient.post()
                .uri("/services/aigc/text-generation/generation")
                .bodyValue(qwenRequest)
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .doOnError(e -> log.error("通义千问流式响应异常: {}", e.getMessage()))
                .onErrorResume(e -> Flux.just("error:" + e.getMessage()));
    }

    @Override
    public List<String> getSupportedModels() {
        return Arrays.asList(
                "qwen-turbo",
                "qwen-plus",
                "qwen-max",
                "qwen-max-longcontext",
                "qwen-long"
        );
    }

    @Override
    public boolean healthCheck() {
        try {
            ChatRequest healthRequest = ChatRequest.builder()
                    .model("qwen-turbo")
                    .prompt("ping")
                    .maxTokens(5)
                    .temperature(0.0)
                    .build();
            chat(healthRequest);
            return true;
        } catch (Exception e) {
            log.warn("Qwen health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getProvider() {
        return PROVIDER_NAME;
    }

    private String mapToQwenModel(String model) {
        if (model == null || model.isBlank()) {
            return DEFAULT_MODEL;
        }
        return MODEL_MAPPING.getOrDefault(model, model);
    }

    private QwenChatRequest buildQwenRequest(ChatRequest request, String model) {
        List<QwenChatRequest.QwenMessage> messages;

        if (request.hasMessages()) {
            messages = request.getMessages().stream()
                    .map(this::convertToQwenMessage)
                    .collect(Collectors.toList());
        } else if (request.getPrompt() != null && !request.getPrompt().isBlank()) {
            messages = List.of(QwenChatRequest.QwenMessage.builder()
                    .role("user")
                    .content(request.getPrompt())
                    .build());
        } else {
            messages = List.of();
        }

        QwenChatRequest.QwenInput input = QwenChatRequest.QwenInput.builder()
                .messages(messages)
                .build();

        Map<String, Object> params = new HashMap<>();
        params.put("temperature", request.getTemperature() != null ? request.getTemperature() : 0.7);
        params.put("max_tokens", request.getMaxTokens() != null ? request.getMaxTokens() : 2000);

        return QwenChatRequest.builder()
                .model(model)
                .input(Map.of("messages", messages))
                .parameters(params)
                .build();
    }

    private QwenChatRequest.QwenMessage convertToQwenMessage(ChatMessage message) {
        String role = message.getRole();
        if ("assistant".equals(role)) {
            role = "assistant";
        } else if ("system".equals(role)) {
            role = "system";
        } else {
            role = "user";
        }

        return QwenChatRequest.QwenMessage.builder()
                .role(role)
                .content(message.getContent())
                .build();
    }

    private LLMResponse convertToLLMResponse(QwenChatResponse response, String model, long latencyMs) {
        String content = "";
        if (response.getOutput() != null) {
            if (response.getOutput().getText() != null) {
                content = response.getOutput().getText();
            } else if (response.getOutput().getChoices() != null && !response.getOutput().getChoices().isEmpty()) {
                content = response.getOutput().getChoices().get(0);
            }
        }

        Integer promptTokens = null;
        Integer completionTokens = null;
        if (response.getUsage() != null) {
            promptTokens = response.getUsage().getInput_tokens();
            completionTokens = response.getUsage().getOutput_tokens();
        }

        return LLMResponse.builder()
                .content(content)
                .model(model)
                .latencyMs(latencyMs)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .success(true)
                .build();
    }

    private void handleHttpError(WebClientResponseException e) {
        if (e.getStatusCode().value() == 429) {
            throw new LLMRateLimitException(PROVIDER_NAME, 60000);
        }
        if (e.getStatusCode().value() == 401) {
            throw new LLMException(PROVIDER_NAME, "通义千问API认证失败，请检查API Key");
        }
    }
}
