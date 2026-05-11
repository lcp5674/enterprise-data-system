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
public class OpenAICompatibleConnector implements LLMConnector {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private String baseUrl;
    private String apiKey;

    public OpenAICompatibleConnector() {
        this.objectMapper = new ObjectMapper();
        this.webClient = WebClient.builder()
                .build();
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public LLMResponse generate(LLMRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            String apiUrl = baseUrl + "/v1/chat/completions";

            WebClient.RequestBodySpec requestSpec = webClient.post().uri(apiUrl).contentType(MediaType.APPLICATION_JSON);

            if (apiKey != null && !apiKey.isEmpty()) {
                requestSpec = requestSpec.header("Authorization", "Bearer " + apiKey);
            }

            String requestBody = objectMapper.writeValueAsString(new Object() {
                public String model = request.getModel();
                public List<Object> messages = List.of(new Object() {
                    public String role = "user";
                    public String content = request.getPrompt();
                });
                public Integer max_tokens = request.getMaxTokens() != null ? request.getMaxTokens() : 4096;
                public double temperature = request.getTemperature() != null ? request.getTemperature() : 0.7;
                public String[] stop = request.getStop();
            });

            String responseBody = requestSpec
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode responseJson = objectMapper.readTree(responseBody);

            if (responseJson.has("error")) {
                return LLMResponse.builder()
                        .success(false)
                        .errorMessage(responseJson.get("error").get("message").asText())
                        .responseTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }

            String content = "";
            int promptTokens = 0;
            int completionTokens = 0;

            if (responseJson.has("choices") && responseJson.get("choices").size() > 0) {
                JsonNode firstChoice = responseJson.get("choices").get(0);
                if (firstChoice.has("message") && firstChoice.get("message").has("content")) {
                    content = firstChoice.get("message").get("content").asText();
                }
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
            log.error("OpenAI Compatible generate failed: {}", e.getMessage(), e);
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
            log.warn("OpenAI Compatible connection test failed: {}", e.getMessage());
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
            log.error("Failed to list OpenAI Compatible models: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public String getConnectorType() {
        return "OPENAI_COMPATIBLE";
    }
}
