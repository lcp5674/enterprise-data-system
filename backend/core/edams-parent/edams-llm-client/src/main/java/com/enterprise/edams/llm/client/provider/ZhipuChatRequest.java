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
public class ZhipuChatRequest {

    private String model;
    private List<ZhipuMessage> messages;
    private Double temperature;
    private Integer max_tokens;
    private Integer top_p;
    private Integer stream;
    private String request_id;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ZhipuMessage {
        private String role;
        private String content;
    }
}
