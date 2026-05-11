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
public class QwenChatResponse {

    private String request_id;
    private String code;
    private String message;
    private QwenOutput output;
    private QwenUsage usage;
    private Boolean success;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QwenOutput {
        private String text;
        private List<String> choices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QwenUsage {
        private Integer input_tokens;
        private Integer output_tokens;
        private Integer total_tokens;
    }
}
