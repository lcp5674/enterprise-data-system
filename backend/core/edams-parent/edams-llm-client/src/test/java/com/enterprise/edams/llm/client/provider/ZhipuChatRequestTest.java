package com.enterprise.edams.llm.client.provider;

import com.enterprise.edams.llm.client.model.ChatMessage;
import com.enterprise.edams.llm.client.model.ChatRequest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ZhipuChatRequestTest {

    @Test
    void testBuilder() {
        ZhipuChatRequest request = ZhipuChatRequest.builder()
                .model("glm-4")
                .temperature(0.7)
                .max_tokens(1000)
                .request_id("test-id")
                .build();

        assertEquals("glm-4", request.getModel());
        assertEquals(0.7, request.getTemperature());
        assertEquals(1000, request.getMax_tokens());
        assertEquals("test-id", request.getRequest_id());
    }

    @Test
    void testZhipuMessageBuilder() {
        ZhipuChatRequest.ZhipuMessage message = ZhipuChatRequest.ZhipuMessage.builder()
                .role("user")
                .content("你好")
                .build();

        assertEquals("user", message.getRole());
        assertEquals("你好", message.getContent());
    }

    @Test
    void testZhipuChatResponseBuilder() {
        ZhipuChatRequest.ZhipuMessage msg = ZhipuChatRequest.ZhipuMessage.builder()
                .role("assistant")
                .content("智谱回答")
                .build();

        ZhipuChatResponse.ZhipuChoice choice = ZhipuChatResponse.ZhipuChoice.builder()
                .index(0)
                .message(ZhipuChatResponse.ZhipuMessage.builder()
                        .role("assistant")
                        .content("智谱回答")
                        .build())
                .finish_reason("stop")
                .build();

        ZhipuChatResponse.ZhipuUsage usage = ZhipuChatResponse.ZhipuUsage.builder()
                .prompt_tokens(50)
                .completion_tokens(30)
                .total_tokens(80)
                .build();

        ZhipuChatResponse response = ZhipuChatResponse.builder()
                .id("12345")
                .model("glm-4")
                .choices(List.of(choice))
                .usage(usage)
                .build();

        assertEquals("12345", response.getId());
        assertEquals("glm-4", response.getModel());
        assertEquals(1, response.getChoices().size());
        assertEquals("智谱回答", response.getChoices().get(0).getMessage().getContent());
    }

    @Test
    void testConvertChatMessageToZhipuMessage() {
        ChatMessage userMessage = ChatMessage.user("测试");
        ZhipuChatRequest.ZhipuMessage zhipuMessage = ZhipuChatRequest.ZhipuMessage.builder()
                .role(userMessage.getRole())
                .content(userMessage.getContent())
                .build();

        assertEquals("user", zhipuMessage.getRole());
        assertEquals("测试", zhipuMessage.getContent());
    }

    @Test
    void testBuildRequestFromChatRequest() {
        ChatRequest chatRequest = ChatRequest.builder()
                .model("glm-4-flash")
                .messages(Arrays.asList(
                        ChatMessage.system("你是一个助手"),
                        ChatMessage.user("Hello")
                ))
                .temperature(0.5)
                .maxTokens(500)
                .build();

        List<ZhipuChatRequest.ZhipuMessage> messages = chatRequest.getMessages().stream()
                .map(msg -> ZhipuChatRequest.ZhipuMessage.builder()
                        .role(msg.getRole())
                        .content(msg.getContent())
                        .build())
                .toList();

        ZhipuChatRequest zhipuRequest = ZhipuChatRequest.builder()
                .model(chatRequest.getModel())
                .messages(messages)
                .temperature(chatRequest.getTemperature())
                .max_tokens(chatRequest.getMaxTokens())
                .build();

        assertEquals("glm-4-flash", zhipuRequest.getModel());
        assertEquals(2, zhipuRequest.getMessages().size());
        assertEquals("system", zhipuRequest.getMessages().get(0).getRole());
        assertEquals("你是一个助手", zhipuRequest.getMessages().get(0).getContent());
    }
}
