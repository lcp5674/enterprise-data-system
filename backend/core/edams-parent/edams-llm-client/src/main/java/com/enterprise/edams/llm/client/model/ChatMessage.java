package com.enterprise.edams.llm.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    private String role;

    private String content;

    private String name;

    public static ChatMessage user(String content) {
        return ChatMessage.builder()
                .role("user")
                .content(content)
                .build();
    }

    public static ChatMessage assistant(String content) {
        return ChatMessage.builder()
                .role("assistant")
                .content(content)
                .build();
    }

    public static ChatMessage system(String content) {
        return ChatMessage.builder()
                .role("system")
                .content(content)
                .build();
    }
}
