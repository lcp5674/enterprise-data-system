package com.enterprise.edams.analysis.llm.impl;

import com.enterprise.edams.analysis.llm.LLMRequest;
import com.enterprise.edams.analysis.llm.LLMResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OllamaConnectorTest {

    private OllamaConnector connector;

    @BeforeEach
    void setUp() {
        connector = new OllamaConnector();
        connector.setBaseUrl("http://localhost:11434");
    }

    @Test
    void testGetConnectorType() {
        assertEquals("OLLAMA", connector.getConnectorType());
    }

    @Test
    void testGenerate_InvalidEndpoint() {
        connector.setBaseUrl("http://invalid-endpoint:9999");
        
        LLMRequest request = LLMRequest.builder()
                .model("qwen2.5-7b")
                .prompt("Hello")
                .maxTokens(100)
                .temperature(0.7)
                .build();

        LLMResponse response = connector.generate(request);

        assertFalse(response.getSuccess());
        assertNotNull(response.getErrorMessage());
        assertNotNull(response.getResponseTimeMs());
    }

    @Test
    void testGenerate_WithValidModel() {
        LLMRequest request = LLMRequest.builder()
                .model("qwen2.5-7b")
                .prompt("Say 'Test OK'")
                .maxTokens(50)
                .temperature(0.1)
                .build();

        LLMResponse response = connector.generate(request);

        if (response.getSuccess()) {
            assertNotNull(response.getContent());
            assertTrue(response.getResponseTimeMs() >= 0);
        }
    }

    @Test
    void testTestConnection_Failure() {
        connector.setBaseUrl("http://invalid-endpoint:9999");
        
        boolean result = connector.testConnection();
        
        assertFalse(result);
    }

    @Test
    void testListModels_Failure() {
        connector.setBaseUrl("http://invalid-endpoint:9999");
        
        List<String> models = connector.listModels();
        
        assertNotNull(models);
        assertTrue(models.isEmpty());
    }
}
