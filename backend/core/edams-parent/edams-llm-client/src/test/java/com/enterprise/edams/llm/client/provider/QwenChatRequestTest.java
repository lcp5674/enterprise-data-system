package com.enterprise.edams.llm.client.provider;

import com.enterprise.edams.llm.client.model.ChatMessage;
import com.enterprise.edams.llm.client.model.ChatRequest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QwenChatRequestTest {

    @Test
    void testBuilder() {
        QwenChatRequest request = QwenChatRequest.builder()
                .model("qwen-turbo")
                .input(Map.of("messages", List.of("test")))
                .parameters(Map.of("temperature", 0.7))
                .build();

        assertEquals("qwen-turbo", request.getModel());
        assertNotNull(request.getInput());
        assertNotNull(request.getParameters());
    }

    @Test
    void testQwenMessageBuilder() {
        QwenChatRequest.QwenMessage message = QwenChatRequest.QwenMessage.builder()
                .role("user")
                .content("你好通义")
                .build();

        assertEquals("user", message.getRole());
        assertEquals("你好通义", message.getContent());
    }

    @Test
    void testQwenInputBuilder() {
        List<QwenChatRequest.QwenMessage> messages = Arrays.asList(
                QwenChatRequest.QwenMessage.builder()
                        .role("user")
                        .content("Hello")
                        .build()
        );

        QwenChatRequest.QwenInput input = QwenChatRequest.QwenInput.builder()
                .messages(messages)
                .build();

        assertEquals(1, input.getMessages().size());
        assertEquals("Hello", input.getMessages().get(0).getContent());
    }

    @Test
    void testQwenParametersBuilder() {
        QwenChatRequest.QwenParameters params = QwenChatRequest.QwenParameters.builder()
                .temperature(0.8)
                .max_tokens(2000)
                .top_p(0.9)
                .stream(false)
                .build();

        assertEquals(0.8, params.getTemperature());
        assertEquals(2000, params.getMax_tokens());
        assertEquals(0.9, params.getTop_p());
        assertFalse(params.getStream());
    }

    @Test
    void testQwenChatResponseBuilder() {
        QwenChatResponse.QwenOutput output = QwenChatResponse.QwenOutput.builder()
                .text("通义千问的回答")
                .choices(List.of("选项1", "选项2"))
                .build();

        QwenChatResponse.QwenUsage usage = QwenChatResponse.QwenUsage.builder()
                .input_tokens(50)
                .output_tokens(30)
                .total_tokens(80)
                .build();

        QwenChatResponse response = QwenChatResponse.builder()
                .request_id("req-123")
                .code(null)
                .message("success")
                .output(output)
                .usage(usage)
                .success(true)
                .build();

        assertEquals("req-123", response.getRequest_id());
        assertTrue(response.getSuccess());
        assertEquals("通义千问的回答", response.getOutput().getText());
        assertEquals(50, response.getUsage().getInput_tokens());
        assertEquals(30, response.getUsage().getOutput_tokens());
    }

    @Test
    void testConvertChatMessageToQwenMessage() {
        ChatMessage userMessage = ChatMessage.user("测试通义");
        QwenChatRequest.QwenMessage qwenMessage = QwenChatRequest.QwenMessage.builder()
                .role(userMessage.getRole())
                .content(userMessage.getContent())
                .build();

        assertEquals("user", qwenMessage.getRole());
        assertEquals("测试通义", qwenMessage.getContent());
    }

    @Test
    void testBuildRequestFromChatRequest() {
        ChatRequest chatRequest = ChatRequest.builder()
                .model("qwen-plus")
                .messages(Arrays.asList(
                        ChatMessage.system("你是阿里云助手"),
                        ChatMessage.user("Hello")
                ))
                .temperature(0.5)
                .maxTokens(500)
                .build();

        List<QwenChatRequest.QwenMessage> messages = chatRequest.getMessages().stream()
                .map(msg -> QwenChatRequest.QwenMessage.builder()
                        .role(msg.getRole())
                        .content(msg.getContent())
                        .build())
                .toList();

        QwenChatRequest qwenRequest = QwenChatRequest.builder()
                .model(chatRequest.getModel())
                .input(Map.of("messages", messages))
                .parameters(Map.of(
                        "temperature", chatRequest.getTemperature(),
                        "max_tokens", chatRequest.getMaxTokens()
                ))
                .build();

        assertEquals("qwen-plus", qwenRequest.getModel());
        assertNotNull(qwenRequest.getInput());
        assertEquals(2, messages.size());
    }
}
