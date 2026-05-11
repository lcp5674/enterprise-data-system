package com.enterprise.edams.llm.client.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChatRequestTest {

    @Test
    void testBuilder() {
        ChatRequest request = ChatRequest.builder()
                .model("gpt-4")
                .prompt("Hello")
                .temperature(0.7)
                .maxTokens(1000)
                .build();

        assertEquals("gpt-4", request.getModel());
        assertEquals("Hello", request.getPrompt());
        assertEquals(0.7, request.getTemperature());
        assertEquals(1000, request.getMaxTokens());
    }

    @Test
    void testHasMessages() {
        ChatRequest requestWithMessages = ChatRequest.builder()
                .messages(Arrays.asList(ChatMessage.user("Hello")))
                .build();
        assertTrue(requestWithMessages.hasMessages());

        ChatRequest requestWithEmptyMessages = ChatRequest.builder()
                .messages(List.of())
                .build();
        assertFalse(requestWithEmptyMessages.hasMessages());

        ChatRequest requestWithNullMessages = ChatRequest.builder().build();
        assertFalse(requestWithNullMessages.hasMessages());
    }

    @Test
    void testGetEffectivePrompt() {
        ChatRequest request = ChatRequest.builder()
                .prompt("Direct prompt")
                .build();
        assertEquals("Direct prompt", request.getEffectivePrompt());

        ChatRequest requestWithMessages = ChatRequest.builder()
                .messages(Arrays.asList(ChatMessage.user("Hello")))
                .build();
        assertEquals("Hello", requestWithMessages.getEffectivePrompt());

        ChatRequest emptyRequest = ChatRequest.builder().build();
        assertEquals("", emptyRequest.getEffectivePrompt());
    }
}
