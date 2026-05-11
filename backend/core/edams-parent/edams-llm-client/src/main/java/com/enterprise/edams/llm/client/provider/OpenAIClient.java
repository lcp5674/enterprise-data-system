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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
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
public class OpenAIClient implements LLMClient {

    private static final String PROVIDER_NAME = "openai";
    private static final String DEFAULT_MODEL = "gpt-3.5-turbo";

    private final WebClient webClient;
    private final String apiKey;
    private final String baseUrl;
    private final Long timeoutMs;
    private final LLMProperties llmProperties;

    public OpenAIClient(LLMProperties llmProperties) {
        this.llmProperties = llmProperties;
        LLMProperties.ProviderConfig config = llmProperties.getProviders().get(PROVIDER_NAME);

        if (config == null) {
            throw new IllegalStateException("OpenAI provider configuration not found");
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
        String model = request.getModel() != null ? request.getModel() : DEFAULT_MODEL;

        try {
            OpenAIChatRequest openAIRequest = buildOpenAIRequest(request, model);

            OpenAIChatResponse response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(openAIRequest)
                    .retrieve()
                    .bodyToMono(OpenAIChatResponse.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            if (response == null) {
                throw new LLMException(PROVIDER_NAME, "OpenAI API返回空响应");
            }

            if (response.getError() != null) {
                throw new LLMException(PROVIDER_NAME, response.getError());
            }

            long latencyMs = System.currentTimeMillis() - startTime;
            return convertToLLMResponse(response, model, latencyMs);

        } catch (WebClientResponseException e) {
            log.error("OpenAI API调用失败: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            handleHttpError(e);
            throw new LLMException(PROVIDER_NAME, "OpenAI API调用失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("OpenAI API调用异常: {}", e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                throw new LLMTimeoutException(PROVIDER_NAME, timeoutMs, e);
            }
            throw new LLMException(PROVIDER_NAME, "OpenAI API调用异常", e);
        }
    }

    @Override
    public Flux<String> streamChat(ChatRequest request) {
        String model = request.getModel() != null ? request.getModel() : DEFAULT_MODEL;
        OpenAIChatRequest openAIRequest = buildOpenAIRequest(request, model);
        openAIRequest.setStream(true);

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(openAIRequest)
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(Duration.ofMillis(timeoutMs))
                .doOnError(e -> log.error("OpenAI流式响应异常: {}", e.getMessage()))
                .onErrorResume(e -> Flux.just("error:" + e.getMessage()));
    }

    @Override
    public List<String> getSupportedModels() {
        return Arrays.asList(
                "gpt-4",
                "gpt-4-turbo",
                "gpt-4o",
                "gpt-4o-mini",
                "gpt-4-32k",
                "gpt-3.5-turbo",
                "gpt-3.5-turbo-16k"
        );
    }

    @Override
    public boolean healthCheck() {
        try {
            ChatRequest healthRequest = ChatRequest.builder()
                    .model("gpt-3.5-turbo")
                    .prompt("ping")
                    .maxTokens(5)
                    .temperature(0.0)
                    .build();
            chat(healthRequest);
            return true;
        } catch (Exception e) {
            log.warn("OpenAI health check failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getProvider() {
        return PROVIDER_NAME;
    }

    private OpenAIChatRequest buildOpenAIRequest(ChatRequest request, String model) {
        List<OpenAIChatRequest.OpenAIMessage> messages;

        if (request.hasMessages()) {
            messages = request.getMessages().stream()
                    .map(this::convertToOpenAIMessage)
                    .collect(Collectors.toList());
        } else if (request.getPrompt() != null && !request.getPrompt().isBlank()) {
            messages = List.of(OpenAIChatRequest.OpenAIMessage.builder()
                    .role("user")
                    .content(request.getPrompt())
                    .build());
        } else {
            messages = List.of();
        }

        return OpenAIChatRequest.builder()
                .model(model)
                .messages(messages)
                .temperature(request.getTemperature() != null ? request.getTemperature() : 0.7)
                .max_tokens(request.getMaxTokens() != null ? request.getMaxTokens() : 2000)
                .build();
    }

    private OpenAIChatRequest.OpenAIMessage convertToOpenAIMessage(ChatMessage message) {
        return OpenAIChatRequest.OpenAIMessage.builder()
                .role(message.getRole() != null ? message.getRole() : "user")
                .content(message.getContent())
                .name(message.getName())
                .build();
    }

    private LLMResponse convertToLLMResponse(OpenAIChatResponse response, String model, long latencyMs) {
        if (response.getChoices() == null || response.getChoices().isEmpty()) {
            return LLMResponse.failure("响应Choices为空", model);
        }

        OpenAIChatResponse.OpenAIChoice choice = response.getChoices().get(0);
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
            throw new LLMException(PROVIDER_NAME, "OpenAI API认证失败，请检查API Key");
        }
    }
}
