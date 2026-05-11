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
public class OllamaConnector implements LLMConnector {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private String baseUrl;

    public OllamaConnector() {
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
            String apiUrl = baseUrl + "/api/generate";

            String requestBody = objectMapper.writeValueAsString(new Object() {
                public String model = request.getModel();
                public String prompt = request.getPrompt();
                public Integer max_tokens = request.getMaxTokens() != null ? request.getMaxTokens() : 4096;
                public Double temperature = request.getTemperature() != null ? request.getTemperature() : 0.7;
                public String[] stop = request.getStop();
                public boolean stream = false;
            });

            String responseBody = webClient.post()
                    .uri(apiUrl)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode responseJson = objectMapper.readTree(responseBody);
            String content = responseJson.has("response") ? responseJson.get("response").asText() : "";
            int promptTokens = responseJson.has("prompt_eval_count") ? responseJson.get("prompt_eval_count").asInt() : 0;
            int completionTokens = responseJson.has("eval_count") ? responseJson.get("eval_count").asInt() : 0;

            return LLMResponse.builder()
                    .content(content)
                    .finishReason(responseJson.has("done") && responseJson.get("done").asBoolean() ? "stop" : "length")
                    .promptTokens(promptTokens)
                    .completionTokens(completionTokens)
                    .totalTokens(promptTokens + completionTokens)
                    .success(true)
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .build();

        } catch (Exception e) {
            log.error("Ollama generate failed: {}", e.getMessage(), e);
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
            String tagsUrl = baseUrl + "/api/tags";
            webClient.get()
                    .uri(tagsUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return true;
        } catch (Exception e) {
            log.warn("Ollama connection test failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> listModels() {
        try {
            String tagsUrl = baseUrl + "/api/tags";
            String responseBody = webClient.get()
                    .uri(tagsUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            List<String> models = new ArrayList<>();
            JsonNode responseJson = objectMapper.readTree(responseBody);

            if (responseJson.has("models")) {
                JsonNode modelsNode = responseJson.get("models");
                for (JsonNode model : modelsNode) {
                    if (model.has("name")) {
                        models.add(model.get("name").asText());
                    }
                }
            }

            return models;
        } catch (Exception e) {
            log.error("Failed to list Ollama models: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public String getConnectorType() {
        return "OLLAMA";
    }
}
