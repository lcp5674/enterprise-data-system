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
public class ZhipuChatResponse {

    private String id;
    private Long created;
    private String model;
    private List<ZhipuChoice> choices;
    private ZhipuUsage usage;
    private String error;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ZhipuChoice {
        private Integer index;
        private ZhipuMessage message;
        private String finish_reason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ZhipuMessage {
        private String role;
        private String content;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ZhipuUsage {
        private Integer prompt_tokens;
        private Integer completion_tokens;
        private Integer total_tokens;
    }
}
