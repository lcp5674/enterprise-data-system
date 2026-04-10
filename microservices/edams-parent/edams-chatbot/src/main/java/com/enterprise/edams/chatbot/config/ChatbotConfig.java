package com.enterprise.edams.chatbot.config;

import io.milvus.client.MilvusClient;
import io.milvus.param.ConnectParam;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 智能问答服务配置类
 *
 * @author Chatbot Team
 * @version 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "chatbot")
public class ChatbotConfig {

    /**
     * LLM服务配置
     */
    private LlmConfig llm = new LlmConfig();

    /**
     * 知识库配置
     */
    private KnowledgeConfig knowledge = new KnowledgeConfig();

    /**
     * Milvus配置
     */
    private MilvusConfig milvus = new MilvusConfig();

    /**
     * 对话配置
     */
    private ConversationConfig conversation = new ConversationConfig();

    /**
     * 提示词配置
     */
    private PromptConfig prompt = new PromptConfig();

    @Data
    public static class LlmConfig {
        private String serviceUrl;
        private Integer timeout;
        private Integer maxRetries;
    }

    @Data
    public static class KnowledgeConfig {
        private String serviceUrl;
        private String defaultGraphId;
        private Integer topK;
        private Double similarityThreshold;
    }

    @Data
    public static class MilvusConfig {
        private String host;
        private Integer port;
        private String collectionName;
        private Integer dimension;
        private String metricType;
        private String indexType;
    }

    @Data
    public static class ConversationConfig {
        private Integer maxHistorySize;
        private Integer timeout;
        private Integer contextWindow;
    }

    @Data
    public static class PromptConfig {
        private String system;
        private String contextTemplate;
        private String followUpTemplate;
    }

    /**
     * Milvus客户端
     */
    @Bean
    public MilvusClient milvusClient() {
        try {
            ConnectParam connectParam = ConnectParam.newBuilder()
                    .withHost(milvus.getHost())
                    .withPort(milvus.getPort())
                    .build();

            return new io.milvus.client.MilvusServiceClient(connectParam);
        } catch (Exception e) {
            // Milvus连接失败不影响服务启动
            return null;
        }
    }
}
