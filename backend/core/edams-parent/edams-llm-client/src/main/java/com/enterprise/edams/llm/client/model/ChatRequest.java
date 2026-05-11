package com.enterprise.edams.llm.client.model;

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
public class ChatRequest {

    private String model;

    private String prompt;

    private List<ChatMessage> messages;

    private Double temperature;

    private Integer maxTokens;

    private Map<String, Object> extraParams;

    public boolean hasMessages() {
        return messages != null && !messages.isEmpty();
    }

    public String getEffectivePrompt() {
        if (prompt != null && !prompt.isBlank()) {
            return prompt;
        }
        if (hasMessages() && messages.get(messages.size() - 1) != null) {
            return messages.get(messages.size() - 1).getContent();
        }
        return "";
    }
}
