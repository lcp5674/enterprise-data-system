package com.enterprise.edams.llm.client.provider;

import com.enterprise.edams.llm.client.model.ChatMessage;
import com.enterprise.edams.llm.client.model.ChatRequest;
import com.enterprise.edams.llm.client.model.LLMResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIChatRequestTest {

    private OpenAIChatRequest.OpenAIChatRequestBuilder builder;

    @BeforeEach
    void setUp() {
        builder = OpenAIChatRequest.builder()
                .model("gpt-4")
                .temperature(0.7)
                .max_tokens(1000);
    }

    @Test
    void testBuilder() {
        OpenAIChatRequest request = builder.build();

        assertEquals("gpt-4", request.getModel());
        assertEquals(0.7, request.getTemperature());
        assertEquals(1000, request.getMax_tokens());
    }

    @Test
    void testOpenAIMessageBuilder() {
        OpenAIChatRequest.OpenAIMessage message = OpenAIChatRequest.OpenAIMessage.builder()
                .role("user")
                .content("Hello")
                .build();

        assertEquals("user", message.getRole());
        assertEquals("Hello", message.getContent());
    }

    @Test
    void testOpenAIChatResponseBuilder() {
        OpenAIChatResponse.OpenAIMessage msg = OpenAIChatResponse.OpenAIMessage.builder()
                .role("assistant")
                .content("Response content")
                .build();

        OpenAIChatResponse.OpenAIChoice choice = OpenAIChatResponse.OpenAIChoice.builder()
                .index(0)
                .message(msg)
                .finish_reason("stop")
                .build();

        OpenAIChatResponse.OpenAIUsage usage = OpenAIChatResponse.OpenAIUsage.builder()
                .prompt_tokens(50)
                .completion_tokens(30)
                .total_tokens(80)
                .build();

        OpenAIChatResponse response = OpenAIChatResponse.builder()
                .id("chatcmpl-123")
                .object("chat.completion")
                .model("gpt-4")
                .choices(List.of(choice))
                .usage(usage)
                .build();

        assertEquals("chatcmpl-123", response.getId());
        assertEquals("chat.completion", response.getObject());
        assertEquals("gpt-4", response.getModel());
        assertEquals(1, response.getChoices().size());
        assertEquals("Response content", response.getChoices().get(0).getMessage().getContent());
        assertEquals(50, response.getUsage().getPrompt_tokens());
        assertEquals(30, response.getUsage().getCompletion_tokens());
    }

    @Test
    void testConvertChatMessageToOpenAIMessage() {
        ChatMessage userMessage = ChatMessage.user("Hello");
        OpenAIChatRequest.OpenAIMessage openAIMessage = OpenAIChatRequest.OpenAIMessage.builder()
                .role(userMessage.getRole())
                .content(userMessage.getContent())
                .build();

        assertEquals("user", openAIMessage.getRole());
        assertEquals("Hello", openAIMessage.getContent());
    }

    @Test
    void testBuildRequestFromChatRequest() {
        ChatRequest chatRequest = ChatRequest.builder()
                .model("gpt-4")
                .messages(Arrays.asList(
                        ChatMessage.system("You are a helpful assistant"),
                        ChatMessage.user("Hello")
                ))
                .temperature(0.5)
                .maxTokens(500)
                .build();

        List<OpenAIChatRequest.OpenAIMessage> messages = chatRequest.getMessages().stream()
                .map(msg -> OpenAIChatRequest.OpenAIMessage.builder()
                        .role(msg.getRole())
                        .content(msg.getContent())
                        .build())
                .toList();

        OpenAIChatRequest openAIRequest = OpenAIChatRequest.builder()
                .model(chatRequest.getModel())
                .messages(messages)
                .temperature(chatRequest.getTemperature())
                .max_tokens(chatRequest.getMaxTokens())
                .build();

        assertEquals("gpt-4", openAIRequest.getModel());
        assertEquals(2, openAIRequest.getMessages().size());
        assertEquals("system", openAIRequest.getMessages().get(0).getRole());
        assertEquals("user", openAIRequest.getMessages().get(1).getRole());
    }
}
