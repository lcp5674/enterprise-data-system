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
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ZhipuClient implements LLMClient {

    private static final String PROVIDER_NAME = "zhipu";
    private static final String DEFAULT_MODEL = "glm-4-flash";

    private static final Map<String, String> MODEL_MAPPING = new HashMap<>();

    static {
        MODEL_MAPPING.put("glm-4", "glm-4");
        MODEL_MAPPING.put("glm-4-flash", "glm-4-flash");
        MODEL_MAPPING.put("glm-4-plus", "glm-4-plus");
        MODEL_MAPPING.put("glm-3-turbo", "glm-3-turbo");
    }

    private final WebClient webClient;
    private final String apiKey;
    private final String baseUrl;
    private final Long timeoutMs;

    public ZhipuClient(LLMProperties llmProperties) {
        LLMProperties.ProviderConfig config = llmProperties.getProviders().get(PROVIDER_NAME);

        if (config == null) {
            throw new IllegalStateException("Zhipu provider configuration not found");
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
        String model = mapToZhipuModel(request.getModel());

        try {
            ZhipuChatRequest zhipuRequest = buildZhipuRequest(request, model);

            ZhipuChatResponse response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(zhipuRequest)
                    .retrieve()
                    .bodyToMono(ZhipuChatResponse.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            if (response == null) {
                throw new LLMException(PROVIDER_NAME, "智谱API返回空响应");
            }

            if (response.getError() != null) {
                throw new LLMException(PROVIDER_NAME, response.getError());
            }

            long latencyMs = System.currentTimeMillis() - startTime;
            return convertToLLMResponse(response, model, latencyMs);

        } catch (WebClientResponseException e) {
            log.error("智谱API调用失败: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            handleHttpError(e);
            throw new LLMException(PROVIDER_NAME, "智谱API调用失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("智谱API调用异常: {}", e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                throw new LLMTimeoutException(PROVIDER_NAME, timeoutMs, e);
            }
            throw new LLMException(PROVIDER_NAME, "智谱API调用异常", e);
        }
    }

    @Override
    public Flux<String> streamChat(ChatRequest request) {
        String model = mapToZhipuModel(request.getModel());
        ZhipuChatRequest zhipuRequest = buildZhipuRequest(request, model);
        zhipuRequest.setStream(1);

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(zhipuRequest)
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .doOnError(e -> log.error("智谱流式响应异常: {}", e.getMessage()))
                .onErrorResume(e -> Flux.just("error:" + e.getMessage()));
    }

    @Override
    public List<String> getSupportedModels() {
        return Arrays.asList(
                "glm-4",
                "glm-4-flash",
                "glm-4-plus",
                "glm-4v",
                "glm-3-turbo"
        );
    }

    @Override
    public boolean healthCheck() {
        try {
            ChatRequest healthRequest = ChatRequest.builder()
                    .model("glm-3-turbo")
                    .prompt("ping")
                    .maxTokens(5)
                    .temperature(0.0)
                    .build();
            chat(healthRequest);
            return true;
        } catch (Exception e) {
            log.warn("Zhipu health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getProvider() {
        return PROVIDER_NAME;
    }

    private String mapToZhipuModel(String model) {
        if (model == null || model.isBlank()) {
            return DEFAULT_MODEL;
        }
        return MODEL_MAPPING.getOrDefault(model, model);
    }

    private ZhipuChatRequest buildZhipuRequest(ChatRequest request, String model) {
        List<ZhipuChatRequest.ZhipuMessage> messages;

        if (request.hasMessages()) {
            messages = request.getMessages().stream()
                    .map(this::convertToZhipuMessage)
                    .collect(Collectors.toList());
        } else if (request.getPrompt() != null && !request.getPrompt().isBlank()) {
            messages = List.of(ZhipuChatRequest.ZhipuMessage.builder()
                    .role("user")
                    .content(request.getPrompt())
                    .build());
        } else {
            messages = List.of();
        }

        ZhipuChatRequest.ZhipuRequestBuilder builder = ZhipuChatRequest.builder()
                .model(model)
                .messages(messages)
                .request_id(UUID.randomUUID().toString());

        if (request.getTemperature() != null) {
            builder.temperature(request.getTemperature());
        }

        if (request.getMaxTokens() != null) {
            builder.max_tokens(request.getMaxTokens());
        }

        return builder.build();
    }

    private ZhipuChatRequest.ZhipuMessage convertToZhipuMessage(ChatMessage message) {
        String role = message.getRole();
        if ("assistant".equals(role)) {
            role = "assistant";
        } else if ("system".equals(role)) {
            role = "system";
        } else {
            role = "user";
        }

        return ZhipuChatRequest.ZhipuMessage.builder()
                .role(role)
                .content(message.getContent())
                .build();
    }

    private LLMResponse convertToLLMResponse(ZhipuChatResponse response, String model, long latencyMs) {
        if (response.getChoices() == null || response.getChoices().isEmpty()) {
            return LLMResponse.failure("响应Choices为空", model);
        }

        ZhipuChatResponse.ZhipuChoice choice = response.getChoices().get(0);
        String content = choice.getMessage() != null ? choice.getMessage().getContent() : "";

        Integer promptTokens = null;
        Integer completionTokens = null;
        if (response.getUsage() != null) {
            promptTokens = response.getUsage().getPrompt_tokens();
            completionTokens = response.getUsage().getCompletion_tokens();
        }

        return LLMResponse.builder()
                .content(content)
                .model(model)
                .finishReason(choice.getFinish_reason())
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .latencyMs(latencyMs)
                .success(true)
                .build();
    }

    private void handleHttpError(WebClientResponseException e) {
        if (e.getStatusCode().value() == 429) {
            throw new LLMRateLimitException(PROVIDER_NAME, 60000);
        }
        if (e.getStatusCode().value() == 401) {
            throw new LLMException(PROVIDER_NAME, "智谱API认证失败，请检查API Key");
        }
    }
}
