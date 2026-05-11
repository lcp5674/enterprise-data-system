package com.enterprise.edams.llm.client.strategy;

import com.enterprise.edams.llm.client.LLMClient;
import com.enterprise.edams.llm.client.config.CircuitBreakerConfig;
import com.enterprise.edams.llm.client.config.LLMProperties;
import com.enterprise.edams.llm.client.exception.LLMAvailabilityException;
import com.enterprise.edams.llm.client.model.ChatMessage;
import com.enterprise.edams.llm.client.model.ChatRequest;
import com.enterprise.edams.llm.client.model.LLMResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LLMFallbackManagerTest {

    @Mock
    private LLMClient openAIClient;

    @Mock
    private LLMClient qwenClient;

    @Mock
    private LLMClient zhipuClient;

    @Mock
    private CircuitBreakerConfig circuitBreakerConfig;

    private LLMProperties llmProperties;
    private LLMFallbackManager fallbackManager;

    @BeforeEach
    void setUp() {
        llmProperties = new LLMProperties();
        LLMProperties.FallbackConfig fallbackConfig = new LLMProperties.FallbackConfig();
        fallbackConfig.setEnabled(true);
        fallbackConfig.setPriority(Arrays.asList("openai", "qwen", "zhipu"));
        llmProperties.setFallback(fallbackConfig);

        Map<String, LLMProperties.ProviderConfig> providers = new HashMap<>();
        providers.put("openai", createProviderConfig(true));
        providers.put("qwen", createProviderConfig(true));
        providers.put("zhipu", createProviderConfig(true));
        llmProperties.setProviders(providers);

        fallbackManager = new LLMFallbackManager(
                Arrays.asList(openAIClient, qwenClient, zhipuClient),
                llmProperties,
                circuitBreakerConfig
        );
    }

    private LLMProperties.ProviderConfig createProviderConfig(boolean enabled) {
        LLMProperties.ProviderConfig config = new LLMProperties.ProviderConfig();
        config.setEnabled(enabled);
        return config;
    }

    @Test
    void testChatWithFallbackSuccessOnFirstProvider() {
        when(openAIClient.getProvider()).thenReturn("openai");
        when(openAIClient.chat(any())).thenReturn(
                LLMResponse.success("Success response", "gpt-4", 100L));
        when(openAIClient.healthCheck()).thenReturn(true);

        ChatRequest request = ChatRequest.builder()
                .model("gpt-4")
                .prompt("Hello")
                .build();

        LLMResponse response = fallbackManager.chatWithFallback(request);

        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals("Success response", response.getContent());
        verify(openAIClient).chat(any());
        verify(qwenClient, never()).chat(any());
        verify(zhipuClient, never()).chat(any());
    }

    @Test
    void testChatWithFallbackFallbackToSecondProvider() {
        when(openAIClient.getProvider()).thenReturn("openai");
        when(openAIClient.chat(any())).thenThrow(new RuntimeException("OpenAI failed"));
        when(openAIClient.healthCheck()).thenReturn(true);
        when(circuitBreakerConfig.isCircuitBreakerOpen("openai")).thenReturn(false);

        when(qwenClient.getProvider()).thenReturn("qwen");
        when(qwenClient.chat(any())).thenReturn(
                LLMResponse.success("Qwen response", "qwen-turbo", 100L));
        when(qwenClient.healthCheck()).thenReturn(true);
        when(circuitBreakerConfig.isCircuitBreakerOpen("qwen")).thenReturn(false);

        ChatRequest request = ChatRequest.builder()
                .model("qwen-turbo")
                .prompt("Hello")
                .build();

        LLMResponse response = fallbackManager.chatWithFallback(request);

        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals("Qwen response", response.getContent());
        verify(openAIClient).chat(any());
        verify(qwenClient).chat(any());
        verify(zhipuClient, never()).chat(any());
    }

    @Test
    void testChatWithFallbackAllProvidersFailed() {
        when(openAIClient.getProvider()).thenReturn("openai");
        when(openAIClient.chat(any())).thenThrow(new RuntimeException("OpenAI failed"));
        when(openAIClient.healthCheck()).thenReturn(true);
        when(circuitBreakerConfig.isCircuitBreakerOpen("openai")).thenReturn(false);

        when(qwenClient.getProvider()).thenReturn("qwen");
        when(qwenClient.chat(any())).thenThrow(new RuntimeException("Qwen failed"));
        when(qwenClient.healthCheck()).thenReturn(true);
        when(circuitBreakerConfig.isCircuitBreakerOpen("qwen")).thenReturn(false);

        when(zhipuClient.getProvider()).thenReturn("zhipu");
        when(zhipuClient.chat(any())).thenThrow(new RuntimeException("Zhipu failed"));
        when(zhipuClient.healthCheck()).thenReturn(true);
        when(circuitBreakerConfig.isCircuitBreakerOpen("zhipu")).thenReturn(false);

        ChatRequest request = ChatRequest.builder()
                .prompt("Hello")
                .build();

        assertThrows(LLMAvailabilityException.class, () -> {
            fallbackManager.chatWithFallback(request);
        });

        verify(openAIClient).chat(any());
        verify(qwenClient).chat(any());
        verify(zhipuClient).chat(any());
    }

    @Test
    void testGetAvailableProviders() {
        when(openAIClient.getProvider()).thenReturn("openai");
        when(openAIClient.healthCheck()).thenReturn(true);
        when(circuitBreakerConfig.isCircuitBreakerOpen("openai")).thenReturn(false);

        when(qwenClient.getProvider()).thenReturn("qwen");
        when(qwenClient.healthCheck()).thenReturn(false);
        when(circuitBreakerConfig.isCircuitBreakerOpen("qwen")).thenReturn(false);

        when(zhipuClient.getProvider()).thenReturn("zhipu");
        when(zhipuClient.healthCheck()).thenReturn(true);
        when(circuitBreakerConfig.isCircuitBreakerOpen("zhipu")).thenReturn(false);

        List<String> availableProviders = fallbackManager.getAvailableProviders();

        assertEquals(2, availableProviders.size());
        assertTrue(availableProviders.contains("openai"));
        assertTrue(availableProviders.contains("zhipu"));
        assertFalse(availableProviders.contains("qwen"));
    }

    @Test
    void testGetProviderStatuses() {
        when(openAIClient.getProvider()).thenReturn("openai");
        when(openAIClient.healthCheck()).thenReturn(true);
        when(circuitBreakerConfig.isCircuitBreakerOpen("openai")).thenReturn(false);

        when(qwenClient.getProvider()).thenReturn("qwen");
        when(qwenClient.healthCheck()).thenReturn(false);
        when(circuitBreakerConfig.isCircuitBreakerOpen("qwen")).thenReturn(true);

        when(zhipuClient.getProvider()).thenReturn("zhipu");
        when(zhipuClient.healthCheck()).thenReturn(true);
        when(circuitBreakerConfig.isCircuitBreakerOpen("zhipu")).thenReturn(false);

        Map<String, LLMFallbackManager.ProviderStatus> statuses = fallbackManager.getProviderStatuses();

        assertEquals(3, statuses.size());

        assertTrue(statuses.get("openai").isHealthy());
        assertFalse(statuses.get("openai").isCircuitBreakerOpen());

        assertFalse(statuses.get("qwen").isHealthy());
        assertTrue(statuses.get("qwen").isCircuitBreakerOpen());

        assertTrue(statuses.get("zhipu").isHealthy());
        assertFalse(statuses.get("zhipu").isCircuitBreakerOpen());
    }

    @Test
    void testChatWithSpecificProvider() {
        when(openAIClient.getProvider()).thenReturn("openai");
        when(openAIClient.healthCheck()).thenReturn(true);
        when(openAIClient.chat(any())).thenReturn(
                LLMResponse.success("Specific response", "gpt-4", 100L));

        ChatRequest request = ChatRequest.builder()
                .model("gpt-4")
                .prompt("Hello")
                .build();

        LLMResponse response = fallbackManager.chatWithSpecificProvider(request, "openai");

        assertNotNull(response);
        assertEquals("Specific response", response.getContent());
    }
}
