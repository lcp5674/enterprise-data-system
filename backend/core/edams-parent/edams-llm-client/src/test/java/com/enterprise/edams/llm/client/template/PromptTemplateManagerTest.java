package com.enterprise.edams.llm.client.template;

import com.enterprise.edams.llm.client.model.ChatMessage;
import com.enterprise.edams.llm.client.model.ChatRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PromptTemplateManagerTest {

    private PromptTemplateManager promptTemplateManager;

    @BeforeEach
    void setUp() {
        promptTemplateManager = new PromptTemplateManager();
    }

    @Test
    void testBuildDataClassificationPrompt() {
        List<String> columns = Arrays.asList("id", "name", "email", "password");
        String prompt = promptTemplateManager.buildDataClassificationPrompt("users", columns);

        assertNotNull(prompt);
        assertTrue(prompt.contains("users"));
        assertTrue(prompt.contains("id"));
        assertTrue(prompt.contains("name"));
        assertTrue(prompt.contains("email"));
        assertTrue(prompt.contains("password"));
    }

    @Test
    void testBuildColumnDescriptionPrompt() {
        List<String> sampleValues = Arrays.asList("user1@example.com", "user2@example.com");
        String prompt = promptTemplateManager.buildColumnDescriptionPrompt(
                "users", "email", "VARCHAR(255)", sampleValues);

        assertNotNull(prompt);
        assertTrue(prompt.contains("users"));
        assertTrue(prompt.contains("email"));
        assertTrue(prompt.contains("VARCHAR(255)"));
    }

    @Test
    void testBuildDataQualityPrompt() {
        List<String> issues = Arrays.asList("发现10%的空值", "存在重复数据");
        String prompt = promptTemplateManager.buildDataQualityPrompt("users", issues);

        assertNotNull(prompt);
        assertTrue(prompt.contains("users"));
        assertTrue(prompt.contains("发现10%的空值"));
        assertTrue(prompt.contains("存在重复数据"));
    }

    @Test
    void testBuildSQLGenerationPrompt() {
        List<String> tableInfo = Arrays.asList("users(id, name, email)", "orders(id, user_id, amount)");
        String prompt = promptTemplateManager.buildSQLGenerationPrompt("统计每个用户的订单总额", tableInfo);

        assertNotNull(prompt);
        assertTrue(prompt.contains("统计每个用户的订单总额"));
        assertTrue(prompt.contains("users"));
        assertTrue(prompt.contains("orders"));
    }

    @Test
    void testBuildClassificationRequest() {
        List<String> columns = Arrays.asList("id", "name", "email");
        ChatRequest request = promptTemplateManager.buildClassificationRequest("users", columns);

        assertNotNull(request);
        assertNotNull(request.getMessages());
        assertFalse(request.getMessages().isEmpty());

        ChatMessage systemMessage = request.getMessages().get(0);
        assertEquals("system", systemMessage.getRole());
        assertNotNull(systemMessage.getContent());

        ChatMessage userMessage = request.getMessages().get(1);
        assertEquals("user", userMessage.getRole());
        assertTrue(userMessage.getContent().contains("users"));
    }

    @Test
    void testBuildColumnDescriptionRequest() {
        List<String> sampleValues = Arrays.asList("value1", "value2");
        ChatRequest request = promptTemplateManager.buildColumnDescriptionRequest(
                "users", "email", "VARCHAR", sampleValues);

        assertNotNull(request);
        assertNotNull(request.getMessages());
        assertEquals(0.3, request.getTemperature());
        assertEquals(1000, request.getMaxTokens());
    }

    @Test
    void testExtractJsonFromResponse() {
        String response = "以下是分析结果：\n```json\n{\"level\": \"public\", \"reason\": \"test\"}\n```\n还有其他问题吗？";
        String json = promptTemplateManager.extractJsonFromResponse(response);

        assertNotNull(json);
        assertTrue(json.contains("level"));
        assertTrue(json.contains("public"));
    }

    @Test
    void testExtractJsonFromResponseWithArray() {
        String response = "结果如下：\n[\"item1\", \"item2\", \"item3\"]\n请确认";
        String json = promptTemplateManager.extractJsonFromResponse(response);

        assertNotNull(json);
        assertTrue(json.startsWith("["));
        assertTrue(json.endsWith("]"));
    }

    @Test
    void testExtractJsonFromResponseWithPlainText() {
        String response = "这是一个纯文本响应，没有JSON";
        String extracted = promptTemplateManager.extractJsonFromResponse(response);

        assertEquals(response.trim(), extracted);
    }

    @Test
    void testExtractJsonFromResponseWithNull() {
        assertNull(promptTemplateManager.extractJsonFromResponse(null));
        assertNull(promptTemplateManager.extractJsonFromResponse(""));
        assertNull(promptTemplateManager.extractJsonFromResponse("   "));
    }
}
