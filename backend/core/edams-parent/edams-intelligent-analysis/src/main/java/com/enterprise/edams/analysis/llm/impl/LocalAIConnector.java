package com.enterprise.edams.analysis.llm.impl;

import com.enterprise.edams.analysis.llm.LLMConnector;
import com.enterprise.edams.analysis.llm.LLMRequest;
import com.enterprise.edams.analysis.llm.LLMResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class LocalAIConnector implements LLMConnector {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private String baseUrl;

    public LocalAIConnector() {
        this.objectMapper = new ObjectMapper();
        this.webClient = WebClient.builder()
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public LLMResponse generate(LLMRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            String apiUrl = baseUrl + "/v1/completions";

            String requestBody = objectMapper.writeValueAsString(new Object() {
                public String model = request.getModel();
                public String prompt = request.getPrompt();
                public int max_tokens = request.getMaxTokens() != null ? request.getMaxTokens() : 4096;
                public double temperature = request.getTemperature() != null ? request.getTemperature() : 0.7;
                public String[] stop = request.getStop();
            });

            String responseBody = webClient.post()
                    .uri(apiUrl)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode responseJson = objectMapper.readTree(responseBody);

            if (responseJson.has("error")) {
                return LLMResponse.builder()
                        .success(false)
                        .errorMessage(responseJson.get("error").asText())
                        .responseTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }

            String content = "";
            int promptTokens = 0;
            int completionTokens = 0;

            if (responseJson.has("choices") && responseJson.get("choices").size() > 0) {
                content = responseJson.get("choices").get(0).has("text")
                        ? responseJson.get("choices").get(0).get("text").asText()
                        : responseJson.get("choices").get(0).has("message")
                                ? responseJson.get("choices").get(0).get("message").get("content").asText()
                                : "";
            }

            if (responseJson.has("usage")) {
                promptTokens = responseJson.get("usage").has("prompt_tokens")
                        ? responseJson.get("usage").get("prompt_tokens").asInt() : 0;
                completionTokens = responseJson.get("usage").has("completion_tokens")
                        ? responseJson.get("usage").get("completion_tokens").asInt() : 0;
            }

            return LLMResponse.builder()
                    .content(content)
                    .finishReason("stop")
                    .promptTokens(promptTokens)
                    .completionTokens(completionTokens)
                    .totalTokens(promptTokens + completionTokens)
                    .success(true)
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .build();

        } catch (Exception e) {
            log.error("LocalAI generate failed: {}", e.getMessage(), e);
            return LLMResponse.builder()
                    .success(false)
                    .errorMessage(e.getMessage())
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    @Override
    public boolean testConnection() {
        try {
            String modelsUrl = baseUrl + "/v1/models";
            webClient.get()
                    .uri(modelsUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return true;
        } catch (Exception e) {
            log.warn("LocalAI connection test failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> listModels() {
        try {
            String modelsUrl = baseUrl + "/v1/models";
            String responseBody = webClient.get()
                    .uri(modelsUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            List<String> models = new ArrayList<>();
            JsonNode responseJson = objectMapper.readTree(responseBody);

            if (responseJson.has("data")) {
                for (JsonNode model : responseJson.get("data")) {
                    if (model.has("id")) {
                        models.add(model.get("id").asText());
                    }
                }
            }

            return models;
        } catch (Exception e) {
            log.error("Failed to list LocalAI models: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public String getConnectorType() {
        return "LOCALAI";
    }
}
