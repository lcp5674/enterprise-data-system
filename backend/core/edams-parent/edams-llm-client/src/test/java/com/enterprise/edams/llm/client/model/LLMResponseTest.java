package com.enterprise.edams.llm.client.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LLMResponseTest {

    @Test
    void testSuccessFactory() {
        LLMResponse response = LLMResponse.success("Test content", "gpt-4", 100L);

        assertEquals("Test content", response.getContent());
        assertEquals("gpt-4", response.getModel());
        assertEquals(100L, response.getLatencyMs());
        assertTrue(response.getSuccess());
        assertNull(response.getErrorMessage());
    }

    @Test
    void testSuccessFactoryWithTokens() {
        LLMResponse response = LLMResponse.success("Test content", "gpt-4", 100L, 50, 30);

        assertEquals("Test content", response.getContent());
        assertEquals("gpt-4", response.getModel());
        assertEquals(100L, response.getLatencyMs());
        assertEquals(50, response.getPromptTokens());
        assertEquals(30, response.getCompletionTokens());
        assertTrue(response.getSuccess());
    }

    @Test
    void testFailureFactory() {
        LLMResponse response = LLMResponse.failure("Error message", "gpt-4");

        assertEquals("Error message", response.getErrorMessage());
        assertEquals("gpt-4", response.getModel());
        assertFalse(response.getSuccess());
        assertNull(response.getContent());
    }

    @Test
    void testGetTotalTokens() {
        LLMResponse response = LLMResponse.builder()
                .promptTokens(50)
                .completionTokens(30)
                .build();

        assertEquals(80, response.getTotalTokens());

        LLMResponse nullTokensResponse = LLMResponse.builder().build();
        assertEquals(0, nullTokensResponse.getTotalTokens());
    }
}
