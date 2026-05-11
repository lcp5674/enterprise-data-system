package com.enterprise.edams.llm.client.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIChatResponse {

    private String id;
    private String object;
    private Long created;
    private String model;
    private List<OpenAIChoice> choices;
    private OpenAIUsage usage;
    private String error;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpenAIChoice {
        private Integer index;
        private OpenAIMessage message;
        private String finish_reason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpenAIMessage {
        private String role;
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpenAIUsage {
        private Integer prompt_tokens;
        private Integer completion_tokens;
        private Integer total_tokens;
    }
}
