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
public class OpenAIChatRequest {

    private String model;
    private List<OpenAIMessage> messages;
    private Double temperature;
    private Integer max_tokens;
    private Boolean stream;
    private String stop;
    private Double top_p;
    private Integer n;
    private Boolean stream;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpenAIMessage {
        private String role;
        private String content;
        private String name;
    }
}
