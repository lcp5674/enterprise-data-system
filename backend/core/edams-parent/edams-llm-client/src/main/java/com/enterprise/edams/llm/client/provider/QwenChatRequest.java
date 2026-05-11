package com.enterprise.edams.llm.client.provider;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QwenChatRequest {

    private String model;
    private Map<String, Object> input;
    private Map<String, Object> parameters;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QwenInput {
        private List<QwenMessage> messages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QwenMessage {
        private String role;
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QwenParameters {
        private Double temperature;
        private Integer max_tokens;
        private Integer top_p;
        private Boolean stream;
    }
}
