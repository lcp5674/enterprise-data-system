package com.enterprise.edams.llm.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LLMResponse {

    private String content;

    private String model;

    private String finishReason;

    private Integer promptTokens;

    private Integer completionTokens;

    private Long latencyMs;

    private Boolean success;

    private String errorMessage;

    public static LLMResponse success(String content, String model, Long latencyMs) {
        return LLMResponse.builder()
                .content(content)
                .model(model)
                .latencyMs(latencyMs)
                .success(true)
                .build();
    }

    public static LLMResponse success(String content, String model, Long latencyMs,
                                       Integer promptTokens, Integer completionTokens) {
        return LLMResponse.builder()
                .content(content)
                .model(model)
                .latencyMs(latencyMs)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .success(true)
                .build();
    }

    public static LLMResponse failure(String errorMessage, String model) {
        return LLMResponse.builder()
                .model(model)
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }

    public int getTotalTokens() {
        int prompt = promptTokens != null ? promptTokens : 0;
        int completion = completionTokens != null ? completionTokens : 0;
        return prompt + completion;
    }
}
